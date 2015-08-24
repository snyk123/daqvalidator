package com.github.jerdeb.daqvalidator.datatypes;

public class Metric {

	private String name;

	public Metric(String metricName) {
		this.name = metricName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object object){
		
		if (object instanceof Metric){
			Metric _other = (Metric) object;
			return this.name.equals(_other.getName());
		}
		
		return false;
	}
}
