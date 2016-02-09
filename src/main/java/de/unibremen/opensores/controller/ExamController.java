package de.unibremen.opensores.controller;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.ExamService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.ExamService;
import de.unibremen.opensores.service.GradingService;
import de.unibremen.opensores.service.LogService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The controller for managing exams.
 * @author Kevin Scheck
 */
@ViewScoped
@ManagedBean
public class ExamController {

    /**
     * The path to the course overview site.
     */
    private static final String PATH_TO_COURSE_OVERVIEW =
            "/course/overview.xhtml?faces-redirect=true";

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(ExamController.class);

    /**
     * The FacesContext of the bean, provides ResourceBundles, HTTPRequests etc.
     */
    private FacesContext context;

    /**
     * The used ResourceBundle used for string properties.
     */
    private ResourceBundle bundle;

    /**
     * CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * ExamService for database transactions related to exams.
     */
    private ExamService examService;

    /**
     * GradingService for database transactions related to gradings.
     * Is used to delete all gradings once the exam gets deleted.
     */
    private GradingService gradingService;

    /**
     * The LogService for creating Exmatrikulator business domain logs.
     */
    private LogService logService;


    /**
     * The course which exams get edited.
     */
    private Course course;

    /**
     * A selected exam for editing (e.g. creating a new one, deleting an exam).
     */
    private Exam selectedExam;

    /**
     * The id of the selected grade type.
     */
    private long oldSelectedGradeTypeId;

    /**
     * Map from the grade type ids to their corresponding labels.
     */
    private Map<Integer, String> gradeTypeLabels;

    /**
     * A string from the user for confirming the exam to be deleted.
     */
    private String examNameDeletionTextInput;

    /**
     * List for filtered exams for PrimeFaces filtering.
     */
    private List<Exam> filteredExams;

    /**
     * The selected file endings for an upload.
     */
    private String allowedFileEndings;

    /**
     * The logged in user.
     */
    private User loggedInUser;

    /**
     * Method called after initialisation.
     * Gets the corresponding course from the http param.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        context = FacesContext.getCurrentInstance();
        HttpServletRequest httpReq
                = (HttpServletRequest) context.getExternalContext().getRequest();
        log.debug("Request URI: " + httpReq.getRequestURI());
        final String courseIdString = httpReq.getParameter(Constants.HTTP_PARAM_COURSE_ID);

        log.debug("course-id: " + courseIdString);
        long courseId = -1;
        if (courseIdString != null) {
            try {
                courseId = Long.parseLong(courseIdString.trim());
            } catch (NumberFormatException e) {
                log.debug("NumberFormatException while parsing courseId");
            }
        }

        if (courseId != -1) {
            course = courseService.find(Course.class, courseId);
        }

        log.debug("Loaded course object: " + course);

        if (course == null) {
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

        log.debug("Course exam list size: " + course.getExams().size());

        gradeTypeLabels = new HashMap<>();
        bundle = ResourceBundle.getBundle("messages",
                context.getViewRoot().getLocale());

        loggedInUser = (User) context.getExternalContext()
                .getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        gradeTypeLabels.put(GradeType.Numeric.getId(), bundle.getString("gradeType.grade"));
        gradeTypeLabels.put(GradeType.Point.getId(), bundle.getString("gradeType.points"));
        gradeTypeLabels.put(GradeType.Boolean.getId(), bundle.getString("gradeType.boolean"));
        gradeTypeLabels.put(GradeType.Percent.getId(), bundle.getString("gradeType.percent"));

        selectedExam = new Exam();
    }


    /*
     * UI related callback methods
     */

    /**
     * Method called when the dialog for adding exams is called.
     */
    public void onAddExamDialogCalled() {
        log.debug("onAddExamDialogCalled() called");

        selectedExam = new Exam();
        allowedFileEndings = "";
    }

