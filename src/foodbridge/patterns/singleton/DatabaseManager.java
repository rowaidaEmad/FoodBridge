package foodbridge.patterns.singleton;

import java.sql.Connection;

/**
 * SINGLETON. The only connection to the Database.
 */
public class DatabaseManager {
    private static DatabaseManager uniqueInstance;
    private String url;
    private String username;
    private String password;

    private DatabaseManager() {
        // TODO: implement
    }

    public static DatabaseManager getInstance() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Connection getConnection() {
        // TODO: implement
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
