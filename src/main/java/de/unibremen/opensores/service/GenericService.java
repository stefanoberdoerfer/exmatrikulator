package de.unibremen.opensores.service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Abstract class for services.
 *
 * @author Kevin Scheck
 * @author Sören Tempel
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
     * Updates a collection of entities.
     * @param collection The collection of entities.
     * @return The updated collection, or null if the collection is null.
     */
    @SuppressWarnings(value = "") //Suppress warnings because of the obj assignment
    public Collection<T> update(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return new ArrayList<>(); //Returning an empty collection.
        }
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            T obj = iterator.next();
            obj = em.merge(obj);
        }
        return collection;
    }

    /**
     * Removes an entity from the database.
     * @param entity The entity to be removed, can't be null and
     *               must be an entity in the database.
     */
    public void remove(T entity) {
        //The to be deleted entity must be in the current transaction
        //http://stackoverflow.com/questions/17027398/java-lang-illegalargumentexception-removing-a-detached-instance-com-test-user5
        em.remove(em.contains(entity) ? entity : em.merge(entity));
    }

    /**
     * Finds an item by its primary key.
     *
     * @param entityClass Entity result class.
     * @param primaryKey PrimaryKey to use for lookup.
     *
     * @return Found entity or null if it doesn't exist.
     */
    public T find(Class<T> entityClass, Object primaryKey) {
        return em.find(entityClass, primaryKey);
    }

}
