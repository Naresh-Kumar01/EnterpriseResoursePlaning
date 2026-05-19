# EnterpriseResourcePlanning — Jenkins CI/CD + Docker Selenium Grid (Senior SDET Guide)

**Repository:** https://github.com/Naresh-Kumar01/EnterpriseResourcePlanning  
**Stack:** Selenium 4 · Java 17 · Cucumber · Maven · Jenkins · Docker Grid · Extent Reports · Gmail SMTP

---

## Architecture (Execution Flow)

```
GitHub (push/commit)
        ↓
GitHub Webhook (POST → Jenkins)
        ↓
Jenkins Declarative Pipeline (Jenkinsfile)
        ↓
┌───────────────────────────────────────┐
│ 1. Checkout Code                      │
│ 2. Maven Build (compile)              │
│ 3. Start Selenium Grid (Docker)       │
│ 4. Execute Tests (mvn clean test)     │
│ 5. Generate Extent Reports            │
│ 6. Archive Reports                    │
│ 7. Email Notification (SUCCESS/FAIL)  │
│ 8. Cleanup Docker Containers          │
└───────────────────────────────────────┘
        ↓
Selenium Grid (hub + chrome node)
        ↓
Maven Surefire → Cucumber → ERP application tests
        ↓
Reports: test-output/extent-reports, target/cucumber-reports
        ↓
Email to team (with logs/attachments)
```

---

## Folder Structure

```
EnterpriseResourcePlanning/
├── Jenkinsfile                    # Declarative pipeline
├── docker-compose.yml             # Selenium Hub + Chrome node
├── pom.xml                        # Maven (+ grid/ci profiles)
├── scripts/
│   ├── start-selenium-grid.ps1
│   ├── stop-selenium-grid.ps1
│   ├── wait-for-grid.ps1
│   └── wait-for-grid.sh
├── docs/
│   └── JENKINS_CI_CD_SETUP.md     # This guide
├── jenkins/
│   └── email-templates/
│       └── build-notification.html
├── src/test/java/
│   ├── com/enterpriseresourceplanning/stepdefinitions/Steps.java
│   ├── testRunner/TestRun.java
│   └── utilities/
│       ├── DriverFactory.java     # Local + Grid WebDriver
│       ├── ConfigLoader.java      # CI property overrides
│       └── AuthenticationValidator.java
└── src/test/resources/
    ├── config.properties          # selenium.grid.* settings
    ├── features/signin.feature
    └── extent.properties
```

---

## 1. Prerequisites (Windows)

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 17+ | Compile & run tests |
| Maven | 3.8+ | Build & test execution |
| Git | Latest | SCM |
| Docker Desktop | Latest | Selenium Grid containers |
| Jenkins LTS | 2.4xx+ | CI server |
| IntelliJ IDEA | Optional | Local development |

---

## 2. Docker Desktop (Windows)

1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/).
2. Enable **WSL 2** backend (recommended).
3. Start Docker Desktop — verify:

```powershell
docker version
docker compose version
```

### Start Selenium Grid locally

```powershell
cd C:\Users\Admin\Desktop\EnterpriseResourcePlanning
docker compose up -d
powershell -File scripts\wait-for-grid.ps1
```

- Grid UI: http://localhost:4444  
- WebDriver: http://localhost:4444/wd/hub  

### Stop Grid

```powershell
docker compose down --remove-orphans
```

---

## 3. Jenkins Installation (Windows)

1. Download Jenkins LTS `.msi` or run as service.
2. Unlock Jenkins → install suggested plugins.
3. Create admin user.

### Required Jenkins Plugins

| Plugin | Purpose |
|--------|---------|
| Pipeline | Jenkinsfile support |
| Git | SCM checkout |
| GitHub Integration | Webhooks |
| GitHub Branch Source | Multibranch (optional) |
| Email Extension | SMTP notifications |
| JUnit | Test result trends |
| Credentials | Secure secrets |
| Timestamper | Log timestamps |
| Workspace Cleanup | Disk hygiene |

**Manage Jenkins → Plugins → Available plugins** → install above → restart.

---

## 4. Jenkins Global Tool Configuration

**Manage Jenkins → Tools**

| Tool | Name | Path / Version |
|------|------|----------------|
| JDK | JDK-17 | `C:\Program Files\Java\jdk-17` |
| Maven | Maven-3.9 | `C:\Program Files\Apache\maven` or auto-install |
| Git | Default | From PATH |

