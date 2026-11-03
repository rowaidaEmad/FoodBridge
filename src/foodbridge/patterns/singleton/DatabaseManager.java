package foodbridge.patterns.singleton;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * SINGLETON. The only connection to the Database.
 * Settings come from environment variables: DB_URL, DB_USER, DB_PASSWORD.
 * Each getConnection() call opens a new JDBC connection; close it with try-with-resources.
 */
public class DatabaseManager {
    private static DatabaseManager uniqueInstance;
    private String url;
    private String username;
    private String password;

    private DatabaseManager() {
        url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/nonprofit_db");
        username = System.getenv().getOrDefault("DB_USER", "postgres");
        password = System.getenv("DB_PASSWORD");
    }

    public static synchronized DatabaseManager getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new DatabaseManager();
        }
        return uniqueInstance;
    }

    public Connection getConnection() throws SQLException {
        if (password == null || password.isBlank()) {
            throw new SQLException("DB_PASSWORD environment variable is missing or empty");
        }
        return DriverManager.getConnection(url, username, password);
    }
}
