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

	@Override
	public String toString() {
		return "AccessRequest [userId=" + id + ", name=" + name + "]";
	}
}
