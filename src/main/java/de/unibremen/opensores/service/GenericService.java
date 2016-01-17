package de.unibremen.opensores.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

/**
 * Created by kevin on 14.01.16.
 */
public abstract class GenericService<T> {

    @PersistenceContext
    protected EntityManager em;

    /**
     * Persists an entity into the database.
     * With em.persist a completely new entity, which is not in the database,
     * gets inserted.
     * @param entity The entity to be inserted, can't be null and must be an entity.
     * @return The persisted entity.
     * @throws ServiceException If the parameter entity
     *          is not an entity or is already in the database.
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
     * @throws ServiceException If the parameter entity
     *          is not an entity or is not in the database.
     */
    public T update(T entity) {
        return em.merge(entity);
    }

    /**
     * Removes an entity from the database.
     * @param entity The entity to be removed, can't be null and
     *               must be an entity in the database.
     * @return The updated entity
     * @throws ServiceException If the parameter entity
     *          is not an entity or is not in the database.
     */
    public void remove(T entity) {
        em.remove(entity);
    }
}