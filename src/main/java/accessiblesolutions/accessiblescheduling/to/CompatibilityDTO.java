package accessiblescheduling.to;

public class CompatibilityDTO {
    private String shiftId="none";
    private String employeeId="default";    
    private boolean compatible=false;
    
    public CompatibilityDTO() {
    }

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getShiftId() {
		return shiftId;
	}

	public void setShiftId(String shiftId) {
		this.shiftId = shiftId;
	}

	public boolean getCompatible() {
		return compatible;
	}

	public void setCompatible(boolean compatible) {
		this.compatible = compatible;
	}
}
