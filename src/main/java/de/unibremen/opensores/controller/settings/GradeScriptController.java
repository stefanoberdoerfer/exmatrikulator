package de.unibremen.opensores.controller.settings;

import de.unibremen.opensores.model.Course;
import de.unibremen.opensores.model.Exam;
import de.unibremen.opensores.model.Grade;
import de.unibremen.opensores.model.GradeFormula;
import de.unibremen.opensores.model.GradeType;
import de.unibremen.opensores.model.Grading;
import de.unibremen.opensores.model.Log;
import de.unibremen.opensores.model.PaboGrade;
import de.unibremen.opensores.model.ParticipationType;
import de.unibremen.opensores.model.Privilege;
import de.unibremen.opensores.model.PrivilegedUser;
import de.unibremen.opensores.model.Student;
import de.unibremen.opensores.model.User;
import de.unibremen.opensores.service.CourseService;
import de.unibremen.opensores.service.GradeFormulaService;
import de.unibremen.opensores.service.GradeService;
import de.unibremen.opensores.service.LogService;
import de.unibremen.opensores.service.ParticipationTypeService;
import de.unibremen.opensores.service.StudentService;
import de.unibremen.opensores.util.Constants;
import de.unibremen.opensores.util.DateUtil;
import org.apache.commons.lang3.CharEncoding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.tabview.TabView;
import org.primefaces.event.TabChangeEvent;
import org.python.core.PyBoolean;
import org.python.core.PyDictionary;
import org.python.core.PyException;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.core.PySystemState;
import org.python.core.PyTuple;
import org.python.util.PythonInterpreter;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * The controller for grade scripts.
 * @author Kevin Scheck
 */
@ManagedBean(name = "scriptController")
@ViewScoped
public class GradeScriptController {

    /**
     * The log4j logger.
     */
    private static Logger log = LogManager.getLogger(GradeScriptController.class);

    /**
     * The block size of the ByteArrayOutPutStream used as the OutputStream of
     * the python interpreter.
     */
    private static final int DEFAULT_OUTPUT_BLOCK_SIZE = 8192;

    /**
     * The None type name to represent not existing grades.
     * (equals null in Java).
     */
    private static final String PY_NONE = "None";

    /**
     * The class name of the decimal object representing BigDecimals in Python.
     * See http://www.jython.org/docs/library/decimal.html
     */
    private static final String PY_DECIMAL_CLASS_NAME = "Decimal";

    /**
     * The number of numbers decimal places in the python environment.
     */
    private static final int PY_DECIMAL_PRECISION = 1000;

    /**
     * Path to the model directory, must be loaded in the namespace
     * of the python interpreter.
     */
    private static final String PATH_MODEL
            = "src/main/java/de/unibremen/opensores/model";


    /**
     * The imports which get executed after resetting the locals before a script
     * gets executed.
     */
    private static final String[] PY_IMPORTS = {
        "import de.unibremen.opensores.model.PaboGrade as PaboGrade",
        "from decimal import *",
        "import inspect",
        "import sys",
        "import codecs"
    };

    /**
     * Sets up the precision in the python environment.
     */
    private static final String PY_SET_DECIMAL_PRECISION =
            "getcontext().prec = " + PY_DECIMAL_PRECISION;


    /**
     * Sets the encoding of the stout to utf8.
     */
    private static final String PY_SET_UTF_8_TO_STDOUT_STDERR =
            "\nsys.stdout = codecs.getwriter('utf8')(sys.stdout)\n"
                    + "sys.stderr = codecs.getwriter('utf8')(sys.stderr)\n";


    /**
     * An array of protected local fields which should not be set to null when
     * the python interpreter gets reset.
     */
    private static final String[] PY_PROTECTED_LOCALS = {
        "__name__", "__builtins__", "__doc__", "__package__", "sys"
    };


    /**
     * The name of the method which is used to grade students.
     */
    private static final String PY_GRADE_METHOD_NAME = "set_final_grade";

    /**
     * Python method which checks if the method for setting final grades
     * "set_final_grade" has been supplied with exactly one argument.
     * Takes the parameter dir which is a list the names of the local namespace
     */
    private static final String PY_INIT_CHECK_SET_FINAL_GRADE_METHOD =
            String.format("%ndef check_%1$s_method(dir):%n"
                    + "    return \"%1$s\" in dir and "
                    + "len(inspect.getargspec(%1$s)[0]) == 1%n",
                    PY_GRADE_METHOD_NAME);

    /**
     * Calls the method check_set_final_grade_method too check if the method
     * which is called to set the grades is in the namespace and takes one
     * argument.
     * Returns a PyBoolean which is true if the method exists and takes one
     * parameter and false otherwise
     */
    private static final String PY_CALL_CHECK_SET_FINAL_GRADE_METHOD =
            String.format("check_%s_method(dir())", PY_GRADE_METHOD_NAME);