    /**
     * Method called when the edit dialog for an exam is called.
     * @param exam The to be edited exam (not null).
     * @throws IllegalArgumentException if the exam is null.
     */
    public void onEditExamDialogCalled(Exam exam) {
        log.debug("onEditExamDialogCalled called: " + exam);
        if (exam == null) {
            throw new IllegalArgumentException("Exam can't be null");
        }
        selectedExam = exam;
        logExamOpenToEdit(exam);
        allowedFileEndings = getAllowedFileEndingsString(selectedExam.getAllowedFileEndings());
        oldSelectedGradeTypeId = exam.getGradeType();
    }

    /**
     * Method called when the deleted dialog for exam is called.
     * @param exam The to be deleted exam (not null).
     * @throws IllegalArgumentException if the exam is null.
     */
    public void onDeleteExamDialogCalled(Exam exam) {
        log.debug("onDeleteExamDialogCalled called: " + exam);
        if (exam == null) {
            throw new IllegalArgumentException("Exam can't be null");
        }
        selectedExam = exam;
        examNameDeletionTextInput = "";
    }

    /**
     * Method for adding the newly created exam to the course.
     * @Pre The selected exam has all required properties set.
     *      All set properties got validated.
     * @Post The exam is added to the course and is persisted in the database.
     *       All selected ui related fields from the user get reset.
     */
    public void addExam() {
        log.debug("addExam() called");

        if (!selectedExam.getGradeType().equals(GradeType.Point.getId())) {
            selectedExam.setMaxPoints(null);
        }

        if (!selectedExam.isUploadAssignment()) {
            selectedExam.setDeadline(null);
            selectedExam.setMaxFileSizeMB(null);
        } else if (!allowedFileEndings.isEmpty()) {
            selectedExam.setAllowedFileEndings(getAllowedFileEndingList());
        }

        selectedExam.setCourse(course);
        course.getExams().add(selectedExam);
        examService.persist(selectedExam);
        log.debug("Number of exams before updating course: " + course.getExams().size());
        course = courseService.update(course);
        logExamCreated(selectedExam);
    }

    /**
     * Saves the edits to the selected exam.
     * @Pre The edited exam has all required properties set, which are all valid.
     * @Post The exam is updated in the database.
     *       All selected ui related fields from the user get reset.
     */
    public void editExam() {
        log.debug("editExam() called");
        logExamEdited(selectedExam);
        if (oldSelectedGradeTypeId != selectedExam.getGradeType()) {
            log.debug("Deleting all gradings from exam");
            logGradesFromExamDeleted(selectedExam);
            gradingService.deleteAllGradingsFromExam(selectedExam);
        }
        course = courseService.update(course); // Assuming CascadeType.MERGE
    }

    /**
     * Deletes the selected exam and all grades associated with the exam.
     * @Pre onDeleteExamDialogCalled was called priorly.
     * @Post The selected exam is deleted from the course and the database.
     *       All Gradings related to the exam get deleted from the database.
     *       Grade Formulas using this exam are unvalid now.
     */
    public void deleteExam() {
        log.debug("deleteExam() called");
        logGradesFromExamDeleted(selectedExam);
        gradingService.deleteAllGradingsFromExam(selectedExam);
        logExamDeleted(selectedExam);
        course.getExams().remove(selectedExam);
        course = courseService.update(course);
        examService.remove(selectedExam);
        //TODO Mark every grade script unvalid which contains the shortcut of exam
    }

    /*
     * Validations
     */

    /**
     * Validates if a shortcut input is valid for a exam.
     * The shortcut of the exam can only contain characters and numerical
     * values and must be at least one character long.
     * It must also be unique in the course, if an other exam has the exact
     * same shortcut, the new shortcut is not valid.
     *
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the input (the shortcut).
     */
    public void validateShortCut(FacesContext ctx,
                             UIComponent comp,
                             Object value) {
        log.debug("validateShortCut() called");

        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        if (!(value instanceof String) || ((String)value).trim().isEmpty()) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageGiveInput")));
            throw new ValidatorException(msgs);
        }

