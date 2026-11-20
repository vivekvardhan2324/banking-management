package com.bank;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthenticationService {
    private static final int SALT_BYTES = 16;
    private static final int ITERATIONS = 120_000;
    private static final int KEY_LENGTH = 256;
    private final SecureRandom secureRandom = new SecureRandom();

    public void register(String userId, char[] password) throws SQLException, GeneralSecurityException {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        String encodedHash = hash(password, salt);

        try (Connection connection = BankDatabase.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "INSERT INTO bank_users (user_id, password_salt, password_hash) VALUES (?, ?, ?)")) {
            statement.setString(1, userId);
            statement.setString(2, encodedSalt);
            statement.setString(3, encodedHash);
            statement.executeUpdate();
        }
    }

    public void login(String userId, char[] password)
            throws SQLException, GeneralSecurityException, InvalidUserException {
        try (Connection connection = BankDatabase.connect();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT password_salt, password_hash FROM bank_users WHERE user_id = ?")) {
            statement.setString(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new InvalidUserException();
                }
                byte[] salt = Base64.getDecoder().decode(result.getString("password_salt"));
                byte[] expected = Base64.getDecoder().decode(result.getString("password_hash"));
                byte[] actual = Base64.getDecoder().decode(hash(password, salt));
                if (!java.security.MessageDigest.isEqual(expected, actual)) {
                    throw new InvalidUserException();
                }
            }
        }
    }

    private String hash(char[] password, byte[] salt) throws GeneralSecurityException {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec)
                    .getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } finally {
            spec.clearPassword();
        }
    }
}
