pipeline {
  agent any

  environment {
    // Images pushed to Docker Hub
    BACKEND_IMAGE = "mahesh1925/examportal-backend"
    FRONTEND_IMAGE = "mahesh1925/examportal-frontend"

    // Deployment target
    SERVER_HOST = "72.60.219.208"
    SERVER_USER = "ubuntu"

    // Repo
    REPO_URL = "https://github.com/maheshpalakonda/examportal.git"
    BRANCH   = "master"

    IMAGE_TAG = "latest"
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
          userRemoteConfigs: [[url: env.REPO_URL]],
          branches: [[name: "*/${env.BRANCH}"]]
        ])
      }
    }

    stage('Build backend image (Java 21)') {
      steps {
        sh """
          docker build -t ${BACKEND_IMAGE}:\${GIT_COMMIT} -t ${BACKEND_IMAGE}:${IMAGE_TAG} ./examportalspring
        """
      }
    }

    stage('Build frontend image (Angular)') {
      steps {
        sh """
          docker build -t ${FRONTEND_IMAGE}:\${GIT_COMMIT} -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ./online-exam-portal
        """
      }
    }

    stage('Login & Push images to Docker Hub') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USR', passwordVariable: 'DOCKERHUB_PSW')]) {
          sh """
            echo "${DOCKERHUB_PSW}" | docker login -u "${DOCKERHUB_USR}" --password-stdin
            docker push ${BACKEND_IMAGE}:\${GIT_COMMIT}
            docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
            docker push ${FRONTEND_IMAGE}:\${GIT_COMMIT}
            docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
          """
        }
      }
    }

    stage('Deploy to server (SSH + Compose)') {
      steps {
        // 'prod-ssh' is an SSH Username with private key credential (user: ubuntu)
        sshagent(credentials: ['prod-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} '
              set -e
              sudo apt-get update -y
              which docker || echo "Docker already installed"
	      which docker-compose || echo "Docker Compose plugin already installed"

              sudo mkdir -p /opt/examportal && sudo chown $USER:$USER /opt/examportal
              cd /opt/examportal

              if [ -d .git ]; then
                git pull
              else
                git clone ${REPO_URL} .
              fi

              # (No server-side .env creation needed — .env is in repo)
            '
          """
          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USR', passwordVariable: 'DOCKERHUB_PSW')]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} '
                set -e
                docker login -u ${DOCKERHUB_USR} -p ${DOCKERHUB_PSW}
                cd /opt/examportal
                docker compose pull
                docker compose up -d
                docker image prune -f
              '
            """
          }
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout || true'
      echo 'Pipeline finished.'
    }
  }
}

