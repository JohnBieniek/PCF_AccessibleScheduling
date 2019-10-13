package accessiblesolutions.accessiblescheduling.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Id;

public class ScheduleStatus implements Comparable<ScheduleStatus> {
	@Id
	@Column(length = 2)
	private String month;
	private boolean generating;
	private boolean generated;
	private boolean assigned;
	private boolean assigning;
	private boolean deleting;
	private boolean errored;
	private boolean stopped;
	private boolean stopping;
	private int scheduled;
	private int unscheduled;
	private String lastUpdated;

	@Override
	public int compareTo(ScheduleStatus status) {
		return Integer.parseInt(month) - Integer.parseInt(status.getMonth());
	}
	
	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdatedToNow() {
    	setLastUpdated(LocalDateTime.now().toString());
    }

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
	
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
	public boolean isGenerating() {
		return generating;
	}

	public void setGenerating(boolean generating) {
		this.generating = generating;
	}

	public boolean isDeleting() {
		return deleting;
	}

	public void setDeleting(boolean deleting) {
		this.deleting = deleting;
	}

	public boolean isStopping() {
		return stopping;
	}

	public void setStopping(boolean stopping) {
		this.stopping = stopping;
	}

	@Override
	public String toString() {
		return "ScheduleStatus [month=" + month + ", generating=" + generating + ", generated=" + generated
				+ ", assigned=" + assigned + ", assigning=" + assigning + ", deleting=" + deleting + ", errored="
				+ errored + ", stopped=" + stopped + ", stopping=" + stopping + ", scheduled=" + scheduled
				+ ", unscheduled=" + unscheduled + ", lastUpdated=" + lastUpdated + "]";
	}
}
