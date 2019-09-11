package accessiblesolutions.accessiblescheduling.domain;

public class CallAuth{
	private String employeeId;
	private boolean manager;
	private boolean admin;

	public CallAuth() {
	}
	
	public CallAuth(boolean manager, boolean admin) {
		this.manager=manager;
		this.admin=admin;
	}

	public boolean isManager() {
		return manager;
	}

	public void setManager(boolean manager) {
		this.manager = manager;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	@Override
	public String toString() {
		return "CallAuth [employeeId=" + employeeId + ", manager=" + manager + ", admin=" + admin + "]";
	}
}
