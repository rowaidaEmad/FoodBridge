package foodbridge.repositories;

import java.util.List;

/**
 * Generic CRUD used by every repository.
 */
public interface Repository<T> {
    void add(T item);

    T findById(int id);

    List<T> findAll();

    void update(T item);

    void delete(int id);
}
