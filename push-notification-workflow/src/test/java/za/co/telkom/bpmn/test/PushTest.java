package za.co.telkom.bpmn.test;

import static io.camunda.zeebe.process.test.assertions.BpmnAssert.assertThat;
import static io.camunda.zeebe.protocol.Protocol.USER_TASK_JOB_TYPE;
import static io.camunda.zeebe.spring.test.ZeebeTestThreadSupport.waitForProcessInstanceCompleted;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import static io.restassured.RestAssured.given;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;
import io.camunda.zeebe.process.test.api.ZeebeTestEngine;
import io.camunda.zeebe.spring.test.ZeebeSpringTest;
import io.restassured.response.Response;
import io.restassured.response.ResponseBody;
import lombok.extern.slf4j.Slf4j;
import za.co.telkom.bpmn.WorkflowApplication;
import za.co.telkom.bpmn.workers.NotificationWorker;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT, classes = WorkflowApplication.class)
@ZeebeSpringTest
@Slf4j
public class PushTest {

	@Autowired
	private ZeebeClient zeebe;

	@Autowired
	private ZeebeTestEngine zeebeTestEngine;

	@InjectMocks
    private NotificationWorker notificationJobWorker;

	private static ClientAndServer mockServer;

	@Test
	public void push_notification_happy() throws Exception {
		mockServer = startClientAndServer(2020);
		Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
		MockServerClient mockServerClient = new MockServerClient("localhost", 2020);

		mockServerClient
				.when(
						request()
								.withMethod("POST")
								.withPath("/v1/api/push-notification/extended/create/hybrid"))
				.respond(

						response().withStatusCode(200)
								.withHeaders(
										new Header("Content-Type", "application/json; charset=utf-8"),
										new Header("Cache-Control", "public, max-age=86400")));

								

		mockServerClient
				.when(
						request()
								.withMethod("PATCH")
								.withPath("/v1/api/push-notification/extended/patch-on-db/hybrid"))
				.respond(

						response().withStatusCode(200)
								.withHeaders(
										new Header("Content-Type", "application/json; charset=utf-8"),
										new Header("Cache-Control", "public, max-age=86400")));


             ZonedDateTime launchTime = ZonedDateTime.now().plusSeconds(20);
			 String dayBeforeDuration = "PT20S"; // 20 seconds before the launchTime
            String dayAfterDuration = "PT20S";

			// Set process variables
			Map<String, Object> variables = new HashMap<>();
			variables.put("launchTime", launchTime.toString());
			variables.put("dayBefore", dayBeforeDuration);
			variables.put("dayAfter", dayAfterDuration);
			variables.put("email", "nsetwaba@gmail.com");
			variables.put("process_id", "kgklkhkl541215");
			variables.put("type", "hybrid");
			variables.put("approval", "approved");
			variables.put("CONTINUE_PROCESS", true);
			


		ProcessInstanceEvent processInstance = zeebe.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(variables) 
				.send()
				.join();

		waitForUserTaskAndComplete("await-approval", Map.of("approval", "approved", "launchTime", launchTime.toString(), "dayBefore", dayBeforeDuration, "dayAfter", dayAfterDuration
		));

		Response response = mock(Response.class);
        when(response.getStatusCode()).thenReturn(200);
        when(response.asString()).thenReturn("{\"status\":\"success\"}");

        // Use Mockito to mock the given().when().post() chain
        given()
            .accept("application/json")
            .contentType("application/json")
            .body(anyString())
            .when()
            .post("http://localhost:8080/api/push-notification")
            .thenReturn();;



		waitForProcessInstanceCompleted(processInstance, Duration.ofSeconds(100));

		assertThat(processInstance)
		.hasPassedElement("find_line_manager")
		.hasPassedElement("create_on_db")
		.hasPassedElement("await-approval")
		.hasPassedElement("process_ETL")
		.hasPassedElement("patch_on_db")
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

	@AfterAll
	public static void stopMockServer() {
		mockServer.stop();
	}

}
