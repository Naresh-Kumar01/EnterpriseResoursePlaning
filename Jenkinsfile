/**
 * EnterpriseResourcePlanning - Simple CI/CD Pipeline
 * Tech Stack: Selenium | Java | Maven | Cucumber | JUnit | Extent Reports
 */
pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timestamps()
        timeout(time: 60, unit: 'MINUTES')
        disableConcurrentBuilds()
    }

    environment {
        PROJECT_NAME = 'EnterpriseResourcePlanning'
        GITHUB_REPO = 'https://github.com/Naresh-Kumar01/EnterpriseResoursePlaning'
        MAVEN_OPTS = '-Xmx1536m -XX:MaxMetaspaceSize=512m'
    }

    triggers {
        githubPush()
    }

    stages {
        stage('📥 Checkout Code') {
            steps {
                echo '════════════════════════════════════════════'
                echo ' Stage 1: Checkout Code from GitHub'
                echo '════════════════════════════════════════════'
                checkout scm
                script {
                    if (isUnix()) {
                        env.GIT_COMMIT_SHORT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                        env.GIT_BRANCH = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                    } else {
                        env.GIT_COMMIT_SHORT = bat(returnStdout: true, script: '@git rev-parse --short HEAD').trim()
                        env.GIT_BRANCH = bat(returnStdout: true, script: '@git rev-parse --abbrev-ref HEAD').trim()
                    }
                    echo "✓ Branch: ${env.GIT_BRANCH}"
                    echo "✓ Commit: ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('🔨 Build') {
            steps {
                echo '════════════════════════════════════════════'
                echo ' Stage 2: Maven Build'
                echo '════════════════════════════════════════════'
                script {
                    if (isUnix()) {
                        sh 'mvn -B clean package -DskipTests'
                    } else {
                        bat 'mvn -B clean package -DskipTests'
                    }
                }
            }
        }

        stage('🧪 Test') {
            steps {
                echo '════════════════════════════════════════════'
                echo ' Stage 3: Run Tests'
                echo '════════════════════════════════════════════'
                script {
                    if (isUnix()) {
                        sh 'mvn -B test'
                    } else {
                        bat 'mvn -B test'
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('📊 Reports') {
            steps {
                echo '════════════════════════════════════════════'
                echo ' Stage 4: Generate Reports'
                echo '════════════════════════════════════════════'
                script {
                    if (fileExists('test-output/extent-reports/SparkReport.html')) {
                        echo '✓ Extent Report generated'
                        archiveArtifacts artifacts: 'test-output/**/*', allowEmptyArchive: true
                    } else {
                        echo '⚠ Extent Report not found'
                    }
                    archiveArtifacts artifacts: 'target/surefire-reports/**/*', allowEmptyArchive: true
                }
            }
        }
    }

    post {
        success {
            echo '════════════════════════════════════════════'
            echo ' ✅ BUILD SUCCESS'
            echo '════════════════════════════════════════════'
            script {
                sendEmail('SUCCESS')
            }
        }

        failure {
            echo '════════════════════════════════════════════'
            echo ' ❌ BUILD FAILED'
            echo '════════════════════════════════════════════'
            script {
                sendEmail('FAILURE')
            }
        }

        always {
            cleanWs()
        }
    }
}

def sendEmail(String status) {
    def recipients = 'Nareshsofttechh@gmail.com'
    def color = status == 'SUCCESS' ? 'green' : 'red'
    
    emailext(
        subject: "[${status}] ${env.PROJECT_NAME} Build #${env.BUILD_NUMBER}",
        body: """
            <h2 style="color:${color};">Build ${status}</h2>
            <p><b>Project:</b> ${env.PROJECT_NAME}</p>
            <p><b>Build:</b> #${env.BUILD_NUMBER}</p>
            <p><b>Branch:</b> ${env.GIT_BRANCH}</p>
            <p><b>Commit:</b> ${env.GIT_COMMIT_SHORT}</p>
            <p><a href="${env.BUILD_URL}">View Build</a></p>
            <p><a href="${env.BUILD_URL}console">Console Log</a></p>
        """,
        to: recipients,
        mimeType: 'text/html'
    )
}