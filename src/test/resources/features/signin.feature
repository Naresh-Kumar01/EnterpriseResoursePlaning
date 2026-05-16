Feature: User Signin

  # Lifecycle per scenario:
  #   beforeEach → launch browser, navigate to login
  #   test (try) → sign in + validate
  #   finally    → close page + browser
  #   afterEach  → backup cleanup → next test starts fresh

  # Happy path: correct username + correct password
  Scenario: Successful Signin
    Given the user navigates to logixerp.com
    When the user signs in with valid credentials
    Then the user should be on the dashboard with successful signin validation

  # test_signin_correctUsername_incorrectPassword (Test Case 4)
  # Validates: no token, auth rejected, user not authenticated, protected route blocked.
  @test_signin_correctUsername_incorrectPassword @Negative
  Scenario: Unsuccessful signin with correct username and incorrect password
    Given the user navigates to logixerp.com
    When the user signs in with correct username and incorrect password
    Then sign-in should fail with an authentication error and user is not logged in

  # test_signin_incorrectUsername_correctPassword (Test Case 2)
  # Validates: no token/session, auth API failure, user null, protected route blocked.
  @test_signin_incorrectUsername_correctPassword @Negative
  Scenario: Unsuccessful signin with incorrect username and correct password
    Given the user navigates to logixerp.com
    When the user signs in with incorrect username and correct password
    Then sign-in should fail with an authentication error and user is not logged in

  # test_signin_incorrectUsername_incorrectPassword (Test Case 3)
  # Validates: no token, auth rejected, auth state invalid, generic error (no username leak).
  @test_signin_incorrectUsername_incorrectPassword @Negative
  Scenario: Unsuccessful signin with incorrect username and incorrect password
    Given the user navigates to logixerp.com
    When the user signs in with incorrect username and incorrect password
    Then sign-in should fail with an authentication error and user is not logged in
