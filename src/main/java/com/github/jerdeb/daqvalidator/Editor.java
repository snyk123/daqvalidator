package com.github.jerdeb.daqvalidator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;

public class Editor {

	Dataset clientSchemas = ValidatorFactory.getDataset();
	private String uid = "";
	private Model schema = null;
	
	public Editor(String uid){
		this.uid = uid;
		this.schema = clientSchemas.getNamedModel(this.uid);
	}
	
	public void createCategory(String categoryURI, String label, String comment){
		
	}
	
	private void linkMetric(String dimensionURI, String metricURI){
		
	}
	
	private void createProperty(String uri){
		
	}
	
}
