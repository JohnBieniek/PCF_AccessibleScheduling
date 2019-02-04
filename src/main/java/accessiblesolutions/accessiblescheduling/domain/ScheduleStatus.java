package accessiblesolutions.accessiblescheduling.domain;

import javax.persistence.Column;
import javax.persistence.Id;

public class ScheduleStatus {
	@Id
	@Column(length = 2)
	private String month;
	private boolean generated;
	private boolean assigned;
	
	public String getMonth() {
		return month;
	}
	public void setMonth(String month) {
		this.month = month;
	}
	public boolean isGenerated() {
		return generated;
	}
	public void setGenerated(boolean generated) {
		this.generated = generated;
	}
	public boolean isAssigned() {
		return assigned;
	}
	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}
	
	@Override
	public String toString() {
		return "ScheduleStatus [month=" + month + ", generated=" + generated + ", assigned=" + assigned + "]";
	}
}
