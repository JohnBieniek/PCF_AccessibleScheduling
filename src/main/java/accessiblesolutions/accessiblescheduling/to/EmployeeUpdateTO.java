package accessiblesolutions.accessiblescheduling.to;

public class EmployeeUpdateTO {
    private String employeeId="default";    
    private boolean booleanResponse=false; 
    private float numericResponse=0;
    
    public EmployeeUpdateTO() {
    }

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public boolean getBooleanResponse() {
		return booleanResponse;
	}

	public void setBooleanResponse(boolean booleanResponse) {
		this.booleanResponse = booleanResponse;
	}

	public float getNumericResponse() {
		return numericResponse;
	}

	public void setNumericResponse(float numericResponse) {
		this.numericResponse = numericResponse;
	}
}
