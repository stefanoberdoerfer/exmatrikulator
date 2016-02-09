package de.unibremen.opensores.service;

import de.unibremen.opensores.model.ParticipationType;

/**
 * The service class for the ParticipationType model class.
 */
public class ParticipationTypeService extends GenericService<ParticipationType> {

    /**
     * Finds the participation type by its unique id.
     * @param id The unique id of the participation type.
     * @return The found ParticipatioNType, or null if the participation type
     *      doesn't exist.
     */
    public ParticipationType findById(long id) {
        return em.find(ParticipationType.class, id);
    }
}
