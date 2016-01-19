package cs263w16;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;

public class Worker extends HttpServlet {
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		String keyname = request.getParameter("keyname");
		String value = request.getParameter("value");
     
		if (keyname != null && value != null) {
			Entity task = new Entity("TaskData", keyname);
			task.setProperty("value", value);
			task.setProperty("date", new Date());
		  
			datastore.put(task);
		}
    
	}
}