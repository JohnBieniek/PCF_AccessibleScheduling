package accessiblesolutions.accessiblescheduling.domain;

public class CallAuth{
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

	@Override
	public String toString() {
		return "CallAuth [manager=" + manager + ", admin=" + admin + "]";
	}
}
