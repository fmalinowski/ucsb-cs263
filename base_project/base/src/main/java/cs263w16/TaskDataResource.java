package cs263w16;

import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.*;
import java.util.logging.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import javax.xml.bind.JAXBElement;

public class TaskDataResource {
	@Context
	UriInfo uriInfo;
	@Context
	Request request;
	String keyname;

	public TaskDataResource(UriInfo uriInfo, Request request, String kname) {
		this.uriInfo = uriInfo;
		this.request = request;
		this.keyname = kname;
	}
	
	// for the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public TaskData getTaskDataHTML() {
		
		return getTaskDataCommon();
	}
	
	// for the application
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public TaskData getTaskData() {
		
		return getTaskDataCommon();
	}

	@PUT
	@Consumes(MediaType.APPLICATION_XML)
	public Response putTaskData(String val) {
		
		Response res = null;
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		
		Key taskKey = KeyFactory.createKey("TaskData", this.keyname);
		
		Entity task;
		try {
			task = datastore.get(taskKey);
			task.setProperty("value", val);
			res = Response.noContent().build();
			
		} catch (EntityNotFoundException e) {
			
			task = new Entity("TaskData", this.keyname);
			task.setProperty("value", val);
			task.setProperty("date", new Date());
			res = Response.created(uriInfo.getAbsolutePath()).build();
		}
		
		datastore.put(task);
		syncCache.put(this.keyname, task);
 
		return res;
	}

	@DELETE
	public void deleteIt() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		
		System.out.println("Deleting TaskData for " + keyname);
		
		try {
			datastore.delete(KeyFactory.createKey("TaskData", this.keyname));
			syncCache.delete(this.keyname);
		}
		catch (Exception e) {
			System.out.println("Couldn't delete " + this.keyname);
		}
    }
	
	private TaskData getTaskDataCommon() throws RuntimeException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		  
		Key taskKey = KeyFactory.createKey("TaskData", this.keyname);
		Entity taskResult;

		if (syncCache.contains(this.keyname)) {
			Entity entity = (Entity) syncCache.get(this.keyname);
			return new TaskData(this.keyname, (String)entity.getProperty("value"), (Date)entity.getProperty("date"));
		} 
		else {
			try {
				taskResult = datastore.get(taskKey);
				String value = (String) taskResult.getProperty("value");
				Date date = (Date) taskResult.getProperty("date");

				syncCache.put(this.keyname, taskResult);

				return new TaskData(this.keyname, value, date);
			} catch (EntityNotFoundException e) {
				throw new RuntimeException("Get: TaskData with keyname " + this.keyname +  " not found");
			}
		}
	}
}
