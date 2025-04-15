pipeline {
    agent {
        docker {
            image 'docker:dind'
            args '-u root:root --privileged'
        }
    }

    environment {
        DOCKER_IMAGE_API = "devly-api"
        DOCKER_IMAGE_BATCH = "devly-batch"
        VERSION = "${BUILD_NUMBER}"
        GOOGLE_CLIENT_ID = credentials('GOOGLE_CLIENT_ID')
        GOOGLE_CLIENT_SECRET = credentials('GOOGLE_CLIENT_SECRET')
        GOOGLE_REDIRECT_URI = credentials('REDIRECT_URI')
        DB_URL = credentials('DB_URL')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET_KEY')
        OPENAI_API_KEY = credentials('OPENAI_API_KEY')
        FRONT_URL = credentials('FRONT_URL')
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        PATH = "$JAVA_HOME/bin:${env.PATH}"
        LOG_DIR = "${WORKSPACE}/logs"
    }

    parameters {
        booleanParam(name: 'BUILD_API', defaultValue: true, description: 'Build and deploy API server?')
        booleanParam(name: 'BUILD_BATCH', defaultValue: true, description: 'Build and deploy Batch server?')
        string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build')
        string(name: 'COMMIT_HASH', defaultValue: '', description: 'Specific commit hash to build (leave empty for latest)')
    }

    stages {
        stage('Setup') {
            steps {
                sh 'mkdir -p ${LOG_DIR}'
                sh '''
                    apk add --no-cache openjdk21
                    export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
                    export PATH=$JAVA_HOME/bin:$PATH
                    java -version
                '''
            }
        }

        stage('Checkout') {
            steps {
                script {
                    if (params.COMMIT_HASH) {
                        checkout([$class: 'GitSCM',
                            branches: [[name: params.COMMIT_HASH]],
                            userRemoteConfigs: [[
                                url: 'https://github.com/Hwasowl/devly.git',
                                credentialsId: 'github-token'
                            ]]
                        ])
                    } else {
                        checkout([$class: 'GitSCM',
                            branches: [[name: "refs/heads/${params.BRANCH}"]],
                            userRemoteConfigs: [[
                                url: 'https://github.com/Hwasowl/devly.git',
                                credentialsId: 'github-token'
                            ]]
                        ])
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    sh 'chmod +x ./gradlew'
                    try {
                        sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-domain:test'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "Domain tests failed: ${e.message}"
                    }

                    try {
                        sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-external:test'
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        error "External tests failed: ${e.message}"
                    }

                    if (params.BUILD_API) {
                        try {
                            sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-api:test'
                        } catch (Exception e) {
                            currentBuild.result = 'FAILURE'
                            error "API tests failed: ${e.message}"
                        }
                    }

                    if (params.BUILD_BATCH) {
                        try {
                            sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-batch:test'
                        } catch (Exception e) {
                            currentBuild.result = 'FAILURE'
                            error "Batch tests failed: ${e.message}"
                        }
                    }
                }
            }
        }

        stage('Build') {
            steps {
                script {
                    sh 'chmod +x ./gradlew'
                    sh './gradlew :devly-domain:clean :devly-domain:build -x test'
                    sh './gradlew :devly-external:clean :devly-external:build -x test'
                    if (params.BUILD_API) {
                        sh '''
                            ./gradlew :devly-api:build -x test -Pspring.profiles.active=prod
                        '''
                    }
                    if (params.BUILD_BATCH) {
                        sh '''
                            ./gradlew :devly-batch:clean :devly-batch:build -x test -Pspring.profiles.active=prod
                        '''
                    }
                }
            }
        }

        stage('Generate Documentation') {
            when {
                expression { return params.BUILD_API }
            }
            steps {
                script {
                    try {
                        // 테스트에서 생성된 스니펫을 보존하면서 문서 생성
                        sh '''
                            export SPRING_PROFILES_ACTIVE=test
                            ./gradlew :devly-api:asciidoctor
                        '''

                        sh 'ls -la devly-api/build/docs/asciidoc/ || echo "No documentation generated"'

                        // 문서를 아티팩트 디렉토리로 복사
                        sh 'mkdir -p ${WORKSPACE}/docs'
                        sh 'cp -R devly-api/build/docs/asciidoc/* ${WORKSPACE}/docs/ || echo "No documentation to copy"'
                    } catch (Exception e) {
                        echo "Documentation generation failed: ${e.message}"
                    }
                }
            }
        }

        stage('Build Docker Images') {
            steps {
                script {
                    if (params.BUILD_API) {
                        sh """
                            docker build -t ${DOCKER_IMAGE_API}:${VERSION} \
                            -f devly-api/Dockerfile .
                        """
                    }
                    if (params.BUILD_BATCH) {
                        sh """
                            docker build -t ${DOCKER_IMAGE_BATCH}:${VERSION} \
                            -f devly-batch/Dockerfile .
                        """
                    }
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def deployContainer = { serviceName, port, envVars ->
                        def envFile = "${serviceName}.env"
                        sh "echo 'SPRING_PROFILES_ACTIVE=prod' > ${envFile}"

                        sh """
                            echo 'SPRING_DATASOURCE_URL=${DB_URL}' >> ${envFile}
                            echo 'SPRING_DATASOURCE_USERNAME=${DB_USERNAME}' >> ${envFile}
                            echo 'SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}' >> ${envFile}
                        """

                        envVars.each { key, value ->
                            sh "echo '${key}=${value}' >> ${envFile}"
                        }

                        sh """
                            docker stop ${serviceName} || true
                            docker rm ${serviceName} || true
                        """

                        sh """
                            docker run -d \\
                                --name ${serviceName} \\
                                -p ${port}:${port} \\
                                --env-file ${envFile} \\
                                --restart unless-stopped \\
                                ${serviceName}:${VERSION}
                        """

                        sh "docker logs ${serviceName} > ${LOG_DIR}/${serviceName}.log"
                        sh "rm ${envFile}"
                    }

                    if (params.BUILD_API) {
                        def apiEnvVars = [
                            'SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID': env.GOOGLE_CLIENT_ID,
                            'SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET': env.GOOGLE_CLIENT_SECRET,
                            'SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_REDIRECT_URI': env.GOOGLE_REDIRECT_URI,
                            'OPENAI_API_KEY': env.OPENAI_API_KEY,
                            'JWT_SECRET_KEY': env.JWT_SECRET,
                            'FRONT_URL': env.FRONT_URL
                        ]
                        deployContainer(env.DOCKER_IMAGE_API, '8080', apiEnvVars)
                    }

                    if (params.BUILD_BATCH) {
                        def batchEnvVars = [
                            'OPENAI_API_KEY': env.OPENAI_API_KEY
                        ]
                        deployContainer(env.DOCKER_IMAGE_BATCH, '8090', batchEnvVars)
                    }
                }
            }
        }

        stage('Cleanup') {
            steps {
                script {
                    // 사용하지 않는 이미지 정리 (dangling 이미지 제거)
                    sh 'docker image prune -f'

                    // 이전 버전 이미지 정리 (최신 3개 버전만 유지)
                    if (params.BUILD_API) {
                        sh """
                            docker images ${DOCKER_IMAGE_API} --format '{{.Repository}}:{{.Tag}}' |
                            sort -r |
                            tail -n +4 |
                            xargs -r docker rmi || true
                        """
                    }

                    if (params.BUILD_BATCH) {
                        sh """
                            docker images ${DOCKER_IMAGE_BATCH} --format '{{.Repository}}:{{.Tag}}' |
                            sort -r |
                            tail -n +4 |
                            xargs -r docker rmi || true
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded!'
            // 배포 성공 시 로그 모니터링
            script {
                if (params.BUILD_API) {
                    sh "docker logs -f ${DOCKER_IMAGE_API} >> ${LOG_DIR}/${DOCKER_IMAGE_API}.log &"
                }
                if (params.BUILD_BATCH) {
                    sh "docker logs -f ${DOCKER_IMAGE_BATCH} >> ${LOG_DIR}/${DOCKER_IMAGE_BATCH}.log &"
                }
            }
        }
        failure {
            echo 'Pipeline failed!'
            // 실패 시 로그 확인 및 롤백 로직 추가
            script {
                echo 'Checking logs for errors...'
                if (params.BUILD_API) {
                    sh "docker logs ${DOCKER_IMAGE_API} > ${LOG_DIR}/${DOCKER_IMAGE_API}_error.log || true"
                }
                if (params.BUILD_BATCH) {
                    sh "docker logs ${DOCKER_IMAGE_BATCH} > ${LOG_DIR}/${DOCKER_IMAGE_BATCH}_error.log || true"
                }

                // 이전 배포 버전으로 롤백 (이전 버전이 있는 경우)
                def previousVersion = VERSION.toInteger() - 1
                if (previousVersion > 0) {
                    echo "Rolling back to previous version ${previousVersion}..."
                    if (params.BUILD_API) {
                        sh """
                            docker stop ${DOCKER_IMAGE_API} || true
                            docker rm ${DOCKER_IMAGE_API} || true

                            if docker image inspect ${DOCKER_IMAGE_API}:${previousVersion} &> /dev/null; then
                                echo "Rolling back API to version ${previousVersion}"
                                docker run -d \\
                                    --name ${DOCKER_IMAGE_API} \\
                                    -p 8080:8080 \\
                                    --env-file ${DOCKER_IMAGE_API}.env \\
                                    --restart unless-stopped \\
                                    ${DOCKER_IMAGE_API}:${previousVersion}
                            else
                                echo "Previous API version not found, cannot rollback"
                            fi
                        """
                    }

                    if (params.BUILD_BATCH) {
                        sh """
                            docker stop ${DOCKER_IMAGE_BATCH} || true
                            docker rm ${DOCKER_IMAGE_BATCH} || true

                            if docker image inspect ${DOCKER_IMAGE_BATCH}:${previousVersion} &> /dev/null; then
                                echo "Rolling back Batch to version ${previousVersion}"
                                docker run -d \\
                                    --name ${DOCKER_IMAGE_BATCH} \\
                                    -p 8090:8090 \\
                                    --env-file ${DOCKER_IMAGE_BATCH}.env \\
                                    --restart unless-stopped \\
                                    ${DOCKER_IMAGE_BATCH}:${previousVersion}
                            else
                                echo "Previous Batch version not found, cannot rollback"
                            fi
                        """
                    }
                }
            }
        }
        always {
            sh 'rm -f *.env || true'
            archiveArtifacts artifacts: "logs/*.log", allowEmptyArchive: true
        }
    }
}
