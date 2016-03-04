package de.unibremen.opensores.controller.admin;

import de.unibremen.opensores.controller.settings.CourseSettingsController;
import de.unibremen.opensores.model.Backup;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.BackupService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.ServerProperties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import javax.ejb.EJB;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.PersistenceException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.List;
import java.lang.ProcessBuilder;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

/**
 * Controller for creating backups.
 *
 * @author Lorenz Huether
 */
@ManagedBean
@ViewScoped
public class BackupController {
    /**
     * The Backupservice.
     */
    @EJB
    private BackupService backupService;

    private LogService logService;

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(
            BackupController.class);

    /**
     * The Dateformat as specified in config.properties.
     */
    private DateFormat dateForm;

    /**
     * The key for the DateFormat.
     */
    private static String dtePropertyKey = "exmatrikulator.backup.dateForm";

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * The backup to restore.
     */
    private Backup toRestore;

    /**
     * The Constant for getting the backup.
     */
    private String backupIdConst = "backup-id";

    /**
     * The Constant for retrieving the cwd.
     */
    private String cwdPropertyKey = "install.path";

    /**
     * The current working directory.
     */
    private String currentWorkingDirectory;

    /**
     * Executed after construction.
     */
    @PostConstruct
    public void init() {
        String format = null;

        try {
            Properties props = ServerProperties.getProperties();
            format = props.getProperty(dtePropertyKey);
            currentWorkingDirectory = props.getProperty(cwdPropertyKey);
        } catch (final IOException e) {
            log.debug(e);
            format = "yyyy-MM-dd_HH:mm:ss";
        } finally {
            dateForm = new SimpleDateFormat(format);
        }


        FacesContext context = FacesContext.getCurrentInstance();
        loggedInUser = (User) context.getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        HttpServletRequest request = (HttpServletRequest) context
                .getExternalContext()
                .getRequest();

        String backupId = request.getParameter(backupIdConst);

        if (backupId != null) {
            toRestore = backupService.findById(Long.parseLong(backupId));
        }
    }

    /**
     * Creates a backup to the path specified in config.properties.
     */
    public void createBackup() {
        Backup backup = null;
        log.debug("createBackup called");
        try {
            backup = backupService.runBackup("ManualBackup");
        } catch (PersistenceException | IOException e) {
            log.fatal(e);
            return;
        }

        backupService.persist(backup);
        logBackupCreated(backup);
    }

    /**
     * Deletes a backup from disk and database.
     *
     * @param backup to be deleted.
     */
    public void deleteBackup(Backup backup) {
        if (backup == null) {
            log.debug("Backup is null, nothing to delete");
            return;
        }

        String path = backup.getPath();
        backupService.remove(backup);
        logBackupDeleted(backup);
        try {
            File file = new File(path);
            backupService.deleteFolder(file);
            log.debug("Deleted Backup " + backup.getName());
        } catch (IOException | SecurityException e) {
            log.error(e);
        }
    }

    /**
     * Returns a Map of all backups.
     *
     * @return HashMap of all backups with name and id.
     */
    public List<Backup> listBackups() {
        return backupService.listBackups();
    }

    /**
     * Converts the backups date to a string.
     *
     * @param backup Backup of which the date is needed.
     *
     * @return the backups date as a string.
     */
    public String dateToString(Backup backup) {
        if (backup == null) {
            return "";
        }

        return dateForm.format(backup.getDate());
    }

    /**
     * Restores a backup.
     *
     * @param backup Backup to restore.
     */
    public void restoreBackup(Backup backup) {
        if (backup == null) {
            log.error("Cannot restore Backup, backup is null!");
            return;
        } else if (backup.getName() == null) {
            log.error("Cannot restore Backup, backupName is null!");
            return;
        }

        String os = System.getProperty("os.name");
        String backupName = backup.getName() + "_"
                + dateForm.format(backup.getDate());

        log.debug("Restoring from " + backupName);
        log.debug("Detected OS: " + os);

        try {
            ProcessBuilder pb = null;
            if (os == null) {
                log.error("Could not determine os.");
            } else if (os.startsWith("Windows")) {
                pb = new ProcessBuilder("restore.bat", backupName);
                pb.directory(new File(currentWorkingDirectory));
                pb.start();
            } else {
                pb = new ProcessBuilder("/bin/sh", "restore.sh", backupName);
                pb.directory(new File(currentWorkingDirectory));
                pb.start();
            }
        } catch (final IOException e) {
            log.error(e);
        }
    }

    /**
     * Restores a backup from a dialog.
     */
    public void restoreBackup() {
        if (toRestore != null) {
            log.debug("Restroing backup, shutting down!" );
            restoreBackup(toRestore);
        } else {
            log.error("The backup to restore is null!");
        }
    }

    public void setToRestore(Backup toRestore) {
        this.toRestore = toRestore;
    }

    public Backup getToRestore() {
        return toRestore;
    }

    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /*
     * Log methods
     */

    /**
     * Logs that a backup has been created by the logged in user.
     * @param backup The created backup.
     */
    private void logBackupCreated(Backup backup) {
        logAction(String.format("The backup %s has been created manually", backup.getName()));
    }

    /**
     * Logs that a backup has been deleted.
     * @param backup The deleted backup.
     */
    private void logBackupDeleted(Backup backup) {
        logAction(String.format("The backup %s has been deleted manually", backup.getName()));
    }

    /**
     * Logs an action with backups with the currently logged in user.
     * @param description The logged in user.
     */
    private void logAction(String description) {
        logService.persist(Log.withoutCourse(loggedInUser, description));
    }
}