**Manage Jenkins → System → Environment variables** (optional):

- `MAVEN_HOME`, `JAVA_HOME` on Windows agent PATH.

---

## 5. Gmail SMTP (Email Extension Plugin)

**Manage Jenkins → System → Extended E-mail Notification**

| Field | Value |
|-------|--------|
| SMTP server | `smtp.gmail.com` |
| SMTP Port | `587` |
| Use TLS | ✓ |
| Username | `Nareshsofttechh@gmail.com` |
| Password | Gmail **App Password** (not regular password) |
| Default content type | `HTML` |
| Default recipients | `Nareshsofttechh@gmail.com` |

**Gmail App Password:** Google Account → Security → 2-Step Verification → App passwords.

**Manage Jenkins → System → E-mail Notification** (JavaMail):

- Same SMTP settings for consistency.

### Jenkins Credentials (recommended)

| ID | Type | Value |
|----|------|-------|
| `gmail-smtp-username` | Secret text | Gmail address |
| `gmail-smtp-password` | Secret text | App password |
| `notification-email` | Secret text | Recipients |

Set job environment variable `NOTIFICATION_EMAIL` or edit `Jenkinsfile` `sendBuildEmail()`.

---

## 6. Jenkins Job Setup

### Option A: Pipeline from SCM (recommended)

1. **New Item** → `EnterpriseResourcePlanning-CI` → **Pipeline**.
2. **Pipeline definition:** Pipeline script from SCM.
3. **SCM:** Git.
4. **Repository URL:** `https://github.com/Naresh-Kumar01/EnterpriseResourcePlanning.git`
5. **Branch:** `*/feature/signin-negative-test-cases` or `*/main`.
6. **Script Path:** `Jenkinsfile`.
7. Save → **Build Now**.

### Option B: GitHub Webhook (auto-trigger on push)

1. Jenkins job → **Build Triggers** → enable if using GitHub plugin.
2. GitHub repo → **Settings → Webhooks → Add webhook**:
   - **Payload URL:** `http://<JENKINS_HOST>:8080/github-webhook/`
   - **Content type:** `application/json`
   - **Events:** Just the push event
3. For local Jenkins, use **ngrok** to expose port 8080:

```powershell
ngrok http 8080
```

4. Use ngrok HTTPS URL in GitHub webhook.

**Note:** `Jenkinsfile` includes `triggers { githubPush() }` for GitHub Branch Source / Organization folders.

---

## 7. Running Tests on Selenium Grid

### Local (with Grid)

```powershell
docker compose up -d
mvn clean test -Pgrid -Dselenium.grid.url=http://localhost:4444/wd/hub
```

### CI profile (Jenkins equivalent)

```powershell
mvn clean test -Pci
```

### Properties

| Property | Default | CI value |
|----------|---------|----------|
| `selenium.grid.enabled` | `false` | `true` |
| `selenium.grid.url` | `http://localhost:4444/wd/hub` | same |
| `headless` | `false` | `true` |

---

## 8. Pipeline Stages (Jenkinsfile)

| Stage | Action |
|-------|--------|
| Checkout Code | `checkout scm` + git metadata |
| Maven Build | `mvn clean compile test-compile` |
| Start Selenium Grid | `docker compose up -d` + wait-for-grid |
| Execute Tests | `mvn clean test` with Grid properties |
| Generate Extent Reports | Log report paths |
| Archive Reports | `test-output/**`, `target/cucumber-reports/**` |
| Send Email Notification | `post { success/failure { emailext } }` |
| Cleanup Docker Containers | `docker compose down` in `post { always }` |

---

## 9. Reports

| Report | Location |
|--------|----------|
| Extent Spark | `test-output/extent-reports/SparkReport.html` |
| Cucumber HTML | `target/cucumber-reports/cucumber.html` |
| Surefire XML | `target/surefire-reports/` |
| Screenshots | `test-output/screenshots/` |

Archived automatically in Jenkins **Build Artifacts**.

---

## 10. Branching & CI/CD Best Practices

