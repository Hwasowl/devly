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

        stage('Prepare Configuration') {
            parallel {
                stage('Domain Config') {
                    steps {
                        sh '''
                            mkdir -p devly-domain/src/main/resources
                            cat > devly-domain/src/main/resources/application-prod.yml << EOL
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
    hibernate:
      ddl-auto: none
EOL
                        '''
                    }
                }
                stage('API Config') {
                    when { expression { params.BUILD_API } }
                    steps {
                        sh '''
                            mkdir -p devly-api/src/main/resources
                            cat > devly-api/src/main/resources/application-prod.yml << EOL
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
jwt:
  secret-key: ${JWT_SECRET}
EOL
                        '''
                    }
                }
                stage('Batch Config') {
                    when { expression { params.BUILD_BATCH } }
                    steps {
                        sh '''
                            mkdir -p devly-batch/src/main/resources
                            cat > devly-batch/src/main/resources/application-prod.yml << EOL
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
openai:
  api-url: https://api.openai.com
  api-key: ${OPENAI_API_KEY}
server:
  port: 8090
EOL
                        '''
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
                                docker stop ${DOCKER_IMAGE_API} || true
                                docker rm ${DOCKER_IMAGE_API} || true
                                docker run -d \
                                    --name ${DOCKER_IMAGE_API} \
                                    -p 8080:8080 \
                                    -e SPRING_PROFILES_ACTIVE=prod \
                                    --restart unless-stopped \
                                    ${DOCKER_IMAGE_API}:${VERSION}
                                docker logs -f ${DOCKER_IMAGE_API} > api.log 2>&1 &
                            """
                        }
                    }
                }
                stage('Deploy Batch Server') {
                    when { expression { params.BUILD_BATCH } }
                    steps {
                        script {
                            sh """
                                docker stop ${DOCKER_IMAGE_BATCH} || true
                                docker rm ${DOCKER_IMAGE_BATCH} || true
                                docker run -d \
                                    --name ${DOCKER_IMAGE_BATCH} \
                                    -p 8090:8090 \
                                    -e SPRING_PROFILES_ACTIVE=prod \
                                    --restart unless-stopped \
                                    ${DOCKER_IMAGE_BATCH}:${VERSION}
                                docker logs -f ${DOCKER_IMAGE_BATCH} > batch.log 2>&1 &
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
    }
}
