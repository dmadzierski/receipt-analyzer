pipeline {
	agent {
		docker {
			image 'docker:latest'
			args '-v /var/run/docker.sock:/var/run/docker.sock -u root'
		}
	}

	environment {
		REGISTRY = 'docker-registry.oziaka.eu'
		REGISTRY_CREDS = credentials('registry-cred')
		DOCKHAND_ADDRESS = 'docker.local'
		DOCKHAND_CRED = credentials('DOCKHUND_RECEIPT_ANALYZER_API_KEY')
		IMAGE_VERSION = "0.0.${env.BUILD_NUMBER}"
	}

	stages {

		stage('Login to Registry') {
			steps {
				sh 'echo $REGISTRY_CREDS_PSW | docker login $REGISTRY -u $REGISTRY_CREDS_USR --password-stdin'
			}
		}

		stage('Build and Publish') {
			steps {
				sh 'docker build -t $REGISTRY/receipt-analyzer-service:$IMAGE_VERSION -t $REGISTRY/receipt-analyzer-service:latest ./receipt-analyzer-service'
				sh 'docker push $REGISTRY/receipt-analyzer-service:$IMAGE_VERSION'
				sh 'docker push $REGISTRY/receipt-analyzer-service:latest'

				sh 'docker build -t $REGISTRY/receipt-analyzer-web-client:$IMAGE_VERSION -t $REGISTRY/receipt-analyzer-web-client:latest ./web-client'
				sh 'docker push $REGISTRY/receipt-analyzer-web-client:$IMAGE_VERSION'
				sh 'docker push $REGISTRY/receipt-analyzer-web-client:latest'
			}
		}

		stage('Trigger Dockhand Webhook') {
			steps {
				httpRequest httpMode: 'GET',
				url: 'http://' + env.DOCKHAND_ADDRESS + '/api/git/stacks/1/webhook?secret=' + env.DOCKHAND_CRED,
				validResponseCodes: '200'
			}
		}
	}

	post {
		always {
			sh 'docker logout $REGISTRY'
			sh 'docker image prune -f'
			sh 'docker rmi $REGISTRY/receipt-analyzer-service:$IMAGE_VERSION || true'
			sh 'docker rmi $REGISTRY/receipt-analyzer-service:latest || true'
			sh 'docker rmi $REGISTRY/receipt-analyzer-web-client:$IMAGE_VERSION || true'
			sh 'docker rmi $REGISTRY/receipt-analyzer-web-client:latest || true'
		}
	}
}