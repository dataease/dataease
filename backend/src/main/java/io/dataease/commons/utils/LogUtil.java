package io.dataease.commons.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogUtil {

    private static final String DEBUG = "DEBUG";
    private static final String INFO = "INFO";
    private static final String WARN = "WARN";
    private static final String ERROR = "ERROR";

    /**
     * 初始化日志
     *
     * @return
     */
    public static Logger getLogger() {
        return LoggerFactory.getLogger(LogUtil.getLogClass());
    }

    public static void writeLog(Object msg, String level) {
        Logger logger = LogUtil.getLogger();

        if (DEBUG.equals(level)) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(LogUtil.getMsg(msg));
            }
        } else if (INFO.equals(level)) {
            if (logger != null && logger.isInfoEnabled()) {
                logger.info(LogUtil.getMsg(msg));
            }
        } else if (WARN.equals(level)) {
            if (logger != null && logger.isWarnEnabled()) {
                logger.warn(LogUtil.getMsg(msg));
            }
        } else if (ERROR.equals(level)) {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error(LogUtil.getMsg(msg));
            }
        } else {
            if (logger != null && logger.isErrorEnabled()) {
                logger.error("");
            }
        }
    }

    public static void info(Object msg) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtil.getMsg(msg));
        }
    }

    public static void info(Object msg, Object o1) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtil.getMsg(msg), o1);
        }
    }

    public static void info(Object msg, Object o1, Object o2) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtil.getMsg(msg), o1, o2);
        }
    }

    public static void info(Object msg, Object[] obj) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isInfoEnabled()) {
            logger.info(LogUtil.getMsg(msg), obj);
        }
    }

    public static void debug(Object msg) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtil.getMsg(msg));
        }
    }

    public static void debug(Object msg, Object o) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtil.getMsg(msg), o);
        }
    }

    public static void debug(Object msg, Object o1, Object o2) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtil.getMsg(msg), o1, o2);
        }
    }

    public static void debug(Object msg, Object[] obj) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isDebugEnabled()) {
            logger.debug(LogUtil.getMsg(msg), obj);
        }
    }

    public static void warn(Object msg) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtil.getMsg(msg));
        }
    }

    public static void warn(Object msg, Object o) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtil.getMsg(msg), o);
        }
    }

    public static void warn(Object msg, Object o1, Object o2) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtil.getMsg(msg), o1, o2);
        }
    }

    public static void warn(Object msg, Object[] obj) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isWarnEnabled()) {
            logger.warn(LogUtil.getMsg(msg), obj);
        }
    }

    public static void error(Object msg) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtil.getMsg(msg));// 并追加方法名称
        }
    }

    public static void error(Object msg, Object o) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtil.getMsg(msg), o);
        }
    }

    public static void error(Object msg, Object o1, Object o2) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtil.getMsg(msg), o1, o2);
        }
    }

    public static void error(Object msg, Object[] obj) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtil.getMsg(msg), obj);
        }
    }

    public static void error(Object msg, Throwable ex) {
        Logger logger = LogUtil.getLogger();
        if (logger != null && logger.isErrorEnabled()) {
            logger.error(LogUtil.getMsg(msg), ex);
        }
    }

    public static String getMsg(Object msg, Throwable ex) {
        String str = "";

        if (msg != null) {
            str = LogUtil.getLogMethod() + "[" + msg.toString() + "]";
        } else {
            str = LogUtil.getLogMethod() + "[null]";
        }
        if (ex != null) {
            str += "[" + ex.getMessage() + "]";
        }

        return str;
    }

    public static String getMsg(Object msg) {
        return LogUtil.getMsg(msg, null);
    }

    /**
     * 得到调用类名称
     *
     * @return
     */
    private static String getLogClass() {
        String str = "";

        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        if (stack.length > 3) {
            StackTraceElement ste = stack[3];
            str = ste.getClassName();// 类名称
        }

        return str;
    }

    /**
     * 得到调用方法名称
     *
     * @return
     */
    private static String getLogMethod() {
        String str = "";

        StackTraceElement[] stack = (new Throwable()).getStackTrace();
        if (stack.length > 4) {
            StackTraceElement ste = stack[4];
            str = "Method[" + ste.getMethodName() + "]";// 方法名称
        }

        return str;
    }

    public static String toString(Throwable e) {
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            //将出错的栈信息输出到printWriter中
            e.printStackTrace(pw);
            pw.flush();
            sw.flush();
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (pw != null) {
                pw.close();
            }
        }
        return sw.toString();
    }

    public static String getExceptionDetailsToStr(Exception e) {
        StringBuilder sb = new StringBuilder(e.toString());
        StackTraceElement[] stackElements = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackElements) {
            sb.append(stackTraceElement.toString());
            sb.append("\n");
        }
        sb.append("\n");
        return sb.toString();
    }
}