        final String stringValue = (String) value;
        log.debug("Input shortcut value: " + stringValue);
        for (Exam exam: course.getExams()) {
            if (exam.getShortcut().equals(stringValue)
                        && ((selectedExam.getExamId() == null)
                            || !exam.getExamId().equals(selectedExam.getExamId()))) {
                log.debug("Failing validation for shortcut(same in exam): " + stringValue);
                msgs.add(new FacesMessage(
                        bundle.getString("examination.messageCourseContains")));
                throw new ValidatorException(msgs);
            }
        }

        for (char c: stringValue.toCharArray()) {
            if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
                log.debug("Failing validation for shortcut(characters): " + stringValue);
                msgs.add(new FacesMessage(
                        bundle.getString("examination.messageUnvalidChar")));
                throw new ValidatorException(msgs);
            }
        }
    }


    /**
     * Validates if the user input matches the name of the to be deleted examination,
     * which is about to be deleted.
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the input (the user input).
     */
    public void validateExamDeletionName(FacesContext ctx,
                                 UIComponent comp,
                                 Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        if (!(value instanceof String) || ((String)value).trim().isEmpty()) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageDeleteGiveInput")));
            throw new ValidatorException(msgs);
        }

        String stringValue = (String) value;
        if (!stringValue.equals(selectedExam.getName())) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageDeleteNotSameName")));
            throw new ValidatorException(msgs);
        }
    }



    /**
     * Validates the user input for setting the max points of an exam with gradetype
     * points. Only positive decimal values seperated by a points are valid
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the input (the maximal Points as BigDecimal).
     */
    public void validateExamMaxPoints(FacesContext ctx,
                                         UIComponent comp,
                                         Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        if (!(value instanceof BigDecimal)) {
            log.debug("Value not instanceof BigDecimal");
            msgs.add(new FacesMessage(bundle.getString("examination.messageGiveMaxPoints")));
            throw new ValidatorException(msgs);
        }

        BigDecimal maxPoints = (BigDecimal) value;

        if (maxPoints.doubleValue() <= 0) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageNotValidPoints")));
            throw new ValidatorException(msgs);
        }
    }

    /**
     * Validates the new mime type which should be added to the allowed file endings
     * of the upload of the exam.
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the input (the user input).
     */
    public void validateFileEndings(FacesContext ctx, UIComponent comp, Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        if (!(value instanceof String) || ((String)value).trim().isEmpty()) {
            // No file endings allowed
            return;
        }

        String stringValue = (String) value;
        String[] fileEndings = stringValue.split(",");
        for (String fileEnding: fileEndings) {
            for (char c: fileEnding.toCharArray()) {
                if (!Character.isDigit(c) && !Character.isAlphabetic(c)
                        && c != '.' && c != '_') {
                    msgs.add(new FacesMessage(bundle
                            .getString("examination.notValidFileEndings")));
                    throw new ValidatorException(msgs);
                }
            }
        }
    }

    /**
     * Validates the new mime type which should be added to the allowed file endings
     * of the upload of the exam.
     * @param ctx The FacesContext for which the validation occurs.
     * @param comp The corresponding ui component.
     * @param value The value of the input (the user input).
     */
    public void validateDeadline(FacesContext ctx, UIComponent comp, Object value) {
        List<FacesMessage> msgs = new ArrayList<FacesMessage>();
        if (!(value instanceof Date)) {
            msgs.add(new FacesMessage(bundle.getString("examination.messageGiveDate")));
            throw new ValidatorException(msgs);
        }

        Date deadline = (Date) value;

        if (deadline.before(new Date())) {
            log.debug("Deadline before current date" + deadline);
            msgs.add(new FacesMessage(bundle.getString("examination.messageDatePassed")));
            throw new ValidatorException(msgs);
        }
    }


    /*
     * Public Methods besides UI and Validation
     */

    /**
     * Gets the label of a gradeType given the id of the gradeType.
     * @param gradeId The id of the gradeType value.
     * @return The lable of the gradeType.
     *         Or an empty string if the gradeType is not found.
     */
    public String gradeLabelFromId(int gradeId) {
        return (gradeTypeLabels.containsKey(gradeId))
                ? gradeTypeLabels.get(gradeId) : "";
    }

    /*
     * Private Methods
     */

    /**
     * Gets a list of allowed file endings given the input text with the comma
     * seperated file endings.
     * @Pre The allowed file endings string is validated.
     * @return A list of allowed file endings.
     */
    private List<String> getAllowedFileEndingList() {
        List<String> fileEndings = new ArrayList<>();
        for (String fileEnding: allowedFileEndings.split(",")) {
            fileEndings.add(fileEnding);
        }
        return fileEndings;
    }

    /**
     * Sets the selected file endings string given a list of file endings.
     * @param fileEndings The list of file endings.
     */
    private String getAllowedFileEndingsString(List<String> fileEndings) {
        log.debug("setAllowedFileEndingsString()");
        StringBuffer buffer = new StringBuffer();

        for (String fileEnding: fileEndings) {
            log.debug("File ending: " + fileEnding);
            buffer.append(fileEnding + ",");
        }

        String filesString = buffer.toString();
        if (!filesString.isEmpty()) {
            filesString = filesString.substring(0, filesString.length() - 1);
        }

        log.debug("allowedFileEndings: " + filesString);
        return filesString;
    }


    /*
     * Private Log methods
     */

    /**
     * Logs that the exam was created by the current user.
     * @param exam The created exam.
     */
    private void logExamCreated(Exam exam) {
        String description = "Has created the exam " + exam.getName();
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
    }

    /**
     * Logs the old version of the exam when the edit dialog has been opened.
     * @param exam The to be edited exam.
     */
    private void logExamOpenToEdit(Exam exam) {
        String description = "Has opened the exam for editing: " + exam.getName();
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
    }


    /**
     * Logs that the exam has been edited by the current user
     * @param exam The edited exam.
     */
    private void logExamEdited(Exam exam) {
        String description = "Has edited the exam " + exam.getName();
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
    }

    /**
     * Logs that all gradings from the exam get deleted.
     * @param exam The exam from which the gradings get deleted.
     */
    private void logGradesFromExamDeleted(Exam exam) {
        String description = "All gradings from exam " + exam.getName() + " get deleted.";
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
    }


    /**
     * Logs tha the exam gets deleted.
     * @param exam The to be deleted exam.
     */
    private void logExamDeleted(Exam exam) {
        String description = "The exam " + exam.getName() + " gets deleted.";
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));
    }


    /*
     * Getters and Setters
     */


    /**
     * Injects the logService in to ExamController.
     * @param logService The logService to be injected.
     */
    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Injects the course service to the ExamController.
     * @param courseService The course service to be injected to the bean.
     */
    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * Injects the exam service to the ExamController.
     * @param examService The service to be injected to the bean.
     */
    @EJB
    public void setExamService(ExamService examService) {
        this.examService = examService;
    }

    /**
     * Injects the grading service to the ExamController.
     * @param gradingService The service to be injected to the bean.
     */
    @EJB
    public void setGradingService(GradingService gradingService) {
        this.gradingService = gradingService;
    }

    public Map<Integer, String> getGradeTypeLabels() {
        return gradeTypeLabels;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Exam getSelectedExam() {
        return selectedExam;
    }

    public void setSelectedExam(Exam selectedExam) {
        this.selectedExam = selectedExam;
    }

    public String getExamNameDeletionTextInput() {
        return examNameDeletionTextInput;
    }

    public void setExamNameDeletionTextInput(String examNameDeletionTextInput) {
        this.examNameDeletionTextInput = examNameDeletionTextInput;
    }

    public List<Exam> getFilteredExams() {
        return filteredExams;
    }

    public void setFilteredExams(List<Exam> filteredExams) {
        this.filteredExams = filteredExams;
    }

    public String getAllowedFileEndings() {
        return allowedFileEndings;
    }

    public void setAllowedFileEndings(String allowedFileEndings) {
        this.allowedFileEndings = allowedFileEndings;
    }
}
