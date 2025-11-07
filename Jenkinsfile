pipeline {
  agent any

  environment {
    // Docker Hub images
    BACKEND_IMAGE = "mahesh1925/examportal-backend"
    FRONTEND_IMAGE = "mahesh1925/examportal-frontend"

    // Deployment server
    SERVER_HOST = "72.60.219.208"
    SERVER_USER = "ubuntu"

    // Git repo
    REPO_URL = "https://github.com/maheshpalakonda/examportal.git"
    BRANCH   = "master"

    IMAGE_TAG = "latest"
  }

  options { timestamps() }

  stages {
    // ---------------------------
    // 1️⃣ CHECKOUT CODE
    // ---------------------------
    stage('Checkout') {
      steps {
        checkout([$class: 'GitSCM',
          userRemoteConfigs: [[url: env.REPO_URL]],
          branches: [[name: "*/${env.BRANCH}"]]
        ])
      }
    }

    // ---------------------------
    // 2️⃣ BUILD BACKEND IMAGE
    // ---------------------------
    stage('Build backend image (Java 21)') {
      steps {
        sh """
          docker build -t ${BACKEND_IMAGE}:\${GIT_COMMIT} \
                       -t ${BACKEND_IMAGE}:${IMAGE_TAG} ./examportalspring
        """
      }
    }

    // ---------------------------
    // 3️⃣ BUILD FRONTEND IMAGE
    // ---------------------------
    stage('Build frontend image (Angular)') {
      steps {
        sh """
          docker build -t ${FRONTEND_IMAGE}:\${GIT_COMMIT} \
                       -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ./online-exam-portal
        """
      }
    }

    // ---------------------------
    // 4️⃣ PUSH IMAGES TO DOCKER HUB
    // ---------------------------
    stage('Login & Push images to Docker Hub') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: 'dockerhub-creds',
          usernameVariable: 'DOCKERHUB_USR',
          passwordVariable: 'DOCKERHUB_PSW'
        )]) {
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

    // ---------------------------
    // 5️⃣ DEPLOY TO SERVER (SAFE)
    // ---------------------------
    stage('Deploy to server (SSH + Compose)') {
      steps {
        sshagent(credentials: ['prod-ssh']) {

          // ✅ Prepare directory & pull latest repo
          sh """
            ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} '
              set -e
              sudo apt-get update -y
              which docker || echo "Docker already installed"
              which docker-compose || echo "Docker Compose plugin already installed"

              sudo mkdir -p /opt/examportal && sudo chown ubuntu:ubuntu /opt/examportal
              cd /opt/examportal

              if [ -d .git ]; then
                git pull
              else
                git clone ${REPO_URL} .
              fi
            '
          """

          // ✅ Deploy safely without touching other containers
          withCredentials([usernamePassword(
            credentialsId: 'dockerhub-creds',
            usernameVariable: 'DOCKERHUB_USR',
            passwordVariable: 'DOCKERHUB_PSW'
          )]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} '
                set -e
                sudo docker login -u ${DOCKERHUB_USR} -p ${DOCKERHUB_PSW}
                cd /opt/examportal
                # Use custom project name to isolate containers
                sudo docker compose --project-name examportal pull
                sudo docker compose --project-name examportal up -d
                # Clean only unused images (safe)
                sudo docker image prune -f
              '
            """
          }
        }
      }
    }
  }

  // ---------------------------
  // ♻️ CLEANUP
  // ---------------------------
  post {
    always {
      sh 'docker logout || true'
      echo '✅ Pipeline finished successfully.'
    }
  }
}

