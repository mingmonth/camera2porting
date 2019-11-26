package yskim.sample.camera2porting.util;

import android.util.Log;

/**
 * Created by yskim on 2015-06-01.
 * Usage : Debug.logd(new Exception(), "");
 */
public class Debug {

    public static boolean D = true;
    private static final int LEVEL = 0;

    private static final int LOG_V = 0;
    private static final int LOG_D = 1;
    private static final int LOG_I = 2;
    private static final int LOG_W = 3;
    private static final int LOG_E = 4;

    private static void log(int type, Exception e, String message)
    {
        if(LEVEL > type) return;

        if (D)
        {
            StackTraceElement l = e.getStackTrace()[0];

            String fullClassName = l.getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = l.getMethodName();
            int lineNumber = l.getLineNumber();

            String tag = className + "." + methodName + "():" + lineNumber;

            switch (type) {
                case LOG_V:
                    Log.v(tag, message);
                    break;
                case LOG_D:
                    Log.d(tag, message);
                    break;
                case LOG_I:
                    Log.i(tag, message);
                    break;
                case LOG_W:
                    Log.w(tag, message);
                    break;
                case LOG_E:
                    Log.e(tag, message);
                    break;
            }

            e = null;
        }
    }

    public static void logv(Exception e, String message) { log(LOG_V, e, "____________________ " + message); }

    public static void logd(Exception e, String message) { log(LOG_D, e, "____________________ " + message); }

    public static void logi(Exception e, String message) { log(LOG_I, e, "____________________ " + message); }

    public static void logw(Exception e, String message) { log(LOG_W, e, "____________________ " + message); }

    public static void loge(Exception e, String message) { log(LOG_E, e, "____________________ " + message); }
}
