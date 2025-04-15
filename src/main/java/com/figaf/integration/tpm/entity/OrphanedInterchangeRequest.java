package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

import static com.figaf.integration.tpm.utils.TpmUtils.GMT_DATE_FORMAT;
import static java.lang.String.format;

@Getter
@Setter
@ToString
public class OrphanedInterchangeRequest {

    private final Date leftBoundDate;
    private Date rightBoundDate;

    public OrphanedInterchangeRequest(Date leftBoundDate) {
        this.leftBoundDate = leftBoundDate;
    }

    public String buildFilter() {
        StringBuilder builder = new StringBuilder();
        builder.append(format("Date ge datetime'%s'", GMT_DATE_FORMAT.format(leftBoundDate)));
        if (rightBoundDate != null) {
            builder.append(format(" and Date le datetime'%s'", GMT_DATE_FORMAT.format(rightBoundDate)));
        }

        return builder.toString();
    }
}
