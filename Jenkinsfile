/**
 * =============================================================================
 * EnterpriseResourcePlanning — Enterprise Declarative Jenkins CI/CD Pipeline
 * =============================================================================
 * Stack : Selenium 4 | Java 17 | Maven | Cucumber BDD | JUnit | Extent Reports
 * Repo  : https://github.com/Naresh-Kumar01/EnterpriseResourcePlanning
 *
 * Trigger : GitHub Webhook (push) → githubPush()
 * Execution: Docker Selenium Grid (hub + chrome) → mvn clean test → Extent Reports
 *
 * Jenkins plugins required:
 *   Pipeline, Git, GitHub, Email Extension, JUnit, HTML Publisher (optional),
 *   Credentials, Timestamper, Workspace Cleanup, Maven Integration
 *
 * Jenkins → Manage Jenkins → System → Extended E-mail Notification (Gmail SMTP):
 *   smtp.gmail.com:587 TLS | App Password
 *
 * Job environment variable (recommended):
 *   NOTIFICATION_EMAIL = your-team@email.com
 * =============================================================================
 */
pipeline {
    agent any

    options {
        buildDiscarder(logRotator(numToKeepStr: '25', artifactNumToKeepStr: '15'))
        timestamps()
        timeout(time: 90, unit: 'MINUTES')
        disableConcurrentBuilds()
        skipDefaultCheckout(true)
    }

    environment {
        PROJECT_NAME          = 'EnterpriseResourcePlanning'
        GITHUB_REPO           = 'https://github.com/Naresh-Kumar01/EnterpriseResourcePlanning'
        MAVEN_OPTS            = '-Xmx1536m -XX:MaxMetaspaceSize=512m'
        SELENIUM_GRID_URL     = 'http://localhost:4444/wd/hub'
        SELENIUM_GRID_ENABLED = 'true'
        HEADLESS              = 'true'
        ENVIRONMENT           = 'dev'
        BROWSER               = 'chrome'
        EXTENT_REPORT_DIR     = 'test-output/extent-reports'
        EXTENT_REPORT_FILE    = 'SparkReport.html'
    }

    triggers {
        // Auto-trigger on GitHub push (requires GitHub webhook + Branch Source or SCM polling fallback)
        githubPush()
    }

    stages {

        stage('Checkout Code') {
            steps {
                echo '══════════════════════════════════════════════════════'
                echo ' STAGE 1: Checkout Code from GitHub'
                echo '══════════════════════════════════════════════════════'
                checkout scm
                script {
                    if (isUnix()) {
                        env.GIT_BRANCH_NAME  = sh(returnStdout: true, script: 'git rev-parse --abbrev-ref HEAD').trim()
                        env.GIT_COMMIT_SHORT = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                        env.GIT_COMMIT_MSG   = sh(returnStdout: true, script: 'git log -1 --pretty=%B').trim()
                    } else {
                        env.GIT_BRANCH_NAME  = bat(returnStdout: true, script: '@git rev-parse --abbrev-ref HEAD').trim()
                        env.GIT_COMMIT_SHORT = bat(returnStdout: true, script: '@git rev-parse --short HEAD').trim()
                        env.GIT_COMMIT_MSG   = bat(returnStdout: true, script: '@git log -1 --pretty=%B').trim()
                    }
                    echo "Branch : ${env.GIT_BRANCH_NAME}"
                    echo "Commit : ${env.GIT_COMMIT_SHORT}"
                }
            }
        }

        stage('Start Selenium Grid') {
            steps {
                echo '══════════════════════════════════════════════════════'
                echo ' STAGE 2: Start Selenium Grid (Docker Hub + Chrome Node)'
                echo '══════════════════════════════════════════════════════'
                script {
                    try {
                        if (isUnix()) {
                            sh 'docker compose -f docker-compose.yml down --remove-orphans 2>/dev/null || true'
                            sh 'docker compose -f docker-compose.yml up -d'
                            sh 'bash scripts/wait-for-grid.sh'
                        } else {
                            bat 'docker compose -f docker-compose.yml down --remove-orphans 2>nul || ver >nul'
                            bat 'docker compose -f docker-compose.yml up -d'
                            bat 'powershell -ExecutionPolicy Bypass -File scripts\\wait-for-grid.ps1'
                        }
                        echo 'Selenium Grid is UP at http://localhost:4444'
                    } catch (Exception e) {
                        error("Failed to start Selenium Grid. Is Docker Desktop running? ${e.message}")
                    }
                }
            }
        }

        stage('Maven Clean Test') {
            steps {
                echo '══════════════════════════════════════════════════════'
                echo ' STAGE 3: Maven Clean Test (Surefire → Cucumber → JUnit)'
                echo ' Command: mvn clean test'
                echo '══════════════════════════════════════════════════════'
                script {
                    try {
                        if (isUnix()) {
                            sh """
                                mvn -B clean test \
                                  -Dselenium.grid.enabled=${SELENIUM_GRID_ENABLED} \
                                  -Dselenium.grid.url=${SELENIUM_GRID_URL} \
                                  -Dheadless=${HEADLESS} \
                                  -Denvironment=${ENVIRONMENT} \
                                  -Dbrowser=${BROWSER}
                            """
                        } else {
                            bat """
                                mvn -B clean test ^
                                  -Dselenium.grid.enabled=%SELENIUM_GRID_ENABLED% ^
                                  -Dselenium.grid.url=%SELENIUM_GRID_URL% ^
                                  -Dheadless=%HEADLESS% ^
                                  -Denvironment=%ENVIRONMENT% ^
                                  -Dbrowser=%BROWSER%
                            """
                        }
                    } catch (Exception e) {
                        currentBuild.result = 'FAILURE'
                        throw e
                    }
                }
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Publish Extent Reports') {
            steps {
                echo '══════════════════════════════════════════════════════'
                echo ' STAGE 4: Publish Extent Reports'
                echo '══════════════════════════════════════════════════════'
                script {
                    def extentPath = "${EXTENT_REPORT_DIR}/${EXTENT_REPORT_FILE}"
                    if (fileExists(extentPath)) {
                        echo "Extent Report generated: ${extentPath}"
                        env.EXTENT_REPORT_LINK = "${env.BUILD_URL}artifact/${extentPath}"
                        // Requires HTML Publisher Plugin
                        try {
                            publishHTML(target: [
                                allowMissing         : true,
                                alwaysLinkToLastBuild: true,
                                keepAll              : true,
                                reportDir            : EXTENT_REPORT_DIR,
                                reportFiles          : EXTENT_REPORT_FILE,
                                reportName           : 'Extent Spark Report',
                                reportTitles         : 'EnterpriseResourcePlanning Automation Report'
                            ])
                            echo "Published HTML report in Jenkins sidebar: Extent Spark Report"
                        } catch (Exception e) {
                            echo "HTML Publisher not installed — report archived as artifact only. ${e.message}"
                        }
                    } else {
                        echo "WARNING: Extent report not found at ${extentPath}"
                        unstable('Extent report file missing')
                    }
                    if (fileExists('target/cucumber-reports/cucumber.html')) {
                        echo 'Cucumber HTML: target/cucumber-reports/cucumber.html'
                    }
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                echo '══════════════════════════════════════════════════════'
                echo ' STAGE 5: Archive Artifacts'
                echo '══════════════════════════════════════════════════════'
                archiveArtifacts artifacts: '''
                    test-output/**/*
                    target/cucumber-reports/**/*
                    target/surefire-reports/**/*
                ''', allowEmptyArchive: true, fingerprint: true, onlyIfSuccessful: false
                echo 'Artifacts archived: Extent, Cucumber, Surefire, screenshots'
            }
        }
    }

    post {
        always {
            echo '══════════════════════════════════════════════════════'
            echo ' STAGE 7: Cleanup Docker Selenium Grid Containers'
            echo '══════════════════════════════════════════════════════'
            script {
                try {
                    if (isUnix()) {
                        sh 'docker compose -f docker-compose.yml down --remove-orphans || true'
                    } else {
                        bat 'docker compose -f docker-compose.yml down --remove-orphans || exit /b 0'
                    }
                    echo 'Docker containers stopped and removed.'
                } catch (Exception e) {
                    echo "Docker cleanup warning: ${e.message}"
                }
            }
        }

        success {
            echo '══════════════════════════════════════════════════════'
            echo ' STAGE 6: Send Email Notification — SUCCESS'
            echo '══════════════════════════════════════════════════════'
            script { sendBuildEmail('SUCCESS') }
        }

        failure {
            echo '══════════════════════════════════════════════════════'
            echo ' STAGE 6: Send Email Notification — FAILURE'
            echo '══════════════════════════════════════════════════════'
            script { sendBuildEmail('FAILURE') }
        }

        unstable {
            script { sendBuildEmail('UNSTABLE') }
        }
    }
}

/**
 * Sends build notification with Extent report link and attachments.
 */
def sendBuildEmail(String status) {
    def recipients = env.NOTIFICATION_EMAIL ?: 'Nareshsofttechh@gmail.com'
    def color      = status == 'SUCCESS' ? '#2e7d32' : (status == 'UNSTABLE' ? '#f57c00' : '#c62828')
    def extentLink = env.EXTENT_REPORT_LINK ?: "${env.BUILD_URL}artifact/${EXTENT_REPORT_DIR}/${EXTENT_REPORT_FILE}"

    emailext(
        subject: "[${status}] ${env.PROJECT_NAME} #${env.BUILD_NUMBER} | ${env.GIT_BRANCH_NAME}",
        body: """<!DOCTYPE html>
<html><body style="font-family:Segoe UI,Arial,sans-serif;margin:0;padding:20px;background:#f0f2f5;">
  <div style="max-width:680px;margin:auto;background:#fff;border-radius:8px;padding:28px;box-shadow:0 2px 12px rgba(0,0,0,.08);">
    <h2 style="color:${color};margin-top:0;">EnterpriseResourcePlanning CI/CD — ${status}</h2>
    <p>Automated Selenium BDD pipeline completed for <b>${env.GITHUB_REPO}</b></p>
    <table style="width:100%;border-collapse:collapse;margin:16px 0;">
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Build</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;">#${env.BUILD_NUMBER}</td></tr>
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Branch</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;">${env.GIT_BRANCH_NAME}</td></tr>
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Commit</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;">${env.GIT_COMMIT_SHORT}</td></tr>
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Grid</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;">${env.SELENIUM_GRID_URL}</td></tr>
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Extent Report</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;"><a href="${extentLink}">Open Extent Spark Report</a></td></tr>
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Jenkins Build</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;"><a href="${env.BUILD_URL}">${env.BUILD_URL}</a></td></tr>
      <tr><td style="padding:10px;border:1px solid #e0e0e0;background:#fafafa;"><b>Console Log</b></td>
          <td style="padding:10px;border:1px solid #e0e0e0;"><a href="${env.BUILD_URL}console">View Console Output</a></td></tr>
    </table>
    <p style="color:#666;font-size:13px;">
      Execution: GitHub → Webhook → Jenkins → Docker Grid → Maven Surefire → Cucumber/JUnit → Extent Reports
    </p>
    <p style="font-size:12px;color:#999;">EnterpriseResourcePlanning SDET Automation | Naresh-Kumar01</p>
  </div>
</body></html>""",
        to: recipients,
        mimeType: 'text/html',
        attachLog: true,
        compressLog: true,
        attachmentsPattern: """
            ${EXTENT_REPORT_DIR}/*.html,
            test-output/screenshots/*.png,
            target/cucumber-reports/*.html,
            target/surefire-reports/*.xml
        """
    )
}
