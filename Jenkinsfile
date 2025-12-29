pipeline {
    agent any

    environment{
     NEW_VERSION= '1.3.0'
}
    //used in build stage in steps  
    echo "Building version ${NEW_VERSION}"
    
    stages {
        stage('Build') {
            steps {
                echo 'Building..'
            }
        }

        stage('Test') {
            steps {
                echo 'Testing..'
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying....'
            }
        }
    }

    post {
        always {
            // This runs no matter what (success or failure)
            echo 'Post build condition'
        }

        failure {
            // This runs only if the build fails
            echo 'Post action if build fails'
        }
    }
}
