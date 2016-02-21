package de.unibremen.opensores.service;

import de.unibremen.opensores.model.RecordBookEntry;

import javax.ejb.Stateless;

/**
 * Stateless service for database transactions related to Recordbookentries in the exmatrikulator.
 * @author Stefan Oberdörfer
 */
@Stateless
public class RecordBookService extends GenericService<RecordBookEntry> {
}
