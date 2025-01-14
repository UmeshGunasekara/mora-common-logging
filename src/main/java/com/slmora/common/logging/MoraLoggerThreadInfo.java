/*
 * Created by IntelliJ IDEA.
 * Language: Java
 * Property of Umesh Gunasekara
 * @Author: SLMORA
 * @DateTime: 7/24/2023 12:00 AM
 */
package com.slmora.common.logging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 *  This Class created for
 *  <ul>
 *      <li>....</li>
 *  </ul>
 *
 * @since   1.0
 *
 * <blockquote><pre>
 * <br>Version      Date            Editor              Note
 * <br>-------------------------------------------------------
 * <br>1.0          7/24/2023      SLMORA                Initial Code
 * </pre></blockquote>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@ToString
public class MoraLoggerThreadInfo {
    private String threadName;
    private Long threadID;
    private String threadPool;
    private StackTraceElement[] threadStackTrace;

    public MoraLoggerThreadInfo(String threadName, Long threadID, StackTraceElement[] threadStackTrace){
        setThreadName(threadName);
        setThreadID(threadID);
        setThreadStackTrace(threadStackTrace);
    }
}
