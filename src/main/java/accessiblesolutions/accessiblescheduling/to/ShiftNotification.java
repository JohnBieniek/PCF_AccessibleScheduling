package accessiblesolutions.accessiblescheduling.to;

import java.util.ArrayList;

//Shift issues can only exist for shifts that haven't already occur. The past troubles me not
public class ShiftNotification {
    private ArrayList<ShiftIssueTO> issues=new ArrayList<ShiftIssueTO>();    
    private String description="Undetermined"; 
    private int priority = 0;
    public ShiftNotification() {
    }

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ArrayList<ShiftIssueTO> getIssues() {
		return issues;
	}

	public void setIssues(ArrayList<ShiftIssueTO> issues) {
		this.issues = issues;
	}
	
	public int getPriority(){
		return priority;
	}
	
	public void setPriority(int priority){
		this.priority=priority;
	}
	
	
	@Override
	public String toString(){
		String out = description;
		for(ShiftIssueTO issue :issues){
			out+=issue.toString();
		}
		return out;
	}
}
