package org.migration.service.client;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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
	
	static String remoteIP = "localhost";
	static String port = "8080";
	static String logDir = "/home/gregor/googleDrive/workspace/org.migration.service.client/data.log";
	static String migrateTimes = "/home/gregor/googleDrive/workspace/org.migration.service.client/migrateTimes";
	Thread runner;
	static String location = "1";
	
	public static void main(String[] args) throws InterruptedException{
        BufferedReader timeReader;
        String migrateHour = "";
        Date migrateTime = null;
        String currentLine;
        String[] strArr;
		try {
			timeReader = new BufferedReader(new FileReader(migrateTimes));
			while ((currentLine = timeReader.readLine()) != null){
				strArr = currentLine.split(",");
				if (location.equals(strArr[0])){
					migrateHour = strArr[1];
					break;
				}
			}
	    } catch (IOException e) {
			e.printStackTrace();
	    }
        SimpleDateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		TimeZone timeZone = TimeZone.getTimeZone("MST");
        dateformat.setTimeZone(timeZone);
        Calendar migrateCalendar = new GregorianCalendar();
        migrateTime = migrateCalendar.getTime();
        int mstOffset = timeZone.getOffset(migrateTime.getTime()) / (60*60*1000);
        int currentOff = migrateCalendar.get(Calendar.ZONE_OFFSET) / (60*60*1000);
        int timeZoneDelta = currentOff - mstOffset;
        migrateTime.setHours(Integer.parseInt(migrateHour) + timeZoneDelta);
        migrateTime.setMinutes(0);
        migrateTime.setSeconds(0);
        
        migrateCalendar.setTime(migrateTime);
        
        if (location.equals("0")) {
        	Calendar c = Calendar.getInstance();
        	c.setTime(migrateTime);
        	c.add(Calendar.DATE, 1);
        	migrateTime = c.getTime();
        }
		
		Calendar currentTime = new GregorianCalendar();
		while (currentTime.before(migrateCalendar)){
			Thread.sleep(3000);
			currentTime = new GregorianCalendar();
	        System.out.println(currentTime.getTime());
	        System.out.println(migrateTime);
		}
		System.out.println("after");
		//	migrate();	    	 
	}
	
	private void setIP(String ip){
		this.remoteIP = ip;
	}

    private static URI getBaseURI() {
    	String uri = "http://" + remoteIP + "port" + "/org.migration.service";
        return UriBuilder.fromUri(uri).build();
    }
    
    public static void startRunner(int StartingRecordId){
    	//start runner, called by local web service
    }
    
    private static void migrate(){
    	
    ClientConfig config = new DefaultClientConfig();
    Client client = Client.create(config);
    WebResource service = client.resource(getBaseURI());
    BufferedReader logReader;
    String currentLine, updateLine = "";
    int updateRecord = 0;
    int lineCounter = 0;
    	
    /**************************
     * Power up remote system *
     **************************/
    
    // get/set ip of remote system
    
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
    // runner.Interupt;
    
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
} 