package accessiblescheduling.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessRequest {
  	@Id
    private String id;
  	private String name;
  	private String email;

    public AccessRequest() {
    }
    
    public AccessRequest(String id, String name) {
    	this.id=id;
    	this.name=name;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "AccessRequest [id=" + id + ", name=" + name + ", email=" + email + "]";
	}
}
