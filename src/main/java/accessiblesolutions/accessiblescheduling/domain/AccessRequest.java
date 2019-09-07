package accessiblesolutions.accessiblescheduling.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessRequest {
  	@Id
    private String userId;
  	private String name;

    public AccessRequest() {
    }
    
    public AccessRequest(String userId, String name) {
    	this.userId=userId;
    	this.name=name;
    }

	public String getUserId() {
		return userId;
	}

	public void setUserID(String userID) {
		this.userId = userID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "AccessRequest [userId=" + userId + ", name=" + name + "]";
	}
}
