package stepsDefinitions;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import utilities.AuthenticationValidator;

public class Steps {

	private static final Logger LOG = Logger.getLogger(Steps.class.getName());

	private static final By USERNAME_FIELD = By.xpath("//input[@name='username']");
	private static final By PASSWORD_FIELD = By.xpath("//input[@name='password']");
	private static final By SIGNIN_BUTTON = By.xpath("//button[@type='submit']");
	private static final By HOME_ELEMENT = By.xpath("//*[normalize-space()='Home']");

	private WebDriver driver;
	private WebDriverWait wait;
	private Properties config;
	private String currentScenarioName;
	private Collection<String> currentScenarioTags;

	// -------------------------------------------------------------------------
	// 1. beforeEach() — launch browser, open page, navigate to login (fresh start)
	// -------------------------------------------------------------------------
	@Before
	public void beforeEach(Scenario scenario) {
		currentScenarioName = scenario.getName();
		currentScenarioTags = scenario.getSourceTagNames();
		loadConfig();
		launchBrowserAndNavigateToLogin();
		LOG.info("beforeEach: browser launched → page created → navigated to LogixERP login");
	}

	@Given("the user navigates to logixerp.com")
	public void the_user_navigates_to_logixerp_com() {
		wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_FIELD));
	}

	// -------------------------------------------------------------------------
	// 2. Test (When) — enter credentials and click Sign in
	// -------------------------------------------------------------------------
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

	// -------------------------------------------------------------------------
	// 2. Test (Then) + 3. finally — validate, then always close page/browser
	// -------------------------------------------------------------------------
	@Then("the user should be on the dashboard with successful signin validation")
	public void the_user_should_be_on_the_dashboard_with_successful_signin_validation() {
		try {
			String currentUrl = driver.getCurrentUrl();
			Assert.assertFalse("Sign in failed: " + currentUrl, hasAuthenticationError(currentUrl));

			WebElement homeLink = wait.until(ExpectedConditions.visibilityOfElementLocated(HOME_ELEMENT));

			Assert.assertFalse("User should leave the login page after sign in", isOnLoginPage(currentUrl));
			Assert.assertTrue("Home element should be displayed on Dashboard", homeLink.isDisplayed());

			LOG.info("Test PASSED: " + currentScenarioName);
		} finally {
			finallyClosePageAndBrowser();
		}
	}

	@Then("sign-in should fail with an authentication error and user is not logged in")
	public void sign_in_should_fail_with_an_authentication_error_and_user_is_not_logged_in() {
		int testCaseNumber = resolveNegativeTestCaseNumber();
		String testCaseTag = resolveNegativeTestCaseTag();

		try {
			// Authentication validation: failure message, no token, stay on login page
			new AuthenticationValidator(driver, config).assertAuthenticationRejected(currentScenarioName);
			System.out.println("\n✓✓✓ TEST CASE " + testCaseNumber + ": Authentication validation complete");
			LOG.info("Test PASSED: " + currentScenarioName + " [" + testCaseTag + "]");
		} finally {
			// ALWAYS close driver after negative test validation (Playwright: await page.close())
			finallyCloseDriverAfterNegativeTest(testCaseNumber, testCaseTag);
		}
	}

	// -------------------------------------------------------------------------
	// 4. afterEach() — backup cleanup if finally did not complete
	// -------------------------------------------------------------------------
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
		WebDriverManager.chromedriver().setup();

		ChromeOptions options = new ChromeOptions();
		if (Boolean.parseBoolean(config.getProperty("headless", "false"))) {
			options.addArguments("--headless=new");
		}

		driver = new ChromeDriver(options);
		int implicitWait = Integer.parseInt(config.getProperty("implicit.wait", "10"));
		int explicitWait = Integer.parseInt(config.getProperty("explicit.wait", "20"));

		driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
		driver.manage().window().maximize();
		wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));

		driver.get(config.getProperty("dev.base.url"));
		wait.until(ExpectedConditions.visibilityOfElementLocated(USERNAME_FIELD));
		AuthenticationValidator.clearBrowserAuthStorage(driver);
	}

	private void attemptSignIn(String username, String password) {
		driver.findElement(USERNAME_FIELD).clear();
		driver.findElement(USERNAME_FIELD).sendKeys(username);
		driver.findElement(PASSWORD_FIELD).clear();
		driver.findElement(PASSWORD_FIELD).sendKeys(password);
		driver.findElement(SIGNIN_BUTTON).click();
	}

	private void finallyClosePageAndBrowser() {
		if (driver == null) {
			return;
		}
		try {
			driver.close();
			LOG.info("finally: page closed");
		} catch (Exception ignored) {
			// window may already be closed
		}
		try {
			driver.quit();
			LOG.info("finally: browser closed");
		} catch (Exception ignored) {
			// session may already be ended
		}
		driver = null;
		wait = null;
		LOG.info("finally: test completed — " + currentScenarioName);
	}

	/**
	 * Closes page and browser after negative test authentication validation.
	 * Equivalent to Playwright {@code await page.close()} in each negative test.
	 */
	private void finallyCloseDriverAfterNegativeTest(int testCaseNumber, String testCaseTag) {
		if (driver == null) {
			return;
		}

		System.out.println("[CLEANUP] Closing driver after negative test case " + testCaseNumber + " (" + testCaseTag + ")...");

		try {
			driver.close();
			System.out.println("[CLEANUP] ✓ Page closed");
		} catch (Exception ignored) {
			// window may already be closed
		}
		try {
			driver.quit();
			System.out.println("[CLEANUP] ✓ Driver closed successfully for Test Case " + testCaseNumber + "\n");
		} catch (Exception ignored) {
			// session may already be ended
		}

		driver = null;
		wait = null;
		LOG.info("finally: negative test cleanup complete — " + testCaseTag);
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
		return 0;
	}

	private String resolveNegativeTestCaseTag() {
		if (hasTag("test_signin_correctUsername_incorrectPassword")) {
			return "test_signin_correctUsername_incorrectPassword";
		}
		if (hasTag("test_signin_incorrectUsername_correctPassword")) {
			return "test_signin_incorrectUsername_correctPassword";
		}
		if (hasTag("test_signin_incorrectUsername_incorrectPassword")) {
			return "test_signin_incorrectUsername_incorrectPassword";
		}
		return "negative_signin";
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
		if (config != null) {
			return;
		}
		config = new Properties();
		try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
			if (stream == null) {
				throw new IllegalStateException("config.properties not found on classpath");
			}
			config.load(stream);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load config.properties", e);
		}
	}

}
