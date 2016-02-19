package de.unibremen.opensores.service;

import de.unibremen.opensores.model.Backup;
import de.unibremen.opensores.util.ServerProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.PersistenceException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.StoredProcedureQuery;
import javax.ejb.Timer;

/**
 * Service class for creating backups.
 *
 * @author Lorenz Huether
 */
@Stateless
public class BackupService extends GenericService<Backup> {

    private static Logger log = LogManager.getLogger(BackupService.class);
    private final String dirPropertyKey = "exmatrikulator.backup.dir";
    private final String dtePropertyKey = "exmatrikulator.backup.dateForm";

    /**
     * Creates a Backup of the Database in the directory specified in
     * config.properties.
     *
     * @param name the name of the Backup.
     *
     * @return Backup entinty.
     *
     * @throws QueryTimeoutException if the query should fail.
     * @throws PersistenceException if persisting should fail.
     * @Throws IOException if config.properties is not readable.
     */
    public Backup runBackup(String name) throws QueryTimeoutException,
           PersistenceException, IOException {
        Properties props = ServerProperties.getProperties();
        Date date = new Date();
        String path = props.getProperty(dirPropertyKey)
                + name + "_" + getDateAsString(date);

        StoredProcedureQuery query = em.createStoredProcedureQuery(
                "SYSCS_UTIL.SYSCS_BACKUP_DATABASE");
        query.registerStoredProcedureParameter(1, String.class,
                ParameterMode.IN);
        query.setParameter(1, path);
        query.execute();

        Backup backup =  generateBackup(name, path, date, getDirectorySize(new File(path)));

        return backup;
    }

    /**
     * Returns a Date as a String in the in config.properties specified form.
     *
     * @param date a Date object.
     *
     * @return String representing the date.
     *
     * @throws IOException if reading config.properties fails.
     */
    private String getDateAsString(Date date) throws IOException {
        Properties props = ServerProperties.getProperties();
        DateFormat dateFormat = new SimpleDateFormat(props.getProperty(
                dtePropertyKey));
        return dateFormat.format(date);
    }

    /**
     * Generates a Backup entity.
     *
     * @param name String of the backup name.
     * @param path String path of the backup.
     * @param date Date of the backup.
     * @param size Long size of the backup.
     *
     * @return Backup entity.
     */
    private Backup generateBackup(String name, String path, Date date,
            Long size) {
        Backup backup = new Backup();
        backup.setName(name);
        backup.setPath(path);
        backup.setDate(date);
        backup.setFileSize(size);

        return backup;
    }

    /**
     * Calculates the Size of a backup.
     *
     * @param folder File object of the parent directory of the backup.
     *
     * @return long size of the specified backup.
     *
     * @throws IOException if something is not readable.
     */
    private long getDirectorySize(File folder) throws IOException {
        long size = 0;

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files == null) {
                throw new IOException();
            }
            for (File file : files) {
                if (file.isFile()) {
                    size += file.length();
                } else {
                    size += getDirectorySize(file);
                }
            }
        } else {
            size += folder.length();
        }
        return size;
    }

    /**
     * Used for scheduled backups, configured in config.properties.
     *
     * @throws IOException if Propertiesfile cannot be opened.
     */
    public void scheduledBackup(Timer timer) {
        try {
            Backup backup = runBackup("ScheduledBackup");
            em.persist(backup);
        } catch (PersistenceException | IOException e) {
            log.error(e);
        }

        log.debug("Scheduled backup executed successfully.");
    }

    /**
     * Retrieves a single Backup by id.
     *
     * @param id long id of the Backup.
     *
     * @return Backup with the id or null.
     */
    public Backup findById(long id) {
        Backup backup = em.find(Backup.class, id);
        return backup;
    }

    /**
     * Retrieves a list of all backups.
     *
     * @return List Backup.
     */
    public List<Backup> listBackups() {
        List<Backup> backupList = em.createQuery(
                "SELECT * "
                + "FROM Backup", Backup.class)
                .getResultList();
        return backupList;
    }

    /**
     * Retrieves a list of Backups with date.
     *
     * @param date of the backup.
     *
     * @return List Backup.
     */
    public List<Backup> listBackupsByDate(Date date) {
        List<Backup> backupList = em.createQuery(
                "SELECT DISTINCT b "
                + "FROM Backup b "
                + "WHERE b.date = :date", Backup.class)
                .setParameter("date", date)
                .getResultList();
        return backupList;
    }

    /**
     * Retrieves a list of Backups with name.
     *
     * @param name of the backup.
     *
     * @return List Backup.
     */
    public List<Backup> listBackupsByName(String name) {
        List<Backup> backupList = em.createQuery(
                "SELECT DISTINCT b "
                + "FROM Backup b "
                + "WHERE b.name = :name", Backup.class)
                .setParameter("name", name)
                .getResultList();
        return backupList;
    }

    /**
     * Removes a backup from disk.
     *
     * @param toDelete File object to be deleted recursively.
     *
     * @throws IOException if IO goes wrong.
     */
    public void deleteFolder(File toDelete) throws IOException,
            SecurityException {
        boolean success;
        if (toDelete.isDirectory()) {
            File[] files = toDelete.listFiles();
            if (files == null) {
                throw new IOException();
            }
            for (File file : files) {
                deleteFolder(file);
            }
            success = toDelete.delete();
            if (!success) {
                throw new IOException();
            }
        } else {
            success = toDelete.delete();
            if (!success) {
                throw new IOException();
            }
        }
    }
}
