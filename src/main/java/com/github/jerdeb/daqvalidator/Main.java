package com.github.jerdeb.daqvalidator;

import java.io.IOException;
import java.net.URI;

import javax.json.stream.JsonGenerator;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;


public class Main {
	// Base URI the Grizzly HTTP server will listen on
	private static final String SCHEME = "http";
	private static final String DOMAIN = "localhost";
	private static final String APPLICATION = "daqvalidator";
//	
	


    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer(String uri) {
        final ResourceConfig rc = new ResourceConfig().packages("com.github.jerdeb.daqvalidator").property(JsonGenerator.PRETTY_PRINTING, true);
       return GrizzlyHttpServerFactory.createHttpServer(URI.create(uri), rc);
    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	    	
    	// Start server and wait for user input to stop
    	String uri = SCHEME+"://"+DOMAIN+":"+args[0]+"/"+ APPLICATION + "/";
    	
        final HttpServer server = startServer(uri);
        
        try {
            server.start();
            System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n", uri));
            // Wait forever (i.e. until the JVM instance is terminated externally)
            Thread.currentThread().join();
        } catch (Exception ioe) {
            System.out.println("Error running service: " + ioe.toString());
        } finally {
        	if(server != null && server.isStarted()) {
        		server.shutdownNow();
        	}
        }
    }
}
