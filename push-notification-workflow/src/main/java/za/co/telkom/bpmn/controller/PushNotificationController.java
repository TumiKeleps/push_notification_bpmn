package za.co.telkom.bpmn.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SimpleAuthentication;
import io.camunda.common.auth.SimpleConfig;
import io.camunda.common.auth.SimpleCredential;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import lombok.extern.slf4j.Slf4j;
import za.co.telkom.bpmn.Response.GenericResponse;
import  za.co.telkom.bpmn.utils.Dater;

@RestController
@RequestMapping("/v2/api/push-notification")
@Slf4j
public class PushNotificationController {
	@Autowired
	private ZeebeClient client;


	
	@PostMapping("/start")
	public ResponseEntity<GenericResponse> vetIBody(@RequestBody String body) 
	{
		
		Map<String,String> Body = new Gson().fromJson(body, new TypeToken<Map<String, String>>() {}.getType());
	
		
		String launchTime = Dater.ISO8601ToUTC(Body.get("launchDate"));
		final ProcessInstanceEvent processInstanceResults = client
				.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(Map.of("CONTINUE_PROCESS", true, "launchTime", launchTime, "dayBefore",Dater.getDayBefore(launchTime), "dayAfter", Dater.getDayAfter(launchTime), 
				"startDate", "2024-07-30T07:39:35.989+00:00" , "endDate", "2024-07-30T07:39:35.989+00:00",
				"username" , Body.get("username"), "body", Body.get("header"), "campaignType",Body.get("campaignType")))
				.send()
				.join();
				GenericResponse  response = new GenericResponse(true, "Process completed successfully." , processInstanceResults);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/approve/{id}")
    public @ResponseBody ResponseEntity<Map<String, Object>> Approval(@RequestBody String approvebody) throws TaskListException 
	{
        //Setup authentication and client configuration
        SimpleConfig simpleConf = new SimpleConfig();
        simpleConf.addProduct(Product.TASKLIST, new SimpleCredential("http://localhost:8082", "demo", "demo"));

        Authentication auth = SimpleAuthentication.builder()
                .withSimpleConfig(simpleConf)
                .build();

        CamundaTaskListClient clients = CamundaTaskListClient.builder()
                .taskListUrl("http://localhost:8082")
                .authentication(auth)
                .cookieExpiration(Duration.ofSeconds(200))
                .shouldReturnVariables()
                .shouldLoadTruncatedVariables()
                .build();

        // Define the search criteria
		Map<String,Object> approvalBody = new Gson().fromJson(approvebody, new TypeToken<Map<String, Object>>() {}.getType());
		
		String processinstanceid = (String) approvalBody.get("processinstaceid");
        TaskSearch taskSearch = new TaskSearch().setProcessInstanceKey(processinstanceid);
		
        //Fetch tasks based on search criteria
        TaskList tasksFromInstance = clients.getTasks(taskSearch);
	
		String id = tasksFromInstance.first().getId();
    
		String approved  = (String) approvalBody.get("approved");
		log.info(approved);
		if(!"APPROVED".equals(approved))
			approvalBody.put("CONTINUE_PROCESS", false);

		client.newCompleteCommand(Long.parseLong(id)).variables(approvalBody).send().join();
		
		return new ResponseEntity<Map<String,Object>>(approvalBody, HttpStatus.OK);
		
    }

}