    /**
     * Python variable name of a list of dictionaries of student grades.
     */
    private static final String PY_VAR_NAME_GRADEDICT_LIST = "student_grades_list";


    private static final String PY_STR_FINAL_GRADE = "final_grade";

    /**
     * Name of a single python dictionary representing the grades of a student.
     */
    private static final String PY_VAR_NAME_GRADEDICT = "grades";

    /**
     * Executes set_final_grades for every student object.
     */
    private static final String PY_EXEC_SET_FINAL_GRADES
            = String.format("for %s in %s:%n\t%s['%s'] = %s(%s)",
            PY_VAR_NAME_GRADEDICT, PY_VAR_NAME_GRADEDICT_LIST,
            PY_VAR_NAME_GRADEDICT, PY_STR_FINAL_GRADE, PY_GRADE_METHOD_NAME,
            PY_VAR_NAME_GRADEDICT);


    /**
     * The CourseService for database transactions related to courses.
     */
    private CourseService courseService;

    /**
     * The ParticipationTypeService for database transactions.
     */
    private ParticipationTypeService participationTypeService;

    /**
     * The logService for exmatrikulator logs.
     */
    private LogService logService;

    /**
     * The studentService for database transactions related to students.
     */
    private StudentService studentService;

    /**
     * The GradeFormulaService fro database transaction related to gradeFornulas.
     */
    private GradeFormulaService gradeFormulaService;

    /**
     * GradeService for database transactions related to grades.
     */
    private GradeService gradeService;

    /**
     * The ResourceBundle for string properties.
     */
    private ResourceBundle bundle;

    /**
     * The course which grade scripts should be edited.
     */
    private Course course;

    /**
     * The logged in user which edits grades.
     * Must have rights in the course to edit grades, or the user gets redirecte
     * to the course overview page.
     */
    private User loggedInUser;

    /**
     * Boolean which indicates if the logged in user is a lecturer in the course.
     * Non lecturer users can only edit the grade formulas.
     */
    private boolean userIsLecturer;

    /**
     * The currently selected participation type.
     */
    private ParticipationType selectedParticipationType;

    /**
     * The index of the active tab.
     */
    private int activeTabIndex;

    /**
     * A list of filtered exams for PrimeFaces filtering.
     */
    private List<Exam> filteredExams;

    /**
     * Map from the grade type ids to their corresponding labels.
     */
    private Map<Integer, String> gradeTypeLabels;

    /**
     * The python pyInterpreter to run python code.
     */
    private PythonInterpreter pyInterpreter;

    /**
     * The set standard outputStream of the python interpreter.
     */
    private ByteArrayOutputStream pyOutPutStream;


    /**
     * The string representation of the script output.
     */
    private String scriptOutput = "";

    /**
     * The confirmation string input of the user.
     */
    private String deleteConfirmation;

    /**
     * A map from the participation types of the course to the currently edited
     * grade formula objects. All gradeFormula objects are new objects which are
     * copies of the last persisted GradeFormula of the participation type.
     */
    private Map<ParticipationType, GradeFormula> editedFormulas;

    /**
     * A map of the participation type and their undeleted, confirmed students.
     */
    private Map<ParticipationType, List<Student>> gradedStudents;

    /**
     * The selected formula to be deleted.
     */
    private GradeFormula deletedFormula;

