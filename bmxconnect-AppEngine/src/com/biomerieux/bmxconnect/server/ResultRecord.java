package com.biomerieux.bmxconnect.server;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.biomerieux.bmxconnect.shared.rest.Result;
import com.google.appengine.api.datastore.Key;

@Entity(name="Result")
public class ResultRecord extends Result {
	private static final long serialVersionUID = 1L;

//	public static final String RESULT_OWNER_KEY = "ResultOwnerKey";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Key key;

//    private Key ownerKey; // only set this when creating a note
//
//    public Key getOwnerKey() {
//		return ownerKey;
//	}
//
//	public void setOwnerKey(Key ownerKey) {
//		this.ownerKey = ownerKey;
//	}

	@Basic
	@Override
	public String getAccountName() {
		return super.getAccountName();
	}

	@Override
	public void setAccountName(String accountName) {
		super.setAccountName(accountName);
//		Key ownerKey = KeyFactory.createKey(RESULT_OWNER_KEY, accountName);
//		setOwnerKey(ownerKey);
	}

	@Basic
	@Override
	public String getResult() {
		return super.getResult();
	}

	@Override
	public void setResult(String result) {
		super.setResult(result);
	}

	@Basic
	@Override
	public Date getResultDate() {
		return super.getResultDate();
	}

	@Override
	public void setResultDate(Date resultDate) {
		super.setResultDate(resultDate);
	}

	
	@Override
	public String toString() {
		return "ResultRecord [key=" + key + super.toString() + "]";
	}
}
