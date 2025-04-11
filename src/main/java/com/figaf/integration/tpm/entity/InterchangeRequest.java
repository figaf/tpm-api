package com.figaf.integration.tpm.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;
import java.util.TimeZone;

import static java.lang.String.format;

@Getter
@Setter
@ToString
public class InterchangeRequest {

    private final static FastDateFormat GMT_DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"));

    private String overallStatus;

    private String agreedSenderIdentiferAtSenderSide;
    private String agreedSenderIdentiferQualifierAtSenderSide;
    private String agreedReceiverIdentiferAtSenderSide;
    private String agreedReceiverIdentiferQualifierAtSenderSide;
    private String agreedSenderIdentiferAtReceiverSide;
    private String agreedSenderIdentiferQualifierAtReceiverSide;
    private String agreedReceiverIdentiferAtReceiverSide;
    private String agreedReceiverIdentiferQualifierAtReceiverSide;
    private String senderAdapterType;
    private String senderDocumentStandard;
    private String senderMessageType;
    private String receiverDocumentStandard;
    private String receiverMessageType;

    public String buildFilter(Date leftBoundDate) {
        StringBuilder builder = new StringBuilder();
        builder.append(format("DocumentCreationTime ge datetime'%s'", GMT_DATE_FORMAT.format(leftBoundDate)));
        if (overallStatus != null) {
            builder.append(format(" and OverallStatus eq '%s'", overallStatus));
        }
        if (agreedSenderIdentiferAtSenderSide != null) {
            builder.append(format(" and AgreedSenderIdentiferAtSenderSide eq '%s'", agreedSenderIdentiferAtSenderSide));
        }
        if (agreedSenderIdentiferQualifierAtSenderSide != null) {
            builder.append(format(" and AgreedSenderIdentiferQualifierAtSenderSide eq '%s'", agreedSenderIdentiferQualifierAtSenderSide));
        }
        if (agreedReceiverIdentiferAtSenderSide != null) {
            builder.append(format(" and AgreedReceiverIdentiferAtSenderSide eq '%s'", agreedReceiverIdentiferAtSenderSide));
        }
        if (agreedReceiverIdentiferQualifierAtSenderSide != null) {
            builder.append(format(" and AgreedReceiverIdentiferQualifierAtSenderSide eq '%s'", agreedReceiverIdentiferQualifierAtSenderSide));
        }

        if (agreedSenderIdentiferAtReceiverSide != null) {
            builder.append(format(" and AgreedSenderIdentiferAtReceiverSide eq '%s'", agreedSenderIdentiferAtReceiverSide));
        }
        if (agreedSenderIdentiferQualifierAtReceiverSide != null) {
            builder.append(format(" and AgreedSenderIdentiferQualifierAtReceiverSide eq '%s'", agreedSenderIdentiferQualifierAtReceiverSide));
        }
        if (agreedReceiverIdentiferAtReceiverSide != null) {
            builder.append(format(" and AgreedReceiverIdentiferAtReceiverSide eq '%s'", agreedReceiverIdentiferAtReceiverSide));
        }
        if (agreedReceiverIdentiferQualifierAtReceiverSide != null) {
            builder.append(format(" and AgreedReceiverIdentiferQualifierAtReceiverSide eq '%s'", agreedReceiverIdentiferQualifierAtReceiverSide));
        }
        if (senderAdapterType != null) {
            builder.append(format(" and SenderAdapterType eq '%s'", senderAdapterType));
        }
        if (senderDocumentStandard != null) {
            builder.append(format(" and SenderDocumentStandard eq '%s'", senderDocumentStandard));
        }
        if (senderMessageType != null) {
            builder.append(format(" and SenderMessageType eq '%s'", senderMessageType));
        }
        if (receiverDocumentStandard != null) {
            builder.append(format(" and ReceiverDocumentStandard eq '%s'", receiverDocumentStandard));
        }
        if (receiverMessageType != null) {
            builder.append(format(" and ReceiverMessageType eq '%s'", receiverMessageType));
        }

        return builder.toString();
    }
}
