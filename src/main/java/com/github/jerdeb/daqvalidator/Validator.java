package com.github.jerdeb.daqvalidator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.jerdeb.daqvalidator.datatypes.Category;
import com.github.jerdeb.daqvalidator.datatypes.Dimension;
import com.github.jerdeb.daqvalidator.datatypes.Metric;
import com.github.jerdeb.daqvalidator.datatypes.Report;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;


public class Validator {
	
	private Logger logger = LoggerFactory.getLogger(Validator.class);
	
	Dataset clientSchemas = ValidatorFactory.getDataset();
	
	public Validator(){}

	public String addSchema(File file){
		throw new UnsupportedOperationException("Adding files is not supported yet");
	}
	
	public String addSchema(Resource uri){
		UUID _uid = UUID.randomUUID();
		
		Model m = ModelFactory.createDefaultModel();
		RDFDataMgr.read(m, uri.getURI());
		clientSchemas.addNamedModel("urn:"+_uid.toString(), m);
		
		return _uid.toString();		
	}
	
	public String addSchema(String schema){
		UUID _uid = UUID.randomUUID();
		
		Model m = ModelFactory.createDefaultModel();
		StringReader sr = new StringReader(schema);
		RDFDataMgr.read(m, sr, "", Lang.TTL);
		clientSchemas.addNamedModel("urn:"+_uid.toString(), m);
		
		return _uid.toString(); 
	}
	
	public String listCompliantCDM(String uid) throws IOException{
		Model m = this.clientSchemas.getNamedModel("urn:"+uid);
		
		URL url = Resources.getResource("queries/listComplyingCDM.sparql");
		String query = Resources.toString(url, Charsets.UTF_8);
		
		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, m);
	    ResultSet rs = qe.execSelect();

	    
	    List<Category> cat = new ArrayList<Category>();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String category = qs.get("category").asResource().getLocalName();
	    	String dimension = qs.get("dimension").asResource().getLocalName();
	    	String metric = qs.get("metric").asResource().getLocalName();
	    	
	    	Category c = new Category(category);
	    	Dimension d = new Dimension(dimension);
	    	Metric met = new Metric(metric);
	    	
	    	if (cat.contains(c)) c = cat.get(cat.indexOf(c));
	    	else cat.add(c);
	    	
	    	List<Dimension> dims = c.getDimensions();
	    	if (dims.contains(d)) d = dims.get(dims.indexOf(d));
	    	else c.addDimension(d);
	    	
	    	d.addMetric(met);
	    }
	    
	    String json = "";
	    for(Category c : cat){
	    	json = "\"category\" : [";
	    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			try {
				json += ow.writeValueAsString(c) + ",";
			} catch (JsonProcessingException e) {
				logger.error("Error transforming to json : {}", e.getMessage());
			}
			json = json.substring(0, json.length()-1);
			json += "]";
	    }
	    
	    return json;
	}

	public String detectErrors(String uid) throws IOException{
		Model m = this.clientSchemas.getNamedModel("urn:"+uid);
		
		Report error = new Report();
		
		// are there any dimensions in multiple categories
		URL url = Resources.getResource("queries/multDim.sparql");
		String query = Resources.toString(url, Charsets.UTF_8);
		
		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, m);
	    ResultSet rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String d = qs.get("dimension").asResource().getLocalName();
	    	String c = qs.get("cat").asLiteral().getString();
	    	
	    	error.addMessage("The dimension " + d + " is in " + c + " categories. ");
	    }
	    	
	    // are there any dimensions in multiple categories
		url = Resources.getResource("queries/multMetric.sparql");
		query = Resources.toString(url, Charsets.UTF_8);
		
		qry = QueryFactory.create(query);
	    qe = QueryExecutionFactory.create(qry, m);
	    rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String d = qs.get("dim").asResource().getLocalName();
	    	String met = qs.get("metric").asResource().getLocalName();

	    	error.addMessage("The metric " + met + " is in " + d + " dimensions. ");
	    
	    }
	    
	    error.setTotal(error.getMessages().size());
	    
	    String json = "";
	   
	    if (error.getTotal() > 0)
    	{
	    	json = "\"errors\" : {";
	    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			try {
				json += ow.writeValueAsString(error);
			} catch (JsonProcessingException e) {
				logger.error("Error transforming to json : {}", e.getMessage());
			}
			json += "}";
    	}

	    
	    return json;
	    
	}

	public String detectWarnings(String uid) throws IOException{
		Model m = this.clientSchemas.getNamedModel("urn:"+uid);
		
		Report warning = new Report();

		
		URL url = Resources.getResource("queries/noLinkedDim.sparql");
		String query = Resources.toString(url, Charsets.UTF_8);
		
		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, m);
	    ResultSet rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String dimension = qs.get("dimension").asResource().getLocalName();
	    	warning.addMessage("The dimension " + dimension + " is not linked to any category");
	    }
	    
		url = Resources.getResource("queries/noLinkMetric.sparql");
		query = Resources.toString(url, Charsets.UTF_8);
		
		qry = QueryFactory.create(query);
	    qe = QueryExecutionFactory.create(qry, m);
	    rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String metric = qs.get("metric").asResource().getLocalName();
	    	warning.addMessage("The metric " + metric + " is not linked to any category");

	    }
	    
	    String json = "";
	    if (warning.getTotal() > 0)
    	{
	    	json = "\"warnings\" : {";
	    	ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			try {
				json += ow.writeValueAsString(warning);
			} catch (JsonProcessingException e) {
				logger.error("Error transforming to json : {}", e.getMessage());
			}
			json += "}";
    	}

	    
	    return json;
	}
}