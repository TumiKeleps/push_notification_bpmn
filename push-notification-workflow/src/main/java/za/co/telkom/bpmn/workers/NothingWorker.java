package za.co.telkom.bpmn.workers;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import org.springframework.stereotype.Component;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NothingWorker {

@JobWorker(type = "do_nothing", autoComplete = true)
	public void doNothing(){
		log.info("Begin : Do Nothing");
		log.info("End : Do Nothing");
	}

	// @JobWorker(type = "send_notification", autoComplete = true)
	// public void sendemail(){
	// 	log.info("sending an email");
	// 	log.info("successfully sent the email");
	// }

}
