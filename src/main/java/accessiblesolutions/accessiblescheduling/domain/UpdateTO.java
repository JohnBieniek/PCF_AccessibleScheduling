package accessiblesolutions.accessiblescheduling.domain;

public class UpdateTO {
    private String id="default";    
    private boolean booleanResponse=false; 
    private float numericResponse=0;
    
    public UpdateTO() {
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
