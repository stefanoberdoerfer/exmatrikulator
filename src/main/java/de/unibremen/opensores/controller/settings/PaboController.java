package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PaboData;
import de.unibremen.opensores.model.PaboErrorMsg;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Role;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.UserService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;
import de.unibremen.opensores.util.ServerProperties;
import de.unibremen.opensores.util.csv.PaboParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * ManagedBean for managing pabo imports and exports.
 * @autor Kevin Scheck
 */
@ManagedBean
@ViewScoped
public class PaboController {


    private static Logger log = LogManager.getLogger(PaboController.class);

    /**
     * Folder Path to temporary file location specified in server properties.
     */
    private Path tempFileFolder;

    /**
     * The logged in user.
     */
    private User user;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;

    /**
     * The filename of the upload of a user to be set to the course if the
     * user confirms.
     */
    private String uploadFileName;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * CourseService for database transactions related to courses.
     */
    private UserService userService;

    private ResourceBundle bundle;

    private SimpleDateFormat dateFormatter;

    /**
     * A list of students which have valid pabo data matched to.
     */
    private List<Student> matchedPaboStudents;

    /**
     * A list of students which have valid pabo data matched to but dont have
     * a pabo grade set.
     */
    private List<Student> matchedPaboStudentsWOPaboGrade;

    /**
     * A list of students for which no pabo import could be found.
     */
    private List<Student> notMatchedPaboStudents;

    /**
     * Lists used for PrimeFaces filtering in datatables.
     */
    private List<Student> filteredMatchedPaboStudents;
    private List<Student> filteredUnMatchedPaboStudents;
    private List<Student> filteredMatchedPaboStudentsWOPaboGrade;

    /**
     * A list of all valid pabo data which got not matched to a student during
     * an upload.
     */
    private List<PaboData> notMatchedValidPaboData;

    /**
     * The course from which pabo files should be imported or exported.
     */
    private Course course;

    /**
     * The pabo file which got uploaded by PrimeFaces.
     */
    private UploadedFile uploadedFile;

    /**
     * The uploaded CSV File which got saved locally.
     */
    private File localUploadedCSVFile;

    /**
     * A list of line indexes and messages about invalid pabo export rows.
     */
    private List<PaboErrorMsg> paboErrorMsgs;

    private boolean uploadAndParseSucceeded;



