/*
 * Created by IntelliJ IDEA.
 * Language: Java
 * Property of Umesh Gunasekara
 * @Author: SLMORA
 * @DateTime: 7/24/2023 12:00 AM
 */
package com.slmora.common.logging;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *  This Class created for MoraLogger
 *  <ul>
 *      <li>....</li>
 *  </ul>
 *
 * @since   1.0
 *
 * <blockquote><pre>
 * <br>Version      Date            Editor              Note
 * <br>-------------------------------------------------------
 * <br>1.0          1/21/2025      SLMORA                Initial Code
 * </pre></blockquote>
 */
public class MoraLogger
{
    final static Logger BASE_LOGGER = LogManager.getLogger(MoraLogger.class);
    private static final String DELIM_STR = "{}";
    private static final String MESSAGE_RESOURCE = "messageResources.properties";
    private Properties propCache;
    private Logger logger = null;

    private static ConcurrentMap<String, MoraLogger> loggerMap = new ConcurrentHashMap<>();

    public static MoraLogger getLogger(Class<?> clazz)
    {
        String className = clazz.getName();
        int index = className.lastIndexOf('.');

        if (index >= 0) {
            className = className.substring(index + 1);
        }

        MoraLogger logger = loggerMap.get(className);

        if (logger == null) {
            MoraLogger newLogger = new MoraLogger(clazz);
            logger = loggerMap.putIfAbsent(className, newLogger);
            if (logger == null) {
                logger = newLogger;
            }
        }

        return logger;
    }

    public MoraLogger(Class<?> clazz)
    {
        this.logger = LogManager.getLogger(clazz);
    }


    //FATAL - 100
    public void fatal(String message)
    {
        this.logger.fatal(message);
    }
    public void fatal(Throwable e)
    {
        fatal(ExceptionUtils.getStackTrace(e));
    }
    public void fatal(String methodName, String msgId, String message )
    {
        fatal(formatMsg(methodName, msgId, message));
    }
    public void fatal(StackTraceElement[] stackTrace, String msgId, String message )
    {
        fatal(formatMsg(stackTrace, msgId, message));
    }
    public void fatal(MoraLoggerThreadInfo threadInfo, String msgId, String message )
    {
        fatal(formatMsg(threadInfo, msgId, message));
    }


    //ERROR - 200
    public void severe(String message)
    {
        error(message);
    }
    public void severe(Throwable e)
    {
        error(e);
    }
    public void severe(String methodName, String msgId, String message )
    {
        error(formatMsg(methodName, msgId, message));
    }
    public void severe(StackTraceElement[] stackTrace, String msgId, String message )
    {
        error(formatMsg(stackTrace, msgId, message));
    }
    public void severe(MoraLoggerThreadInfo threadInfo, String msgId, String message )
    {
        error(formatMsg(threadInfo, msgId, message));
    }


