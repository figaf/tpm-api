package com.figaf.integration.tpm.utils;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.TimeZone;

public class TpmUtils {

    public final static FastDateFormat GMT_DATE_FORMAT = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ss.SSS", TimeZone.getTimeZone("GMT"));
}
