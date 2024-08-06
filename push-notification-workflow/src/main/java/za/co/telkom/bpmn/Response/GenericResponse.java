package za.co.telkom.bpmn.Response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GenericResponse {
    private boolean success;
    private String message;
    private Object data;
}
