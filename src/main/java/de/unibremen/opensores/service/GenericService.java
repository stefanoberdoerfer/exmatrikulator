package de.unibremen.opensores.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * Abstract class for services.
 *
 * @author Kevin Scheck
 * @author Sören Tempel
 */
public abstract class GenericService<T> {
    /**
     * Entity class for this service.
     */
    private Class<T> entityClass;

    @PersistenceContext
    protected EntityManager em;

    /**
     * Persists an entity into the database.
     * With em.persist a completely new entity, which is not in the database,
     * gets inserted.
     * @param entity The entity to be inserted, can't be null and must be an entity.
     * @return The persisted entity.
     */
    public T persist(T entity) {
        em.persist(entity);
        return entity;
    }

    /**
     * Updates an entity from the database.
     * With em.update an entity, which is already in the database, gets updated
     * in the database.
     * @param entity The entity to be updated, can't be null and
     *               must be an entity in the database.
     * @return The updated entity
     */
    public T update(T entity) {
        return em.merge(entity);
    }

    /**
     * Removes an entity from the database.
     * @param entity The entity to be removed, can't be null and
     *               must be an entity in the database.
     * @return The updated entity
     */
    public void remove(T entity) {
        em.remove(entity);
    }

    /**
     * Finds an item by its primary key.
     *
     * @param primaryKey PrimaryKey to use for lookup.
     * @return Found entity or null if it doesn't exist.
     */
    public T find(Object primaryKey) {
        return em.find(entityClass, primaryKey);
    }
}
