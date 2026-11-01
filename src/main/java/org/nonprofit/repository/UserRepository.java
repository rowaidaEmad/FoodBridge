package org.nonprofit.repository;


import java.sql.SQLException;
import java.util.*;
import org.nonprofit.config.DatabaseManager;
import org.nonprofit.model.User;
/** JDBC methods are intentionally TODO until the module's SQL mapping is implemented. */
public class UserRepository {
    private final DatabaseManager db;
    public UserRepository(DatabaseManager db) {this.db=db;}
    public long save(User value) throws SQLException {throw new UnsupportedOperationException("TODO: UserRepository.save JDBC");}
    public Optional<User> findById(long id) throws SQLException {throw new UnsupportedOperationException("TODO: UserRepository.findById JDBC");}
    public List<User> findAll() throws SQLException {throw new UnsupportedOperationException("TODO: UserRepository.findAll JDBC");}
}

