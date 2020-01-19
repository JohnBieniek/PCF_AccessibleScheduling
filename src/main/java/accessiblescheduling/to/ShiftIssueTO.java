package accessiblescheduling.to;

import accessiblescheduling.domain.Shift;
import accessiblescheduling.exception.CorruptDataException;

public class ShiftIssueTO {
    private Shift shift=null;    
    private String description="Undetermined"; 
    public ShiftIssueTO() {
    }

	public Shift getShift() {
		return shift;
	}

	public void setShift(Shift shift) throws CorruptDataException {
		this.shift = shift;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	@Override
	public String toString(){
		return description + shift.toString();
	}
}
