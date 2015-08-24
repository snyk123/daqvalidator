package com.github.jerdeb.daqvalidator.datatypes;

import java.util.ArrayList;
import java.util.List;

public class Category {
	
	private String name;
	private List<Dimension> dimensions = new ArrayList<Dimension>();
	
	public Category(String categoryName) {
		this.name = categoryName;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Dimension> getDimensions() {
		return dimensions;
	}
	public void addDimension(Dimension dimension) {
		this.dimensions.add(dimension);
	}
	
	
	@Override
	public boolean equals(Object object){
		
		if (object instanceof Category){
			Category _other = (Category) object;
			return this.name.equals(_other.getName());
		}
		
		return false;
	}
}
