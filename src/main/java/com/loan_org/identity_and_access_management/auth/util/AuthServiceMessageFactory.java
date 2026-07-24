package com.loan_org.identity_and_access_management.auth.util;

public class AuthServiceMessageFactory {

    private static final String EMAIL_ALREADY_EXISTS_TEMPLATE =
            "An account with email: %s already exists! Please try to login or contact the " +
                    "administrator for the same!";

    private static final String USERNAME_ALREADY_EXISTS_TEMPLATE =
            "An account with username: %s is already there in the database! Please try to " +
                    "login or contact the administrator for the same!";

    private static final String EMAIL_NOT_FOUND_TEMPLATE =
            "An account with email: %s is not found in database! Please register or contact the " +
                    "administrator for the same!";

    private static final String USERNAME_NOT_FOUND_TEMPLATE =
            "An account with username: %s is not found in the database! Please try to " +
                    "register or contact the administrator for the same!";

    private static final String ACCOUNT_NOT_FOUND_TEMPLATE =
            "No account found with the given username or email! Please make sure you enter " +
                    "the correct email/username. Contact the administrator if you think this is " +
                    "a mistake!";

    private static final String ACCOUNT_SUSPENDED_TEMPLATE =
            "The account is suspended! Please contact the administrator for the same or " +
                    "try again with different credentials!";

    private static final String ACCOUNT_LOCKED_TEMPLATE =
            "The account is currently locked and will be active after %d %s. Please " +
                    "try again after this time.";

    private static final String WRONG_PASSWORD_ATTEMPT =
            "The password for the account is incorrect. Please make sure to check the password " +
                    "before signing in. You have %d more attempt left before your account locks, " +
                    "so be careful before doing sign in. Please reset password if you have forgotten the same.";

    private AuthServiceMessageFactory() {
        // Never instantiate this class ever!
        throw new UnsupportedOperationException("ERROR: Utility class; do not try to instantiate!");
    }

    public static String emailAlreadyExists(String email) {
        return String.format(EMAIL_ALREADY_EXISTS_TEMPLATE, email);
    }

    public static String emailNotFound(String email) {
        return String.format(EMAIL_NOT_FOUND_TEMPLATE, email);
    }

    public static String usernameAlreadyExists(String username) {
        return String.format(USERNAME_ALREADY_EXISTS_TEMPLATE, username);
    }

    public static String usernameNotFound(String username) {
        return String.format(USERNAME_NOT_FOUND_TEMPLATE, username);
    }

    public static String accountNotFound() {
        return ACCOUNT_NOT_FOUND_TEMPLATE;
    }

    public static String accountSuspended() {
        return ACCOUNT_SUSPENDED_TEMPLATE;
    }

    public static String accountLocked(long timeLeft, String timeUnits) {
        return String.format(ACCOUNT_LOCKED_TEMPLATE, timeLeft, timeUnits);
    }

    public static String wrongPasswordAttempt(int timeLeft) {
        return String.format(WRONG_PASSWORD_ATTEMPT, timeLeft);
    }

}
