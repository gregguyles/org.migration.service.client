package org.migration.service.client;

import java.net.URI;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Scheduler {
  public static void main(String[] args) {
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    WebResource service = client.resource(getBaseURI());
    String logDir = "/home/gregor/googleDrive/workspace/org.migration.service.client/data.log";
    BufferedReader logReader;
    String currentLine, updateLine = "";
    int updateRecord = 0;
    int lineCounter = 0;
    
    /*********************************************
    * get the oldest record of the remote system *
    * *******************************************/
    ClientResponse response = service.path("migrate")
            .type(MediaType.TEXT_PLAIN)
            .get(ClientResponse.class);
    updateRecord = Integer.parseInt(response.getEntity(String.class));
    
    /****************************
    * compile records to update *
    * ***************************/
	try {
	    logReader = new BufferedReader(new FileReader(logDir));
	    while ((currentLine = logReader.readLine()) != null){
	    	if( lineCounter++ > updateRecord){
	    		updateLine += "\n" + currentLine;
	    	}
	    }
	} catch (IOException e) {
	   	e.printStackTrace();
	    }
    
	/***************************
	* Post the updated records *
	****************************/
    service.path("migrate").accept(MediaType.TEXT_PLAIN).post(String.class, updateLine);

    /***********************
    * Start Remote Service *
    ************************/
    service.path("migrate").path("activate").accept(MediaType.TEXT_PLAIN).post(String.class, "234");
    
    /*********************
    * Stop Local Service *
    **********************/
    
    /***************************
    * Write TempLog to Datalog *
    ****************************/
    
    /*********************************
    * Send TempLog to remote service *
    **********************************/
    
    /*************************
    * Shutdown local machine *
    **************************/
    
    }

    private static URI getBaseURI() {
        return UriBuilder.fromUri("http://localhost:8080/org.migration.service").build();
    }

} 