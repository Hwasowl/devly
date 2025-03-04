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
        DB_URL = credentials('DB_URL')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET_KEY')
        OPENAI_API_KEY = credentials('OPENAI_API_KEY')
        JAVA_HOME = '/usr/lib/jvm/java-21-openjdk'
        PATH = "$JAVA_HOME/bin:${env.PATH}"
    }

    parameters {
        booleanParam(name: 'BUILD_API', defaultValue: true, description: 'Build and deploy API server?')
        booleanParam(name: 'BUILD_BATCH', defaultValue: true, description: 'Build and deploy Batch server?')
        string(name: 'BRANCH', defaultValue: 'main', description: 'Branch to build')
        string(name: 'COMMIT_HASH', defaultValue: '', description: 'Specific commit hash to build (leave empty for latest)')
    }

    stages {
        stage('Setup Java') {
            steps {
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
                    sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-domain:test || true'
                    sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-external:test || true'

                    if (params.BUILD_API) {
                        sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-api:test || true'
                    }

                    if (params.BUILD_BATCH) {
                        sh 'export SPRING_PROFILES_ACTIVE=test && ./gradlew :devly-batch:test || true'
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
                            mkdir -p devly-api/build/generated-snippets
                            ./gradlew :devly-api:clean :devly-api:build -x test -x asciidoctor -Pspring.profiles.active=prod
                        '''
                    }
                    if (params.BUILD_BATCH) {
                        sh '''
                            mkdir -p devly-batch/build/generated-snippets
                            ./gradlew :devly-batch:clean :devly-batch:build -x test -x asciidoctor -Pspring.profiles.active=prod
                        '''
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
            parallel {
                stage('Deploy API Server') {
                    when { expression { params.BUILD_API } }
                    steps {
                        script {
                            sh """
                                echo 'SPRING_PROFILES_ACTIVE=prod' > api.env
                                echo 'SPRING_DATASOURCE_URL=${DB_URL}' >> api.env
                                echo 'SPRING_DATASOURCE_USERNAME=${DB_USERNAME}' >> api.env
                                echo 'SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}' >> api.env
                                echo 'SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}' >> api.env
                                echo 'SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}' >> api.env
                                echo 'JWT_SECRET_KEY=${JWT_SECRET}' >> api.env

                                docker stop ${DOCKER_IMAGE_API} || true
                                docker rm ${DOCKER_IMAGE_API} || true

                                docker run -d \\
                                    --name ${DOCKER_IMAGE_API} \\
                                    -p 8080:8080 \\
                                    --env-file api.env \\
                                    --restart unless-stopped \\
                                    ${DOCKER_IMAGE_API}:${VERSION}

                                docker logs -f ${DOCKER_IMAGE_API} > api.log 2>&1 &

                                rm api.env
                            """
                        }
                    }
                }
                stage('Deploy Batch Server') {
                    when { expression { params.BUILD_BATCH } }
                    steps {
                        script {
                            sh """
                                echo 'SPRING_PROFILES_ACTIVE=prod' > batch.env
                                echo 'SPRING_DATASOURCE_URL=${DB_URL}' >> batch.env
                                echo 'SPRING_DATASOURCE_USERNAME=${DB_USERNAME}' >> batch.env
                                echo 'SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}' >> batch.env
                                echo 'OPENAI_API_KEY=${OPENAI_API_KEY}' >> batch.env

                                docker stop ${DOCKER_IMAGE_BATCH} || true
                                docker rm ${DOCKER_IMAGE_BATCH} || true

                                docker run -d \\
                                    --name ${DOCKER_IMAGE_BATCH} \\
                                    -p 8090:8090 \\
                                    --env-file batch.env \\
                                    --restart unless-stopped \\
                                    ${DOCKER_IMAGE_BATCH}:${VERSION}
                                docker logs -f ${DOCKER_IMAGE_BATCH} > batch.log 2>&1 &
                                rm batch.env
                            """
                        }
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        always {
            sh 'rm -f *.env || true'
        }
    }
}
