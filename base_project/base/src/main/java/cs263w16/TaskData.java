package cs263w16;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

@XmlRootElement
// JAX-RS supports an automatic mapping from JAXB annotated class to XML and
// JSON
public class TaskData {
	private String keyname;
	private String value;
	private Date date;
	
	public TaskData() {
		
	}
	
	public TaskData(String keyname, String value, Date date) {
		this.setKeyname(keyname);
		this.setValue(value);
		this.setDate(date);
	}

	public String getKeyname() {
		return keyname;
	}

	public void setKeyname(String keyname) {
		this.keyname = keyname;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	// add constructors (default () and (String,String,Date))
	// add getters and setters for all fields
}
