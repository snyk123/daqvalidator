package com.github.jerdeb.daqvalidator.datatypes;

import java.util.ArrayList;
import java.util.List;

public class Report {

	private int total = 0;
	private List<String> messages = new ArrayList<String>();
	
	public List<String> getMessages() {
		return messages;
	}
	
	public void addMessage(String message) {
		this.messages.add(message);
	}
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
}
