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
                sh "${SSH} ${HOST} 'cd ${APP_DIR} && git pull origin main'"
            }
        }

        stage('Build Backend') {
            steps {
                sh "${SSH} ${HOST} 'cd ${APP_DIR}/backend && mvn clean package -DskipTests'"
            }
        }

        stage('Build Frontend') {
            steps {
                sh "${SSH} ${HOST} 'cd ${APP_DIR}/frontend && npm ci && npm run build'"
            }
        }

        stage('Deploy') {
            steps {
                sh "${SSH} ${HOST} 'systemctl restart german-flashcards'"
                sh "${SSH} ${HOST} 'systemctl is-active german-flashcards'"
            }
        }
    }

    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Pipeline failed! Check logs above.'
        }
    }
}
