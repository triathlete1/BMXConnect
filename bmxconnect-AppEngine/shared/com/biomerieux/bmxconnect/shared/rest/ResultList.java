package com.biomerieux.bmxconnect.shared.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect
public class ResultList implements Serializable {
	private static final long serialVersionUID = 1L;

	private List<Result> results = new ArrayList<Result>();

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	@Override
	public String toString() {
		return "ResultList [results=" + results + "]";
	}
}
