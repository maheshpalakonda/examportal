pipeline {
  agent any

  environment {
    // Docker Hub creds (Username + Access Token) stored in Jenkins
    REG = credentials('dockerhub-creds') // exposes REG_USR, REG_PSW
    DOCKERHUB_USER = "${REG_USR}"

    BACKEND_IMAGE = "${REG_USR}/examportal-backend"
    FRONTEND_IMAGE = "${REG_USR}/examportal-frontend"
    IMAGE_TAG = "latest"

    // Your server IP
    SERVER_HOST = "72.60.219.208"

    // Your repo (explicit)
    REPO_URL = "https://github.com/maheshpalakonda/examportal.git"
  }

  options { timestamps(); ansiColor('xterm') }

  stages {
    stage('Checkout') {
      steps {
        git branch: 'master', url: "${REPO_URL}"
      }
    }

    stage('Build backend image') {
      steps {
        sh """
          docker build -t ${BACKEND_IMAGE}:${env.GIT_COMMIT} -t ${BACKEND_IMAGE}:${IMAGE_TAG} ./examportalspring
        """
      }
    }

    stage('Build frontend image') {
      steps {
        sh """
          docker build -t ${FRONTEND_IMAGE}:${env.GIT_COMMIT} -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ./online-exam-portal
        """
      }
    }

    stage('Login & Push images') {
      steps {
        sh """
          echo "${REG_PSW}" | docker login -u "${REG_USR}" --password-stdin
          docker push ${BACKEND_IMAGE}:${env.GIT_COMMIT}
          docker push ${BACKEND_IMAGE}:${IMAGE_TAG}
          docker push ${FRONTEND_IMAGE}:${env.GIT_COMMIT}
          docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}
        """
      }
    }

    stage('Deploy on server') {
      steps {
        sshagent(credentials: ['prod-ssh']) {
          sh """
            ssh -o StrictHostKeyChecking=no ubuntu@${SERVER_HOST} '
              set -e
              sudo apt-get update -y
              sudo apt-get install -y docker.io docker-compose-plugin git || true

              sudo mkdir -p /opt/examportal && sudo chown $USER:$USER /opt/examportal
              cd /opt/examportal

              # Pull repo once; then keep pulling
              if [ -d .git ]; then
                git pull
              else
                git clone ${REPO_URL} .
              fi

              # Require .env (created manually once)
              if [ ! -f .env ]; then
                echo ".env missing at /opt/examportal (needs DOCKERHUB_USER, MYSQL_ROOT_PASSWORD, MYSQL_DATABASE, IMAGE_TAG)" >&2
                exit 1
              fi

              docker login -u ${DOCKERHUB_USER} -p ${REG_PSW}
              docker compose pull
              docker compose up -d
              docker image prune -f
            '
          """
        }
      }
    }
  }

  post {
    always {
      sh 'docker logout || true'
      echo 'Done.'
    }
  }
}

