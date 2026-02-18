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
 * The {@code MoraLogger} is a wrapper utility class over Log4j2 to provide
 * structured and enhanced logging for enterprise applications.
 * <p>
 * It supports dynamic message formatting, property-based message resolution,
 * and consistent message prefixing with method name and message ID.
 * </p>
 *
 * <h4>Key Features</h4>
 * <ul>
 *     <li>Centralized logger management using class or string identifiers</li>
 *     <li>Formatted and localized logging based on resource bundles</li>
 *     <li>Support for all log levels with optional and throwable-aware logging</li>
 *     <li>Marker-aware structured log message integration</li>
 * </ul>
 * <h4>Codes</h4>
 * 1 - {@link }<br>
 * <h4>Methods</h4>
 * <ul>
 *     <li>{@link MoraLogger#getLogger(Class)}</li>
 * </ul>
 * <p>
 * <h4>Notes</h4>
 * <ul>
 *     <li>....</li>
 * </ul>
 *
 * @author: SLMORA
 * @since 1.0
 *
 * <h4>Revision History</h4>
 * <blockquote><pre>
 * <br>Version      Date            Editor              Note
 * <br>-------------------------------------------------------
 * <br>1.0          1/21/2025      SLMORA                Initial Code
 * <br>2.0          1/22/2025       SLMORA              Update code with mora-common-logging-003-20250114001
 * </pre></blockquote>
 */
public class MoraLogger
{
    private final Logger BASE_LOGGER = LogManager.getLogger(MoraLogger.class);
    private static final String DELIM_STR = "{}";
    private static final String MESSAGE_RESOURCE = "messageResources.properties";
    private static ConcurrentMap<String, MoraLogger> LOGGER_CACHE = new ConcurrentHashMap<>();
    private Properties propCache;
    private Logger logger = null;

    /**
     * <h3>Get logger by class</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Resolves a simple class name and uses it as a cache key</li>
     *   <li>Creates a MoraLogger if not already cached</li>
     *   <li>Uses a thread-safe cache to avoid duplicate instances</li>
     *   <li>Side effect: may populate the shared logger cache</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * MoraLogger log = MoraLogger.getLogger(MyService.class);
     * }</pre>
     *
     * @param clazz   target class for the logger
     *
     * @return MoraLogger instance for the class
     *
     * @implNote Uses a ConcurrentMap with putIfAbsent for atomic cache insert.
     *
     * @apiNote Intended for general use when a class reference is available.
     *
     * @since 1.0
     *
     * @see MoraLogger#getLogger(String)
     */
    public static MoraLogger getLogger(Class<?> clazz)
    {
        String className = getSimpleName(clazz.getName());

        MoraLogger logger = LOGGER_CACHE.get(className);

        if (logger == null) {
            MoraLogger newLogger = new MoraLogger(clazz);
            logger = LOGGER_CACHE.putIfAbsent(className, newLogger);
            if (logger == null) {
                logger = newLogger;
            }
        }

        return logger;
    }

    /**
     * <h3>Get logger by class name</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Normalizes the class name to its simple form</li>
     *   <li>Looks up a cached MoraLogger by that name</li>
     *   <li>Creates a new instance if missing</li>
     *   <li>Side effect: may populate the shared logger cache</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * MoraLogger log = MoraLogger.getLogger("com.acme.MyService");
     * }</pre>
     *
     * @param className   fully qualified class name
     *
     * @return MoraLogger instance for the class name
     *
     * @implNote Uses the simple class name to keep cache keys consistent.
     *
     * @apiNote Intended for use when only a class name string is available.
     *
     * @since 1.0
     *
     * @see MoraLogger#getLogger(Class)
     */
    public static MoraLogger getLogger(String className)
    {
        className = getSimpleName(className);

        MoraLogger logger = LOGGER_CACHE.get(className);

        if (logger == null) {
            MoraLogger newLogger = new MoraLogger(className);
            logger = LOGGER_CACHE.putIfAbsent(className, newLogger);
            if (logger == null) {
                logger = newLogger;
            }
        }

        return logger;
    }

    /**
     * <h3>Get simple class name</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Finds the last dot in a fully qualified name</li>
     *   <li>Returns the substring after the last dot</li>
     *   <li>Falls back to the original string if no dot exists</li>
     *   <li>No side effects</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String simple = MoraLogger.getSimpleName("a.b.C");
     * }</pre>
     *
     * @param className   full class name
     *
     * @return simple class name
     *
     * @implNote Uses lastIndexOf('.') for minimal overhead.
     *
     * @apiNote Internal helper for logger cache key normalization.
     *
     * @since 1.0
     *
     * @see MoraLogger#getLogger(Class)
     */
    private static String getSimpleName(String className) {
        int lastDot = className.lastIndexOf('.');
        return (lastDot >= 0) ? className.substring(lastDot + 1) : className;
    }

    /**
     * <h3>Create logger with class</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Initializes the Log4j logger using a class reference</li>
     *   <li>Used internally by the static factory methods</li>
     *   <li>No cache update happens here directly</li>
     *   <li>Side effect: allocates a Log4j logger instance</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * MoraLogger log = new MoraLogger(MyService.class);
     * }</pre>
     *
     * @param clazz   class to associate with the logger
     *
     * @implNote Delegates to LogManager.getLogger(clazz).
     *
     * @apiNote Constructor is public but typically accessed via getLogger.
     *
     * @since 1.0
     *
     * @see MoraLogger#getLogger(Class)
     */
    public MoraLogger(Class<?> clazz)
    {
        this.logger = LogManager.getLogger(clazz);
    }

    /**
     * <h3>Create logger with class name</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Initializes the Log4j logger using a class name</li>
     *   <li>Used internally by the static factory methods</li>
     *   <li>No cache update happens here directly</li>
     *   <li>Side effect: allocates a Log4j logger instance</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * MoraLogger log = new MoraLogger("com.acme.MyService");
     * }</pre>
     *
     * @param clazz   fully qualified class name
     *
     * @implNote Delegates to LogManager.getLogger(clazz).
     *
     * @apiNote Constructor is public but typically accessed via getLogger.
     *
     * @since 1.0
     *
     * @see MoraLogger#getLogger(String)
     */
    public MoraLogger(String clazz)
    {
        this.logger = LogManager.getLogger(clazz);
    }


    //FATAL - 100
    /**
     * <h3>Log fatal message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs a plain string at FATAL level</li>
     *   <li>Use for unrecoverable failures</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.fatal("Unrecoverable error");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Directly calls Logger.fatal(String).
     *
     * @apiNote Use when no additional context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(String, String, String)
     */
    public void fatal(String message)
    {
        this.logger.fatal(message);
    }
    /**
     * <h3>Log fatal throwable</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts a Throwable to a stack trace string</li>
     *   <li>Logs the stack trace at FATAL level</li>
     *   <li>Use when an exception terminates a flow</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.fatal(ex);
     * }</pre>
     *
     * @param e   throwable to log
     *
     * @implNote Uses ExceptionUtils.getStackTrace(e).
     *
     * @apiNote Prefer this overload for full stack trace output.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(String)
     */
    public void fatal(Throwable e)
    {
        fatal(ExceptionUtils.getStackTrace(e));
    }
    /**
     * <h3>Log fatal with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and message id</li>
     *   <li>Use when caller has an explicit method name</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.fatal("doWork", "ERRO-00001", "Failed");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to formatMsg and fatal(String).
     *
     * @apiNote Use when you want explicit method labeling.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(StackTraceElement[], String, String)
     */
    public void fatal(String methodName, String msgId, String message )
    {
        fatal(formatMsg(methodName, msgId, message));
    }
    /**
     * <h3>Log fatal with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and message id</li>
     *   <li>Use when a stack trace is already available</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.fatal(Thread.currentThread().getStackTrace(), "ERRO-00001", "Failed");
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to formatMsg and fatal(String).
     *
     * @apiNote Useful for framework-level logging utilities.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(MoraLoggerThreadInfo, String, String)
     */
    public void fatal(StackTraceElement[] stackTrace, String msgId, String message )
    {
        fatal(formatMsg(stackTrace, msgId, message));
    }
    /**
     * <h3>Log fatal with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and message id</li>
     *   <li>Includes stack trace from the thread info</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.fatal(threadInfo, "ERRO-00001", "Failed");
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to formatMsg and fatal(String).
     *
     * @apiNote Use when thread context is important.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(StackTraceElement[], String, String)
     */
    public void fatal(MoraLoggerThreadInfo threadInfo, String msgId, String message )
    {
        fatal(formatMsg(threadInfo, msgId, message));
    }


    //ERROR - 200
    /**
     * <h3>Log severe message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Alias for error(String)</li>
     *   <li>Use when a severe error occurs</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.severe("Operation failed");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Delegates directly to error(String).
     *
     * @apiNote Provided for API parity with other frameworks.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(String)
     */
    public void severe(String message)
    {
        error(message);
    }
    /**
     * <h3>Log severe throwable</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Alias for error(Throwable)</li>
     *   <li>Logs full stack trace</li>
     *   <li>Use for unrecoverable exceptions</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.severe(ex);
     * }</pre>
     *
     * @param e   throwable to log
     *
     * @implNote Delegates to error(Throwable).
     *
     * @apiNote Provided for compatibility with alternate naming.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(Throwable)
     */
    public void severe(Throwable e)
    {
        error(e);
    }
    /**
     * <h3>Log severe with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats with method name and message id</li>
     *   <li>Alias for error formatting at ERROR level</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.severe("doWork", "ERRO-00002", "Failed");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to error(String) after formatting.
     *
     * @apiNote Use for alternate naming with ERROR semantics.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(String, String, String)
     */
    public void severe(String methodName, String msgId, String message )
    {
        error(formatMsg(methodName, msgId, message));
    }
    /**
     * <h3>Log severe with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats with stack trace info and message id</li>
     *   <li>Alias for error formatting at ERROR level</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.severe(Thread.currentThread().getStackTrace(), "ERRO-00002", "Failed");
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to error(String) after formatting.
     *
     * @apiNote Use for alternate naming with ERROR semantics.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(StackTraceElement[], String, String)
     */
    public void severe(StackTraceElement[] stackTrace, String msgId, String message )
    {
        error(formatMsg(stackTrace, msgId, message));
    }
    /**
     * <h3>Log severe with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats with thread info and message id</li>
     *   <li>Alias for error formatting at ERROR level</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.severe(threadInfo, "ERRO-00002", "Failed");
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to error(String) after formatting.
     *
     * @apiNote Use for alternate naming with ERROR semantics.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(MoraLoggerThreadInfo, String, String)
     */
    public void severe(MoraLoggerThreadInfo threadInfo, String msgId, String message )
    {
        error(formatMsg(threadInfo, msgId, message));
    }


    /**
     * <h3>Log error message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs a plain string at ERROR level</li>
     *   <li>Use when message is already formatted</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error("Operation failed");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Directly calls Logger.error(String).
     *
     * @apiNote Use when no additional context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(String, String, String)
     */
    public void error(String message)
    {
        this.logger.error(message);
    }
    /**
     * <h3>Log error throwable</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts a Throwable to a stack trace string</li>
     *   <li>Logs the stack trace at ERROR level</li>
     *   <li>Use when an exception occurs</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(ex);
     * }</pre>
     *
     * @param e   throwable to log
     *
     * @implNote Uses ExceptionUtils.getStackTrace(e).
     *
     * @apiNote Prefer this overload for full stack trace output.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(String)
     */
    public void error(Throwable e)
    {
        error(ExceptionUtils.getStackTrace(e));
    }
    /**
     * <h3>Log error with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and a default error code</li>
     *   <li>Uses Throwable stack trace as the message body</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Optional<String> msg = log.error("doWork", ex);
     * }</pre>
     *
     * @param methodName   method name to include
     * @param cause   throwable to log
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Builds a message with code ERRO-00000.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#error(String)
     */
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
    /**
     * <h3>Log error with method and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and default error code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error("doWork", "Failed id {}", id);
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with ERRO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(String, String, String, Object...)
     */
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
    /**
     * <h3>Log error with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and default error code</li>
     *   <li>Uses Throwable stack trace as the message body</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(Thread.currentThread().getStackTrace(), ex);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param cause   throwable to log
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with ERRO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(StackTraceElement[], String, String, Object...)
     */
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
    /**
     * <h3>Log error with stack trace and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and default error code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(Thread.currentThread().getStackTrace(), "Failed {}", id);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with ERRO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(StackTraceElement[], String, String, Object...)
     */
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
    /**
     * <h3>Log error with stack trace and explicit prefix</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and custom prefix</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(trace, "ERRO-00042", "Failed {}", id);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgPrefix   message id prefix
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with the provided prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(StackTraceElement[], String, String, Object...)
     */
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
    /**
     * <h3>Log error with thread context and throwable</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and default error code</li>
     *   <li>Uses Throwable stack trace as the message body</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(threadInfo, ex);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param cause   throwable to log
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with ERRO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(MoraLoggerThreadInfo, String, String, Object...)
     */
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
    /**
     * <h3>Log error with thread context and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and default error code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(threadInfo, "Failed {}", id);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with ERRO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(MoraLoggerThreadInfo, String, String, Object...)
     */
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
    /**
     * <h3>Log error with thread context and explicit prefix</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and custom prefix</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.error(threadInfo, "ERRO-00042", "Failed {}", id);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgPrefix   message id prefix
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with the provided prefix.
     *
     * @apiNote Returns Optional.empty() when ERROR is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(MoraLoggerThreadInfo, String, String, Object...)
     */
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
    /**
     * <h3>Log debug message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs a plain string at DEBUG level</li>
     *   <li>Use for verbose or diagnostic output</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.debug("Starting job");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Directly calls Logger.debug(String).
     *
     * @apiNote Use when no additional context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#debug(String, String, Object...)
     */
    public void debug(String message)
    {
        this.logger.debug(message);
    }
    /**
     * <h3>Log debug with method and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and default debug code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.debug("doWork", "Value {}", value);
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with DEBG-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when DEBUG is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(String, String, String, Object...)
     */
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
    /**
     * <h3>Log debug with stack trace and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and default debug code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.debug(Thread.currentThread().getStackTrace(), "Value {}", value);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with DEBG-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when DEBUG is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(StackTraceElement[], String, String, Object...)
     */
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
    /**
     * <h3>Log debug with thread context and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and default debug code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.debug(threadInfo, "Value {}", value);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with DEBG-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when DEBUG is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(MoraLoggerThreadInfo, String, String, Object...)
     */
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
    /**
     * <h3>Log detail message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Alias for debug(String)</li>
     *   <li>Use for verbose or diagnostic output</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.detail("Starting job");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Delegates to debug(String).
     *
     * @apiNote Provided as an alias for DEBUG-level logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#debug(String)
     */
    public void detail(String message)
    {
        debug(message);
    }
    /**
     * <h3>Log detail with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and message id</li>
     *   <li>Alias for debug formatting at DEBUG level</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.detail("doWork", "DEBG-00001", "Started");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to debug(String) after formatting.
     *
     * @apiNote Provided as an alias for DEBUG-level logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#debug(String)
     */
    public void detail(String methodName, String msgId, String message)
    {
        debug(formatMsg(methodName, msgId, message));
    }
    /**
     * <h3>Log detail with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and message id</li>
     *   <li>Alias for debug formatting at DEBUG level</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.detail(Thread.currentThread().getStackTrace(), "DEBG-00001", "Started");
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to debug(String) after formatting.
     *
     * @apiNote Provided as an alias for DEBUG-level logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#debug(String)
     */
    public void detail(StackTraceElement[] stackTrace, String msgId, String message)
    {
        debug(formatMsg(stackTrace, msgId, message));
    }
    /**
     * <h3>Log detail with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and message id</li>
     *   <li>Alias for debug formatting at DEBUG level</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.detail(threadInfo, "DEBG-00001", "Started");
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to debug(String) after formatting.
     *
     * @apiNote Provided as an alias for DEBUG-level logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#debug(String)
     */
    public void detail(MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        debug(formatMsg(threadInfo, msgId, message));
    }


    //INFO - 400
    /**
     * <h3>Log info message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs a plain string at INFO level</li>
     *   <li>Use for normal operational messages</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info("Service started");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Directly calls Logger.info(String).
     *
     * @apiNote Use when no additional context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#info(String, String, Object...)
     */
    public void info(String message)
    {
        this.logger.info(message);
    }
    /**
     * <h3>Log info with method and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and default info code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info("doWork", "Processed {}", count);
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with INFO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when INFO is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(String, String, String, Object...)
     */
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
    /**
     * <h3>Log info with stack trace and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and default info code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info(Thread.currentThread().getStackTrace(), "Processed {}", count);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with INFO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when INFO is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(StackTraceElement[], String, String, Object...)
     */
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
    /**
     * <h3>Log info with thread context and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and default info code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info(threadInfo, "Processed {}", count);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with INFO-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when INFO is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(MoraLoggerThreadInfo, String, String, Object...)
     */
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
    /**
     * <h3>Log entering message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Uses INFO level with an empty message id</li>
     *   <li>Convenience overload for method entry logging</li>
     *   <li>Delegates to entering(String, String, String)</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.entering("doWork", "start");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param message   entry message
     *
     * @implNote Delegates to entering(methodName, "", message).
     *
     * @apiNote Use to standardize entry logs without a message id.
     *
     * @since 1.0
     *
     * @see MoraLogger#entering(String, String, String)
     */
    public void entering(String methodName, String message)
    {
        entering(methodName, "", message);
    }
    /**
     * <h3>Log entering message with id</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs an INFO message with method name and id</li>
     *   <li>Convenience overload for method entry logging</li>
     *   <li>Delegates to info(String, String, Object...)</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.entering("doWork", "INFO-00001", "start");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   entry message
     *
     * @implNote Delegates to info(methodName, msgId, message).
     *
     * @apiNote Use to standardize entry logs with a message id.
     *
     * @since 1.0
     *
     * @see MoraLogger#info(String, String, Object...)
     */
    public void entering(String methodName, String msgId, String message)
    {
        info(methodName, msgId, message);
    }
    /**
     * <h3>Log audit message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats an INFO message with method name and message id</li>
     *   <li>Intended for audit-style events</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.audit("doWork", "AUDI-00001", "user action");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   audit message
     *
     * @implNote Delegates to formatMsg and info(String).
     *
     * @apiNote Use for audit trails at INFO level.
     *
     * @since 1.0
     *
     * @see MoraLogger#info(String)
     */
    public void audit(String methodName, String msgId, String message)
    {
        info(formatMsg(methodName, msgId, message));
    }
    /**
     * <h3>Log info with marker</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs an INFO message with a Marker</li>
     *   <li>Use when marker-based routing is configured</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info(marker, "audit event");
     * }</pre>
     *
     * @param marker   Log4j marker
     * @param message   log message
     *
     * @implNote Delegates to Logger.info(Marker, String).
     *
     * @apiNote Use when marker-based appenders are configured.
     *
     * @since 1.0
     *
     * @see Logger#info(Marker, String)
     */
    public void info(Marker marker, String message)
    {
        this.logger.info(marker, message);
    }

    /**
     * <h3>Log info with marker and stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message using stack trace info</li>
     *   <li>Logs at INFO level with a Marker</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info(marker, Thread.currentThread().getStackTrace(), "audit event");
     * }</pre>
     *
     * @param marker   Log4j marker
     * @param stackTrace   stack trace for context
     * @param message   log message
     *
     * @implNote Delegates to Logger.info(Marker, String) with formatted text.
     *
     * @apiNote Use when marker-based appenders are configured.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatMsg(StackTraceElement[], String)
     */
    public void info(Marker marker, StackTraceElement[] stackTrace, String message)
    {
        this.logger.info(marker, formatMsg(stackTrace, message));
    }
    /**
     * <h3>Log info with marker and thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message using thread info and stack trace</li>
     *   <li>Logs at INFO level with a Marker</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.info(marker, threadInfo, "audit event");
     * }</pre>
     *
     * @param marker   Log4j marker
     * @param threadInfo   thread info container
     * @param message   log message
     *
     * @implNote Delegates to Logger.info(Marker, String) with formatted text.
     *
     * @apiNote Use when marker-based appenders are configured.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatMsg(MoraLoggerThreadInfo, String)
     */
    public void info(Marker marker, MoraLoggerThreadInfo threadInfo, String message)
    {
        this.logger.info(marker, formatMsg(threadInfo, message));
    }


    //WARN - 300
    /**
     * <h3>Log warn message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs a plain string at WARN level</li>
     *   <li>Use for recoverable problems or warnings</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.warn("Retrying request");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Directly calls Logger.warn(String).
     *
     * @apiNote Use when no additional context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#warn(String, String, Object...)
     */
    public void warn(String message)
    {
        this.logger.warn(message);
    }
    /**
     * <h3>Log warning message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Alias for warn(String)</li>
     *   <li>Use for recoverable problems or warnings</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.warning("Retrying request");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Delegates to warn(String).
     *
     * @apiNote Provided for API parity with other frameworks.
     *
     * @since 1.0
     *
     * @see MoraLogger#warn(String)
     */
    public void warning(String message)
    {
        warn(message);
    }
    /**
     * <h3>Log warn with method and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and default warn code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.warn("doWork", "Low disk {}", percent);
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with WARN-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when WARN is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(String, String, String, Object...)
     */
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
    /**
     * <h3>Log warn with stack trace and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and default warn code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.warn(Thread.currentThread().getStackTrace(), "Low disk {}", percent);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with WARN-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when WARN is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(StackTraceElement[], String, String, Object...)
     */
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
    /**
     * <h3>Log warn with thread context and format</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and default warn code</li>
     *   <li>Supports {}-style arguments and Mora message keys</li>
     *   <li>Returns the formatted message if logging is enabled</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.warn(threadInfo, "Low disk {}", percent);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message if logged, otherwise empty
     *
     * @implNote Uses buildMessage with WARN-00000 prefix.
     *
     * @apiNote Returns Optional.empty() when WARN is disabled.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMessage(MoraLoggerThreadInfo, String, String, Object...)
     */
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
    /**
     * <h3>Log trace message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Logs a plain string at TRACE level</li>
     *   <li>Use for very fine-grained diagnostics</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.trace("Entering loop");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Directly calls Logger.trace(String).
     *
     * @apiNote Use when no additional context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#trace(String, String, String)
     */
    public void trace(String message)
    {
        this.logger.trace(message);
    }
    /**
     * <h3>Log trace with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with method name and message id</li>
     *   <li>Use when trace needs explicit method labeling</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.trace("doWork", "TRAC-00001", "looping");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to formatMsg and trace(String).
     *
     * @apiNote Use when explicit method labeling is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#trace(StackTraceElement[], String, String)
     */
    public void trace(String methodName, String msgId, String message)
    {
        trace(formatMsg(methodName, msgId, message));
    }
    /**
     * <h3>Log trace with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with stack trace info and message id</li>
     *   <li>Use when trace needs stack context</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.trace(Thread.currentThread().getStackTrace(), "TRAC-00001", "looping");
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to formatMsg and trace(String).
     *
     * @apiNote Use when stack context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#trace(MoraLoggerThreadInfo, String, String)
     */
    public void trace(StackTraceElement[] stackTrace, String msgId, String message)
    {
        trace(formatMsg(stackTrace, msgId, message));
    }
    /**
     * <h3>Log trace with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats message with thread info and message id</li>
     *   <li>Includes stack trace from thread info</li>
     *   <li>No template lookup performed</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.trace(threadInfo, "TRAC-00001", "looping");
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to formatMsg and trace(String).
     *
     * @apiNote Use when thread context is required.
     *
     * @since 1.0
     *
     * @see MoraLogger#trace(String, String, String)
     */
    public void trace(MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        trace(formatMsg(threadInfo, msgId, message));
    }
    /**
     * <h3>Log fine message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Alias for trace(String)</li>
     *   <li>Use for very fine-grained diagnostics</li>
     *   <li>No template lookup or context formatting</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.fine("Entering loop");
     * }</pre>
     *
     * @param message   log message
     *
     * @implNote Delegates to trace(String).
     *
     * @apiNote Provided for API parity with other frameworks.
     *
     * @since 1.0
     *
     * @see MoraLogger#trace(String)
     */
    public void fine(String message)
    {
        trace(message);
    }

    //LOG
    /**
     * <h3>Log at level with exception and method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts Exception to stack trace string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided method name and empty message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.ERROR, "doWork", ex);
     * }</pre>
     *
     * @param level   log level
     * @param methodName   method name to include
     * @param exception   exception to log
     *
     * @implNote Delegates to logWithLevel(level, methodName, "", stackTrace).
     *
     * @apiNote This is a convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, String, String, String)
     */
    public void log(Level level, String methodName, Exception exception)
    {
        logWithLevel(level, methodName, "", ExceptionUtils.getStackTrace(exception));
    }
    /**
     * <h3>Log at level with exception and stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts Exception to stack trace string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided stack trace and empty message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.ERROR, Thread.currentThread().getStackTrace(), ex);
     * }</pre>
     *
     * @param level   log level
     * @param stackTrace   stack trace for context
     * @param exception   exception to log
     *
     * @implNote Delegates to logWithLevel(level, stackTrace, "", stackTrace).
     *
     * @apiNote This is a convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, StackTraceElement[], String, String)
     */
    public void log(Level level, StackTraceElement[] stackTrace, Exception exception)
    {
        logWithLevel(level, stackTrace, "", ExceptionUtils.getStackTrace(exception));
    }
    /**
     * <h3>Log at level with exception and thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts Exception to stack trace string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided thread info and empty message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.ERROR, threadInfo, ex);
     * }</pre>
     *
     * @param level   log level
     * @param threadInfo   thread info container
     * @param exception   exception to log
     *
     * @implNote Delegates to logWithLevel(level, threadInfo, "", stackTrace).
     *
     * @apiNote This is a convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, MoraLoggerThreadInfo, String, String)
     */
    public void log(Level level, MoraLoggerThreadInfo threadInfo, Exception exception)
    {
        logWithLevel(level, threadInfo, "", ExceptionUtils.getStackTrace(exception));
    }
    /**
     * <h3>Log at level with message and method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided method name and empty message id</li>
     *   <li>Message is passed as-is</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.INFO, "doWork", "started");
     * }</pre>
     *
     * @param level   log level
     * @param methodName   method name to include
     * @param message   log message
     *
     * @implNote Delegates to logWithLevel(level, methodName, "", message).
     *
     * @apiNote Convenience overload for level-based logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, String, String, String)
     */
    public void log(Level level, String methodName, String message)
    {
        logWithLevel(level, methodName, "", message);
    }
    /**
     * <h3>Log at level with message and stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided stack trace and empty message id</li>
     *   <li>Message is passed as-is</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.INFO, Thread.currentThread().getStackTrace(), "started");
     * }</pre>
     *
     * @param level   log level
     * @param stackTrace   stack trace for context
     * @param message   log message
     *
     * @implNote Delegates to logWithLevel(level, stackTrace, "", message).
     *
     * @apiNote Convenience overload for level-based logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, StackTraceElement[], String, String)
     */
    public void log(Level level, StackTraceElement[] stackTrace, String message)
    {
        logWithLevel(level, stackTrace, "", message);
    }
    /**
     * <h3>Log at level with thread context and message id</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided thread info and message id</li>
     *   <li>Message is passed as-is</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.INFO, threadInfo, "INFO-00001", "started");
     * }</pre>
     *
     * @param level   log level
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Delegates to logWithLevel(level, threadInfo, msgId, message).
     *
     * @apiNote Convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, MoraLoggerThreadInfo, String, String)
     */
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String msgId, String message)
    {
        logWithLevel(level, threadInfo, msgId, message);
    }
    /**
     * <h3>Log at level with method context and object message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts message object to string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided method name and message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.INFO, "doWork", "INFO-00001", obj);
     * }</pre>
     *
     * @param level   log level
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   message object
     *
     * @implNote Calls toString() on the message object.
     *
     * @apiNote Convenience overload for object messages.
     *
     * @since 1.0
     *
     * @see MoraLogger#log(Level, String, String, String)
     */
    public void log(Level level, String methodName, String msgId, Object message)
    {
        logWithLevel(level, methodName, msgId, message.toString());
    }
    /**
     * <h3>Log at level with stack trace context and object message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts message object to string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided stack trace and message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.INFO, trace, "INFO-00001", obj);
     * }</pre>
     *
     * @param level   log level
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   message object
     *
     * @implNote Calls toString() on the message object.
     *
     * @apiNote Convenience overload for object messages.
     *
     * @since 1.0
     *
     * @see MoraLogger#log(Level, StackTraceElement[], String, String)
     */
    public void log(Level level, StackTraceElement[] stackTrace, String msgId, Object message)
    {
        logWithLevel(level, stackTrace, msgId, message.toString());
    }
    /**
     * <h3>Log at level with thread context and object message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts message object to string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided thread info and message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.INFO, threadInfo, "INFO-00001", obj);
     * }</pre>
     *
     * @param level   log level
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   message object
     *
     * @implNote Calls toString() on the message object.
     *
     * @apiNote Convenience overload for object messages.
     *
     * @since 1.0
     *
     * @see MoraLogger#log(Level, MoraLoggerThreadInfo, String, String)
     */
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String msgId, Object message)
    {
        logWithLevel(level, threadInfo, msgId, message.toString());
    }
    /**
     * <h3>Log at level with exception and message id</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts Exception to stack trace string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided method name and message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.ERROR, "doWork", "ERRO-00001", ex);
     * }</pre>
     *
     * @param level   log level
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param exception   exception to log
     *
     * @implNote Delegates to logWithLevel(level, methodName, msgId, stackTrace).
     *
     * @apiNote Convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, String, String, String)
     */
    public void log(Level level, String methodName, String msgId, Exception exception)
    {
        logWithLevel(level, methodName, msgId, ExceptionUtils.getStackTrace(exception));
    }
    /**
     * <h3>Log at level with exception and stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts Exception to stack trace string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided stack trace and message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.ERROR, trace, "ERRO-00001", ex);
     * }</pre>
     *
     * @param level   log level
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param exception   exception to log
     *
     * @implNote Delegates to logWithLevel(level, stackTrace, msgId, stackTrace).
     *
     * @apiNote Convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, StackTraceElement[], String, String)
     */
    public void log(Level level, StackTraceElement[] stackTrace, String msgId, Exception exception)
    {
        logWithLevel(level, stackTrace, msgId, ExceptionUtils.getStackTrace(exception));
    }
    /**
     * <h3>Log at level with exception and thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Converts Exception to stack trace string</li>
     *   <li>Dispatches to a level-specific log method</li>
     *   <li>Uses provided thread info and message id</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.log(Level.ERROR, threadInfo, "ERRO-00001", ex);
     * }</pre>
     *
     * @param level   log level
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param exception   exception to log
     *
     * @implNote Delegates to logWithLevel(level, threadInfo, msgId, stackTrace).
     *
     * @apiNote Convenience overload for exception logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#logWithLevel(Level, MoraLoggerThreadInfo, String, String)
     */
    public void log(Level level, MoraLoggerThreadInfo threadInfo, String msgId, Exception exception)
    {
        logWithLevel(level, threadInfo, msgId, ExceptionUtils.getStackTrace(exception));
    }

    /**
     * <h3>Dispatch logging by level with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Maps Level.intLevel() to the matching log method</li>
     *   <li>Uses method name and message id for formatting</li>
     *   <li>Falls through without action for unknown levels</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.logWithLevel(Level.INFO, "doWork", "INFO-00001", "started");
     * }</pre>
     *
     * @param level   log level
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Uses Level.intLevel() switch for routing.
     *
     * @apiNote Central dispatch method for level-based logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(String, String, String)
     */
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

    /**
     * <h3>Dispatch logging by level with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Maps Level.intLevel() to the matching log method</li>
     *   <li>Uses stack trace info and message id for formatting</li>
     *   <li>Falls through without action for unknown levels</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.logWithLevel(Level.INFO, trace, "INFO-00001", "started");
     * }</pre>
     *
     * @param level   log level
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Uses Level.intLevel() switch for routing.
     *
     * @apiNote Central dispatch method for level-based logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(StackTraceElement[], String, String)
     */
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

    /**
     * <h3>Dispatch logging by level with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Maps Level.intLevel() to the matching log method</li>
     *   <li>Uses thread info and message id for formatting</li>
     *   <li>Falls through without action for unknown levels</li>
     *   <li>Side effect: writes to configured appenders</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.logWithLevel(Level.INFO, threadInfo, "INFO-00001", "started");
     * }</pre>
     *
     * @param level   log level
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @implNote Uses Level.intLevel() switch for routing.
     *
     * @apiNote Central dispatch method for level-based logging.
     *
     * @since 1.0
     *
     * @see MoraLogger#fatal(MoraLoggerThreadInfo, String, String)
     */
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


    /**
     * <h3>Build message with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Prefixes the message with method name</li>
     *   <li>Chooses Mora template or {}-delimited formatting</li>
     *   <li>Uses a default prefix when not a Mora key</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = buildMessage("doWork", "INFO-00000", "Value {}", value);
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgPrefix   message id prefix
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message string
     *
     * @implNote Uses isMoraMessage to select formatting mode.
     *
     * @apiNote Internal helper for message composition.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMoraLogMessage(String, Object[])
     */
    private String buildMessage(String methodName, String msgPrefix, String msgFormat, Object... args)
    {
        StringBuilder builder = new StringBuilder().append('[').append(methodName).append("] ");

        if (null == msgFormat || msgFormat.isBlank()) {
            builder.append("<Empty message>");
        } else if (isMoraMessage(msgFormat)) {
            msgFormat = msgFormat.trim();
            builder.append(msgFormat).append(": ").append(buildMoraLogMessage(msgFormat, args));
        } else {
            builder.append(msgPrefix).append(": ").append(buildDelimitedMessage(msgFormat, args));
        }

        return builder.toString();
    }

    /**
     * <h3>Build message with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Prefixes the message with stack trace info</li>
     *   <li>Chooses Mora template or {}-delimited formatting</li>
     *   <li>Uses a default prefix when not a Mora key</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = buildMessage(trace, "INFO-00000", "Value {}", value);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgPrefix   message id prefix
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message string
     *
     * @implNote Uses isMoraMessage to select formatting mode.
     *
     * @apiNote Internal helper for message composition.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildDelimitedMessage(String, Object[])
     */
    private String buildMessage(StackTraceElement[] stackTrace, String msgPrefix, String msgFormat, Object... args)
    {
        StringBuilder builder = new StringBuilder().append('[').append(formatLineInfo(stackTrace)).append("] ");

        if (null == msgFormat || msgFormat.isBlank()) {
            builder.append("<Empty message>");
        } else if (isMoraMessage(msgFormat)) {
            msgFormat = msgFormat.trim();
            builder.append(msgFormat).append(": ").append(buildMoraLogMessage(msgFormat, args));
        } else {
            builder.append(msgPrefix).append(": ").append(buildDelimitedMessage(msgFormat, args));
        }

        return builder.toString();
    }

    /**
     * <h3>Build message with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Prefixes the message with thread info and stack trace info</li>
     *   <li>Chooses Mora template or {}-delimited formatting</li>
     *   <li>Uses a default prefix when not a Mora key</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = buildMessage(threadInfo, "INFO-00000", "Value {}", value);
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgPrefix   message id prefix
     * @param msgFormat   message template or Mora key
     * @param args   formatting arguments
     *
     * @return formatted message string
     *
     * @implNote Uses isMoraMessage to select formatting mode.
     *
     * @apiNote Internal helper for message composition.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildDelimitedMessage(String, Object[])
     */
    private String buildMessage(MoraLoggerThreadInfo threadInfo, String msgPrefix, String msgFormat, Object... args)
    {
        StringBuilder builder = new StringBuilder()
                .append('[')
                .append(formatLineThreadInfo(threadInfo))
                .append("] [")
                .append(formatLineInfo(threadInfo.getThreadStackTrace())).append("] ");

        if (null == msgFormat || msgFormat.isBlank()) {
            builder.append("<Empty message>");
        } else if (isMoraMessage(msgFormat)) {
            msgFormat = msgFormat.trim();
            builder.append(msgFormat).append(": ").append(buildMoraLogMessage(msgFormat, args));
        } else {
            builder.append(msgPrefix).append(": ").append(buildDelimitedMessage(msgFormat, args));
        }

        return builder.toString();
    }

    /**
     * <h3>Build delimited message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Replaces {} placeholders with argument values</li>
     *   <li>Appends remaining arguments if placeholders are fewer</li>
     *   <li>Formats object arrays with a custom helper</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * StringBuilder sb = buildDelimitedMessage("A {}", new Object[]{"B"});
     * }</pre>
     *
     * @param msgFormat   message template with {} placeholders
     * @param args   formatting arguments
     *
     * @return formatted message builder
     *
     * @implNote Uses DELIM_STR to find placeholder positions.
     *
     * @apiNote Internal helper for {}-style formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#appendArray(StringBuilder, Object[])
     */
    private StringBuilder buildDelimitedMessage(String msgFormat,  Object[] args )
    {
        // Format message using {} placeholders, then append any remaining args.
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
                // Handle object arrays explicitly; leave primitives as a marker string.
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

    /**
     * <h3>Append array values</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Formats an object array into a bracketed list</li>
     *   <li>Separates elements with commas</li>
     *   <li>Does not handle primitive arrays</li>
     *   <li>No side effects besides builder mutation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * appendArray(builder, new Object[]{"a", "b"});
     * }</pre>
     *
     * @param builder   target builder
     * @param arr   array of objects
     *
     * @implNote Caller is responsible for null checks.
     *
     * @apiNote Internal helper for array formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildDelimitedMessage(String, Object[])
     */
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

    /**
     * <h3>Build Mora message</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Looks up a template from message resources</li>
     *   <li>Formats with MessageFormat and arguments</li>
     *   <li>Falls back to raw key if formatting fails</li>
     *   <li>Side effect: logs formatting errors to base logger</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * StringBuilder sb = buildMoraLogMessage("INFO-00001", new Object[]{"x"});
     * }</pre>
     *
     * @param msgFormat   Mora message key
     * @param args   formatting arguments
     *
     * @return formatted message builder
     *
     * @implNote Uses MessageFormat and messageResources.properties.
     *
     * @apiNote Internal helper for Mora message keys.
     *
     * @since 1.0
     *
     * @see MoraLogger#getAllPropertiesFromResource(String)
     */
    private StringBuilder buildMoraLogMessage(String msgFormat, Object[] args )
    {
        // Resolve message templates from the resource bundle and format with MessageFormat.
        StringBuilder builder = new StringBuilder();

        String val = getProp().getProperty(msgFormat);
        if (null == val || val.isBlank()) {
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

    /**
     * <h3>Check log level enabled</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Maps intLevel to corresponding Log4j level</li>
     *   <li>Uses Logger.isEnabled for the target level</li>
     *   <li>Returns true for supported levels only</li>
     *   <li>No side effects</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * if (log.isLoggable(Level.DEBUG)) { ... }
     * }</pre>
     *
     * @param level   log level
     *
     * @return true if the level is enabled
     *
     * @implNote Uses Level.intLevel() switch for routing.
     *
     * @apiNote This method is used by Optional-returning overloads.
     *
     * @since 1.0
     *
     * @see Logger#isEnabled(Level)
     */
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

    /**
     * <h3>Check Mora message key</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Validates the MORA prefix pattern (DEBG/INFO/WARN/ERRO/AUDI)</li>
     *   <li>Requires a dash at index 4</li>
     *   <li>Used to decide template lookup vs delimiter formatting</li>
     *   <li>No side effects</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * boolean mora = isMoraMessage("INFO-00001");
     * }</pre>
     *
     * @param msgFormat   message format or key
     *
     * @return true if the string matches Mora message format
     *
     * @implNote Uses regex and index checks for validation.
     *
     * @apiNote Internal helper for message formatting selection.
     *
     * @since 1.0
     *
     * @see MoraLogger#buildMoraLogMessage(String, Object[])
     */
    private boolean isMoraMessage(
            String msgFormat )
    {
        // Detect Mora message keys like INFO-00001, WARN-12345, etc.
        if (msgFormat.length() < 5) {
            return false;
        }
        return ('-' == msgFormat.charAt(4))
                && (msgFormat.matches("(DEBG-|INFO-|WARN-|ERRO-|AUDI-).*"));
    }

    /**
     * <h3>Check debug enabled</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Delegates to Logger.isDebugEnabled()</li>
     *   <li>Use for quick guards around debug-heavy work</li>
     *   <li>No side effects</li>
     *   <li>Returns current logger configuration state</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * if (log.isDebugEnabled()) { ... }
     * }</pre>
     *
     * @return true if DEBUG is enabled
     *
     * @implNote Directly calls Logger.isDebugEnabled().
     *
     * @apiNote Prefer isLoggable(Level.DEBUG) for level-agnostic checks.
     *
     * @since 1.0
     *
     * @see Logger#isDebugEnabled()
     */
    public boolean isDebugEnabled()
    {
        return this.logger.isDebugEnabled();
    }

    /**
     * <h3>Format message with method context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Builds a bracketed method name prefix</li>
     *   <li>Appends message id and message text</li>
     *   <li>Used by various log overloads</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = formatMsg("doWork", "INFO-00001", "started");
     * }</pre>
     *
     * @param methodName   method name to include
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @return formatted message string
     *
     * @implNote Uses a StringBuilder for assembly.
     *
     * @apiNote Internal helper for consistent message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatMsg(StackTraceElement[], String, String)
     */
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

    /**
     * <h3>Format message with stack trace context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Builds a bracketed stack trace info prefix</li>
     *   <li>Appends message id and message text</li>
     *   <li>Used by various log overloads</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = formatMsg(trace, "INFO-00000", "Value {}", value);
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @return formatted message string
     *
     * @implNote Uses formatLineInfo for stack trace details.
     *
     * @apiNote Internal helper for consistent message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatLineInfo(StackTraceElement[])
     */
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

    /**
     * <h3>Format message with stack trace context only</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Builds a bracketed stack trace info prefix</li>
     *   <li>Appends the message text without a message id</li>
     *   <li>Used by marker-based info overloads</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = formatMsg(trace, "started");
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     * @param message   log message
     *
     * @return formatted message string
     *
     * @implNote Uses formatLineInfo for stack trace details.
     *
     * @apiNote Internal helper for consistent message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatLineInfo(StackTraceElement[])
     */
    private String formatMsg(StackTraceElement[] stackTrace, String message)
    {
        StringBuilder sb = new StringBuilder("[");
        sb.append(formatLineInfo(stackTrace));
        sb.append("] ");
        sb.append(": ");
        sb.append(message);
        return sb.toString();
    }

    /**
     * <h3>Format message with thread context and id</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Builds a bracketed thread info prefix</li>
     *   <li>Appends stack trace details, message id, and message text</li>
     *   <li>Used by thread-aware log overloads</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = formatMsg(threadInfo, "INFO-00001", "started");
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param msgId   message id prefix
     * @param message   log message
     *
     * @return formatted message string
     *
     * @implNote Uses formatLineThreadInfo and formatLineInfo.
     *
     * @apiNote Internal helper for consistent message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatLineThreadInfo(MoraLoggerThreadInfo)
     */
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

    /**
     * <h3>Format message with thread context</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Builds a bracketed thread info prefix</li>
     *   <li>Appends stack trace details and message text</li>
     *   <li>Used by marker-based info overloads</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String msg = formatMsg(threadInfo, "started");
     * }</pre>
     *
     * @param threadInfo   thread info container
     * @param message   log message
     *
     * @return formatted message string
     *
     * @implNote Uses formatLineThreadInfo and formatLineInfo.
     *
     * @apiNote Internal helper for consistent message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatLineThreadInfo(MoraLoggerThreadInfo)
     */
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

    /**
     * <h3>Format stack trace line info</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Extracts module, class, method, and line number</li>
     *   <li>Uses stackTrace[1] for caller context</li>
     *   <li>Builds a readable string for logs</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String info = formatLineInfo(Thread.currentThread().getStackTrace());
     * }</pre>
     *
     * @param stackTrace   stack trace for context
     *
     * @return formatted line info
     *
     * @implNote Uses stackTrace[1], which assumes caller depth.
     *
     * @apiNote Internal helper for message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatMsg(StackTraceElement[], String, String)
     */
    private String formatLineInfo(StackTraceElement[] stackTrace){
        StringBuilder sb = new StringBuilder("Module - ");
        sb.append(stackTrace[1].getModuleName());
        sb.append(" | Class - ");
        sb.append(stackTrace[1].getClassName());
        sb.append(" | Method - ");
        sb.append(stackTrace[1].getMethodName());
        sb.append(" | Line - ");
        sb.append(stackTrace[1].getLineNumber());
        return sb.toString();
    }

    /**
     * <h3>Format thread info line</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Extracts thread id and thread name</li>
     *   <li>Builds a readable string for logs</li>
     *   <li>Used by thread-aware message formatting</li>
     *   <li>No side effects besides string allocation</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * String info = formatLineThreadInfo(threadInfo);
     * }</pre>
     *
     * @param threadInfo   thread info container
     *
     * @return formatted thread info
     *
     * @implNote Uses threadInfo getters directly.
     *
     * @apiNote Internal helper for message formatting.
     *
     * @since 1.0
     *
     * @see MoraLogger#formatMsg(MoraLoggerThreadInfo, String)
     */
    private String formatLineThreadInfo(MoraLoggerThreadInfo threadInfo){
        StringBuilder sb = new StringBuilder("T_ID - ");
        sb.append(threadInfo.getThreadID()+"");
        sb.append(" | T_NAME - ");
        sb.append(threadInfo.getThreadName());
        return sb.toString();
    }

    /**
     * <h3>Get message properties</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Loads message properties from the default resource</li>
     *   <li>Used by Mora message formatting</li>
     *   <li>No caching is currently applied</li>
     *   <li>Side effect: reads a classpath resource</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Properties p = getProp();
     * }</pre>
     *
     * @return loaded properties
     *
     * @implNote Delegates to getAllPropertiesFromResource.
     *
     * @apiNote Internal helper for message templates.
     *
     * @since 1.0
     *
     * @see MoraLogger#getAllPropertiesFromResource(String)
     */
    private Properties getProp()
    {
        Properties messageProperties = getAllPropertiesFromResource(MESSAGE_RESOURCE);
        return messageProperties;
    }

    /**
     * <h3>Get underlying logger</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Returns the underlying Log4j Logger instance</li>
     *   <li>Use when direct Log4j access is required</li>
     *   <li>No side effects</li>
     *   <li>Returns the current logger reference</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Logger raw = log.getLogger();
     * }</pre>
     *
     * @return Log4j Logger instance
     *
     * @implNote Returns the internal logger field.
     *
     * @apiNote Exposes raw Log4j access when needed.
     *
     * @since 1.0
     *
     * @see Logger
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * <h3>Set underlying logger</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Replaces the internal Log4j Logger instance</li>
     *   <li>Use for testing or custom logger wiring</li>
     *   <li>Does not update the shared cache</li>
     *   <li>Side effect: changes logger used for subsequent calls</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * log.setLogger(LogManager.getLogger("custom"));
     * }</pre>
     *
     * @param logger   Log4j Logger to set
     *
     * @implNote Replaces the internal logger field.
     *
     * @apiNote Use with care; affects all subsequent logging.
     *
     * @since 1.0
     *
     * @see LogManager#getLogger(String)
     */
    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    /**
     * <h3>Load properties from resource</h3>
     * Brief one-line description of what the method does.
     *
     * <p><b>Detailed Description:</b></p>
     * Explain:
     * <ul>
     *   <li>Loads a Properties file from the classpath</li>
     *   <li>Used for message resource resolution</li>
     *   <li>Logs any IOException to the base logger</li>
     *   <li>Side effect: reads a classpath resource</li>
     * </ul>
     *
     * <p><b>Example:</b></p>
     * <pre>{@code
     * Properties p = getAllPropertiesFromResource("messageResources.properties");
     * }</pre>
     *
     * @param propertyFileName   resource name of the properties file
     *
     * @return loaded properties (may be empty)
     *
     * @implNote Uses ClassLoader.getResourceAsStream.
     *
     * @apiNote Internal helper for message resources.
     *
     * @since 1.0
     *
     * @see Properties#load(InputStream)
     */
    private Properties getAllPropertiesFromResource(String propertyFileName){
        BASE_LOGGER.debug("Properties from file {}", propertyFileName);
        Properties properties = new Properties();
        try(InputStream iStream = getClass().getClassLoader().getResourceAsStream(propertyFileName)){
            // Load message resources from classpath; empty properties if resource is missing.
            properties.load(iStream);
        } catch (IOException e) {
            BASE_LOGGER.error(formatMsg(Thread.currentThread().getStackTrace(), ExceptionUtils.getStackTrace(e)));
        }finally {
            return properties;
        }
    }
}
