package com.github.jerdeb.daqvalidator;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;


public class Main {
	// Base URI the Grizzly HTTP server will listen on
	private static final String SCHEME = "http";
	private static final String DOMAIN = "localhost";
	private static final String APPLICATION = "daqvalidator";
	private static final String PORT = (System.getenv("PORT")!=null?System.getenv("PORT"):"9998");

	private static final String BASEURI = SCHEME+"://"+DOMAIN+":"+PORT+"/"+ APPLICATION + "/";

	



    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
    	    	
    	// Start server and wait for user input to stop
    	    	
        

        final Map<String, String> initParams = new HashMap<String, String>();

        initParams.put("com.sun.jersey.config.property.packages","resources");
        
        System.out.println("Starting grizzly...");
        SelectorThread threadSelector = GrizzlyWebContainerFactory.create(BASEURI, initParams);
        threadSelector.start();
        System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n", BASEURI));
        
//        try {
//        	threadSelector.start();
//            System.out.println(String.format("Jersey app started with WADL available at " + "%sapplication.wadl\n", BASEURI));
//            // Wait forever (i.e. until the JVM instance is terminated externally)
//            Thread.currentThread().join();
//        } catch (Exception ioe) {
//            System.out.println("Error running service: " + ioe.toString());
//        } finally {
//        	if(server != null && threadSelector.i.isStarted()) {
//        		server.shutdownNow();
//        	}
//        }
    }
}
