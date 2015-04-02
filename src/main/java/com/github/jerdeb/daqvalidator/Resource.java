package com.github.jerdeb.daqvalidator;

import java.util.Date;
import java.util.List;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;

@Path("/")
public class Resource {

	final static Logger logger = LoggerFactory.getLogger(Resource.class);
	
	@POST
	@Path("validate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validate(MultivaluedMap<String, String> formParams) {
		
		String schema = "";
		com.hp.hpl.jena.rdf.model.Resource resource = null ;
		String inputType = "";
		String uuid = "";
		StringBuilder sb = new StringBuilder();
		
		try {
			logger.info("Validating Schema");
			
			List<String> lstSchema = formParams.get("input");
			List<String> lstType = formParams.get("type");
			
			if (lstType == null || lstType.size() <= 0){
				logger.error("Type parameter was not provided");
				throw new IllegalArgumentException("Type parameter was not provided");
			}
			if (lstSchema == null || lstSchema.size() <= 0){
				logger.error("Input parameter was not provided");
				throw new IllegalArgumentException("Input parameter was not provided");
			}
			inputType = lstType.get(0);
			
			Validator v = new Validator();

			if (inputType.equals("m")){
				schema = lstSchema.get(0);
				uuid = v.addSchema(schema);
			} 
			else if (inputType.equals("u")){
				resource = ModelFactory.createDefaultModel().createResource(lstSchema.get(0));
				uuid = v.addSchema(resource);
			}
			else if (inputType.equals("f")) {}
			else {
				logger.error("Type parameter was incorrectly provided: {}", inputType);
				throw new IllegalArgumentException("Type parameter was incorrectly provided : "+ inputType);
			}
			
			
			sb.append("{");
			sb.append("\"uid\" : \""+uuid+"\",");
			sb.append(v.listCompliantCDM(uuid));
			sb.append(",");
			sb.append(v.detectErrors(uuid));
			sb.append(",");		
			sb.append(v.detectWarnings(uuid));
			sb.append("}");
		} catch(Exception ex) {
			String errorTimeStamp = Long.toString((new Date()).getTime());
			sb.append("{");
			sb.append("\"uid\" : \""+uuid+"\",");
			sb.append("\"timestamp\" : \""+errorTimeStamp+"\",");
			sb.append("\"errormessage\" : \""+ex.getMessage()+"\"");
			sb.append("}");
		}
		
		logger.info("Response: {}", sb.toString());
		return Response.ok(sb.toString(),MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, OPTIONS")
			      .header("Access-Control-Allow-Headers", "x-requested-with, x-requested-by").build();
	}
}
