package za.co.telkom.bpmn.workers;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import static io.restassured.RestAssured.given;
import io.camunda.zeebe.client.api.response.ActivatedJob;
import io.camunda.zeebe.spring.client.annotation.JobWorker;

@Component
public class NotificationWorker {

    @JobWorker(type = "send-notification", autoComplete = true)
    public void sendNotification(final ActivatedJob job){
        Map<String , Object> variables = job.getVariablesAsMap();
        sendNotification(variables);
    }

    public void sendNotification(Map<String, Object> variables) {
     Map<String, Object> requestMap = Map.of(
                "pushNotificationHeader", "Push Notification Header",
                "description", "description",
                "email", variables.get("email"),
                "process_id", variables.get("process_id"),
                "template_key", "push_notification"
        );

        String requestJson = new Gson().toJson(requestMap);
        System.out.println(requestJson);

        given()
                .accept("application/json")
                .contentType("application/json")
                .body(requestJson)
                .when()
                .post("http://localhost:8080/api/push-notification");
    }


}
