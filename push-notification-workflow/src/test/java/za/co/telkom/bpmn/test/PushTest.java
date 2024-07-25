package za.co.telkom.bpmn.test;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import lombok.extern.slf4j.Slf4j;
import za.co.telkom.bpmn.WorkflowApplication;
import za.co.telkom.bpmn.NothingWorker;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = WorkflowApplication.class)
@ZeebeSpringTest
@Slf4j
public class PushTest {

	@Autowired
	private ZeebeClient zeebe;

	@Autowired
	private ZeebeTestEngine zeebeTestEngine;

	private static ClientAndServer mockServer;


	//Test Create Push on DB 
	//Test The Error Condition : Bad Request
	//@Test
	public void createPushFailureBadRequest()throws Exception{

		mockServer = startClientAndServer(2020);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		MockServerClient mockServerClient = new MockServerClient("localhost", 2020);

		//Mock Create Push Notification Service's Response
			mockServerClient
			.when(
					request()
							.withMethod("POST")
							.withPath("/v1/api/push-notification/extended/create/")
			)
			.respond(
					response().withStatusCode(400)
					.withHeaders(
							new Header("Content-Type", "application/json; charset=utf-8"),
							new Header("Cache-Control","public, max-age=86400"))
					.withBody(new Gson().toJson("Failed : Bad Request Error "))
			);


		ProcessInstanceEvent processInstanceEvent = zeebe.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(Map.of("customerType","Prepaid","profileType","ONNET","duration","11:27:55",
				"userName","johndoe@gmail.com","launchtime","2024-11-01'T'13:00:00","header","","body","{}","campaignType","General"))
				.send().join();

		// Now the process should run to the end
		waitForProcessInstanceCompleted(processInstanceEvent, Duration.ofSeconds(5));

		assertThat(processInstanceEvent)
		.hasPassedElement("find_line_manager")
		.hasNotPassedElement("create_on_db")
		.isCompleted();		
	}

	//Test The Error Condition : PreCondition
	//@Test
	public void createPushFailurePreCondition()throws Exception{

		mockServer = startClientAndServer(2020);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		MockServerClient mockServerClient = new MockServerClient("localhost", 2020);

		//Mock Create Push Notification Service's Response
			mockServerClient
			.when(
					request()
							.withMethod("POST")
							.withPath("/v1/api/push-notification/extended/create/")
			)
			.respond(
					response().withStatusCode(412)
					.withHeaders(
							new Header("Content-Type", "application/json; charset=utf-8"),
							new Header("Cache-Control","public, max-age=86400"))
					.withBody(new Gson().toJson("Failed : Precondition Not Met"))
			);


		ProcessInstanceEvent processInstanceEvent = zeebe.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(Map.of("customerType","Prepaid","profileType","ONNET","duration","11:27:55",
				"userName","johndoe@gmail.com","launchtime","2024-11-01'T'13:00:00","header","","body","{}","campaignType","General"))
				.send().join();

		// Now the process should run to the end
		waitForProcessInstanceCompleted(processInstanceEvent, Duration.ofSeconds(5));

		assertThat(processInstanceEvent)
		.hasPassedElement("find_line_manager")
		.hasNotPassedElement("create_on_db")
		.isCompleted();		
	}


	//Test The Error Condition : Service Unavailable
	//@Test
	public void createPushFailureServiceUnavail()throws Exception{

		mockServer = startClientAndServer(2020);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		MockServerClient mockServerClient = new MockServerClient("localhost", 2020);

		//Mock Create Push Notification Service's Response
			mockServerClient
			.when(
					request()
							.withMethod("POST")
							.withPath("/v1/api/push-notification/extended/create/")
			)
			.respond(
					response().withStatusCode(503)
					.withHeaders(
							new Header("Content-Type", "application/json; charset=utf-8"),
							new Header("Cache-Control","public, max-age=86400"))
					.withBody(new Gson().toJson("Failed : Service Unavailable"))
			);


		ProcessInstanceEvent processInstanceEvent = zeebe.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(Map.of("customerType","Prepaid","profileType","ONNET","duration","11:27:55",
				"userName","johndoe@gmail.com","launchtime","2024-11-01'T'13:00:00","header","","body","{}","campaignType","General"))
				.send().join();

		// Now the process should run to the end
		waitForProcessInstanceCompleted(processInstanceEvent, Duration.ofSeconds(5));

		assertThat(processInstanceEvent)
		.hasPassedElement("find_line_manager")
		.hasNotPassedElement("create_on_db")
		.isCompleted();		
	}

	


	public void waitForUserTaskAndComplete(String userTaskId, Map<String, Object> variables)
			throws InterruptedException, TimeoutException {
		new Thread().sleep(Duration.ofSeconds(2));
		// Let the workflow engine do whatever it needs to do
		zeebeTestEngine.waitForIdleState(Duration.ofMinutes(5));

		// Now get all user tasks
		List<ActivatedJob> jobs = zeebe.newActivateJobsCommand().jobType(USER_TASK_JOB_TYPE).maxJobsToActivate(1)
				.workerName("waitForUserTaskAndComplete").send().join().getJobs();

		// Should be only one
		assertTrue(jobs.size() > 0, "Job for user task '" + userTaskId + "' does not exist");
		ActivatedJob userTaskJob = jobs.get(0);
		// Make sure it is the right one
		if (userTaskId != null) {
			assertEquals(userTaskId, userTaskJob.getElementId());
		}

		// And complete it passing the variables
		if (variables != null && variables.size() > 0) {
			zeebe.newCompleteCommand(userTaskJob.getKey()).variables(variables).send().join();
		} else {
			zeebe.newCompleteCommand(userTaskJob.getKey()).send().join();
		}
	}

}
