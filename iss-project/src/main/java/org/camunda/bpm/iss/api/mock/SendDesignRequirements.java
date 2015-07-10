package org.camunda.bpm.iss.api.mock;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.engine.cdi.BusinessProcess;
import org.camunda.bpm.iss.DTO.out.JobIdDTO;
import org.camunda.bpm.iss.ejb.JobIdService;
import org.camunda.bpm.iss.entity.JobId;

@Named
public class SendDesignRequirements implements Runnable {

	private String jsonToSend;
	
	private String url;
	
	private long waitTime;

	@Inject
	JobIdService jobIdService;

	@Inject
	private BusinessProcess businessProcess;

	private static Logger LOGGER = Logger
			.getLogger(SendDesignRequirements.class.getName());

	public SendDesignRequirements(String json, String url, long waitTime,String logInfo) {
	       this.jsonToSend = json;
	       this.url = url;
	       this.waitTime = waitTime;
	       LOGGER = Logger.getLogger("SEND-THREAD-" + logInfo);
	}

	public void run() {
		JobIdDTO jobIdDTO = new JobIdDTO();
		Object entity = new Object();
		try {
			//Wait 10 seconds in order to let the other side process the answer the Webservice request
			Thread.sleep(waitTime);
			LOGGER.info("Send JSON: " + this.jsonToSend);
	        
	        //Instantiate
	        Client client = ClientBuilder.newClient();
	        //Send post request
	        Response response = client.target(url).request().post(Entity.entity(this.jsonToSend, MediaType.APPLICATION_JSON));
	        if (response == null){
	        	LOGGER.info("Null-response, dude!");
	        	return;
	        }
	        //Check, if status is "ok"
	        if (response.getStatus() != 200) {
	        	throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
	        }	
	        
			// this is the most important part for the first interface
			// we get the id for the remaining process communication
	        
	        jobIdDTO = response.readEntity(JobIdDTO.class);
	       
	        if (jobIdDTO == null) LOGGER.info("jobIdDTO is Null, dude!");
	        
	        LOGGER.info("JobIdDTO to String: "+ jobIdDTO.toString());
	        LOGGER.info("jobIdDTO JobId: " + jobIdDTO.getJobId());
	        
			// Looks complicated, but is easy: We set the Id from the DTO to a new entity called "jobIdEntity"
	        JobId jobIdEntity = new JobId();
			jobIdEntity.setJobId(jobIdDTO.getJobId());
			
			LOGGER.info("jobIdEntity JobId: " +jobIdEntity.getJobId());
			// Now we persist "jobIdEntity"
			jobIdEntity = jobIdService.create(jobIdEntity);
			
			businessProcess.setVariable("jobId", jobIdEntity.getId());
			LOGGER.log(Level.INFO, "This is jobId in the business process: " + businessProcess.getVariable("jobId"));
			  
		} catch (Exception e) {
			e.printStackTrace();	       
	  	}
		} 

	}

	