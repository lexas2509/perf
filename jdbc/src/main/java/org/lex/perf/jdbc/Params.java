package org.lex.perf.jdbc;

/**
 * Created with IntelliJ IDEA.
 * User: lexas
 * Date: 21.05.14
 * Time: 20:43
 * To change this template use File | Settings | File Templates.
 */
public class Params {
    private static boolean noDatabase;
    private static boolean systemActionsEnabled;
    private static String serverInfo;

    public static boolean isNoDatabase() {
        return noDatabase;
    }

    public static boolean isSystemActionsEnabled() {
        return systemActionsEnabled;
    }

    public static String getServerInfo() {
/*        assert context != null;
        this.servletContext = context;
        String serverInfo = servletContext.getServerInfo();
*/
        return serverInfo;
    }
}
