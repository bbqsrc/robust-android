package so.brendan.robust.utils;

/**
 * Holds relevant application-wide constants, and relevant constant generation methods.
 */
public class Constants {
    public static final String APP_NAME = "Robust";
    public static final String PACKAGE_ROOT = "so.brendan.robust";

    /**
     * Convenience method for creating package unique <code>TAG</code> constants for debugging.
     *
     * @param cls
     * @return
     */
    public static String createTag(Class cls) {
        return APP_NAME + "/" + cls.getSimpleName();
    }

    /**
     * Convenience method for creating package unique tags for views.
     *
     * @param name
     * @return
     */
    public static String createViewTag(String name) {
        return PACKAGE_ROOT + ".tag." + name;
    }

    /**
     * Convenience method for creating package unique constants for extras.
     *
     * @param name
     * @return
     */
    public static String createExtra(String name) {
        return PACKAGE_ROOT + ".extra." + name;
    }

    /**
     * Convenience method for creating package unique constants for actions.
     *
     * @param name
     * @return
     */
    public static String createAction(String name) {
        return PACKAGE_ROOT + ".action." + name;
    }

    private Constants() {}
}
