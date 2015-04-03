package com.github.jerdeb.daqvalidator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

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

	    Map<String, Category> cat = new HashMap<String, Category>();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String category = qs.get("category").asResource().getLocalName();
	    	String dimension = qs.get("dimension").asResource().getLocalName();
	    	String metric = qs.get("metric").asResource().getLocalName();

	    	if(cat.containsKey(category)){
	    		Category c = cat.get(category);
	    		if (c.dim.containsKey(dimension)){
	    			List<String> mList = c.dim.get(dimension);
	    			mList.add(metric);
	    		} else {
	    			List<String> mList = new ArrayList<String>();
	    			mList.add(metric);
	    			c.dim.put(dimension,mList);
	    		}
	    	} else {
	    		Category c = new Category();
	    		c.name = category;
    			List<String> mList = new ArrayList<String>();
    			mList.add(metric);
    			c.dim.put(dimension,mList);
    			cat.put(category, c);
	    	}
	    }
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("\"category\" : [");
	    for(String c : cat.keySet()){
	    	Category _c = cat.get(c);
	    	sb.append(_c.toString());
	    	sb.append(",");
	    }
	    sb.deleteCharAt(sb.length() - 1);
	    sb.append("]");
	    return sb.toString();
	}

	public String detectErrors(String uid) throws IOException{
		Model m = this.clientSchemas.getNamedModel("urn:"+uid);
		
		// are there any dimensions in multiple categories
		URL url = Resources.getResource("queries/multDim.sparql");
		String query = Resources.toString(url, Charsets.UTF_8);
		
		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, m);
	    ResultSet rs = qe.execSelect();
	    
	    Map<String,List<String>> multDim = new HashMap<String,List<String>>();
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String category = qs.get("category").asResource().getLocalName();
	    	String dimension = qs.get("dimension").asResource().getLocalName();
	    
	    	if (multDim.containsKey(dimension)){
	    		List<String> cat = multDim.get(dimension);
	    		cat.add(category);
	    		multDim.put(dimension, cat);
	    	} else {
	    		List<String> cat = new ArrayList<String>();
	    		cat.add(category);
	    		multDim.put(dimension, cat);
	    	}
	    }
	    	
	    // are there any dimensions in multiple categories
		url = Resources.getResource("queries/multMetric.sparql");
		query = Resources.toString(url, Charsets.UTF_8);
		
		qry = QueryFactory.create(query);
	    qe = QueryExecutionFactory.create(qry, m);
	    rs = qe.execSelect();
	    
	    Map<String,List<String>> multMetric = new HashMap<String,List<String>>();
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String dimension = qs.get("dimension").asResource().getLocalName();
	    	String metric = qs.get("metric").asResource().getLocalName();

	    
	    	if (multMetric.containsKey(metric)){
	    		List<String> dim = multMetric.get(metric);
	    		dim.add(dimension);
	    		multMetric.put(metric, dim);
	    	} else {
	    		List<String> dim = new ArrayList<String>();
	    		dim.add(dimension);
	    		multMetric.put(metric, dim);
	    	}
	    }
	    
	    int totalErrors = multDim.size() + multMetric.size();
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("\"errors\" : {");
	    sb.append("\"total\" : "+totalErrors+",");
	    
	    if (totalErrors > 0){
		    sb.append("\"messages\" : [");
		    
		    for(String d : multDim.keySet()){
		    	List<String> c = multDim.get(d);
		    	StringBuilder error = new StringBuilder("The dimension " + d + " is in " + c.size() + " categories. ");
		    	for(String _c : c) 
		    		error.append(_c + ", ");
		    	error.deleteCharAt(error.length() - 2);
		    	sb.append("\""+error.toString().trim() + "\",");
		    }
		    
		    for(String met : multMetric.keySet()){
		    	List<String> d = multMetric.get(met);
		    	StringBuilder error = new StringBuilder("The metric " + met + " is in " + d.size() + " dimensions. ");
		    	for(String _d : d) error.append(_d + ", ");
		    	error.deleteCharAt(error.length() - 2);
		    	sb.append("\""+error.toString().trim() + "\",");
		    }
		    sb.deleteCharAt(sb.length() - 1);
		    sb.append("]");
	    }
	    else sb.deleteCharAt(sb.length() - 1);
	    sb.append("}");
	    
	    return sb.toString();
	}

	public String detectWarnings(String uid) throws IOException{
		Model m = this.clientSchemas.getNamedModel("urn:"+uid);
		
		int totalWarnings = 0;
		List<String> dim = new ArrayList<String>();
		List<String> met = new ArrayList<String>();

		
		URL url = Resources.getResource("queries/noLinkedDim.sparql");
		String query = Resources.toString(url, Charsets.UTF_8);
		
		Query qry = QueryFactory.create(query);
	    QueryExecution qe = QueryExecutionFactory.create(qry, m);
	    ResultSet rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String dimension = qs.get("dimension").asResource().getLocalName();
	    	dim.add(dimension);
	    	totalWarnings++;
	    }
	    
		url = Resources.getResource("queries/noLinkMetric.sparql");
		query = Resources.toString(url, Charsets.UTF_8);
		
		qry = QueryFactory.create(query);
	    qe = QueryExecutionFactory.create(qry, m);
	    rs = qe.execSelect();
	    
	    while (rs.hasNext()){
	    	QuerySolution qs = rs.next();
	    	String metric = qs.get("metric").asResource().getLocalName();
	    	met.add(metric);
	    	totalWarnings++;
	    }
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("\"warnings\" : {");
	    sb.append("\"total\" : "+totalWarnings+",");
	    
	    if(totalWarnings > 0){
		    sb.append("\"messages\" : [");
		    for(String s : dim)
		    	sb.append("\"The dimension " + s + " is not linked to any category.\",");
		    for(String s : met)
		    	sb.append("\"The metric " + s + " is not linked to any dimension.\",");
		    sb.deleteCharAt(sb.length() - 1);
		    sb.append("]");
	    }
	    else sb.deleteCharAt(sb.length() - 1);
	    sb.append("}");
	    
	    return sb.toString();
	}
	
	private class Category{
		String name;
		Map<String, List<String>> dim = new HashMap<String, List<String>>();
		
		@Override
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			sb.append("\"name\" : "+ toJSON(name) + ",");
			sb.append("\"dimension\" : [");
			
			//create dimensions
			for(String d : dim.keySet()){
				sb.append("{");
				sb.append("\"name\" : "+ toJSON(d) + ",");
				sb.append("\"metric\" : [");
				//create metrics
				List<String> mL = dim.get(d);
				for(String m : mL){
					sb.append(toJSON(m)+",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("]");
				sb.append("},");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("]");
			sb.append("}");
			
			return sb.toString();
		}
		
		private String toJSON(String term){
			String ret = "\""+term+"\"";
			return ret;
		}
	}

}