package accessiblesolutions.accessiblescheduling.domain;

import javax.persistence.Column;
import javax.persistence.Id;

public class ScheduleStatus {
	@Id
	@Column(length = 2)
	private String month;
	private boolean generated;
	private boolean assigned;
	private boolean assigning;
	private boolean errored;
	private boolean stopped;
	private int scheduled;
	private int unscheduled;
	
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
	
	public boolean isAssigning() {
		return assigning;
	}
	public void setAssigning(boolean assigning) {
		this.assigning = assigning;
	}
	public boolean isErrored() {
		return errored;
	}
	public void setErrored(boolean errored) {
		this.errored = errored;
	}
	public boolean isStopped() {
		return stopped;
	}
	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}
	@Override
	public String toString() {
		return "ScheduleStatus [month=" + month + ", generated=" + generated + ", assigned=" + assigned + ", assigning="
				+ assigning + ", errored=" + errored + ", stopped=" + stopped + "]";
	}
}