    /**
     * Init method which loads the severPropertie,s the logged in user, the course
     * and the studetnts with or without valid pabo files.
     */
    @PostConstruct
    public void init() {
        log.debug("init called");
        bundle = ResourceBundle.getBundle("messages",
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();

        course = courseService.findCourseById(req.getParameter(Constants.HTTP_PARAM_COURSE_ID));
        dateFormatter = new SimpleDateFormat("dd_MM_yyy_HH_mm_ss");
        dateFormatter.setTimeZone(TimeZone.getTimeZone(Constants.SYSTEM_TIMEZONE));
        boolean validationPassed = false;
        try {
            user = (User) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get(Constants.SESSION_MAP_KEY_USER);
        } catch (ClassCastException e) {
            log.error("User is not set in session map");
            log.error(e);
        }

        if (user != null && course != null) {
            if (!userService.hasCourseRole(user, Role.LECTURER, course)) {
                log.debug("User is not a lecturer");
                PrivilegedUser privilegedUser = courseService.findTutor(course, user.getEmail());
                log.debug("Got priv user: " + privilegedUser);
                validationPassed = (privilegedUser != null
                        && privilegedUser.hasPrivilege(Privilege.ExportData));
            } else {
                log.debug("User is a lecturer");
                validationPassed = true;
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

        if (tempFileFolder == null || !Files.exists(tempFileFolder)) {
            validationPassed = false;
        }

        if (!validationPassed) {
            log.debug("Validation hasnt passed");
            try {
                FacesContext.getCurrentInstance()
                        .getExternalContext().redirect(FacesContext
                        .getCurrentInstance().getExternalContext()
                        .getApplicationContextPath() + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            } catch (IOException e) {
                log.error(e);
                log.fatal("Could not redirect to " + Constants.PATH_TO_COURSE_OVERVIEW);
                return;
            }
        }
        initStudentLists();
    }

    /**
     * Method called when a pabo export file gets uploaded by the user.
     */
    public void uploadPaboExportFile(FileUploadEvent event)  {
        log.debug("uploadPaboExportFile() called " + event);
        if (event.getFile() != null && tempFileFolder != null) {
            log.debug("Upload success");
            uploadedFile = event.getFile();

            log.debug("Trying to store the file on the server");
            localUploadedCSVFile = storeFileOnServer();
        }
        if (localUploadedCSVFile == null || !localUploadedCSVFile.exists()) {
            addErrorMessage(bundle.getString("courses.create.uploadFail"));
            return;
        }

        log.debug("Storing locally suceeded");
        List<PaboData> rawParsedData = null;
        try {
            rawParsedData = PaboParser.parsePaboUpload(localUploadedCSVFile);
        } catch (IOException e) {
            log.error(e);
            addErrorMessage(bundle.getString("course.paboFile.messageErrorParsing"));
            return;
        }
        uploadFileName = event.getFile().getFileName();
        logFileUploaded(uploadFileName);
        List<PaboData> dataCopy = new ArrayList<>(rawParsedData);
        log.debug("Size of raw parsed data: " + rawParsedData);

        paboErrorMsgs = getPaboErrorsFromPaboData(dataCopy);
        matchedPaboStudents = matchPaboDataToStudents(rawParsedData, course.getStudents());
        notMatchedPaboStudents = course.getStudents().stream()
                .filter(s -> s.getPaboData() == null).collect(Collectors.toList());

        List<PaboData> matchedPaboData = matchedPaboStudents.stream()
                .map(s -> s.getPaboData()).collect(Collectors.toList());
        log.debug("Mathced pabo students size: " + matchedPaboStudents.size());
        log.debug("Matched pabo data");
        matchedPaboData.forEach(d -> log.debug((d.getMatriculation())));
        log.debug("Size of matched pabo data:  " + matchedPaboData.size());
        log.debug("Size of data copy: " + dataCopy.size());
        notMatchedValidPaboData = dataCopy.stream()
                .filter(d -> d.isValdid() && !matchedPaboData.contains(d))
                .collect(Collectors.toList());
        uploadAndParseSucceeded = true;
    }


    /**
     * Confirms that the students get updated with the uploaded file.
     * Sets a new pabo upload date for the course and updates all pabo data of
     * the students.
     * Shows a faces message that the pabo data has been integrated in the system.
     * @pre uploadPaboExportFile is uploaded with a valid file.
     */
    public void confirmPaboUpload() {
        course.setPaboUploadDate(DateUtil.getDateTime());
        course.setPaboUploadFileName(uploadFileName);
        course = courseService.update(course);
        logPaboDataConfirmed(matchedPaboStudents);
        addInfoMessage(bundle.getString("courses.paboFiles.confirmPaboUpload"));
        onReset();
    }


    /**
     * Method called when an import is cancelled. Resets the uploaded file and
     * list of students.
     */
    public void onReset() {
        uploadAndParseSucceeded = false;
        uploadedFile = null;
        localUploadedCSVFile = null;
        initStudentLists();
        notMatchedValidPaboData = null;

    }

    /**
     * Updates the exam date of the course.
     */
    public void updateExamDate() {
        log.debug("updateExamDate() has been called");
        course = courseService.update(course);
        logExamDateChanged(course.getPaboExamDate());
    }

    /**
     * Parses all valid students to a csv file and starts executing the csv download.
     */
    public void generatePaboDownload() {

        log.debug("generatePaboDownload() called");
        String fileName = String.format("pabo_export_%s%s.csv",course.getCourseId(),
                dateFormatter.format(DateUtil.getDateTime()));
        String dirPath = tempFileFolder.toString();
        log.debug(String.format("Creating a pabo csv file with filename %s and dirPath %s",
                fileName, dirPath));


        List<Student> studentsWithPaboAndGrade =
                course.getStudents().stream()
                        .filter(s -> s.getPaboData() != null && s.getPaboGrade() != null)
                        .collect(Collectors.toList());
        File csvFile = null;

        try {
            csvFile = PaboParser.parsePaboDownload(studentsWithPaboAndGrade,
                    course.getPaboExamDate(),fileName,dirPath);
        } catch (IOException e) {
            log.error(e);
            log.error("Error parsing the csv file.\n");
            addErrorMessage(bundle
                    .getString("courses.paboFile.errorWhileGeneratingDownload"));
            return;
        }

        logPaboDownload();
        executeCSVFileDownload(csvFile, course.getPaboUploadFileName());
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
     * Returns if the course has ever been finalized.
     * @return True if the course has been finalized, false otherwise.
     */
    public boolean isFinalized() {
        return course.getLastFinalization() != null;
    }

    /**
     * Returns whether there was ever a pabo file upload.
     * @return True if the course has ever upload a pabo file, false otherwise.
     */
    public boolean hasEverUploadedPabo() {
        return course.getPaboUploadDate() != null;
    }

    /**
     * Converts a filesize from simple long to human readable display-value.
     *
     * @param fsize long value representing a filesize in bytes
     * @return Human readable string representation
     */
    public String getHumanReadableFileSize(long fsize) {
        return FileUtils.byteCountToDisplaySize(fsize);
    }


    /**
     * Gets the displayed student lists from the service.
     */
    private void initStudentLists() {
        log.debug("initStudentLists called");
        matchedPaboStudents = courseService.findStudentsWithPaboData(course);
        notMatchedPaboStudents = courseService.findStudentWithNoPaboData(course);
        matchedPaboStudentsWOPaboGrade = courseService
                .findStudentWithPaboDataButNoPaboGrade(course);
    }

    /**
     * Creates a list of students which got matched valid pabo data.
     * @param paboDataList a list of pabo data.
     * @return The list of students which got valid pabo data.
     */
    private List<Student> matchPaboDataToStudents(List<PaboData> paboDataList,
                                                  List<Student> allStudents) {
        log.debug("matchPaboDataToStudents called");
        List<PaboData> validPaboList = paboDataList.stream()
                .filter(paboData -> paboData.isValdid()).collect(Collectors.toList());
        List<Student> matchedStudents = new ArrayList<>(allStudents.size());

        for (Student student: allStudents) {
            String studentMatr = student.getUser().getMatriculationNumber();
            log.debug("Trying to match student with matri" + studentMatr);
            if (studentMatr != null && !studentMatr.trim().isEmpty()) {
                Iterator<PaboData> paboDataIterator = validPaboList.iterator();
                while (paboDataIterator.hasNext()) {
                    PaboData data = paboDataIterator.next();
                    if (data.getMatriculation().equals(studentMatr)) {
                        log.debug(String.format("Matriculation numbers "
                                        + "matched; Student: %s Pabo: %s",
                                studentMatr, data.getMatriculation()));
                        student.setPaboData(data);
                        matchedStudents.add(student);
                        paboDataIterator.remove();
                        break;
                    }
                }
            }
        }
        log.debug(" matched pabo students size: " + matchedStudents);
        return matchedStudents;
    }


    /**
     * Stores the uploade file on the server.
     * @return The local file on the server.
     */
    private File storeFileOnServer() {
        log.debug("storeFilesOnServer called");
        File storedFile = null;
        String filename = FilenameUtils.getBaseName(uploadedFile.getFileName());
        String extension = FilenameUtils.getExtension(uploadedFile.getFileName());

        try {
            Path fileP = Files.createTempFile(tempFileFolder, filename + "-", "." + extension);
            Files.copy(uploadedFile.getInputstream(), fileP, StandardCopyOption.REPLACE_EXISTING);
            log.debug("Successfully saved File: " + filename + "." + extension);
            storedFile = (fileP.toFile());
        } catch (IOException ex) {
            log.error(ex);
            log.debug("Failed to save File: " + filename + "." + extension);
            addErrorMessage(bundle.getString("courses.create.storeError"));
        }

        return storedFile;
    }


    /**
     * Gets a map of the row indexes of invalid pabo data descriptions.
     * and error strings describing the fault of the data. The map stores the row of the data.
     * The first row of the file gets thrown away, so the index i of the faulty data
     * is the and for the user rows start with one, the row index is i+2.
     * @see PaboParser
     * @see PaboData
     * @return The map of the row indexes and invalid pabo data descriptions.
     */
    private List<PaboErrorMsg> getPaboErrorsFromPaboData(List<PaboData> dataList) {
        List<PaboErrorMsg> paboErrorMsgs = new ArrayList<>();
        int rowIdx = 2; // Starting with the first row
        for (PaboData data: dataList) {
            if (data.isValdid()) {
                rowIdx++;
                continue;
            }
            switch (data.getValidationId()) {
                case PaboData.VALID:
                    log.error("The PaboData validId is valid but the boolean says its not");
                    break;
                case PaboData.INVALID_MATRICULATION:
                    paboErrorMsgs.add(new PaboErrorMsg(rowIdx,
                            bundle.getString("course.paboFile.invalidMatriculation")));
                    break;
                case PaboData.INVALID_ATTEMPT:
                    paboErrorMsgs.add(new PaboErrorMsg(rowIdx,
                            bundle.getString("course.paboFile.invalidAttempts")));
                    break;
                case PaboData.INVALID_EXAM_NAME:
                    paboErrorMsgs.add(new PaboErrorMsg(rowIdx,
                            bundle.getString("course.paboFile.invalidExamName")));
                    break;
                case PaboData.INVALID_MAJOR:
                    paboErrorMsgs.add(new PaboErrorMsg(rowIdx,
                            bundle.getString("course.paboFile.invalidMajor")));
                    break;
                case PaboData.INVALID_ALL_EMPTY:
                    paboErrorMsgs.add(new PaboErrorMsg(rowIdx,
                            bundle.getString("course.paboFile.invalidRowSyntax")));
                    break;
                default:
                    break;
            }
            rowIdx++;
        }
        return paboErrorMsgs;
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


    /*
     * Logs
     */

    /**
     * Logs that the date of the exam has changed.
     * @param date The date to which the exam has changed.
     */
    private void logExamDateChanged(Date date) {
        logAction("Has changed the exam date to " + date.toString());
    }

    /**
     * Logs that the user has clicked a button to upload a file
     * @param fileName The filename of the uploaded file.
     */
    private void logFileUploaded(String fileName) {
        logAction("Has uploaded the file %s as PABO upload. Maybe the file wasnt confirmed");
    }

    /**
     * Logs that a user has download a pabo csv file.
     */
    private void logPaboDownload() {
        logAction("Has downloaded a PABO CSV File.");
    }

    /**
     * Logs that a student got confirmed pabo data.
     * @param confirmedStudents A list of students with valid pabo data.
     */
    private void logPaboDataConfirmed(List<Student> confirmedStudents) {
        for (Student student: confirmedStudents) {
            logAction(String.format("Student %s got valid Pabo data."
                            + "Major: %s, ExamName %s, Attempts: %d",
                    student.getUser(), student.getPaboData().getMajor(),
                    student.getPaboData().getExamName(), student.getPaboData().getAttempt()));
        }
    }

    /**
     * Logs that an action has occured with the logged in user and the selected
     * course.
     *
     * @param description The description of the action
     */
    private void logAction(String description) {
        logService.persist(Log.from(user, course.getCourseId(), description));
    }

    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @EJB
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public List<Student> getNotMatchedPaboStudents() {
        return notMatchedPaboStudents;
    }


    public List<PaboData> getNotMatchedValidPaboData() {
        return notMatchedValidPaboData;
    }

    public void setNotMatchedValidPaboData(List<PaboData> notMatchedValidPaboData) {
        this.notMatchedValidPaboData = notMatchedValidPaboData;
    }

    public Course getCourse() {
        return course;
    }

    public List<Student> getMatchedPaboStudentsWOPaboGrade() {
        return matchedPaboStudentsWOPaboGrade;
    }

    public List<Student> getMatchedPaboStudents() {
        return matchedPaboStudents;
    }

    public List<Student> getFilteredMatchedPaboStudents() {
        return filteredMatchedPaboStudents;
    }

    public void setFilteredMatchedPaboStudents(List<Student> filteredMatchedPaboStudents) {
        this.filteredMatchedPaboStudents = filteredMatchedPaboStudents;
    }

    public List<Student> getFilteredUnMatchedPaboStudents() {
        return filteredUnMatchedPaboStudents;
    }

    public void setFilteredUnMatchedPaboStudents(List<Student> filteredUnMatchedPaboStudents) {
        this.filteredUnMatchedPaboStudents = filteredUnMatchedPaboStudents;
    }

    public List<Student> getFilteredMatchedPaboStudentsWOPaboGrade() {
        return filteredMatchedPaboStudentsWOPaboGrade;
    }

    public void setFilteredMatchedPaboStudentsWOPaboGrade(
            List<Student> filteredMatchedPaboStudentsWOPaboGrade) {
        this.filteredMatchedPaboStudentsWOPaboGrade = filteredMatchedPaboStudentsWOPaboGrade;
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public File getLocalUploadedCSVFile() {
        return localUploadedCSVFile;
    }

    public boolean isUploadAndParseSucceeded() {
        return uploadAndParseSucceeded;
    }

    public List<PaboErrorMsg> getPaboErrorMsgs() {
        return paboErrorMsgs;
    }
}
