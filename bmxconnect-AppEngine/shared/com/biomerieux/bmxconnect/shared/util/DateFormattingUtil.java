package com.biomerieux.bmxconnect.shared.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateFormattingUtil {

	private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

	public String formatDateTime(Date date) {
		if (date == null) {
			return "null";
		}
		
	    String dateString = createDateFormatter().format(date);
	    return dateString;
	}

	public String formatUTCDateTime(Date date) {
	    return formatDateTime(date, UTC_TIME_ZONE);
	}

	public String formatDateTime(Date date, TimeZone tz) {
		if (date == null) {
			return "null";
		}
		
	    String dateString = createDateFormatter(tz).format(date);
	    return dateString;
	}

	public Date convertDateTimeStringToDate(String dateTimeString) {
		return convertDateTimeStringToDate(dateTimeString, TimeZone.getDefault());
	}

	public Date convertDateTimeStringToDate(String dateTimeString, final TimeZone tz) {
		Date date = null;
		try {
			date = createDateFormatter(tz).parse(dateTimeString);
		} catch (ParseException e) {
			// return null
		}
		return date;
	}
	
	public Date convertUTCDateTimeStringToLocalDate(String dateTimeString) {
		return convertDateTimeStringToDate(dateTimeString, UTC_TIME_ZONE);
	}
	
	private SimpleDateFormat createDateFormatter() {
		SimpleDateFormat sdf = createDateFormatter(TimeZone.getDefault());
		return sdf;
	}
	
	private SimpleDateFormat createDateFormatter(final TimeZone tz) {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm a");
		sdf.setTimeZone(tz);
		return sdf;
	}
}
