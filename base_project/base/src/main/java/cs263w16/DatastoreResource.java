package cs263w16;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.util.logging.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

//Map this class to /ds route
@Path("/ds")
public class DatastoreResource {
	
	// Allows to insert contextual objects into the class,
	// e.g. ServletContext, Request, Response, UriInfo
	@Context
	UriInfo uriInfo;
	@Context
	Request request;

	// Return the list of entities to the user in the browser
	@GET
	@Produces(MediaType.TEXT_XML)
	public List<TaskData> getEntitiesBrowser() {
		
		return getEntityList();
	}

	// Return the list of entities to applications
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public List<TaskData> getEntities() {
		
		return getEntityList();
	}

	// Add a new entity to the datastore
	@POST
	@Produces(MediaType.TEXT_HTML)
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public void newTaskData(@FormParam("keyname") String keyname, @FormParam("value") String value,
			@Context HttpServletResponse servletResponse) throws IOException {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
		
		Date date = new Date();
		Entity task = new Entity("TaskData", keyname);
		task.setProperty("value", value);
		task.setProperty("date", date);

		datastore.put(task);
		syncCache.put(keyname, task);
		
		System.out.println("Posting new TaskData: " + keyname + " val: " + value + " ts: " + date);
		servletResponse.sendRedirect("/done.html");
	}

	// The @PathParam annotation says that keyname can be inserted as parameter
	// after this class's route /ds
	@Path("{keyname}")
	public TaskDataResource getEntity(@PathParam("keyname") String keyname) {
		
		System.out.println("GETting TaskData for " + keyname);
		return new TaskDataResource(uriInfo, request, keyname);
	}

	private List<TaskData> getEntityList() {
		// datastore dump -- only do this if there are a small # of entities
		List<TaskData> list = new ArrayList<TaskData>();

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query allTasksQuery = new Query("TaskData");
		List<Entity> entities = datastore.prepare(allTasksQuery).asList(
				FetchOptions.Builder.withLimit(100));

		for (Entity result : entities) {
			String keyname = (String) result.getKey().getName();
			String value = (String) result.getProperty("value");
			Date date = (Date) result.getProperty("date");

			TaskData taskData = new TaskData(keyname, value, date);
			list.add(taskData);
		}

		return list;
	}
}