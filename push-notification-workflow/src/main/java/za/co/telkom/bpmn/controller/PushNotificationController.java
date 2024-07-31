package za.co.telkom.bpmn.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZonedDateTime;

import io.camunda.common.auth.Authentication;
import io.camunda.common.auth.Product;
import io.camunda.common.auth.SimpleAuthentication;
import io.camunda.common.auth.SimpleConfig;
import io.camunda.common.auth.SimpleCredential;
import io.camunda.tasklist.CamundaTaskListClient;
import io.camunda.tasklist.dto.TaskList;
import io.camunda.tasklist.dto.TaskSearch;
import io.camunda.tasklist.exception.TaskListException;
// import io.camunda.tasklist.CamundaTaskListClient;
// import io.camunda.tasklist.dto.TaskList;
// import io.camunda.tasklist.dto.TaskSearch;
// import io.camunda.tasklist.exception.TaskListException;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;
import io.camunda.zeebe.client.api.worker.JobClient;
import io.camunda.zeebe.spring.client.annotation.JobWorker;
import lombok.extern.slf4j.Slf4j;
import za.co.telkom.bpmn.Variables.ProcessVariableHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.time.Duration;
import io.camunda.zeebe.client.api.response.ActivatedJob;

@RestController
@RequestMapping("/v2/api/push-notification")
@Slf4j
public class PushNotificationController {
	@Autowired
	private ZeebeClient client;

	@GetMapping("/id/{id}")
	public String vetIBody(@PathVariable(name = "id") String id) {
		ZonedDateTime launchTime = ZonedDateTime.now().plusSeconds(20000000);
		String dayBeforeDuration = "PT20H"; // 20 seconds before the launchTime
		String dayAfterDuration = "PT20H";

		final ProcessInstanceResult processInstanceResults = client
				.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(Map.of("id", id, "CONTINUE_PROCESS", true, "launchTime", launchTime.toString(), "dayBefore",
						dayBeforeDuration, "dayAfter", dayAfterDuration))
				.withResult()
				.send()
				.join();

		ProcessVariableHolder.setProcessInstanceId(processInstanceResults.getProcessInstanceKey());
		return processInstanceResults.getBpmnProcessId();
	}

	@GetMapping("/approve")
	// @JobWorker(type = "io.camunda.zeebe:userTask")
	public String postMethodName() throws TaskListException 
	{
		
		SimpleConfig simpleConf = new SimpleConfig();
		simpleConf.addProduct(Product.TASKLIST, new SimpleCredential("http://localhost:8082", "demo", "demo"));
		Authentication auth = SimpleAuthentication.builder().withSimpleConfig(simpleConf).build();//.withSimpleConfig(simpleConf).build();
		CamundaTaskListClient clients = CamundaTaskListClient.builder()
		.taskListUrl("http://localhost:8082")
		 		.authentication(auth)
		.cookieExpiration(Duration.ofSeconds(200))
		 		.build();
				
		
		CamundaTaskListClient client = CamundaTaskListClient.builder().taskListUrl("http://localhost:8082")
		.shouldReturnVariables().shouldLoadTruncatedVariables().authentication(auth).build();
		
		TaskSearch ts = new TaskSearch().setProcessInstanceKey("2251799813808997");
		TaskList tasksFromInstance = client.getTasks(ts);
		client.claim("2251799813809035", "demo");
		client.completeTask("2251799813809035", Map.of("dogs","win"));
		return "none";
	}

}