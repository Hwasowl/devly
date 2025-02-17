pipeline {
    agent any

    environment {
        DOCKER_IMAGE_API = "devly-api"
        DOCKER_IMAGE_BATCH = "devly-batch"
        VERSION = "${BUILD_NUMBER}"
        GOOGLE_CREDS = credentials('GOOGLE_CLIENT')
        DB_USERNAME = credentials('DB_USERNAME')
        DB_PASSWORD = credentials('DB_PASSWORD')
        JWT_SECRET = credentials('JWT_SECRET_KEY')
        OPENAI_API_KEY = credentials('OPENAI_API_KEY')
    }

    parameters {
        booleanParam(name: 'BUILD_API', defaultValue: true, description: 'Build and deploy API server?')
        booleanParam(name: 'BUILD_BATCH', defaultValue: true, description: 'Build and deploy Batch server?')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Prepare Configuration') {
            parallel {
                stage('Domain Config') {
                    steps {
                        sh '''
                            mkdir -p devly-domain/src/main/resources
                            cat > devly-domain/src/main/resources/application.yml << EOL
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/devly?useUnicode=yes&characterEncoding=UTF-8&allowMultiQueries=true&serverTimezone=Asia/Seoul
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
                },
                stage('API Config') {
                    when { expression { params.BUILD_API } }
                    steps {
                        sh '''
                            mkdir -p devly-api/src/main/resources
                            cat > devly-api/src/main/resources/application.yml << EOL
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://localhost:3306/devly?useUnicode=yes&characterEncoding=UTF-8&allowMultiQueries=true&serverTimezone=Asia/Seoul
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CREDS_USR}
            client-secret: ${GOOGLE_CREDS_PSW}
jwt:
  secret-key: ${JWT_SECRET}
front:
  url: https://devly.kro.kr
EOL
                        '''
                    }
                }
                stage('Batch Config') {
                    when { expression { params.BUILD_BATCH } }
                    steps {
                        sh '''
                            mkdir -p devly-batch/src/main/resources
                            cat > devly-batch/src/main/resources/application.yml << EOL
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://localhost:3306/devly?useUnicode=yes&characterEncoding=UTF-8&allowMultiQueries=true&serverTimezone=Asia/Seoul
    username: springuser
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
                    // domain 모듈은 항상 빌드
                    sh './gradlew :devly-domain:clean :devly-domain:build -x test'
                    if (params.BUILD_API) {
                        sh './gradlew :devly-api:clean :devly-api:build -x test'
                    }
                    if (params.BUILD_BATCH) {
                        sh './gradlew :devly-batch:clean :devly-batch:build -x test'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    if (params.BUILD_API) {
                        sh './gradlew :devly-api:test'
                    }
                    if (params.BUILD_BATCH) {
                        sh './gradlew :devly-batch:test'
                    }
                }
            }
            post {
                always {
                    junit '**/build/test-results/test/*.xml'
                }
            }
        }

        stage('Docker Build') {
            parallel {
                stage('API Server Docker Build') {
                    when { expression { params.BUILD_API } }
                    steps {
                        script {
                            docker.build("${DOCKER_IMAGE_API}:${VERSION}", "-f devly-api/Dockerfile .")
                        }
                    }
                }
                stage('Batch Server Docker Build') {
                    when { expression { params.BUILD_BATCH } }
                    steps {
                        script {
                            docker.build("${DOCKER_IMAGE_BATCH}:${VERSION}", "-f devly-batch/Dockerfile .")
                        }
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
