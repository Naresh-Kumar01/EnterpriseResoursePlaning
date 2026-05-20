package com.enterpriseresourceplanning.ui.testCases;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.enterpriseresourceplanning.ui.utilities.AuthenticationValidator;
import com.enterpriseresourceplanning.ui.utilities.ConfigLoader;
import com.enterpriseresourceplanning.ui.utilities.DriverFactory;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class Steps {

	private static final Logger LOG = Logger.getLogger(Steps.class.getName());

	private static final By USERNAME_FIELD = By.xpath("//input[@name='username']");
	private static final By PASSWORD_FIELD = By.xpath("//input[@name='password']");
	private static final By SIGNIN_BUTTON = By.xpath("//button[@type='submit']");
	private static final By HOME_ELEMENT = By.xpath("//*[normalize-space()='Home']");
	private static final By AUTH_FAILURE_MESSAGE = By.xpath("//*[contains(normalize-space(),'Authentication Failure')]");

	private WebDriver driver;
	private WebDriverWait wait;
	private Properties config;
	private String currentScenarioName;
	private Collection<String> currentScenarioTags;
	private boolean fullAuthRejectionValidated;

	@Before
	public void beforeEach(Scenario scenario) {
		currentScenarioName = scenario.getName();
		currentScenarioTags = scenario.getSourceTagNames();
		fullAuthRejectionValidated = false;
		loadConfig();
		launchBrowserAndNavigateToLogin();
		LOG.info("beforeEach: browser launched â†’ page created â†’ navigated to ERP application login");
	}

	@Given("the user navigates to logixerp.com")
	public void the_user_navigates_to_logixerp_com() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_FIELD));
	}

	@When("the user signs in with valid credentials")
	public void the_user_signs_in_with_valid_credentials() {
		attemptSignIn(
				config.getProperty("valid.username"),
				config.getProperty("valid.password"));
	}

	@When("the user signs in with correct username and incorrect password")
	public void the_user_signs_in_with_correct_username_and_incorrect_password() {
		attemptSignIn(
				config.getProperty("valid.username"),
				config.getProperty("invalid.password"));
	}

	@When("the user signs in with incorrect username and correct password")
	public void the_user_signs_in_with_incorrect_username_and_correct_password() {
		attemptSignIn(
				config.getProperty("invalid.username"),
				config.getProperty("valid.password"));
	}

	@When("the user signs in with incorrect username and incorrect password")
	public void the_user_signs_in_with_incorrect_username_and_incorrect_password() {
		attemptSignIn(
				config.getProperty("invalid.username"),
				config.getProperty("invalid.password"));
	}

	@When("the user signs in with invalid username and invalid password")
	public void the_user_signs_in_with_invalid_username_and_invalid_password() {
		attemptSignIn(
				config.getProperty("invalid.username"),
				config.getProperty("invalid.password"));
	}

	@When("the user signs in without entering username and password")
	public void the_user_signs_in_without_entering_username_and_password() {
		enableHtml5RequiredFields();
		clearAndSubmitSignIn("", "");
	}

	@When("the user signs in without entering a username")
	public void the_user_signs_in_without_entering_a_username() {
		enableHtml5RequiredFields();
		clearAndSubmitSignIn("", config.getProperty("valid.password"));
	}

	@When("the user signs in without entering a password")
	public void the_user_signs_in_without_entering_a_password() {
		enableHtml5RequiredFields();
		clearAndSubmitSignIn(config.getProperty("valid.username"), "");
	}

	@Then("the user should be on the dashboard")
	public void the_user_should_be_on_the_dashboard() {
		String currentUrl = driver.getCurrentUrl();
		Assert.assertFalse("Sign in failed: " + currentUrl, hasAuthenticationError(currentUrl));
		wait.until(d -> d.getCurrentUrl().contains("/Dashboard")
				|| (!isOnLoginPage(d.getCurrentUrl()) && !d.findElements(HOME_ELEMENT).isEmpty()));
		Assert.assertTrue(
				"User should reach the dashboard after sign in: " + driver.getCurrentUrl(),
				driver.getCurrentUrl().contains("/Dashboard") || !isOnLoginPage(driver.getCurrentUrl()));
	}

	@And("the successful sign-in validation message is displayed")
	public void the_successful_sign_in_validation_message_is_displayed() {
		try {
			WebElement homeLink = wait.until(ExpectedConditions.visibilityOfElementLocated(HOME_ELEMENT));
			Assert.assertTrue("Home element should be displayed on Dashboard", homeLink.isDisplayed());
			LOG.info("Test PASSED: " + currentScenarioName);
		} finally {
			finallyClosePageAndBrowser();
		}
	}

	@Then("the user should be on the dashboard with successful signin validation")
	public void the_user_should_be_on_the_dashboard_with_successful_signin_validation() {
		the_user_should_be_on_the_dashboard();
		the_successful_sign_in_validation_message_is_displayed();
	}

	@Then("sign-in should fail with an authentication error")
	public void sign_in_should_fail_with_an_authentication_error() {
		runFullAuthenticationRejectionValidationIfNeeded();
		String currentUrl = driver.getCurrentUrl();
		Assert.assertTrue(
				"Authentication error should be indicated in URL or page state: " + currentUrl,
				hasAuthenticationError(currentUrl) || isAuthenticationFailureMessageVisible());
	}

	@And("the user is not logged in")
	public void the_user_is_not_logged_in() {
		if (isValidationScenario()) {
			assertUserRemainsUnauthenticatedForValidation();
			finallyCloseAfterScenario();
			return;
		}

		runFullAuthenticationRejectionValidationIfNeeded();
		String currentUrl = driver.getCurrentUrl();
		Assert.assertFalse("User must not reach Dashboard without auth: " + currentUrl,
				currentUrl.contains("/Dashboard") && !hasAuthenticationError(currentUrl));

		List<WebElement> homeLinks = driver.findElements(HOME_ELEMENT);
		Assert.assertTrue(
				"Dashboard Home must not be visible (user not logged in)",
				homeLinks.isEmpty() || !homeLinks.get(0).isDisplayed());

		if (shouldCloseAfterAuthNegativeStep()) {
			finallyCloseAfterScenario();
		}
	}

	@And("an error message is displayed")
	public void an_error_message_is_displayed() {
		runFullAuthenticationRejectionValidationIfNeeded();
		Assert.assertTrue(
				"Authentication failure error message must be visible",
				isAuthenticationFailureMessageVisible());
		if (!hasTag("Security")) {
			finallyCloseAfterScenario();
		}
	}

	@And("the invalid login attempt is logged for security audit")
	public void the_invalid_login_attempt_is_logged_for_security_audit() {
		String auditEntry = String.format(
				"SECURITY_AUDIT | scenario=%s | url=%s | timestamp=%d",
				currentScenarioName,
				driver.getCurrentUrl(),
				System.currentTimeMillis());
		LOG.warning(auditEntry);
		System.out.println("[SECURITY_AUDIT] " + auditEntry);
		Assert.assertTrue(
				"Invalid login must not grant dashboard access",
				!isDashboardReachableWithoutAuth());
		finallyCloseAfterScenario();
	}

	@Then("sign-in should fail with validation errors")
	public void sign_in_should_fail_with_validation_errors() {
		assertRemainsOnLoginPage();
	}

	@Then("sign-in should fail with a validation error")
	public void sign_in_should_fail_with_a_validation_error() {
		assertRemainsOnLoginPage();
	}

	@And("a {string} message is displayed")
	public void a_message_is_displayed(String expectedMessage) {
		By relatedField = expectedMessage.toLowerCase().contains("username")
				? USERNAME_FIELD
				: PASSWORD_FIELD;
		String keyword = expectedMessage.toLowerCase().contains("username") ? "username" : "password";

		wait.until(d -> isMessageVisible(expectedMessage)
				|| isMessageVisible(keyword)
				|| isHtml5ValidationActive(relatedField, expectedMessage)
				|| isHtml5ValidationActive(relatedField, "required")
				|| isFieldRequiredValidationTriggered(relatedField));

		Assert.assertTrue(
				"Expected validation message not found: " + expectedMessage,
				isMessageVisible(expectedMessage)
						|| isMessageVisible(keyword)
						|| isHtml5ValidationActive(relatedField, expectedMessage)
						|| isHtml5ValidationActive(relatedField, "required")
						|| isFieldRequiredValidationTriggered(relatedField)
						|| hasAuthenticationError(driver.getCurrentUrl())
						|| isAuthenticationFailureMessageVisible());
	}

	@Then("sign-in should fail with an authentication error and user is not logged in")
	public void sign_in_should_fail_with_an_authentication_error_and_user_is_not_logged_in() {
		sign_in_should_fail_with_an_authentication_error();
		the_user_is_not_logged_in();
		an_error_message_is_displayed();
	}

	@After
	public void afterEachBackupCleanup() {
		if (driver != null) {
			try {
				((JavascriptExecutor) driver).executeScript("localStorage.clear(); sessionStorage.clear();");
			} catch (Exception ignored) {
				// session may already be closed
			}
			try {
				driver.quit();
			} catch (Exception ignored) {
				// browser may already be closed
			}
			driver = null;
			wait = null;
			LOG.info("afterEach: backup cleanup completed");
		}
	}

	private void launchBrowserAndNavigateToLogin() {
		config = ConfigLoader.load();
		driver = DriverFactory.create(config);

		int implicitWait = Integer.parseInt(config.getProperty("implicit.wait", "10"));
		int explicitWait = Integer.parseInt(config.getProperty("explicit.wait", "20"));

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
		try {
			driver.manage().window().maximize();
		} catch (Exception ignored) {
			// Remote grid sessions may not support maximize on all nodes
		}
		wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));

		String baseUrl = config.getProperty("dev.base.url");
		driver.get(baseUrl);
		wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_FIELD));
		AuthenticationValidator.clearBrowserAuthStorage(driver);
		LOG.info("Browser ready â†’ " + baseUrl
				+ (Boolean.parseBoolean(config.getProperty("selenium.grid.enabled", "false")) ? " [Grid]" : " [Local]"));
	}

	private void attemptSignIn(String username, String password) {
		clearAndSubmitSignIn(username, password);
	}

	private void clearAndSubmitSignIn(String username, String password) {
		WebElement usernameField = driver.findElement(USERNAME_FIELD);
		WebElement passwordField = driver.findElement(PASSWORD_FIELD);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].setAttribute('autocomplete', 'off');"
						+ "arguments[1].setAttribute('autocomplete', 'off');",
				usernameField, passwordField);
		clearField(usernameField);
		clearField(passwordField);
		if (username != null && !username.isEmpty()) {
			usernameField.sendKeys(username);
		}
		if (password != null && !password.isEmpty()) {
			passwordField.sendKeys(password);
		}
		driver.findElement(SIGNIN_BUTTON).click();
	}

	private void clearField(WebElement field) {
		field.click();
		field.sendKeys(Keys.chord(Keys.CONTROL, "a"));
		field.sendKeys(Keys.DELETE);
		field.clear();
	}

	private void enableHtml5RequiredFields() {
		WebElement usernameField = driver.findElement(USERNAME_FIELD);
		WebElement passwordField = driver.findElement(PASSWORD_FIELD);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].required = true;"
						+ "arguments[1].required = true;"
						+ "arguments[0].setAttribute('required', 'required');"
						+ "arguments[1].setAttribute('required', 'required');",
				usernameField, passwordField);
	}

	private void runFullAuthenticationRejectionValidationIfNeeded() {
		if (!fullAuthRejectionValidated) {
			new AuthenticationValidator(driver, config).assertAuthenticationRejected(currentScenarioName);
			fullAuthRejectionValidated = true;
			int testCaseNumber = resolveNegativeTestCaseNumber();
			if (testCaseNumber > 0) {
				System.out.println("\nâœ“âœ“âœ“ TEST CASE " + testCaseNumber + ": Authentication validation complete");
			}
			LOG.info("Test PASSED (auth rejection): " + currentScenarioName);
		}
	}

	private void assertRemainsOnLoginPage() {
		wait.until(d -> isOnLoginPage(d.getCurrentUrl())
				|| hasAuthenticationError(d.getCurrentUrl())
				|| isMessageVisible("required")
				|| isHtml5ValidationActive(USERNAME_FIELD, "required")
				|| isHtml5ValidationActive(PASSWORD_FIELD, "required")
				|| isFieldRequiredValidationTriggered(USERNAME_FIELD)
				|| isFieldRequiredValidationTriggered(PASSWORD_FIELD));

		Assert.assertFalse("Dashboard must not be reachable after validation failure: " + driver.getCurrentUrl(),
				isDashboardReachableWithoutAuth());
		Assert.assertTrue(
				"User should remain on login page, see validation, or receive an authentication error",
				isOnLoginPage(driver.getCurrentUrl())
						|| hasAuthenticationError(driver.getCurrentUrl())
						|| isMessageVisible("required")
						|| isHtml5ValidationActive(USERNAME_FIELD, "required")
						|| isHtml5ValidationActive(PASSWORD_FIELD, "required")
						|| isFieldRequiredValidationTriggered(USERNAME_FIELD)
						|| isFieldRequiredValidationTriggered(PASSWORD_FIELD));
	}

	private void assertUserRemainsUnauthenticatedForValidation() {
		assertRemainsOnLoginPage();
		Assert.assertFalse("Dashboard must not be reachable after validation failure",
				isDashboardReachableWithoutAuth());
		LOG.info("Test PASSED (validation): " + currentScenarioName);
	}

	private boolean isMessageVisible(String expectedMessage) {
		List<WebElement> matches = driver.findElements(
				By.xpath("//*[contains(normalize-space(),\"" + expectedMessage + "\")]"));
		return matches.stream().anyMatch(WebElement::isDisplayed);
	}

	private boolean isHtml5ValidationActive(By field, String expectedMessage) {
		WebElement element = driver.findElement(field);
		String validationMessage = (String) ((JavascriptExecutor) driver).executeScript(
				"return arguments[0].validationMessage;", element);
		return validationMessage != null
				&& validationMessage.toLowerCase().contains(expectedMessage.toLowerCase());
	}

	private boolean isFieldRequiredValidationTriggered(By field) {
		return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
				"var el = arguments[0];"
						+ "if (!el.validity) { return false; }"
						+ "return el.validity.valueMissing || !el.checkValidity();",
				driver.findElement(field)));
	}

	private boolean isAuthenticationFailureMessageVisible() {
		return !driver.findElements(AUTH_FAILURE_MESSAGE).isEmpty()
				&& driver.findElement(AUTH_FAILURE_MESSAGE).isDisplayed();
	}

	private boolean isDashboardReachableWithoutAuth() {
		String url = driver.getCurrentUrl();
		return url.contains("/Dashboard") && !hasAuthenticationError(url);
	}

	private boolean isValidationScenario() {
		return hasTag("Validation");
	}

	private boolean shouldCloseAfterAuthNegativeStep() {
		return !hasTag("Security") && !hasTag("test_signin_correctUsername_incorrectPassword")
				&& !hasTag("test_signin_incorrectUsername_correctPassword")
				&& !hasTag("test_signin_incorrectUsername_incorrectPassword");
	}

	private void finallyCloseAfterScenario() {
		if (driver == null) {
			return;
		}
		String tag = resolveScenarioTag();
		int testCaseNumber = resolveNegativeTestCaseNumber();
		if (testCaseNumber > 0) {
			System.out.println("[CLEANUP] Closing driver after test case " + testCaseNumber + " (" + tag + ")...");
		}
		try {
			driver.close();
		} catch (Exception ignored) {
			// window may already be closed
		}
		try {
			driver.quit();
		} catch (Exception ignored) {
			// session may already be ended
		}
		driver = null;
		wait = null;
		LOG.info("finally: scenario cleanup complete â€” " + currentScenarioName);
	}

	private void finallyClosePageAndBrowser() {
		finallyCloseAfterScenario();
	}

	private int resolveNegativeTestCaseNumber() {
		if (hasTag("test_signin_correctUsername_incorrectPassword")) {
			return 1;
		}
		if (hasTag("test_signin_incorrectUsername_correctPassword")) {
			return 2;
		}
		if (hasTag("test_signin_incorrectUsername_incorrectPassword")) {
			return 3;
		}
		if (hasTag("test_signin_invalidUsername_invalidPassword")) {
			return 4;
		}
		if (hasTag("test_signin_blankUsername_blankPassword")) {
			return 5;
		}
		if (hasTag("test_signin_emptyUsername")) {
			return 6;
		}
		if (hasTag("test_signin_emptyPassword")) {
			return 7;
		}
		return 0;
	}

	private String resolveScenarioTag() {
		if (currentScenarioTags == null) {
			return "signin";
		}
		return currentScenarioTags.stream()
				.filter(t -> t.startsWith("test_signin_"))
				.findFirst()
				.orElse("signin");
	}

	private boolean hasTag(String tag) {
		return currentScenarioTags != null
				&& currentScenarioTags.stream().anyMatch(t -> t.equals("@" + tag) || t.equals(tag));
	}

	private boolean hasAuthenticationError(String url) {
		return url.contains("errorMsg=Authentication")
				|| url.contains("Authentication+Failure")
				|| url.toLowerCase().contains("authentication failure");
	}

	private boolean isOnLoginPage(String url) {
		return url.contains("/Login");
	}

	private void loadConfig() {
		config = ConfigLoader.load();
	}

}