    public void error(String message)
    {
        this.logger.error(message);
    }
    public void error(Throwable e)
    {
        error(ExceptionUtils.getStackTrace(e));
    }
    public Optional<String> error(String methodName, Throwable cause)
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
//            String msg =  buildMessage(methodName, "ERRO-00000", cause.getMessage());
            String msg =  buildMessage(methodName, "ERRO-00000", ExceptionUtils.getStackTrace(cause));
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(String methodName, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(methodName, "ERRO-00000", msgFormat, args);
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(StackTraceElement[] stackTrace, Throwable cause)
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
//            String msg =  buildMessage(stackTrace, "ERRO-00000", cause.getMessage());
            String msg =  buildMessage(stackTrace, "ERRO-00000", ExceptionUtils.getStackTrace(cause));
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(StackTraceElement[] stackTrace, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(stackTrace, "ERRO-00000", msgFormat, args);
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(StackTraceElement[] stackTrace, String msgPrefix, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(stackTrace, msgPrefix, msgFormat, args);
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(MoraLoggerThreadInfo threadInfo, Throwable cause)
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
//            String msg =  buildMessage(stackTrace, "ERRO-00000", cause.getMessage());
            String msg =  buildMessage(threadInfo, "ERRO-00000", ExceptionUtils.getStackTrace(cause));
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(MoraLoggerThreadInfo threadInfo, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(threadInfo, "ERRO-00000", msgFormat, args);
            error(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> error(MoraLoggerThreadInfo threadInfo, String msgPrefix, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.ERROR)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(threadInfo, msgPrefix, msgFormat, args);
            error(msg);
            return Optional.of(msg);
        }
    }




    //DEBUG - 500
    public void debug(String message)
    {
        this.logger.debug(message);
    }
    public Optional<String> debug(String methodName, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.DEBUG)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(methodName, "DEBG-00000", msgFormat, args);
            debug(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> debug(StackTraceElement[] stackTrace, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.DEBUG)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(stackTrace, "DEBG-00000", msgFormat, args);
            debug(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> debug(MoraLoggerThreadInfo threadInfo, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.DEBUG)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(threadInfo, "DEBG-00000", msgFormat, args);
            debug(msg);
            return Optional.of(msg);
        }
    }
    public void detail(String message)
    {
        debug(message);
    }
    public void detail(String methodName, String msgId, String message)
    {
        debug(formatMsg(methodName, msgId, message));
    }
    public void detail(StackTraceElement[] stackTrace, String msgId, String message)
    {
        debug(formatMsg(stackTrace, msgId, message));
    }
    public void detail(MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        debug(formatMsg(threadInfo, msgId, message));
    }


    //INFO - 400
    public void info(String message)
    {
        this.logger.info(message);
    }
    public Optional<String> info(String methodName, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.INFO)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(methodName, "INFO-00000", msgFormat, args);
            info(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> info(StackTraceElement[] stackTrace, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.INFO)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(stackTrace, "INFO-00000", msgFormat, args);
            info(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> info(MoraLoggerThreadInfo threadInfo, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.INFO)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(threadInfo, "INFO-00000", msgFormat, args);
            info(msg);
            return Optional.of(msg);
        }
    }
    public void entering(String methodName, String message)
    {
        entering(methodName, "", message);
    }
    public void entering(String methodName, String msgId, String message)
    {
        info(methodName, msgId, message);
    }
    public void audit(String methodName, String msgId, String message)
    {
        info(formatMsg(methodName, msgId, message));
    }
    public void info(Marker marker, String message)
    {
        this.logger.info(marker, message);
    }

    public void info(Marker marker, StackTraceElement[] stackTrace, String message)
    {
        this.logger.info(marker, formatMsg(stackTrace, message));
    }
    public void info(Marker marker, MoraLoggerThreadInfo threadInfo, String message)
    {
        this.logger.info(marker, formatMsg(threadInfo, message));
    }


    //WARN - 300
    public void warn(String message)
    {
        this.logger.warn(message);
    }
    public void warning(String message)
    {
        warn(message);
    }
    public Optional<String> warn(String methodName, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.WARN)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(methodName, "WARN-00000", msgFormat, args);
            warn(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> warn(StackTraceElement[] stackTrace, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.WARN)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(stackTrace, "WARN-00000", msgFormat, args);
            warn(msg);
            return Optional.of(msg);
        }
    }
    public Optional<String> warn(MoraLoggerThreadInfo threadInfo, String msgFormat, Object... args )
    {
        if(!isLoggable(Level.WARN)){
            return Optional.empty();
        }else {
            String msg =  buildMessage(threadInfo, "WARN-00000", msgFormat, args);
            warn(msg);
            return Optional.of(msg);
        }
    }


    //TRACE - 600
    public void trace(String message)
    {
        this.logger.trace(message);
    }
    public void trace(String methodName, String msgId, String message)
    {
        trace(formatMsg(methodName, msgId, message));
    }
    public void trace(StackTraceElement[] stackTrace, String msgId, String message)
    {
        trace(formatMsg(stackTrace, msgId, message));
    }
    public void trace(MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        trace(formatMsg(threadInfo, msgId, message));
    }
    public void fine(String message)
    {
        trace(message);
    }

    //LOG
    public void log(Level level, String methodName, Exception exception)
    {
        logWithLevel(level, methodName, "", ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, StackTraceElement[] stackTrace, Exception exception)
    {
        logWithLevel(level, stackTrace, "", ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, MoraLoggerThreadInfo threadInfo, Exception exception)
    {
        logWithLevel(level, threadInfo, "", ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, String methodName, String message)
    {
        logWithLevel(level, methodName, "", message);
    }
    public void log(Level level, StackTraceElement[] stackTrace, String message)
    {
        logWithLevel(level, stackTrace, "", message);
    }
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String message)
    {
        logWithLevel(level, threadInfo, "", message);
    }
    public void log(Level level, String message )
    {
        logWithLevel(level, "", "", message);
    }
    public void log(Level level, String methodName, Throwable exception)
    {
        logWithLevel(level, methodName, "", ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, StackTraceElement[] stackTrace, Throwable exception)
    {
        logWithLevel(level, stackTrace, "", ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, MoraLoggerThreadInfo threadInfo, Throwable exception)
    {
        logWithLevel(level, threadInfo, "", ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, String methodName, String msgId, String message)
    {
        logWithLevel(level, methodName, msgId, message);
    }
    public void log(Level level, StackTraceElement[] stackTrace, String msgId, String message)
    {
        logWithLevel(level, stackTrace, msgId, message);
    }
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        logWithLevel(level, threadInfo, msgId, message);
    }
    public void log(Level level, String methodName, String msgId, Object message)
    {
        logWithLevel(level, methodName, msgId, message.toString());
    }
    public void log(Level level, StackTraceElement[] stackTrace, String msgId, Object message)
    {
        logWithLevel(level, stackTrace, msgId, message.toString());
    }
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String msgId, Object message)
    {
        logWithLevel(level, threadInfo, msgId, message.toString());
    }
    public void log(Level level, String methodName, String msgId, Exception exception)
    {
        logWithLevel(level, methodName, msgId, ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, StackTraceElement[] stackTrace, String msgId, Exception exception)
    {
        logWithLevel(level, stackTrace, msgId, ExceptionUtils.getStackTrace(exception));
    }
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String msgId, Exception exception)
    {
        logWithLevel(level, threadInfo, msgId, ExceptionUtils.getStackTrace(exception));
    }

    public void logWithLevel(Level level, String methodName, String msgId, String message)
    {
        switch (level.intLevel()) {
            case 100:
                fatal(methodName, msgId, message);
                break; // FATAL
            case 200:
                error(methodName, msgId, message);
                break; // ERROR
            case 300:
                warn(methodName, msgId, message);
                break; // WARN
            case 400:
                info(methodName, msgId, message);
                break; // INFO
            case 500:
                debug(methodName, msgId, message);
                break; // DEBUG
            case 600:
                trace(methodName, msgId, message);
                break; // TRACE
            default:
        }
    }

    public void logWithLevel(Level level, StackTraceElement[] stackTrace, String msgId, String message)
    {
        switch (level.intLevel()) {
            case 100:
                fatal(stackTrace, msgId, message);
                break; // FATAL
            case 200:
                error(stackTrace, msgId, message);
                break; // ERROR
            case 300:
                warn(stackTrace, msgId, message);
                break; // WARN
            case 400:
                info(stackTrace, msgId, message);
                break; // INFO
            case 500:
                debug(stackTrace, msgId, message);
                break; // DEBUG
            case 600:
                trace(stackTrace, msgId, message);
                break; // TRACE
            default:
        }
    }

    public void logWithLevel(Level level, MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        switch (level.intLevel()) {
            case 100:
                fatal(threadInfo, msgId, message);
                break; // FATAL
            case 200:
                error(threadInfo, msgId, message);
                break; // ERROR
            case 300:
                warn(threadInfo, msgId, message);
                break; // WARN
            case 400:
                info(threadInfo, msgId, message);
                break; // INFO
            case 500:
                debug(threadInfo, msgId, message);
                break; // DEBUG
            case 600:
                trace(threadInfo, msgId, message);
                break; // TRACE
            default:
        }
    }


    private String buildMessage(String methodName, String msgPrefix, String msgFormat, Object... args)
    {
        StringBuilder builder = new StringBuilder().append('[').append(methodName).append("] ");

        if (null == msgFormat || !"".equals(msgFormat.trim())) {
            builder.append("<Empty message>");
        } else if (isMoraMessage(msgFormat)) {
            msgFormat = msgFormat.trim();
            builder.append(msgFormat).append(": ").append(buildMoraLogMessage(msgFormat, args));
        } else {
            builder.append(msgPrefix).append(": ").append(buildDelimitedMessage(msgFormat, args));
        }

        return builder.toString();
    }

    private String buildMessage(StackTraceElement[] stackTrace, String msgPrefix, String msgFormat, Object... args)
    {
        StringBuilder builder = new StringBuilder().append('[').append(formatLineInfo(stackTrace)).append("] ");

        if (null == msgFormat || !"".equals(msgFormat.trim())) {
            builder.append("<Empty message>");
        } else if (isMoraMessage(msgFormat)) {
            msgFormat = msgFormat.trim();
            builder.append(msgFormat).append(": ").append(buildMoraLogMessage(msgFormat, args));
        } else {
            builder.append(msgPrefix).append(": ").append(buildDelimitedMessage(msgFormat, args));
        }

        return builder.toString();
    }

    private String buildMessage(MoraLoggerThreadInfo threadInfo, String msgPrefix, String msgFormat, Object... args)
    {
        StringBuilder builder = new StringBuilder()
                .append('[')
                .append(formatLineThreadInfo(threadInfo))
                .append("] [")
                .append(formatLineInfo(threadInfo.getThreadStackTrace())).append("] ");

        if (null == msgFormat || !"".equals(msgFormat.trim())) {
            builder.append("<Empty message>");
        } else if (isMoraMessage(msgFormat)) {
            msgFormat = msgFormat.trim();
            builder.append(msgFormat).append(": ").append(buildMoraLogMessage(msgFormat, args));
        } else {
            builder.append(msgPrefix).append(": ").append(buildDelimitedMessage(msgFormat, args));
        }

        return builder.toString();
    }

    private StringBuilder buildDelimitedMessage(String msgFormat,  Object[] args )
    {
        StringBuilder builder = new StringBuilder();

        int start = 0;
        int delimIdx = msgFormat.indexOf(DELIM_STR);
        if (-1 == delimIdx) {
            builder.append(msgFormat);
            for (Object obj : args) {
                builder.append(' ').append(obj);
            }

            return builder;
        } else if (null == args) {
            builder.append(msgFormat);
            return builder;
        }

        for (Object obj : args) {
            builder.append(msgFormat.substring(start, delimIdx));
            if ((null != obj) && (obj.getClass().isArray())) {
                // .. object array
                if (obj.getClass().getName().startsWith("[L")) {
                    appendArray(builder, (Object[]) obj);
                } else {
                    builder.append("<Primitive Array: ").append(obj).append('>');
                }
            } else {
                builder.append(obj);
            }

            start = delimIdx + 2;
            if (msgFormat.length() <= start) {
                break;
            }

            delimIdx = msgFormat.indexOf(DELIM_STR, start);
            if (-1 == delimIdx) {
                break;
            }
        }

        // .. remaining portion of format string
        builder.append(msgFormat.substring(start));

        return builder;
    }

    private void appendArray(StringBuilder builder, Object[] arr )
    {
        builder.append('[');
        boolean first = true;
        for (Object obj : arr) {
            if (first) {
                first = false;
            } else {
                builder.append(',');
            }

            builder.append(obj);
        }
        builder.append(']');
    }

    private StringBuilder buildMoraLogMessage(String msgFormat, Object[] args )
    {
        StringBuilder builder = new StringBuilder();

        String val = getProp().getProperty(msgFormat);
        if (null == val || !"".equals(val.trim())) {
            for (Object obj : args) {
                builder.append(' ').append(obj);
            }
        } else {
            try {
                builder.append(MessageFormat.format(val, args));
            } catch (IllegalArgumentException e) {
                BASE_LOGGER.error(formatMsg(Thread.currentThread().getStackTrace(), ExceptionUtils.getStackTrace(e)));
                builder.append(msgFormat);
            }
        }

        return builder;
    }

    public boolean isLoggable(Level level)
    {
        boolean value = true;
        switch (level.intLevel()) {
            case 100:
                value = this.logger.isEnabled(Level.FATAL);
                break; // FATAL
            case 200:
                value = this.logger.isEnabled(Level.ERROR);
                break; // ERROR
            case 300:
                value = this.logger.isEnabled(Level.WARN);
                break; // WARN
            case 400:
                value = this.logger.isEnabled(Level.INFO);
                break; // INFO
            case 500:
                value = this.logger.isEnabled(Level.DEBUG);
                break; // DEBUG
            case 600:
                value = this.logger.isEnabled(Level.TRACE);
                break; // TRACE
        }
        return value;
    }

    private boolean isMoraMessage(
            String msgFormat )
    {
        if (msgFormat.length() < 5) {
            return false;
        }
        return ('-' == msgFormat.charAt(4))
                && (msgFormat.matches("(DEBG-|INFO-|WARN-|ERRO-|AUDI-).*"));
    }

    public boolean isDebugEnabled()
    {
        return this.logger.isDebugEnabled();
    }

    private String formatMsg(String methodName, String msgId, String message)
    {
        StringBuilder sb = new StringBuilder("[");
        sb.append(methodName);
        sb.append("] ");
        sb.append(msgId);
        sb.append(": ");
        sb.append(message);
        return sb.toString();
    }

    private String formatMsg(StackTraceElement[] stackTrace, String msgId, String message)
    {
        StringBuilder sb = new StringBuilder("[");
        sb.append(formatLineInfo(stackTrace));
        sb.append("] ");
        sb.append(msgId);
        sb.append(": ");
        sb.append(message);
        return sb.toString();
    }

    private String formatMsg(StackTraceElement[] stackTrace, String message)
    {
        StringBuilder sb = new StringBuilder("[");
        sb.append(formatLineInfo(stackTrace));
        sb.append("] ");
        sb.append(": ");
        sb.append(message);
        return sb.toString();
    }

    private String formatMsg(MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        StringBuilder sb = new StringBuilder("[");
        sb.append(formatLineThreadInfo(threadInfo));
        sb.append("] [");
        sb.append(formatLineInfo(threadInfo.getThreadStackTrace()));
        sb.append("] ");
        sb.append(msgId);
        sb.append(": ");
        sb.append(message);
        return sb.toString();
    }

    private String formatMsg(MoraLoggerThreadInfo threadInfo, String message)
    {
        StringBuilder sb = new StringBuilder("[");
        sb.append(formatLineThreadInfo(threadInfo));
        sb.append("] [");
        sb.append(formatLineInfo(threadInfo.getThreadStackTrace()));
        sb.append("] ");
        sb.append(": ");
        sb.append(message);
        return sb.toString();
    }

    private String formatLineInfo(StackTraceElement[] stackTrace){
        StringBuilder sb = new StringBuilder(" | Class - ");
        sb.append(stackTrace[1].getClassName());
        sb.append(" | Method - ");
        sb.append(stackTrace[1].getMethodName());
        sb.append(" | Line - ");
        sb.append(stackTrace[1].getLineNumber());
        return sb.toString();
    }

    private String formatLineThreadInfo(MoraLoggerThreadInfo threadInfo){
        StringBuilder sb = new StringBuilder("T_ID - ");
        sb.append(threadInfo.getThreadID()+"");
        sb.append(" | T_NAME - ");
        sb.append(threadInfo.getThreadName());
        return sb.toString();
    }

    private Properties getProp()
    {
        Properties messageProperties = getAllPropertiesFromResource(MESSAGE_RESOURCE);
        return messageProperties;
    }

    public Logger getLogger()
    {
        return logger;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * Read Given property form given property file in project resource
     *
     * @param propertyFileName as String Object with file name of property file
     * @return String Object will return with requested property or null
     * @throws IOException with file notfound or compatibility issue
     * @apiNote Read property file and fetch requested property value in to one String Object
     * @Note Files.lines() method doesn't include line-termination character. If we want to read all text from a file
     * in to a String we can use this
     */
    private Properties getAllPropertiesFromResource(String propertyFileName){
        BASE_LOGGER.debug("Properties from file {}", propertyFileName);
        Properties properties = new Properties();
        try(InputStream iStream = getClass().getClassLoader().getResourceAsStream(propertyFileName)){
            properties.load(iStream);
        } catch (IOException e) {
            BASE_LOGGER.error(formatMsg(Thread.currentThread().getStackTrace(), ExceptionUtils.getStackTrace(e)));
        }finally {
            return properties;
        }
    }
}
