package org.nonprofit.config;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Eager, thread-safe Singleton. Each call creates a new JDBC connection. */
public final class DatabaseManager {
    private static final DatabaseManager INSTANCE = new DatabaseManager();
    private final String url;
    private final String username;
    private final String password;

    //private constructor for singleton
    private DatabaseManager() {
        url = System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/nonprofit_db");
        username = System.getenv().getOrDefault("DB_USER", "postgres");
        password = System.getenv("DB_PASSWORD");
    }

    public static DatabaseManager getInstance(){
        return INSTANCE;
    }

    //chekc connection
    public Connection getConnection() throws SQLException {
        if(password == null||password.isBlank()) throw new SQLException("DB_PASSWORD environment variable is missing or empty");
        return DriverManager.getConnection(url, username, password);
    }
}

