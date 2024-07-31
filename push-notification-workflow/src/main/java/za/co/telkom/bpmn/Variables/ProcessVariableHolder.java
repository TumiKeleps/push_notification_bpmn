package za.co.telkom.bpmn.Variables;

public class ProcessVariableHolder 
{
    private static long processInstanceId;
    public static void setProcessInstanceId(long id)
    {
        processInstanceId = id;
    }
    public static long getProcessInstanceId()
    {
        return processInstanceId;
    }
}
