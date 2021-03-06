package org.camunda.bpm.iss.ejb;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.camunda.bpm.iss.entity.JobId;

@Named
@Stateless
public class JobIdService {

	@PersistenceContext
	private EntityManager entityManager;

	private static Logger LOGGER = Logger
			.getLogger(JobIdService.class.getName());

	public JobId create(JobId jobId) {
		LOGGER.info("This is create JobId");
		entityManager.persist(jobId);
		// entityManager.flush();
		LOGGER.info("Created the JobId with Id: " + jobId.getId()
				+ "and jobId: " + jobId.getJobId());
		return jobId;
	}

	public JobId getJobId(Long jobIdId) {
		// Load entity from database
		return entityManager.find(JobId.class, jobIdId);
	}

}
