package za.co.telkom.bpmn.workers;

import static io.restassured.RestAssured.given;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NothingWorker {

	@JobWorker(type = "do_nothing", autoComplete = true)
	public void doNothing() 
	{
		log.info("Begin : Do Nothing");
		log.info("End : Do Nothing");
	}


	@JobWorker(type = "send_mail", autoComplete = true)
	public void sendemail() 
	{

		log.info("================ sending email ==============================");
		try
		{
		Map<String, Object> requestMap = new HashMap<String,Object>();
		requestMap.put("pushNotificationHeader", "Push Notification Header");
		requestMap.put("description", "description");
		requestMap.put("receivers", List.of("ritshidzenemu@gmail.com"));
		requestMap.put("process_id", "kgklkhkl541215");
		requestMap.put("template_key", "push_notification");
		String emailRequestBody = new Gson().toJson(requestMap);
		given()
				.accept("application/json")
				.contentType("application/json")
				.body(emailRequestBody)
				.expect()
				.statusCode(200)
				.log()
				.all()
				.when()
				.post("http://10.227.44.41:7220/email/api/push-notification").thenReturn();
		
		log.info("============= email Sent to receivers !!!! ==================");
		}
		catch(Exception e)
		{
			log.info("failed to send email retrying !!!!!");
			sendemail();
		}
	}

}
