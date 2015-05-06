package edu.elon.herps;

import java.io.Serializable;

public class NameValuePair implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public String name;
	public Object value;
	
	public NameValuePair(String name, Object value) {
		this.name = name;
		this.value = value;
	}
}
