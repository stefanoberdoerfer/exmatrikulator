package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.MailTemplate;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.Tutorial;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.MailTemplateService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;

import de.unibremen.opensores.util.DateUtil;
import de.unibremen.opensores.util.ServerProperties;
import de.unibremen.opensores.util.csv.CourseDataParser;
import de.unibremen.opensores.util.csv.PaboParser;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.StreamedContent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;

/**
 * The controller for the overview page of the course settings.
 */
@ManagedBean(name = "courseSettings")
@ViewScoped
public class CourseSettingsController {

    /**
     * The path to the course overview site.
     */
    private static final String PATH_TO_COURSE_OVERVIEW =
            "/course/overview.xhtml?faces-redirect=true";

    /**
     * The http parameter key by which the course id gets passed.
     */
    private static final String HTTP_PARAM_COURSE_ID = "course-id";

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(CourseSettingsController.class);

    /**
     * The GradeService for retrieving grade names.
     */
    @EJB
    private GradeService gradeService;

    /**
     * The MailTemplateService for getting the default template.
     */
    @EJB
    private MailTemplateService mailTemplateService;

    /**
     * CourseService for database transactions related to courses.
     */
    @EJB
    private CourseService courseService;

    /**
     * UserService for database transactions related to users.
     */
    @EJB
    private UserService userService;

    /**
     * The logService for exmatrikulator logs.
     */
    @EJB
    private LogService logService;

    /**
     * The course for which the overview page gets accessed.
     */
    private Course course;

    /**
     * The user currently logged in.
     */
    private User user;

    private boolean isLecturer;

    private PrivilegedUser privUser;

    /**
     * Folder Path to temporary file location specified in server properties.
     */
    private Path tempFileFolder;

    private SimpleDateFormat dateFormatter;

    private ResourceBundle bundle;

    /**
     * Method called when the bean is initialised.
     * Gets the course id from the http params and gets the corresponding course.
     * Redirects to the course overview page if the course is not found (incase
     * an invalid passed course id).
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());
        HttpServletRequest httpReq
                = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();
        log.debug("Request URI: " + httpReq.getRequestURI());
        course = courseService.findCourseById(httpReq.getParameter(
                Constants.HTTP_PARAM_COURSE_ID));

        user = (User) FacesContext.getCurrentInstance()
                .getExternalContext()
                .getSessionMap()
                .get(Constants.SESSION_MAP_KEY_USER);


        if (course != null && user != null) {
            isLecturer = userService.hasCourseRole(user, Role.LECTURER, course);
            log.debug("User is lecturer: " + isLecturer);

            if (!isLecturer) {
                log.debug("User is not a lecturer, getting the priv user");
                privUser = courseService.findPrivileged(course, user.getEmail());
                log.debug("Found priv user: " + privUser);
            }

        }


        Properties properties = null;

        try {
            properties = ServerProperties.getProperties();
        } catch (IOException ex) {
            log.error("config.properties could not be loaded");
            log.error(ex);
        }

        if (properties != null) {
            final String strRootPath = properties.getProperty("fileupload.root","");
            final String strTempPath = properties.getProperty("fileupload.tempfolder","");

            //check properties
            if (strRootPath.trim().isEmpty() || strTempPath.trim().isEmpty()) {
                log.info("One or more fileupload paths in config.properties"
                        + "are empty or not declared");
            }

            //build path
            try {
                tempFileFolder = Paths.get(strRootPath,strTempPath);
            } catch (InvalidPathException ex) {
                log.error(ex);
            }

            //mkdir
            if (!Files.exists(tempFileFolder)) {
                try {
                    Files.createDirectories(tempFileFolder);
                } catch (IOException ex) {
                    log.error(ex);
                }
            }
        }


        log.debug("Loaded course object: " + course);
        if (course == null || user == null || (!isLecturer && privUser == null)
                || tempFileFolder == null || !Files.exists(tempFileFolder)) {
            log.debug("trying to redirect to /course/overview");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                log.error(e);
                log.fatal("Could not redirect to " + PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }

        dateFormatter = new SimpleDateFormat("dd_MM_yyy_HH_mm_ss");
        dateFormatter.setTimeZone(TimeZone.getTimeZone(Constants.SYSTEM_TIMEZONE));

    }

    /**
     * Generates the file for a course data csv.
     */
    public void generateCourseDataCSV() {
        String fileName = String.format("course_export_%s_%s.csv",course.getCourseId(),
                dateFormatter.format(DateUtil.getDateTime()));
        String dirPath = tempFileFolder.toString();
        log.debug(String.format("Creating a pabo csv file with filename %s and dirPath %s",
                fileName, dirPath));

        File csvFile = null;

        try {
            csvFile = CourseDataParser.parseCourseToCSV(course,isLecturer,
                    privUser,dirPath,fileName);
        } catch (IOException e) {
            log.error(e);
            log.error("Error parsing the csv file.\n");
            addErrorMessage(bundle
                    .getString("courses.paboFile.errorWhileGeneratingDownload"));
            return;
        }

        executeCSVFileDownload(csvFile, fileName);
    }