    /**
     * Method called by JSF when the bean is initialised.
     * Gets the course given the course id from the HTTP Param.
     * Gets the facesContext and bundle.
     * @Pre The HTTP Request has a paramater course-id with the course id
     *      of the to be edited course.
     */
    @PostConstruct
    public void init() {
        log.debug("init() called");
        initPyInterpreter();
        bundle = ResourceBundle.getBundle(Constants.BUNDLE_NAME,
                FacesContext.getCurrentInstance().getViewRoot().getLocale());

        HttpServletRequest httpReq = (HttpServletRequest) FacesContext.getCurrentInstance()
                .getExternalContext().getRequest();

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

        Object userObj = FacesContext.getCurrentInstance()
                .getExternalContext().getSessionMap().get(Constants.SESSION_MAP_KEY_USER);

        if (course == null || !(userObj instanceof User)) {
            log.debug("trying to redirect to /course/overview");
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

        loggedInUser = (User) userObj;
        userIsLecturer = course.getLecturerFromUser(loggedInUser) != null;
        log.debug("Logged in user is lecturer: " + userIsLecturer);

        if (!userIsLecturer) {
            PrivilegedUser privilegedUser = course.getPrivilegedUserFromUser(loggedInUser);
            if (privilegedUser == null
                    || privilegedUser.hasPrivilege(Privilege.EditExams.name())) {
                log.debug("Logged in user is not a lecturer nor has a "
                        + "privileged user association with privileges to edit formulas");
                try {
                    FacesContext.getCurrentInstance()
                            .getExternalContext().redirect(FacesContext
                            .getCurrentInstance().getExternalContext()
                            .getApplicationContextPath()
                            + Constants.PATH_TO_COURSE_OVERVIEW);
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                    log.fatal("Could not redirect to " + Constants.PATH_TO_COURSE_OVERVIEW);
                    return;
                }
            }
        }

        gradeTypeLabels = new HashMap<>();
        gradeTypeLabels.put(GradeType.Numeric.getId(), bundle.getString("gradeType.grade"));
        gradeTypeLabels.put(GradeType.Point.getId(), bundle.getString("gradeType.points"));
        gradeTypeLabels.put(GradeType.Boolean.getId(), bundle.getString("gradeType.boolean"));
        gradeTypeLabels.put(GradeType.Percent.getId(), bundle.getString("gradeType.percent"));

        editedFormulas = new HashMap<>();
        for (ParticipationType type: course.getParticipationTypes()) {
            log.debug("Inserting formula of participation type " + type.getName());
            GradeFormula latestCopy = formulaCopyOf(type.getLatestFormula());
            latestCopy.setEditDescription("");
            editedFormulas.put(type, latestCopy);
            log.debug("Formula: " + editedFormulas.get(type));
        }

        selectedParticipationType = course.getDefaultParticipationType();
        activeTabIndex = course.getParticipationTypes().indexOf(selectedParticipationType);
        scriptOutput = "";
        gradedStudents = studentService
                .getUndeletedAndConfirmedStudentsOf(course.getParticipationTypes());
    }


    /**
     * Method called before the bean is destroyed.
     * Closes the Python interpreter.
     */
    @PreDestroy
    public void destroy() {
        if (pyInterpreter != null) {
            pyInterpreter.cleanup();
            pyInterpreter.close();
        }
        if (pyOutPutStream != null) {
            try {
                pyOutPutStream.close();
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    /*
     * Public methods
     */

    /**
     * Method called when the tab is changed.
     * @param event The PrimeFaces tabChange event.
     */
    public void onTabChange(TabChangeEvent event) {
        if (event == null || !(event.getComponent() instanceof TabView)) {
            throw new IllegalArgumentException("The event cant be null.");
        }
        TabView tv = (TabView) event.getComponent();
        this.activeTabIndex = tv.getActiveIndex();
    }

    /**
     * Method called when the formula deletion dialog gets deleted.
     * @param formula The formula to be deleted.
     */
    public void onDeleteDialogCalled(GradeFormula formula) {
        log.debug("onDeleteDialogCalled with: " + formula);
        if (formula == null) {
            throw new IllegalArgumentException("The formula can't be null");
        }
        deletedFormula = formula;
        deleteConfirmation = "";
    }

    /**
     * Tests if the script is a valid grade script
     * A valid grade script has valid python syntax, handles the grade dictionary
     * object without raising any errors and returns a PaboGrade object.
     * @param type The ParticipationType which gradeScript should be tested.
     */
    public void execScript(ParticipationType type) {
        log.debug("execScript called with participation type " + type);
        pyOutPutStream.reset();
        pyInterpreter.cleanup();
        resetPyInterpreterLocals();

        log.debug("Trying to execute the formula");
        String script = editedFormulas.get(type).getFormula();
        GradeFormula gradeFormula = getActiveGradeFormula();

        try {
            pyInterpreter.exec(script);
        } catch (PyException e) {
            log.error(e);
            scriptOutput = e.toString();
            gradeFormula.setValid(false);
            addFailMessage(bundle.getString("gradingFormula.messageNotInterpretable"));
            return;
        }

        log.debug("Trying to execute the formula");
        PyBoolean gradingMethodSet;
        try {
            gradingMethodSet =
                (PyBoolean) pyInterpreter.eval(PY_CALL_CHECK_SET_FINAL_GRADE_METHOD);
        } catch (PyException e) {
            log.error(e);
            scriptOutput = e.toString();
            gradingMethodSet = new PyBoolean(false);
        }

        if (!gradingMethodSet.getBooleanValue()) {
            log.debug("The method set_final_grade hasn't been set with one arg");
            gradeFormula.setValid(false);
            addFailMessage(bundle.getString("gradingFormula.messageMethodNotSet"));
            return;
        }

        log.debug("Trying to set the pabo Grades");
        final Map<Student, PyDictionary> studentPyGradeDictMap = createStudentPyGradeDictMap();
        pyInterpreter.set(PY_VAR_NAME_GRADEDICT_LIST, new PyList(studentPyGradeDictMap.values()));

        try {
            pyInterpreter.exec(PY_EXEC_SET_FINAL_GRADES);
        } catch (PyException e) {
            log.error(e);
            scriptOutput = e.toString();
            gradeFormula.setValid(false);
            addFailMessage(bundle.getString("gradingFormula.messageFailExecutingGradeMethod"));
            return;
        }

        boolean allValidPaboGradesSet = true;
        for (Map.Entry<Student,PyDictionary> entry: studentPyGradeDictMap.entrySet()) {
            Object paboGradeObj = entry.getValue().get(new PyString(PY_STR_FINAL_GRADE))
                                        .__tojava__(PaboGrade.class);
            if (!(paboGradeObj instanceof PaboGrade)) {
                entry.getKey().setPaboGrade(null);
                allValidPaboGradesSet = false;
            } else {
                entry.getKey().setPaboGrade(((PaboGrade) paboGradeObj).name());
                entry.getKey().setPaboGradeFormula(script);
            }
        }

        try {
            scriptOutput = pyOutPutStream.toString(CharEncoding.UTF_8);
        } catch (UnsupportedEncodingException e) {
            log.error(e);
            addFailMessage(bundle.getString("gradingFormula.messageErrorGettingScriptOutput"));
        }

        if (!allValidPaboGradesSet) {
            log.error("Not all PaboGrades set in script");
            gradeFormula.setValid(false);
            addFailMessage(bundle.getString("gradingFormula.messageNotAllPaboGradesReturned"));
            return;
        }

        gradeFormula.setValid(true);
        addInfoMessage(bundle.getString("gradingFormula.messageScriptIsValid"));
    }

    /**
     * Saves the currently edited formula to the active participation type.
     * Executes the script to check if it is valid and return PaboGrade for every
     * student.
     */
    public void saveEditedFormula() {
        log.debug("saveEditedFormula() called");
        GradeFormula editedFormula = editedFormulas.get(getActiveParticipationType());
        editedFormula.setEditor(loggedInUser);
        editedFormula.setSaveDate(DateUtil.getDateTime());
        ParticipationType activeType = getActiveParticipationType();
        editedFormula.setValid(isActiveScriptValid());
        activeType.addNewFormula(editedFormula);
        log.debug("Active tab index" + activeTabIndex);
        activeType = participationTypeService.update(activeType);
        course.getParticipationTypes().set(activeTabIndex, activeType);
        logGradeFormulaSaved(editedFormula);
        addInfoMessage(bundle.getString("gradingFormula.messageScriptSaved"));
    }

    /**
     * Deletes a versioned formula of a participation type.
     * @param formula The formula which should be deleted.
     */
    public void deleteFormula(GradeFormula formula) {
        if (formula == null) {
            throw new IllegalArgumentException("The type and formula cant be null."
                + " The formula has to be in the grade formula history of the type");
        }
        getActiveParticipationType().getGradeFormulas().remove(formula);
        course.getParticipationTypes().set(activeTabIndex,
                participationTypeService.update(getActiveParticipationType()));
        gradeFormulaService.remove(formula);
        logGradeFormulaDeleted(formula);
        addInfoMessage(bundle.getString("gradingFormula.messageFormulaRemoved"));
        //course.getParticipationTypes().set(getActiveTabIndex(),type);
        deletedFormula = null;
    }

    /**
     * Sets a copy old grade script formula from the active participation type
     * as the currently edited grade script formula. Only the GradeFormula formula
     * string and whether the script is valid get restored. The edited user, and
     * the edited date get set when the restored formula is saved again.
     * The editMessage is set to a default restoration message.
     * @param oldFormula The old GradeFormula from the currently selected
     *                   participation type, which formula string and valid
     *                   boolean get copied to the currently edited formula.
     * @throws IllegalArgumentException If the parameter oldFormula is null.
     */
    public void restoreToFormula(GradeFormula oldFormula) {
        if (oldFormula == null) {
            throw new IllegalArgumentException("The GradeFormula oldFormula cant be null");
        }
        editedFormulas.put(getActiveParticipationType(), formulaCopyOf(oldFormula));
        editedFormulas.get(getActiveParticipationType()).setEditDescription("[Restored] "
                + oldFormula.getEditDescription());
        addInfoMessage(bundle.getString("gradingFormula.messageRestored"));
    }

    /**
     * Update the students from a participation type.
     * @param participationType The participation type which student should be updated.
     * @throws IllegalArgumentException If the participationType is null.
     */
    public void updateStudentsFrom(ParticipationType participationType) {
        if (participationType == null) {
            throw new IllegalArgumentException("The participationType can't be null");
        }
        gradedStudents.put(participationType,
                new ArrayList<>(studentService.update((gradedStudents.get(participationType)))));
        logStudentsGraded(participationType);
        addInfoMessage(bundle.getString("gradingFormula.messageStudentPaboSaved"));
    }

    /**
     * Gets the graded and edited students from a participation type.
     * @param type the participation type which graded students should be got.
     * @return A list of the currently edited students of the participation type.
     * @throw IllegalArgumentException if the type is null.
     */
    public List<Student> getGradedStudentsFrom(ParticipationType type) {
        if (type == null) {
            throw new IllegalArgumentException("The type cant be null.");
        }
        return gradedStudents.containsKey(type) ? gradedStudents.get(type) : new ArrayList<>();
    }

    /**
     * Redirects to send a n email with the currently edited formula
     *  The subject will be (assuming english locale):
     * "[Exmatrikulator] Course {course.name}: Grade Formula of ParticipationType
     * {activeParticipationType.name}".
     * The body will be the formula string of the currently edited formula.
     * FacesMessages will be sent after the redirection or if an error occurs.
     */
    public void redirectEmailToWithFormula() {
        String url = getEmailToWithFormula();
        if (!url.isEmpty()) {
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect(url);
            } catch (IOException e) {
                log.debug(e);
                addFailMessage("gradingFormula.messageErrorMailto");
            }
        } else {
            addFailMessage("gradingFormula.messageErrorMailto");
        }
    }

    /**
     * Gets the currently selected participation type by the active tab.
     * @return The currently selected participation type.
     */
    public ParticipationType getActiveParticipationType() {
        return course.getParticipationTypes().get(activeTabIndex);
    }


    /**
     * Gets the currently edited grade formula from the participation type.
     * @param type The participation type from which the gradeFormula should be returned.
     * @return The edited formula of the participation type.
     */
    public GradeFormula getEditedGradeFormulaFrom(ParticipationType type) {
        return (editedFormulas.containsKey(type)) ? editedFormulas.get(type) : null;
    }

    /**
     * Gets the grade formula of the active participation type.
     * @return The grade formula of the active participation type.
     */
    public GradeFormula getActiveGradeFormula() {
        return getEditedGradeFormulaFrom(getActiveParticipationType());
    }


    /**
     * Gets the label of a gradeType given the id of the gradeType.
     * @param gradeId The id of the gradeType value.
     * @return The lable of the gradeType.
     *         Or an empty string if the gradeType is not found.
     */
    public String gradeLabelFromId(int gradeId) {
        return (gradeTypeLabels.containsKey(gradeId)) ? gradeTypeLabels.get(gradeId) : "";
    }

    /*
     * Validations
     */

    /**
     * Validates in the deletion dialog that the user has written the first and last name
     * of the deleted user from the given association with the course.
     * @param context The FacesContext in which the validation is done.
     * @param component The UIComponent for which the validation is done.
     * @param value The value from the UI Component
     * @throws ValidatorException If the input string doesnt match the First name followed
     *                            by the last name of the user of the to be
     *                            deleted participation class.
     */
    public void validateDeletionNameInput(FacesContext context,
                                          UIComponent component,
                                          Object value)   {
        if (deletedFormula == null) {
            throw new IllegalStateException("The deleted formula cant be null");
        }
        if (context == null || !(value instanceof String)) {
            throw new IllegalArgumentException("The context cant be null "
                    + "and the value must be a string.");
        }
        log.debug("validateDeletionNameInput called: " + value);
        List<FacesMessage> messages = new ArrayList<>();
        messages.add(new FacesMessage(bundle
                .getString("gradingFormula.remove.validatorMessage")));

        final String stringValue = (String) value;
        final String expectedValue = deletedFormula.getEditDescription();

        if (!stringValue.equals(expectedValue)) {
            log.debug("Expected value: [" + expectedValue + "] does not match"
                    + " value from user: [" + stringValue + "]; throwing exception");
            throw new ValidatorException(messages);

        }
    }

    /*
     * Private methods
     */

    /**
     * Gets an mailto: url which opens the default email application for sharing
     * the currently edited grading formula.
     * The subject will be (assuming english locale):
     * "[Exmatrikulator] Course {course.name}: Grade Formula of ParticipationType
     * {activeParticipationType.name}".
     * The body will be the formula string of the currently edited formula.
     * If an exception occurs, a FacesMessage gets displayed and an empty string
     * gets returned.
     * @return The mailto: url for sharing the mail or an empty string.
     */
    private String getEmailToWithFormula()  {
        String subject = String.format("[%s] %s %s: %s %s %s %s",
                bundle.getString("common.exmatrikulator"),
                bundle.getString("common.course"),
                course.getName(),
                bundle.getString("courses.copy.formula"),
                bundle.getString("common.from").toLowerCase(),
                bundle.getString("common.participationType"),
                getActiveParticipationType().getName());

        String body = getActiveGradeFormula().getFormula();
        String ref = "";
        try {
            URI mailUri = new URI("mailto", "%20",
                    String.format("subject=%s&body=%s", subject, body));
            ref = "mailto:?" + mailUri.getRawFragment();
        } catch (URISyntaxException e) {
            log.error(e);
        }
        log.debug("Mail url:" + ref);
        return ref;
    }

    /**
     * Checks if a grade formula is valid while not changing the exposing state
     * of the controller (e.g. script output, student grades etc.).
     * @return True if the current grade script sets valid PaboGrades by executing
     *         the formula, false otherwise.
     */
    private boolean isActiveScriptValid() {
        pyOutPutStream.reset();
        pyInterpreter.cleanup();
        resetPyInterpreterLocals();
        GradeFormula activeFormula = getActiveGradeFormula();

        try {
            pyInterpreter.exec(activeFormula.getFormula());
        } catch (PyException e) {
            return false;
        }

        log.debug("Trying to execute the formula");
        PyBoolean gradingMethodSet;
        try {
            gradingMethodSet =
                    (PyBoolean) pyInterpreter.eval(PY_CALL_CHECK_SET_FINAL_GRADE_METHOD);
        } catch (PyException e) {
            return false;
        }

        if (!gradingMethodSet.getBooleanValue()) {
            log.debug("The method set_final_grade hasn't been set with one arg");
            return false;
        }

        log.debug("Trying to set the pabo Grades");
        final Map<Student, PyDictionary> studentPyGradeDictMap = createStudentPyGradeDictMap();
        pyInterpreter.set(PY_VAR_NAME_GRADEDICT_LIST, new PyList(studentPyGradeDictMap.values()));

        try {
            pyInterpreter.exec(PY_EXEC_SET_FINAL_GRADES);
        } catch (PyException e) {
            return false;
        }

        boolean allValidPaboGradesSet = true;
        for (Map.Entry<Student, PyDictionary> entry: studentPyGradeDictMap.entrySet()) {
            Object paboGradeObj = entry.getValue().get(new PyString(PY_STR_FINAL_GRADE))
                    .__tojava__(PaboGrade.class);
            if (!(paboGradeObj instanceof PaboGrade)) {
                allValidPaboGradesSet = false;
                log.error("Not all PaboGrades set in script");
            }
        }

        return allValidPaboGradesSet;
    }

    /**
     * Initialised the python interpreter.
     * Sets a new system state and imports the PaboGrade Enum class.
     */
    private void initPyInterpreter() {
        if (pyInterpreter == null) {
            pyInterpreter = new PythonInterpreter(null, new PySystemState());
        }
        PySystemState sys = pyInterpreter.getSystemState();
        sys.path.append(new PyString(PATH_MODEL));
        pyOutPutStream = new ByteArrayOutputStream(DEFAULT_OUTPUT_BLOCK_SIZE);
        pyInterpreter.setOut(pyOutPutStream);
        execPySetup();
        pyInterpreter.exec(PY_SET_UTF_8_TO_STDOUT_STDERR);
    }

    /**
     * Executes python strings for imports, settings in the environment.
     */
    private void execPySetup() {
        for (String importStr: PY_IMPORTS) {
            pyInterpreter.exec(importStr);
        }
        pyInterpreter.exec(PY_SET_DECIMAL_PRECISION);
        pyInterpreter.exec(PY_INIT_CHECK_SET_FINAL_GRADE_METHOD);
    }

    /**
     * Sets up a python dictionary of the students gradings of the exams.
     * The shortcut of a exam of the grading gets put as key of the dictionary,
     * the value of the grade of the grading gets put as value depending of
     * the grade type(see gradeValueToPyObject).
     * @param exams The list of exams for which should be included in the grade dict.
     * @param student The student which gradings should be put in the dict.
     * @return The Python dictionary of the exam shortcuts.
     */
    private PyDictionary getPyGradeDict(List<Exam> exams, Student student) {
        log.debug("getPyGradeDict called with student " + student.getUser());
        PyDictionary pyGradeDict = new PyDictionary();
        for (Exam exam: exams) {
            Grading grading = student.getGradingFromExam(exam);
            log.debug("Got grading " + grading + " for exam " + exam.getName());
            pyGradeDict.put(new PyString(exam.getShortcut()),
                    gradeValueToPyObject((grading == null) ? null : grading.getGrade()));
        }
        return pyGradeDict;
    }

    /**
     * Converts a grade value of the exmatrikulator system in a python representation
     * depending of the gradeType and value of the grade. The following mappings
     * take place:
     *  * GradeType.Boolean will be represented as PyBoolean
     *    for passed(true) / not passed(false).
     *  * GradeType.Numeric and GradeType.Percent will be represented as python
     *    Decimals with the numeric or percent values.
     *  * GradeType.Point will be represented as tuple of the reached and max
     *    points.
     *  * If the grade has no value (no grade has been given): The grade will be None.
     * @param grade The grade object representing the grade object.
     * @return The Python object representation of the grade value.
     */
    private PyObject gradeValueToPyObject(Grade grade) {
        log.debug("gradeValueToPyObject called with: " + grade);
        if (grade == null || grade.getValue() == null) {
            return pyInterpreter.get(PY_NONE);
        }
        if (GradeType.Boolean.getId().equals(grade.getGradeType())) {
            return booleanGradeToPyBoolean(grade);
        } else if (GradeType.Numeric.getId().equals(grade.getGradeType())
                || GradeType.Percent.getId().equals(grade.getGradeType())) {
            return pyDecimalFromBigDecimal(grade.getValue());
        } else if (GradeType.Point.getId().equals(grade.getGradeType())) {
            return pyTupleFromPointGrade(grade);
        } else {
            throw new IllegalStateException(
                    "Grade was not a Boolean, Numeric nor PointGrade");
        }
    }

    /**
     * Converts a grade of the type boolean grade to a Python Boolean object.
     * @param grade A grade of the type GradeType.Boolean. It must have as value
     *              either "0" for not passed or "1" for passed.
     *              If the gradeType is not a boolean grade or the value of the
     *              grade does not match, an IllegalArgumentException gets thrown-
     * @return A PyBoolean which is true if the grade has a value of 1, or false
     *         if the grade has a value of 0.
     * @throws IllegalArgumentException If the value of the grade does not
     *                                  equal 1 or 0.
     */
    private PyBoolean booleanGradeToPyBoolean(Grade grade) {
        log.debug("booleanGradeToPyBoolean called with " + grade);
        if (grade.getValue().equals(new BigDecimal("1"))) {
            return new PyBoolean(true);
        } else if (grade.getValue().equals(new BigDecimal("0"))) {
            return new PyBoolean(false);
        } else {
            throw new IllegalArgumentException("The value of the grade does not equal 1 or 0");
        }
    }

    /**
     * Creates a python decimal object from a java BigDecimal object.
     * @param bigDecimal The BigDecimal object from which the python decimal
     *                   object should be created from-
     * @return A pyObject representing the python decimal.
     */
    private PyObject pyDecimalFromBigDecimal(BigDecimal bigDecimal) {
        log.debug("pyDecimalFromBigDecimal called with bigDecimal: " + bigDecimal);
        PyObject pyDecimal = pyInterpreter.get(PY_DECIMAL_CLASS_NAME);
        return pyDecimal.__call__(new PyString(bigDecimal.toString()));
    }

    /**
     * Creates a python tuple from a point grade representing the reached and
     * max points of the grade. The first part of the tuple are the reached points,
     * the second part are the maximal points of the exam. Both points are
     * represented as python decimals.
     * @param grade The grade with type PointGrade with which the
     * @return A tuple representing the reached points and the maximal points
     *         of the grade.
     */
    private PyTuple pyTupleFromPointGrade(Grade grade) {
        log.debug("pyTupleFromPointGrade called with " + grade);
        PyObject[] pyTupleArgs = {pyDecimalFromBigDecimal(grade.getValue()),
                pyDecimalFromBigDecimal(grade.getMaxPoints())};
        return new PyTuple(pyTupleArgs);
    }

    /**
     * Creates a map from each student to a python dictionary containing
     * the gradings of each exam of the student.
     * The python dictionary has the shortcut of the exam as key and the value
     * of the students grading as value (see gradeValueToPyObject).
     * @return The map of student gradings.
     */
    private Map<Student, PyDictionary> createStudentPyGradeDictMap() {
        log.debug("createStudentPyGradeDictMap() called");
        Map<Student, PyDictionary> studentPyGradeDictMap = new HashMap<>();
        for (Student student: gradedStudents.get(getActiveParticipationType())) {
            log.debug("Putting student " + student.getUser() + " in dict");
            studentPyGradeDictMap.put(student, getPyGradeDict(course.getExams(), student));
        }
        return studentPyGradeDictMap;
    }

    /**
     * Adds a fail message to the FacesContext.
     * @param message the message to be displayed.
     */
    private void addFailMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_FATAL,
                bundle.getString("common.error"),
                message));
    }

