package com.biomerieux.bmxconnect.shared.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormattingUtil {

	public String formatDateTime(Date date) {
		if (date == null) {
			return "null";
		}
		
	    String dateString = createDateFormatter().format(date);
	    return dateString;
	}
	
	public Date convertDateTimeStringToDate(String dateTimeString) {
		Date date = null;
		try {
			date = createDateFormatter().parse(dateTimeString);
		} catch (ParseException e) {
			// return null
		}
		return date;
	}
	
	private SimpleDateFormat createDateFormatter() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm a");
		return sdf;
	}
}
