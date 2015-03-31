package com.github.jerdeb.daqvalidator;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.DatasetFactory;

public class ValidatorFactory {

	protected static Dataset clientSchemas = DatasetFactory.createMem(); 
	
	protected ValidatorFactory(){}
	
	public static Dataset getDataset(){
		return clientSchemas;
	}
}
