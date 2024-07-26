package za.co.telkom.bpmn.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZonedDateTime;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.ProcessInstanceResult;

@RestController
@RequestMapping("/v2/api/push-notification")
public class PushNotificationController {
	@Autowired
	private ZeebeClient client;

	@GetMapping("/id/{id}")
	public String vetIBody(@PathVariable(name = "id") String id) 
	{
		ZonedDateTime launchTime = ZonedDateTime.now().plusSeconds(20);
		String dayBeforeDuration = "PT20S"; // 20 seconds before the launchTime
		String dayAfterDuration = "PT20S";

		final ProcessInstanceResult processInstanceResults = client
				.newCreateInstanceCommand()
				.bpmnProcessId("process-push-notification")
				.latestVersion()
				.variables(Map.of("id", id, "CONTINUE_PROCESS", true, "launchTime", launchTime.toString(), "dayBefore",
						dayBeforeDuration, "dayAfter", dayAfterDuration))
				.withResult()
				.send()
				.join();

		return processInstanceResults.getBpmnProcessId();
	}
}