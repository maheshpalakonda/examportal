pipeline {
  agent any

  environment {
    BACKEND_IMAGE  = "mahesh1925/examportal-backend"
    FRONTEND_IMAGE = "mahesh1925/examportal-frontend"
    IMAGE_TAG      = "latest"

    SERVER_HOST = "72.60.219.208"
    SERVER_USER = "ubuntu"

    REPO_URL  = "https://github.com/maheshpalakonda/examportal.git"
    BRANCH    = "master"

    DOCKER_COMPOSE_PROJECT = "examportal"
  }

  stages {

    stage('Checkout') {
      steps {
        echo "📥 Checking out source code..."
        checkout([$class: 'GitSCM',
          userRemoteConfigs: [[url: env.REPO_URL]],
          branches: [[name: "*/${env.BRANCH}"]]
        ])
      }
    }

    stage('Build Backend (Spring Boot)') {
      steps {
        echo "⚙️ Building Spring Boot backend Docker image..."
        sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} ./examportalspring"
      }
    }

    stage('Build Frontend (Angular)') {
      steps {
        echo "⚙️ Building Angular frontend Docker image..."
        sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} ./online-exam-portal"
      }
    }

    stage('Push Images to Docker Hub') {
      steps {
        echo "📤 Pushing Docker images to Docker Hub (with retry logic)..."
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USR', passwordVariable: 'DOCKERHUB_PSW')]) {
          sh '''
            echo "$DOCKERHUB_PSW" | docker login -u "$DOCKERHUB_USR" --password-stdin

            echo "🚀 Pushing backend image..."
            for i in 1 2 3; do
              docker push ${BACKEND_IMAGE}:${IMAGE_TAG} && break || \
              echo "❌ Push attempt $i failed. Retrying in 15 seconds..." && sleep 15
            done

            echo "🚀 Pushing frontend image..."
            for i in 1 2 3; do
              docker push ${FRONTEND_IMAGE}:${IMAGE_TAG} && break || \
              echo "❌ Push attempt $i failed. Retrying in 15 seconds..." && sleep 15
            done
          '''
        }
      }
    }

    stage('Deploy to EC2') {
      steps {
        echo "🚀 Deploying to EC2 (${SERVER_HOST}) ..."
        sshagent(credentials: ['prod-ssh']) {
          withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKERHUB_USR', passwordVariable: 'DOCKERHUB_PSW')]) {
            sh """
              ssh -o StrictHostKeyChecking=no ${SERVER_USER}@${SERVER_HOST} '
                set -e

                echo "🧹 Stopping old containers (preserving DB data)..."
                sudo docker compose --project-name ${DOCKER_COMPOSE_PROJECT} down || true

                echo "📂 Ensuring latest repo..."
                cd /opt
                if [ ! -d ${DOCKER_COMPOSE_PROJECT} ]; then
                  git clone ${REPO_URL} ${DOCKER_COMPOSE_PROJECT}
                fi
                cd ${DOCKER_COMPOSE_PROJECT}
                git pull origin ${BRANCH}

                echo "🔑 Docker login..."
                echo ${DOCKERHUB_PSW} | sudo docker login -u ${DOCKERHUB_USR} --password-stdin

                echo "💾 (Optional) Backing up MySQL database..."
                if sudo docker ps | grep -q examportal-db; then
                  sudo mkdir -p /opt/${DOCKER_COMPOSE_PROJECT}/backup
                  sudo docker exec examportal-db mysqldump -u root -p\\'root@123\\' newexam > /opt/${DOCKER_COMPOSE_PROJECT}/backup/backup-$(date +%F_%H-%M).sql || true
                  echo "✅ Backup saved in /opt/${DOCKER_COMPOSE_PROJECT}/backup/"
                fi

                echo "📦 Pulling latest Docker images..."
                sudo docker compose --project-name ${DOCKER_COMPOSE_PROJECT} pull

                echo "🚀 Recreating only backend & frontend containers..."
                sudo docker compose --project-name ${DOCKER_COMPOSE_PROJECT} up -d --force-recreate backend frontend

                echo "🧼 Cleaning unused images..."
                sudo docker image prune -f

                echo "✅ Deployment successful!"
              '
            """
          }
        }
      }
    }
  }

  post {
    success {
      echo "✅ CI/CD pipeline completed successfully."
    }
    failure {
      echo "❌ CI/CD pipeline failed. Check Jenkins console logs."
    }
    always {
      sh 'docker logout || true'
    }
  }
}

