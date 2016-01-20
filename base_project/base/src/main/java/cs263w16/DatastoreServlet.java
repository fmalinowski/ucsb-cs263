package cs263w16;

import javax.servlet.*;
import javax.servlet.http.*;

import java.io.*;
import java.util.*;
import java.util.logging.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.*;

@SuppressWarnings("serial")
public class DatastoreServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	  DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	  MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
	  syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
	  
	  resp.setContentType("text/html");
      resp.getWriter().println("<html><body>");
	  
	  // No parameters are passed in
	  if(!req.getParameterNames().hasMoreElements()) {
		  Query allTasksQuery = new Query("TaskData");
		  PreparedQuery preparedQuery = datastore.prepare(allTasksQuery);
		  ArrayList<String> keynames = new ArrayList<String>();
		  
		  resp.getWriter().println("<h1>Here are all the TaskData</h1>");
		  
		  for (Entity result : preparedQuery.asIterable()) {
			  String keyname = (String) result.getKey().getName();
			  String value = (String) result.getProperty("value");
			  Date date = (Date) result.getProperty("date");
			  
			  keynames.add(keyname);
			  
			  resp.getWriter().println(keyname + " - " + value + " - " + date + "<br>");
		  }
		  
		  resp.getWriter().println("<hr><h2>Here are the entries in memcache</h2>");
		  for (String keyname : keynames) {
			  if (syncCache.contains(keyname)) {
				  Entity entity = (Entity)syncCache.get(keyname);
				  resp.getWriter().println(keyname + " - " + entity.getProperty("value") + " - " + (Date)entity.getProperty("date") + "<br>");
			  }
		  }
		  
	  }
	  else if (req.getParameter("keyname") != null && !req.getParameter("keyname").isEmpty() && req.getParameter("value") == null) {
		  Key taskKey = KeyFactory.createKey("TaskData", req.getParameter("keyname"));
		  Entity taskResult;
		  boolean foundInMemcache = false;
		  
		  resp.getWriter().println("<h1>TaskData for " + req.getParameter("keyname") + "</h1>");
		  
		  foundInMemcache = syncCache.contains(req.getParameter("keyname"));		  
		  
		  try {
			  taskResult = datastore.get(taskKey);
			  String foundString = foundInMemcache ? "Both" : "Datastore";
			  String value = (String) taskResult.getProperty("value");
			  Date date = (Date) taskResult.getProperty("date");
			  
			  if (!foundInMemcache) {
				  syncCache.put(req.getParameter("keyname"), taskResult);
			  }
			  
			  resp.getWriter().println("Found: " + foundString + " | " + value + " - " + date + "<br><br>");
		  } catch (EntityNotFoundException e) {
			  resp.getWriter().println("Neither - No entities found in either Datastore or Memcache");
		  }
		  
		  
	  } 
	  else if (req.getParameter("keyname") != null & !req.getParameter("keyname").isEmpty() && req.getParameter("value") != null) {
		  String keyname = req.getParameter("keyname");
		  String value = req.getParameter("value");
		  Entity task = new Entity("TaskData", keyname);
		  task.setProperty("value", value);
		  task.setProperty("date", new Date());
		  
		  datastore.put(task);
		  syncCache.put(keyname, task);
		  
		  resp.getWriter().println("Stored " + keyname + " and " + value + " in Datastore<br>");
		  resp.getWriter().println("Stored " + keyname + " and " + value + " in Memcache");
	  }
	  else {
		  resp.getWriter().println("This action doesn't work man!");
	  }
	  
	  
	  resp.getWriter().println("</body></html>");
  }
}
