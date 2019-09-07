package accessiblesolutions.accessiblescheduling.domain;

import com.fasterxml.jackson.annotation.JsonProperty;


public class User{
	@JsonProperty("issued_to")
	private String issuedTo;
	
	private String audience;
	
	@JsonProperty("user_id")
	private String userId;
	  
	@JsonProperty("expires_in")
	private int expiresIn;
	  
	private String email;
	  
	@JsonProperty("verified_email")
	private boolean verifiedEmail;
	  
	private String issuer;
	  
	@JsonProperty("issued_at")
	private int issuedAt;
	
	private boolean user;
	private boolean manager;
	private boolean admin;

	public User() {
	}

	public String getIssuedTo() {
		return issuedTo;
	}

	public void setIssuedTo(String issuedTo) {
		this.issuedTo = issuedTo;
	}

	public String getAudience() {
		return audience;
	}

	public void setAudience(String audience) {
		this.audience = audience;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isVerifiedEmail() {
		return verifiedEmail;
	}

	public void setVerifiedEmail(boolean verifiedEmail) {
		this.verifiedEmail = verifiedEmail;
	}

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public int getIssuedAt() {
		return issuedAt;
	}

	public void setIssuedAt(int issuedAt) {
		this.issuedAt = issuedAt;
	}

	public boolean isUser() {
		return user;
	}

	public void setUser(boolean user) {
		this.user = user;
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
		return "User [issuedTo=" + issuedTo + ", audience=" + audience + ", userId=" + userId + ", expiresIn="
				+ expiresIn + ", email=" + email + ", verifiedEmail=" + verifiedEmail + ", issuer=" + issuer
				+ ", issuedAt=" + issuedAt + ", user=" + user + ", manager=" + manager + ", admin=" + admin + "]";
	}
}