    /**
     * Executes the download for a csv file.
     * @param csvFile The CSV File to be downloaded by the user.
     */
    private void executeCSVFileDownload(File csvFile, String fileName) {
        log.debug("Got CSV File: " + csvFile.toPath());
        log.debug("Trying to get the input stream");
        FileInputStream input;
        try {
            input = new FileInputStream(csvFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            log.error("Error while creating input stream of the file");
            addErrorMessage(bundle.getString("courses.paboFile.errorWhileGeneratingDownload"));
            return;
        }

        FacesContext fc = FacesContext.getCurrentInstance();
        ExternalContext ec = fc.getExternalContext();

        log.debug("Trying to set up the download request");
        ec.responseReset();
        ec.setResponseContentType("application/csv");
        ec.setResponseContentLength((int)csvFile.length());
        ec.setResponseHeader("Content-Disposition",
                "attachment; filename=\"" + fileName + "\"");

        log.debug("Trying to set up the output stream");
        OutputStream output;
        try {
            output = ec.getResponseOutputStream();
        } catch (IOException e) {
            log.error(e);
            log.error("Error creating output stream");
            addErrorMessage(bundle.getString("courses.paboFile.errorWhileGeneratingDownload"));
            if (input != null ) {
                try {
                    input.close();
                } catch (IOException e1) {
                    log.error(e1);
                }
            }
            return;
        }

        log.debug("Creation suceeded, trying to copy from the input stream and closing");
        try {
            IOUtils.copy(input,output);
            input.close();
            output.close();
        } catch (IOException e) {
            log.error(e);
            addErrorMessage(bundle.getString("courses.paboFile.errorWhileGeneratingDownload"));
            return;
        }


        log.debug("Response is complete");
        fc.responseComplete();
    }

    /**
     * Deletes a course.
     *
     * @return String redirect to Settings page"
     */
    public String deleteCourse() {
        log.debug("Deleting course " + course.getName());
        logService.persist(Log.from(user, course.getCourseId(),
                "Has deleted the course"));
        course.setDeleted(true);
        courseService.update(course);
        return PATH_TO_COURSE_OVERVIEW;
    }

    /**
     * Finalizes the course and sends mail to all students.
     */
    public void finalizeAndSend() {
        course.setLastFinalization(new Date());
        courseService.update(course);
        logService.persist(Log.from(user, course.getCourseId(),
                "Has finalized the course"));
        sendCourse();
    }

    /**
     * Wether the user is allowed to print or not.
     *
     * @return boolean true if yes
     */
    public boolean isLecturer() {
        return isLecturer;
    }


    /**
     * Whether the user is allowed to print or not.
     *
     * @return boolean true if yes
     */
    public boolean mayPrint() {
        return checkPrivilege(Privilege.GenerateCredits);
    }

    /**
     * Whether the user is allowed to edit exams or not.
     * @return True if the user may edit exams, false otherwise.
     */
    public boolean mayEditExams() {
        return checkPrivilege(Privilege.EditExams);
    }

    /**
     * Whether the user is allowed to edit formulas or not.
     * @return True if the user may edit formulas, false otherwise.
     */
    public boolean mayEditFormulas() {
        return checkPrivilege(Privilege.EditFormulas);
    }

    /**
     * Whether the user is allowed to export / import pabo data or not
     * @return True if the user may edit formulas, false otherwise.
     */
    public boolean mayExportPabo() {
        return checkPrivilege(Privilege.ExportData);
    }

    /**
     * Checks the users the privilege.
     * @param priv The privilege to be checked.
     * @return True if the user is the most privileged (lecturer) or has a privilege
     *         as privileged user.
     */
    private boolean checkPrivilege(Privilege priv) {
        return isLecturer || (privUser != null && privUser.hasPrivilege(priv));
    }

    /**
     * Adds an error message to the facesContext
     * @param message The message to be displayed.
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_ERROR, bundle.getString("common.error"), message));
    }

    /**
     * Adds an info message to the facesContext
     * @param message The message to be displayed.
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance()
                .addMessage(null, new FacesMessage(FacesMessage
                        .SEVERITY_INFO, "", message));
    }

    /**
     * Sends the final Mail to the whole Course.
     */
    public void sendCourse() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("messages",
            facesContext.getViewRoot().getLocale());

        MailTemplate template = mailTemplateService.getDefaultTemplate(course);
        logService.persist(Log.from(user, course.getCourseId(),
                "Sends the final mail to the whole course."));

        if (template == null) {
            String msg = bundle.getString("mailtemplates.doesNotExist");
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        log.debug("Sending mails to course" + course.getName());
        try {
            template.issue(course.getStudents());
        } catch (IOException | MessagingException e) {
            String msg = bundle.getString("mailtemplates.fail");
            facesContext.addMessage(null, new FacesMessage(FacesMessage
                .SEVERITY_FATAL, bundle.getString("common.error"), msg));
            return;
        }

        String msg = bundle.getString("mailtemplates.success");
        facesContext.addMessage(null, new FacesMessage(FacesMessage
            .SEVERITY_INFO, bundle.getString("common.success"), msg));
    }

    /*
     * Getters and Setters
     */

    /**
     * Gets the course for which the setings overview page gets accessed.
     * @return The course object.
     */
    public Course getCourse() {
        return course;
    }
}
