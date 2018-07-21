package accessiblesolutions.accessiblescheduling.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomFieldData {
	@Id
	@Column(length = 40)
	@GeneratedValue(generator = "randomId")
	@GenericGenerator(name = "randomId", strategy = "org.cloudfoundry.samples.music.domain.RandomIdGenerator")
	private String id;
	
	private boolean booleanData;

	private String customFieldId;

	private String ownerId;

	private String variableType = "boolean";

	public CustomFieldData() {
	}

	public boolean getBooleanData() {
		return booleanData;
	}

	public String getCustomFieldId() {
		return customFieldId;
	}

	public String getId() {
		return id;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public String getVariableType() {
		return variableType;
	}

	public void setBooleanData(boolean booleanData) {
		this.booleanData = booleanData;
	}

	public void setCustomFieldId(String customFieldId) {
		this.customFieldId = customFieldId;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
}
