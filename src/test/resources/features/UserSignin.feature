@SignIn @UserAuthentication
Feature: User Sign In
  As a user
  I want to sign in to the application
  So that I can access the dashboard

  # Browser launch and navigation to the login page are handled by test hooks (@Before).
  # Each scenario starts from a clean session (cleared storage, fresh driver).

  Background:
    Given the user navigates to logixerp.com

  @test_signin_validCredentials @Positive @Smoke
  Scenario: Successful Sign In with valid credentials
    When the user signs in with valid credentials
    Then the user should be on the dashboard
    And the successful sign-in validation message is displayed

  @test_signin_correctUsername_incorrectPassword @Negative
  Scenario: Unsuccessful Sign In with correct username and incorrect password
    When the user signs in with correct username and incorrect password
    Then sign-in should fail with an authentication error
    And the user is not logged in
    And an error message is displayed

  @test_signin_incorrectUsername_correctPassword @Negative
  Scenario: Unsuccessful Sign In with incorrect username and correct password
    When the user signs in with incorrect username and correct password
    Then sign-in should fail with an authentication error
    And the user is not logged in
    And an error message is displayed

  @test_signin_incorrectUsername_incorrectPassword @Negative
  Scenario: Unsuccessful Sign In with incorrect username and incorrect password
    When the user signs in with incorrect username and incorrect password
    Then sign-in should fail with an authentication error
    And the user is not logged in
    And an error message is displayed

  @test_signin_invalidUsername_invalidPassword @Negative @Security
  Scenario: Unsuccessful Sign In with invalid username and invalid password
    When the user signs in with invalid username and invalid password
    Then sign-in should fail with an authentication error
    And the user is not logged in
    And an error message is displayed
    And the invalid login attempt is logged for security audit

  @test_signin_blankUsername_blankPassword @Negative @Validation
  Scenario: Unsuccessful Sign In with blank username and blank password
    When the user signs in without entering username and password
    Then sign-in should fail with validation errors
    And a "Username is required" message is displayed
    And a "Password is required" message is displayed
    And the user is not logged in

  @test_signin_emptyUsername @Negative @Validation
  Scenario: Unsuccessful Sign In with empty username
    When the user signs in without entering a username
    Then sign-in should fail with a validation error
    And a "Username is required" message is displayed
    And the user is not logged in

  @test_signin_emptyPassword @Negative @Validation
  Scenario: Unsuccessful Sign In with empty password
    When the user signs in without entering a password
    Then sign-in should fail with a validation error
    And a "Password is required" message is displayed
    And the user is not logged in
