pipeline {
    agent any

    environment {
        HOST = 'root@172.17.0.1'
        APP_DIR = '/root/german-flashcards'
        SSH = 'ssh -o StrictHostKeyChecking=no'
    }

    stages {
        stage('Checkout') {
            steps {
                sh "${SSH} ${HOST} 'cd ${APP_DIR} && git stash && git pull origin main'"
            }
        }

        stage('Build Backend') {
            when {
                expression {
                    sh(script: "${SSH} ${HOST} 'cd ${APP_DIR} && git diff HEAD~1 --name-only | grep -q ^backend/'", returnStatus: true) == 0
                }
            }
            steps {
                sh "${SSH} ${HOST} 'cd ${APP_DIR}/backend && mvn clean package -DskipTests'"
            }
        }

        stage('Build Frontend') {
            when {
                expression {
                    sh(script: "${SSH} ${HOST} 'cd ${APP_DIR} && git diff HEAD~1 --name-only | grep -q ^frontend/'", returnStatus: true) == 0
                }
            }
            steps {
                sh "${SSH} ${HOST} 'cd ${APP_DIR}/frontend && npm ci && npm run build'"
            }
        }

        stage('Deploy Backend') {
            when {
                expression {
                    sh(script: "${SSH} ${HOST} 'test -f ${APP_DIR}/backend/target/german-flashcards-0.0.1-SNAPSHOT.jar'", returnStatus: true) == 0 &&
                    sh(script: "${SSH} ${HOST} 'cd ${APP_DIR} && git diff HEAD~1 --name-only | grep -q ^backend/'", returnStatus: true) == 0
                }
            }
            steps {
                sh "${SSH} ${HOST} 'systemctl restart german-flashcards && systemctl is-active german-flashcards'"
            }
        }
    }

    post {
        success {
            echo "Pipeline successful - build #${env.BUILD_NUMBER}"
        }
        failure {
            echo "Pipeline failed - check logs for build #${env.BUILD_NUMBER}"
        }
    }
}