    /**
     * Adds an info message to the FacesContext.
     * @param message The info message to be displayed.
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                FacesMessage.SEVERITY_INFO,
                "",
                message));
    }

    /**
     * Resets the python interpreter locales which got imported while running
     * a grade script.
     */
    private void resetPyInterpreterLocals() {
        List<String> scriptLocals = new ArrayList<>();
        PyObject locals = pyInterpreter.getLocals();
        for (PyObject item : locals.__iter__().asIterable()) {
            scriptLocals.add(item.toString());
        }
        for (String local : scriptLocals) {
            if (!isProtectedPyLocal(local)) {
                pyInterpreter.set(local, null);
            }
        }
        execPySetup();
    }

    /**
     * Checks if a string of the name of a local is of a protected local.
     * @param local The string of a python local.
     * @return True if the string local is a protected local, false otherwise.
     */
    private boolean isProtectedPyLocal(String local) {
        for (String protectedLocal: PY_PROTECTED_LOCALS) {
            if (protectedLocal.equals(local)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Copies a formula object to a new formula object. Only the GradeFormula formula
     * string and whether the script is valid get restored. The save date
     * gets set when the copy is saved to the database.
     */
    private GradeFormula formulaCopyOf(GradeFormula formula) {
        log.debug("formulaCopyOf called with: " + formula);
        GradeFormula copy = new GradeFormula();
        copy.setFormula(formula.getFormula());
        copy.setEditor(loggedInUser);
        copy.setValid(formula.isValid());
        copy.setEditDescription(formula.getEditDescription() == null
                ? "" : formula.getEditDescription());
        copy.setValid(formula.isValid());
        return copy;
    }


    /*
     * Logging methods
     */


    /**
     * Logs for every edited student of the participation type the pabo grade.
     * @param type The participation type which students pabo grades get logged.
     */
    private void logStudentsGraded(ParticipationType type) {
        for (Student student: gradedStudents.get(type)) {
            logAction("Student " + student.getUser() + " of the participation type "
                + type.getName() + " got the Pabo Grade "
                    + ((student.getPaboGrade() == null)
                    ? null :  PaboGrade.valueOf(student.getPaboGrade())));
        }
    }

    /**
     * Logs that a grade formula has been saved.
     * @param formula The grade formula which got saved.
     */
    private void logGradeFormulaSaved(GradeFormula formula) {
        String descr = "A GradeFormula with the description " + formula.getEditDescription()
                + " got saved for the participation type"
                + formula.getParticipationType().getName() + "."
                + (formula.isValid()
                    ? " The formula was valid." : " The formula was not valid.");
        logAction(descr);
    }

    /**
     * Logs that a grade formula has been deleted.
     * @param formula The grade formula which got deleted.
     */
    private void logGradeFormulaDeleted(GradeFormula formula) {
        String descr = "A GradeFormula with the description " + formula.getEditDescription()
                + " got deleted for the participation type"
                + formula.getParticipationType().getName() + "."
                + (formula.isValid()
                ? " The formula was valid." : " The formula was not valid.");
        logAction(descr);
    }

    /**
     * Logs an action in the GradeScriptController with the currently logged in
     * user and the course id of the current course.
     * @param description The description of the action.
     */
    private void logAction(String description) {
        logService.persist(Log.from(loggedInUser, course.getCourseId(), description));

    }


    /*
     * Getters and Setters
     */

    @EJB
    public void setGradeFormulaService(GradeFormulaService gradeFormulaService) {
        this.gradeFormulaService = gradeFormulaService;
    }

    @EJB
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    @EJB
    public void setParticipationTypeService(ParticipationTypeService service) {
        this.participationTypeService = service;
    }

    @EJB
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    @EJB
    public void setStudentService(StudentService studentService) {
        this.studentService = studentService;
    }

    @EJB
    public void setGradeService(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    public void setPythonInterpreter(PythonInterpreter pyInterpreter) {
        this.pyInterpreter = pyInterpreter;
    }

    public Course getCourse() {
        return course;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public ParticipationType getSelectedParticipationType() {
        return selectedParticipationType;
    }

    public void setSelectedParticipationType(ParticipationType selectedParticipationType) {
        this.selectedParticipationType = selectedParticipationType;
    }

    public List<Exam> getFilteredExams() {
        return filteredExams;
    }

    public void setFilteredExams(List<Exam> filteredExams) {
        this.filteredExams = filteredExams;
    }

    public Map<Integer, String> getGradeTypeLabels() {
        return gradeTypeLabels;
    }

    public Integer getActiveTabIndex() {
        return activeTabIndex;
    }

    public void setActiveTabIndex(Integer tabIndex) {
        log.debug("setActiveTabIndex called: " + tabIndex);
        this.activeTabIndex = tabIndex;
    }

    public GradeFormula getDeletedFormula() {
        return deletedFormula;
    }

    public void setDeletedFormula(GradeFormula deletedFormula) {
        this.deletedFormula = deletedFormula;
    }

    public String getDeleteConfirmation() {
        return deleteConfirmation;
    }

    public void setDeleteConfirmation(String deleteConfirmation) {
        this.deleteConfirmation = deleteConfirmation;
    }


    public String getScriptOutput() {
        return scriptOutput;
    }

    public void setScriptOutput(String scriptOutput) {
        this.scriptOutput = scriptOutput;
    }

    public boolean getUserIsLecturer() {
        return userIsLecturer;
    }

    public String getPaboGradeName(final String name) {
        return gradeService.paboGradeDisplayName(name);
    }

}