| Practice | Recommendation |
|----------|----------------|
| Branch naming | `feature/<ticket>-<description>`, `bugfix/`, `release/` |
| Main branch | `main` — protected, PR required |
| CI trigger | Webhook on `push` to feature + main |
| Credentials | Never commit SMTP passwords; use Jenkins Credentials |
| Environment vars | `ENVIRONMENT`, `BROWSER`, `SELENIUM_GRID_URL` in Jenkinsfile |
| Parallel tests | Increase `SE_NODE_MAX_SESSIONS` in docker-compose; use Cucumber parallel runner (future) |
| Build retention | `logRotator` in Jenkinsfile (20 builds) |

---

## 11. Troubleshooting

### Docker daemon not running

```
error during connect: open //./pipe/dockerDesktopLinuxEngine
```

**Fix:** Start Docker Desktop; wait until engine is green.

### Selenium Grid node disconnected

**Fix:**

```powershell
docker compose down
docker compose up -d
curl http://localhost:4444/wd/hub/status
```

Check `ready: true` in JSON response.

### Jenkins webhook not triggering

- Verify webhook delivery in GitHub (Recent Deliveries → 200 OK).
- Jenkins URL must be reachable from internet (use ngrok for local).
- Check Jenkins **Manage Jenkins → System Log** for GitHub events.

### Maven build failed

```powershell
mvn -B clean test -X
```

- Verify JDK 17: `java -version`
- Verify Maven: `mvn -version`
- Check `pom.xml` dependencies download (network/proxy).

### Chrome browser session issues on Grid

- Increase `shm_size: 2gb` in docker-compose (already set).
- Add `--disable-dev-shm-usage` in `DriverFactory` (already set).
- Reduce parallel sessions: `SE_NODE_MAX_SESSIONS=2`.

### Email not sent

- Configure Extended E-mail Notification + test email.
- Use Gmail App Password (not account password).
- Check **Manage Jenkins → System Log** for JavaMail errors.

---

## 12. Future Enhancements

| Enhancement | Benefit |
|-------------|---------|
| AWS EC2 Jenkins agent | Scalable CI workers |
| Kubernetes (K8s) Grid | Enterprise-scale parallel browsers |
| Allure Reports | Richer test analytics |
| Slack / Teams notifications | Real-time team alerts |
| Cross-browser matrix | Chrome + Firefox + Edge nodes in compose |
| SonarQube stage | Code quality gates |
| Blue Ocean UI | Visual pipeline |

---

## 13. Interview Q&A

### Jenkins

**Q: What is a Declarative Pipeline?**  
A: Jenkins Pipeline defined in `Jenkinsfile` with structured `pipeline { agent, stages, post }` blocks — version-controlled CI as code.

**Q: Difference between Scripted and Declarative pipeline?**  
A: Scripted uses Groovy freely; Declarative enforces structure and is easier for teams to maintain.

**Q: What is `post { always }`?**  
A: Runs after all stages regardless of success/failure — ideal for cleanup and archiving.

### Docker

**Q: Why `shm_size: 2gb` for Chrome container?**  
A: Chrome needs shared memory; default 64MB causes crashes in Docker.

**Q: docker compose vs docker-compose?**  
A: Compose V2 uses `docker compose` as CLI plugin; same purpose, orchestrates multi-container apps.

### Selenium Grid

**Q: Hub vs Node?**  
A: Hub routes commands; Nodes execute browsers. Tests connect to Hub URL.

**Q: RemoteWebDriver URL in Grid 4?**  
A: Typically `http://localhost:4444/wd/hub` or `http://localhost:4444`.

### CI/CD

**Q: What triggers your pipeline?**  
A: GitHub webhook on push → Jenkins job → pipeline stages → tests on Grid → reports → email.

**Q: How do you secure credentials in Jenkins?**  
A: Jenkins Credentials Plugin + never hardcode secrets in Jenkinsfile; inject via `credentials()` or env vars.

### GitHub Webhooks

**Q: What does GitHub send on push?**  
A: JSON payload with repo, branch, commit, pusher — Jenkins GitHub plugin receives POST and queues build.

---

## 14. Quick Command Reference

```powershell
# Local Grid + tests
docker compose up -d
mvn clean test -Pgrid

# Stop Grid
docker compose down

# Jenkins-equivalent Maven
mvn clean test -Pci

# Compile only
mvn clean compile test-compile -DskipTests
```

---

**Author:** Naresh-Kumar01 · EnterpriseResourcePlanning SDET Automation  
**Last updated:** 2026
