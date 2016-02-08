package de.unibremen.opensores.util;

/**
 * Constants shared between various views.
 *
 * @author Sören Tempel
 */
public class Constants {
    /**
     * Minimum password length.
     */
    public static final int MIN_PASSWORD_LENGTH = 6;

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
     * The course id http parameter key used to pass courses to course related
     * pages.
     */
    public static final String HTTP_PARAM_COURSE_ID = "course-id";
}
