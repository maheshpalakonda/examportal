pipeline {
  agent any

  environment {
    BACKEND_IMAGE = "mahesh1925/examportal-backend"
    FRONTEND_IMAGE = "mahesh1925/examportal-frontend"

    SERVER_HOST = "72.60.219.208"
    SERVER_USER = "ubuntu"

    REPO_URL = "https://github.com/maheshpalakonda/examportal.git"
    BRANCH   = "master"
    IMAGE_TAG = "latest"
  }

  stages {
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
          userRemoteConfigs: [[url: env.REPO_URL]],
          branches: [[name: "*/${env.BRANCH}"]]
        ])
      }
    }

    stage('Build Backend (Spring Boot)') {
      steps {
        sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} ./examportalspring"
      }
    }

    stage('Build Frontend (Angular)') {
      steps {
        sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ./online-exam-portal"
      }
    }

    stage('Push Images to Docker Hub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USR', passwordVariable: 'DOCKERHUB_PSW')]) {
          sh """
            echo "$DOCKERHUB_PSW" | docker login -u "$DOCKERHUB_USR" --password-stdin
            docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
            docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
          """
        }
      }
    }

    stage('Deploy to EC2') {
      steps {
        sshagent(credentials: ['prod-ssh']) {
          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USR', passwordVariable: 'DOCKERHUB_PSW')]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} '
                set -e
                echo "🧹 Cleaning old containers and volumes..."
                sudo docker compose --project-name examportal down -v || true

                echo "📦 Pulling latest code..."
                cd /opt
                if [ -d examportal ]; then
                  cd examportal && git pull
                else
                  git clone ${REPO_URL} examportal && cd examportal
                fi

                echo "🔑 Docker login..."
                echo ${DOCKERHUB_PSW} | sudo docker login -u ${DOCKERHUB_USR} --password-stdin

                echo "🚀 Deploying fresh stack..."
                sudo docker compose --project-name examportal pull
                sudo docker compose --project-name examportal up -d --build
                sudo docker image prune -f
              '
            """
          }
        }
      }
    }
  }

  post {
    always {
      echo "✅ CI/CD pipeline completed."
      sh 'docker logout || true'
    }
  }
}

