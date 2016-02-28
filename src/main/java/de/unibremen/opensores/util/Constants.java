package de.unibremen.opensores.util;

import de.unibremen.opensores.model.Role;

/**
 * Constants shared between various views.
 *
 * @author Sören Tempel, Kevin Scheck
 */
public final class Constants {

    /**
     * Empty constructor for no initialization.
     */
    private Constants() {

    }

    /**
     * The name of the message bundle used by the Exmatrikulator.
     */
    public static final String BUNDLE_NAME = "messages";

    /**
     * Minimum password length.
     */
    public static final int MIN_PASSWORD_LENGTH = 6;

    /**
     * Minimum name length.
     */
    public static final int MIN_NAME_LENGTH = 1;

    /**
     * Maximum name length.
     */
    public static final int MAX_NAME_LENGTH = 100;

    /**
     * Maximum filesize per uploaded file in bytes.
     */
    public static final int MAX_UPLOADFILE_SIZE = 100000000; //100Mb


    /**
     * Maxmimum length of a tutorial event description.
     */
    public static final int MAX_TUT_EVENT_DESCR_LENGTH = 1000;
    /**
     * Email regex used for mail verification.
     *
     * <p>
     * This regex is far from complete infact it's pretty hard to match email
     * addresses using a regex but this one has also been used by the libAwesome
     * project and is thus probably good enough.
     * </p>
     */
    public static final String EMAIL_REGEX =
        "[\\w\\.-]*[a-zA-Z0-9_]@[\\w\\.-]*[a-zA-Z0-9]\\.[a-zA-Z][a-zA-Z\\.]*[a-zA-Z]";

    /**
     * The http parameter key by which the course id gets passed.
     */
    public static final String HTTP_PARAM_COURSE_ID = "course-id";

    /**
     * The http parameter key by which the student id gets passed.
     */
    public static final String HTTP_PARAM_STUDENT_ID = "student-id";

    /**
     * The http parameter key by which the tutorial id gets passed.
     */
    public static final String HTTP_PARAM_TUTORIAL_ID = "tutorial-id";

    /**
     * The http parameter key by which the exam id gets passed.
     */
    public static final String HTTP_PARAM_EXAM_ID = "exam-id";

    /**
     * The http parameter key by which the mail template id gets passed.
     */
    public static final String HTTP_PARAM_MAILTEMPLATE_ID = "mailtemplate-id";

    /**
     * The path to the course overview site.
     */
    public static final String PATH_TO_COURSE_OVERVIEW =
            "/course/overview.xhtml?faces-redirect=true";

    /**
     * The path to the certificate template.
     */
    public static final String PATH_TO_CERTIFICATE_TEMPLATE =
            "/certificate/printview.xhtml";

    /**
     * The key of the logged in user in the session map.
     */
    public static final String SESSION_MAP_KEY_USER = "user";

    /**
     * The timezone of the system in Exmatrikulator.
     */
    public static final String SYSTEM_TIMEZONE = "Europe/Berlin";

    /**
     * Live search threshold. Searching should begin if searchString
     * has a length >= threshold
     */
    public static final int LIVE_SEARCH_THRESHOLD = 3;

    /**
     * String for deleted string values.
     */
    public static final String DELETED = "deleted";

    /**
     * The default grade script for new courses / participation types.
     */
    public static final String DEFAULT_GRADE_SCRIPT =
         "def set_final_grade(grades, student_info, other_course_grades):\n"
          + "    \"\"\"\n"
          + "    This method is the main method which sets the final grades of\n"
          + "    each student in this participation type. This method must return a PaboGrade.\n"
          + "    This method takes the following parameters, which are all dictionaries:\n"
          + "    :grades A dictionary of the current grades. The key is the exam shortcut\n"
          + "            and the value is the grade representation.\n"
          + "    :student_info A dictionary has the basic information about the student like\n"
          + "                  the email address or the matriculation number.\n"
          + "    :other_course_grades: A dictionary of other courses in of the student \n"
          + "                  in which the logged in user is a lecturer.\n"
          + "    :return A valid PaboGrade (PaboGrade.1_0, or PaboGrade.1_3,..., PaboGrade.5_0, \n"
          + "            PaboGrade.GRADE_CHEATED, PaboGrade.GRADE_NEGLECTED\n"
          + "    \"\"\"\n"
          + "    print grades\n"
          + "    print student_info\n"
          + "    print other_course_grades\n"
          + "    pass #return PaboGrade.1_0, or PaboGrade.1_3, PaboGrade,1_7,...PaboGrade.5_0";

    /**
     * Message for default grade script initialization.
     */
    public static final String DEFAULT_SCRIPT_EDIT_MESSAGE =
            "Initialized default grade script";
}
