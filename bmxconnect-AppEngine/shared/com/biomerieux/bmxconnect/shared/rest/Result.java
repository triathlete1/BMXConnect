package com.biomerieux.bmxconnect.shared.rest;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonAutoDetect(JsonMethod.NONE)
//@JsonIgnoreProperties({"key", "ownerKey"})
public class Result implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonProperty("owner") private String accountName;
	@JsonProperty private String result;
	@JsonProperty private Date resultDate;

	public Result() {}


	public String getAccountName() {
		return accountName;
	}

	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public Date getResultDate() {
		return resultDate;
	}

	public void setResultDate(Date resultDate) {
		this.resultDate = resultDate;
	}

	public String getResultDateString() {
		if (resultDate == null) {
			return "null";
		}
		
	    String dateString = createDateFormatter().format(resultDate);
	    return dateString;
	}

	public void setResultDateString(String dateString) {
		try {
			Date date = createDateFormatter().parse(dateString);
			setResultDate(date);
		} catch (ParseException e) {
			setResultDate(null);
		}
	}

	private SimpleDateFormat createDateFormatter() {
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd hh:mm a");
		return sdf;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accountName == null) ? 0 : accountName.hashCode());
		result = prime * result
				+ ((this.result == null) ? 0 : this.result.hashCode());
		result = prime * result
				+ ((resultDate == null) ? 0 : resultDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Result other = (Result) obj;
		if (accountName == null) {
			if (other.accountName != null)
				return false;
		} else if (!accountName.equals(other.accountName))
			return false;
		if (result == null) {
			if (other.result != null)
				return false;
		} else if (!result.equals(other.result))
			return false;
		if (resultDate == null) {
			if (other.resultDate != null)
				return false;
		} else if (!resultDate.equals(other.resultDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Result [accountName=" + accountName + ", result=" + result + ", resultDate=" + resultDate + "]";
	}
}
