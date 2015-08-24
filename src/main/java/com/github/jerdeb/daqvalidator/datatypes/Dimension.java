package com.github.jerdeb.daqvalidator.datatypes;

import java.util.ArrayList;
import java.util.List;

public class Dimension {

	private String name;
	private List<Metric> metrics = new ArrayList<Metric>();
	
	public Dimension(String dimensionName) {
		this.name = dimensionName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Metric> getMetrics() {
		return metrics;
	}
	public void addMetric(Metric metric) {
		this.metrics.add(metric);
	}
	
	@Override
	public boolean equals(Object object){
		
		if (object instanceof Dimension){
			Dimension _other = (Dimension) object;
			return this.name.equals(_other.getName());
		}
		
		return false;
	}
	
}
