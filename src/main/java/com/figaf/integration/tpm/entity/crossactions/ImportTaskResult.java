package com.figaf.integration.tpm.entity.crossactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
public class ImportTaskResult {

    @JsonProperty("OverallTaskStatus")
    private String overallTaskStatus;

    @JsonProperty("LogEntries")
    private Map<String, LogEntry> logEntries;

    @Getter
    @Setter
    @ToString
    public static class LogEntry {

        @JsonProperty("LogStatus")
        private String logStatus;

        @JsonProperty("LogMessage")
        private String logMessage;
    }
}
