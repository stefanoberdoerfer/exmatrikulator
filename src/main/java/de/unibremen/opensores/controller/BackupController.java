package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Backup;
import de.unibremen.opensores.service.BackupService;
import de.unibremen.opensores.util.ServerProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.persistence.PersistenceException;
import javax.persistence.QueryTimeoutException;
import javax.persistence.StoredProcedureQuery;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.List;

/**
 * Controller for creating backups.
 *
 * @author Lorenz Huether
 */
@ManagedBean
@RequestScoped
public class BackupController {
    /**
     * The Backupservice.
     */
    @EJB
    private BackupService backupService;

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            CourseSettingsController.class);

    /**
     * The Dateformat as specified in config.properties.
     */
    private DateFormat dateForm;

    /**
     * The key for the DateFormat.
     */
    private static String dtePropertyKey = "exmatrikulator.backup.dateForm";

    /**
     * Executed after construction.
     */
    @PostConstruct
    public void init() {
        String format = null;

        try {
            Properties props = ServerProperties.getProperties();
            format = props.getProperty(dtePropertyKey);
        } catch (final IOException e) {
            log.debug(e);
            format = "yyyy-MM-dd_HH:mm:ss";
        } finally {
            dateForm = new SimpleDateFormat(format);
        }
    }

    /**
     * Creates a backup to the path specified in config.properties.
     *
     * @param name the name of the Backup.
     */
    public void createBackup(String name) {
        Backup backup = null;

        try {
            backup = backupService.runBackup("name");
        } catch (PersistenceException | IOException e) {
            log.fatal(e);
        }

        backupService.persist(backup);
    }

    /**
     * Deletes a backup from disk and database.
     *
     * @param id of the backup to be deleted.
     */
    public void deleteBackup(String id) {
        Backup b = backupService.findById(Long.parseLong(id));
        String path = b.getPath();
        backupService.remove(b);

        try {
            File file = new File(path);
            backupService.deleteFolder(file);
        } catch (IOException | SecurityException e) {
            log.error(e);
        }
    }

    /**
     * Returns a Map of all backups.
     *
     * @return HashMap of all backups with name and id.
     */
    public Map<String, String> listBackups() {
        Map<String, String> map = new HashMap<>();
        List<Backup> list = backupService.listBackups();

        for (Backup b : list) {
            String name = b.getName() + "_" + dateForm.format(b.getDate());
            String id = String.valueOf(b.getBackupId());
            map.put(name, id);
        }

        return map;
    }

    /**
     * Returns a Map of all backups with the name...
     *
     * @return HashMap of backups with name and id.
     */
    public Map<String, String> listBackupsByName(String name) {
        Map<String, String> map = new HashMap<>();
        List<Backup> list = backupService.listBackupsByName(name);

        for (Backup b : list) {
            name = b.getName() + "_" + dateForm.format(b.getDate());
            String id = String.valueOf(b.getBackupId());
            map.put(name, id);
        }

        return map;
    }
}
