package com.enterpriseresourceplanning.ui.pageObjects;

import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 * Base Page Object with reusable wait and interaction helpers.
 */
public abstract class BasePage {

	protected final WebDriver driver;
	protected final WebDriverWait wait;

	protected BasePage(WebDriver driver, int timeoutSeconds) {
		this.driver = driver;
		this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
	}

	protected void click(By locator) {
		wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
	}

	protected void type(By locator, String text) {
		WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
		element.clear();
		element.sendKeys(text);
	}

	protected boolean isVisible(By locator) {
		return wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
	}

}
