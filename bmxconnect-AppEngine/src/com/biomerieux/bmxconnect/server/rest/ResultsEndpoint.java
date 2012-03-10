package com.biomerieux.bmxconnect.server.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mortbay.log.Log;

import com.biomerieux.bmxconnect.server.DAO;
import com.biomerieux.bmxconnect.server.ResultRecord;
import com.biomerieux.bmxconnect.shared.rest.Result;
import com.biomerieux.bmxconnect.shared.rest.ResultList;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.sun.jersey.spi.resource.Singleton;

@Singleton
@Path("/results")
@Produces({ MediaType.APPLICATION_JSON })
public class ResultsEndpoint {

	private final DAO<ResultRecord> resultRecordDao = new DAO<ResultRecord>();
	
	@GET
	public Response queryResults(@QueryParam("since") Date since) {
		String accountName = getAccountName();
		ResultList resultList = new ResultList();
		if (since != null) {
			//TODO: query based on last modified date and accountName
		} else {
			// Pull back all results
			resultList.setResults(getAllResultsForAccount(accountName));
		}

		return Response.ok(resultList).build();
	}

	private String getAccountName() {
	    UserService userService = UserServiceFactory.getUserService();
	    User user = userService.getCurrentUser();
	    if (user == null) {
	      throw new WebApplicationException(Response.Status.UNAUTHORIZED);
	    }
	    return user.getEmail();
	}
	  
	private List<Result> getAllResultsForAccount(String accountName) {
		List<Result> resultRecords = new ArrayList<Result>();
		try {
//			String queryStr = "select from " + ResultRecord.class.getName() + " r where r.ownerKey = :accountName";
			String queryStr = "select from " + ResultRecord.class.getName() + " r where r.accountName = :accountName order by r.resultDate desc";
//			Query query = em.createQuery(queryStr);
//			Key accountNameKey = KeyFactory.createKey(ResultRecord.RESULT_OWNER_KEY, accountName);
//			query.setParameter("accountName", accountNameKey);
			resultRecords.addAll(resultRecordDao.queryList(queryStr, "accountName", accountName));
		} catch (Exception e) {
			e.printStackTrace();
			Log.debug(e);
		}
		return resultRecords;
	  }
}
