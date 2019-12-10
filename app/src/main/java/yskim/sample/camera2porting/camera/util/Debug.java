package yskim.sample.camera2porting.camera.util;

import android.util.Log;

import yskim.sample.camera2porting.BuildConfig;

/**
 * Created by yskim on 2015-06-01.
 * Usage : Debug.logd(new Exception(), "");
 */
public class Debug {

    private static boolean isDebug = BuildConfig.DEBUG;
    private static int debugLevel = 0;  // default value 0

    public static final int LOG_V = 1 << 0;
    public static final int LOG_D = 1 << 1;
    public static final int LOG_I = 1 << 2;
    public static final int LOG_W = 1 << 3;
    public static final int LOG_E = 1 << 4;

    public static void setDebug(boolean isDebug) {
        Debug.isDebug = isDebug;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static void setDebugLevel(int debugLevel) {
        Debug.debugLevel = debugLevel;
    }

    public static int getDebugLevel() {
        return debugLevel;
    }

    private static void log(int type, Exception e, String message) {
        // 설정한 디버그 레벨보다 작으면 로그 출력하지 않음.
        if(debugLevel > type) return;

        // 디버그 모드이면. 로그 출력.
        if (isDebug)  {
            StackTraceElement l = e.getStackTrace()[0];

            String fullClassName = l.getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = l.getMethodName();
            int lineNumber = l.getLineNumber();

            StringBuilder sb = new StringBuilder();

            sb.append(className);
            sb.append(".");
            sb.append(methodName);
            sb.append("():");
            sb.append(lineNumber);

            String tag = sb.toString();

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
