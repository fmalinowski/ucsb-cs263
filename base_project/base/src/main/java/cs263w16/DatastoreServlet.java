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
	  
	  resp.setContentType("text/html");
      resp.getWriter().println("<html><body>");
	  
	  // No parameters are passed in
	  if(!req.getParameterNames().hasMoreElements()) {
		  Query allTasksQuery = new Query("TaskData");
		  PreparedQuery preparedQuery = datastore.prepare(allTasksQuery);
		  
		  resp.getWriter().println("<h1>Here are all the TaskData</h1>");
		  
		  for (Entity result : preparedQuery.asIterable()) {
			  String keyname = (String) result.getKey().getName();
			  String value = (String) result.getProperty("value");
			  Date date = (Date) result.getProperty("date");
			  resp.getWriter().println(keyname+ " - " + value + " - " + date + "<br>");
		  }
	  }
	  else if (req.getParameter("keyname") != null && !req.getParameter("keyname").isEmpty() && req.getParameter("value") == null) {
		  Key taskKey = KeyFactory.createKey("TaskData", req.getParameter("keyname"));
		  Entity taskResult;
		  
		  resp.getWriter().println("<h1>TaskData for " + req.getParameter("keyname") + "</h1>");
		  
		  try {
			  taskResult = datastore.get(taskKey);
			  String value = (String) taskResult.getProperty("value");
			  Date date = (Date) taskResult.getProperty("date");
			  resp.getWriter().println(value + " - " + date + "<br><br>");
		  } catch (EntityNotFoundException e) {
			  resp.getWriter().println("No entities found");
		  }
		  
		  
	  } 
	  else if (req.getParameter("keyname") != null & !req.getParameter("keyname").isEmpty() && req.getParameter("value") != null) {
		  String keyname = req.getParameter("keyname");
		  String value = req.getParameter("value");
		  Entity task = new Entity("TaskData", keyname);
		  task.setProperty("value", value);
		  task.setProperty("date", new Date());
		  
		  datastore.put(task);
		  
		  resp.getWriter().println("Stored " + keyname + " and " + value + " in Datastore");
	  }
	  else {
		  resp.getWriter().println("This action doesn't work man!");
	  }
	  
	  
	  resp.getWriter().println("</body></html>");
  }
}
