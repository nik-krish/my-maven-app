pipeline {
  agent any
  environment {
    AWS_REGION = 'us-east-1'
    ECR_REGISTRY = "<AWS_ACCOUNT_ID>.dkr.ecr.${AWS_REGION}.amazonaws.com"
    ECR_REPO = "my-maven-app"
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    AWS_CREDENTIALS_ID = 'aws-creds'
  }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps { sh 'mvn clean test' }
    }

    stage('Package App') {
      steps { sh 'mvn package -DskipTests' }
    }

    stage('Build Docker Image') {
      steps { sh "docker build -t ${ECR_REPO}:${IMAGE_TAG} ." }
    }

    stage('Login & Push to AWS ECR') {
      steps {
        withCredentials([usernamePassword(credentialsId: "${AWS_CREDENTIALS_ID}", usernameVariable: 'AWS_ID', passwordVariable: 'AWS_SECRET')]) {
          sh '''
            aws configure set aws_access_key_id "$AWS_ID"
            aws configure set aws_secret_access_key "$AWS_SECRET"
            aws configure set region ${AWS_REGION}
            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REGISTRY}
            docker tag ${ECR_REPO}:${IMAGE_TAG} ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}
            docker push ${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}
          '''
        }
      }
    }

    stage('Deploy to ECS Fargate') {
      steps {
        script {
          def taskDef = """
          {
            "family": "my-maven-task",
            "networkMode": "awsvpc",
            "requiresCompatibilities": ["FARGATE"],
            "cpu": "256",
            "memory": "512",
            "containerDefinitions": [
              {
                "name": "my-maven-app",
                "image": "${ECR_REGISTRY}/${ECR_REPO}:${IMAGE_TAG}",
                "essential": true,
                "portMappings": [{"containerPort":8080,"protocol":"tcp"}],
                "logConfiguration": {
                  "logDriver": "awslogs",
                  "options": {
                    "awslogs-group": "/ecs/my-maven-app",
                    "awslogs-region": "${AWS_REGION}",
                    "awslogs-stream-prefix": "ecs"
                  }
                }
              }
            ]
          }
          """
          writeFile file: 'taskdef.json', text: taskDef
          sh '''
            aws ecs register-task-definition --cli-input-json file://taskdef.json
            aws ecs update-service --cluster my-maven-cluster --service my-maven-service --force-new-deployment
          '''
        }
      }
    }
  }

  post {
    success {
      mail to: 'your.email@example.com',
           subject: "✅ SUCCESS: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
           body: "Build succeeded!\n\nURL: ${env.BUILD_URL}"
    }
    failure {
      mail to: 'your.email@example.com',
           subject: "❌ FAILURE: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
           body: "Build failed.\n\nURL: ${env.BUILD_URL}"
    }
  }
}
