package accessiblescheduling.domain;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;

import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {
	@Id
	private String token;
	
	private String userId;
	
	private LocalDateTime expires;
	
	public Session() {
	}

	public boolean hasExpired() {
		boolean expired = true;
		
		if(expires!=null) {
			expired = LocalDateTime.now().isAfter(expires);
		}
		
		return expired;
	}
	
	public LocalDateTime getExpires() {
		return expires;
	}

	public void setExpires(LocalDateTime expires) {
		this.expires = expires;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "Session [token=" + token + ", userId=" + userId + ", expires=" + expires + "]";
	}
}
