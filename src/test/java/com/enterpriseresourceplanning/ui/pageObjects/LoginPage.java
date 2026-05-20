package com.enterpriseresourceplanning.ui.pageObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Login page object for Logix ERP application.
 */
public class LoginPage extends BasePage {

	private static final By USERNAME = By.xpath("//input[@name='username']");
	private static final By PASSWORD = By.xpath("//input[@name='password']");
	private static final By SIGN_IN = By.xpath("//button[@type='submit']");
	private static final By HOME = By.xpath("//*[normalize-space()='Home']");
	private static final By AUTH_FAILURE = By.xpath("//*[contains(normalize-space(),'Authentication Failure')]");

	public LoginPage(WebDriver driver, int timeoutSeconds) {
		super(driver, timeoutSeconds);
	}

	public void enterUsername(String username) {
		type(USERNAME, username);
	}

	public void enterPassword(String password) {
		type(PASSWORD, password);
	}

	public void clickSignIn() {
		click(SIGN_IN);
	}

	public void signIn(String username, String password) {
		enterUsername(username);
		enterPassword(password);
		clickSignIn();
	}

	public boolean isLoginFormVisible() {
		return isVisible(USERNAME);
	}

	public boolean isHomeVisible() {
		try {
			return isVisible(HOME);
		} catch (Exception e) {
			return false;
		}
	}

	public boolean isAuthFailureVisible() {
		try {
			return isVisible(AUTH_FAILURE);
		} catch (Exception e) {
			return false;
		}
	}

	public By getUsernameLocator() {
		return USERNAME;
	}

}
