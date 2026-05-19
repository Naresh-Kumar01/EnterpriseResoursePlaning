# Enterprise Resource Planning BDD Automation Framework

Production-ready BDD test automation framework using **Selenium 4**, **Cucumber 7**, **TestNG 7**, **Log4j2**, and **Extent Reports**. Built with Page Object Model (POM) for team scalability.

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Selenium 4.x | Browser automation |
| Cucumber 7.x | BDD / Gherkin scenarios |
| TestNG 7.x | Test execution & parallel runs |
| WebDriverManager | Automatic driver binaries |
| Log4j2 | Structured logging |
| Extent Reports | HTML reporting with screenshots |
| RestAssured | API testing support |
| Apache POI | Excel test data |
| Jackson | JSON test data |
| Maven | Build & dependency management |

## Project Structure

```
EnterpriseResourcePlanning/
├── pom.xml
├── testng.xml
├── README.md
├── .github/workflows/ci.yml
└── src/test/
    ├── java/com/enterpriseresourceplanning/
    │   ├── runner/             # Cucumber + JUnit test runner
    │   ├── stepdefinitions/    # Gherkin step bindings
    │   └── utilities/          # Config, driver, grid, auth validation
    └── resources/
        ├── features/           # .feature files
        ├── testdata/           # JSON & Excel data
        ├── config.properties
        ├── cucumber.properties
        ├── extent.properties
        └── log4j2.xml
```

## Prerequisites

- **JDK 17+**
- **Maven 3.8+**
- Chrome / Firefox / Edge (WebDriverManager handles drivers)

## CI/CD (Jenkins + Docker Selenium Grid)

| Resource | Description |
|----------|-------------|
| [Jenkinsfile](Jenkinsfile) | Declarative pipeline (webhook → Grid → `mvn clean test` → email) |
| [docker-compose.yml](docker-compose.yml) | Selenium Hub + Chrome node |
| [docs/JENKINS_CI_CD_SETUP.md](docs/JENKINS_CI_CD_SETUP.md) | Full Jenkins/Docker/webhook/SMTP setup |
| [docs/CI_CD_EXECUTION_FLOW.md](docs/CI_CD_EXECUTION_FLOW.md) | Architecture diagram & workflow |
| [jenkins/GMAIL_SMTP_SETUP.md](jenkins/GMAIL_SMTP_SETUP.md) | Gmail email notifications |

```powershell
docker compose up -d
mvn clean test -Pci
```

## Quick Start

```bash
# Clone and enter project
cd EnterpriseResourcePlanning

# Run all tests (default: dev + chrome)
mvn clean test

# Run smoke tests only
mvn clean test -Dcucumber.filter.tags="@smoke"

# Run on staging with Firefox headless
mvn clean test -Pstaging -Pfirefox -Dheadless=true
```

## Configuration

Edit `src/test/resources/config.properties` or pass system properties:

| Property | Description | Example |
|----------|-------------|---------|
| `environment` | dev / staging / prod | `-Denvironment=staging` |
| `browser` | chrome / firefox / edge | `-Dbrowser=firefox` |
| `headless` | Run without UI | `-Dheadless=true` |
| `retry.count` | Flaky test retries | `2` |

Base URLs are keyed by environment: `dev.base.url`, `staging.base.url`, etc.

## Writing New Tests

### 1. Create a feature file

```gherkin
@myFeature @smoke
Feature: My Feature
  Scenario: My scenario
    Given the user is on the login page
    When the user logs in with username "standard_user" and password "secret_sauce"
    Then the user should be redirected to the home page
```

### 2. Add page object (if needed)

```java
public class MyPage extends BasePage {
    private static final By ELEMENT = By.id("my-element");
    public MyPage(WebDriver driver) { super(driver); }
    public void clickElement() { click(ELEMENT); }
}
```

### 3. Add step definitions

```java
public class MyStepDefinitions extends BaseStep {
    @When("I perform an action")
    public void iPerformAnAction() {
        logStep("Performing action");
        // use WebDriverUtil.getDriver() and page objects
    }
}
```

### 4. Run

```bash
mvn test -Dcucumber.filter.tags="@myFeature"
```

## Framework Features

- **Page Object Model** – Reusable pages with explicit/fluent waits
- **BDD/Gherkin** – Readable scenarios, Background, Scenario Outlines
- **Hooks** – Driver init, screenshots on failure, report flush
- **Test data** – Excel, JSON, Gherkin Examples, DataTables
- **Reporting** – Extent HTML + Cucumber HTML + Log4j2 logs
- **Parallel execution** – TestNG `parallel="methods"` in `testng.xml`
- **Retry logic** – `RetryAnalyzer` for flaky tests
- **Multi-environment** – Maven profiles: dev, staging, prod
- **Mobile readiness** – Chrome mobile emulation via `mobile.enabled=true`
- **API support** – `ApiUtil` with RestAssured

## Reports & Logs

After execution:

| Output | Location |
|--------|----------|
| Extent HTML Report | `test-output/extent-reports/` |
| Cucumber HTML Report | `target/cucumber-reports/cucumber.html` |
| Screenshots | `test-output/screenshots/` |
| Logs | `test-output/logs/automation.log` |

## CI/CD Integration

### GitHub Actions

Pipeline runs on push/PR. See `.github/workflows/ci.yml`.

### Jenkins (example)

```groovy
stage('Test') {
    sh 'mvn clean test -Denvironment=staging -Dbrowser=chrome -Dheadless=true'
}
publishHTML(target: [reportDir: 'test-output/extent-reports', reportFiles: '*.html'])
```

### Azure DevOps (example)

```yaml
- script: mvn clean test -Dheadless=true
  displayName: Run BDD Tests
- task: PublishBuildArtifacts@1
  inputs:
    PathtoPublish: 'test-output'
```

## Demo Application

Sample tests target [SauceDemo](https://www.saucedemo.com) (Swag Labs). Replace URLs in `config.properties` for your application under test.

| User | Password |
|------|----------|
| standard_user | secret_sauce |
| locked_out_user | secret_sauce |

## Best Practices

1. **No hardcoded values** – Use `config.properties` and test data files
2. **DRY** – Reuse steps and `BasePage` helpers
3. **Tags** – `@smoke`, `@regression` for selective runs
4. **Independent scenarios** – Each scenario sets up via Background/Hooks
5. **Meaningful assertions** – Use `CustomAssert` for report-friendly messages

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Driver not found | WebDriverManager auto-downloads; check network |
| Element not found | Increase `explicit.wait` in config |
| Excel not found | Run `ExcelDataGenerator` main class |
| Tag filter runs 0 tests | Remove `cucumber.filter.tags` from cucumber.properties |

## License

Internal use – Enterprise Resource Planning QA Team.
