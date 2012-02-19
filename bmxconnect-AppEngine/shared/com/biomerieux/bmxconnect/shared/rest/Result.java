package com.biomerieux.bmxconnect.shared.rest;

import java.io.Serializable;
import java.util.Date;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import com.biomerieux.bmxconnect.shared.util.DateFormattingUtil;

@JsonAutoDetect(JsonMethod.NONE)
//@JsonIgnoreProperties({"key", "ownerKey"})
public class Result implements Serializable {
	private static final long serialVersionUID = 1L;

	@JsonIgnore
	private static final DateFormattingUtil dateFormattingUtil = new DateFormattingUtil();
	
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
	    return dateFormattingUtil.formatDateTime(getResultDate());
	}

	public void setResultDateString(String dateString) {
		setResultDate(dateFormattingUtil.convertDateTimeStringToDate(dateString));
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
