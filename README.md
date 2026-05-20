# Enterprise Resource Planning — Unified UI + API Automation Framework

Production-ready automation framework combining **Selenium/Cucumber UI** and **REST Assured/TestNG API** automation in a single Maven project.

## Tech Stack

| Layer | Technology |
|-------|------------|
| UI | Selenium 4, Cucumber 7, JUnit 4, WebDriverManager |
| API | REST Assured 5, TestNG 7, Jackson, JSON Schema Validator |
| Build | Maven 3.8+ |
| Data | JSON, Excel (Apache POI) |
| Logging | Log4j2 |
| Reporting | Extent Reports (+ optional Allure) |
| CI/CD | Jenkins, GitHub Actions, Docker Selenium Grid |

## Project Structure

```
src/test/java/com/enterpriseresourceplanning/
├── api/
│   ├── base/           BaseAPI.java
│   ├── endpoints/      Routes.java
│   ├── payloads/       UserPayload.java
│   ├── models/         User.java
│   ├── testcases/      UserGETTest, UserPOSTTest, UserPUTTest, UserDELETETest
│   ├── utilities/      APIUtils, ConfigManager, RequestSpecBuilder, ...
│   └── validators/     ResponseValidator, SchemaValidator
├── ui/
│   ├── pageObjects/    BasePage, LoginPage
│   ├── testCases/      Steps.java (Cucumber glue)
│   ├── utilities/      ConfigLoader, DriverFactory, GridConnectionHelper
│   └── runner/         TestRun.java
├── listeners/          ExtentReportManager, TestListener
└── base/               BaseClass.java

src/test/resources/
├── config/             qa.properties, uat.properties, prod.properties
├── testdata/           users.json, users.xlsx
├── schemas/            user-schema.json, users-list-schema.json
├── features/           UserSignin.feature
└── config.properties   Master UI + global settings
```

## Prerequisites

- JDK 17+
- Maven 3.8+
- Chrome (local UI runs) or Docker for Selenium Grid

## Quick Start

```bash
# Run UI + API tests
mvn clean test

# API tests only (fast, no browser)
mvn clean test -Papi-only

# UI BDD tests only
mvn clean test -Pui-only

# API on UAT environment
mvn clean test -Papi-only -Puat -Dapi.environment=uat

# UI on Selenium Grid (Docker)
docker compose up -d
mvn clean test -Pui-only -Pgrid -Dheadless=true
```

## API Automation

### Environment configuration

| File | Purpose |
|------|---------|
| `config.properties` | Master config (`api.environment=qa`) |
| `config/qa.properties` | QA base URL, timeouts |
| `config/uat.properties` | UAT overrides |
| `config/prod.properties` | Production overrides |

Override at runtime:

```bash
mvn test -Papi-only -Dapi.environment=uat -Dbase.url=https://your-api.uat.com
```

### Sample CRUD tests

Demo API: [JSONPlaceholder](https://jsonplaceholder.typicode.com) (`/users`).

| Test Class | HTTP | Endpoint |
|------------|------|----------|
| `UserGETTest` | GET | `/users`, `/users/{id}` |
| `UserPOSTTest` | POST | `/users` |
| `UserPUTTest` | PUT | `/users/1` |
| `UserDELETETest` | DELETE | `/users/1` |

### Validations

- Status code — `ResponseValidator.validateStatusCode()`
- Response body — `validateBodyField()`, `validateBodyContains()`
- JSON schema — `SchemaValidator.validate(response, "schemas/user-schema.json")`
- Headers — `validateHeader()`, `validateHeaderExists()`
- Response time — `validateResponseTime()`, `validateDefaultResponseTime()`

### TestNG suite

```bash
mvn test -Dsurefire.suiteXmlFiles=testng-api.xml
```

Or use `testng.xml` for the full API package.

### Reports & logs

| Output | Location |
|--------|----------|
| API Extent Report | `test-output/extent-reports/API_Automation_Report_*.html` |
| Logs | `test-output/logs/automation.log` |
| Surefire | `target/surefire-reports/` |

Request/response bodies are attached via `ApiLogFilter` → `ExtentReportManager`.

### Optional Allure

```bash
mvn clean test -Papi-only -Pallure
mvn allure:serve
```

## UI Automation

Cucumber BDD tests run via JUnit (`ui.runner.TestRun`).

```bash
mvn test -Pui-only -Dcucumber.filter.tags="@Smoke"
```

| Report | Location |
|--------|----------|
| Cucumber HTML | `target/cucumber-reports/cucumber.html` |
| Extent (via adapter) | `test-output/extent-reports/` |

## CI/CD

### GitHub Actions

`.github/workflows/ci.yml` supports `test_scope`: `all`, `ui`, `api`.

### Jenkins

```groovy
stage('API Tests') {
    sh 'mvn clean test -Papi-only -Dapi.environment=qa'
}
stage('UI Tests') {
    sh 'mvn clean test -Pui-only -Pci'
}
```

See [Jenkinsfile](Jenkinsfile) and [docs/JENKINS_CI_CD_SETUP.md](docs/JENKINS_CI_CD_SETUP.md).

### Docker + Grid

```powershell
docker compose up -d
mvn clean test -Pci
```

## Adding New API Tests

1. Add route in `api/endpoints/Routes.java`
2. Add model in `api/models/`
3. Add payload builder in `api/payloads/`
4. Create test class extending `BaseAPI` in `api/testcases/`
5. Use `APIUtils` for HTTP calls and validators for assertions

## Adding New UI Tests

1. Add `.feature` under `src/test/resources/features/`
2. Implement steps in `ui.testCases` or use `LoginPage` page object
3. Run with tags: `-Dcucumber.filter.tags="@YourTag"`

## Best Practices

- No hardcoded URLs — use `config/{env}.properties`
- Reuse `APIUtils`, `ResponseValidator`, `UserPayload`
- Keep UI locators in page objects
- Use `-Papi-only` / `-Pui-only` in CI for faster feedback
- Store secrets via Jenkins credentials / env vars (`-Dapi.auth.token`)

## License

Internal use — Enterprise Resource Planning QA Team.
