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
}
