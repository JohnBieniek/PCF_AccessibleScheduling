package accessiblescheduling.domain;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import accessiblescheduling.util.Util;
@Entity
@JsonIgnoreProperties
public class Client implements Comparable<Client>{
    @Id
    @Column(length=40)
    @GeneratedValue(generator="randomId")
    @GenericGenerator(name="randomId", strategy="accessiblescheduling.util.RandomIdGenerator")
    private String id;

    private String name;
    private String favoriteStaffId;
    private boolean ownsCats;
    private boolean noSmokers;
    private boolean noMaleStaff;
    private boolean noFemaleStaff;
    private boolean preferSigning;
    private boolean signingOnly;
    private boolean medPass;
    private boolean fixedSchedule;
    private String lastUpdated;
    
    public Client() {
    }
    
    public void setLastUpdatedToNow() {
    	setLastUpdated(LocalDateTime.now().toString());
    }

	
	@JsonIgnore
	public LocalDateTime lastUpdatedTime() {
		return Util.getLocalDateTimeFromString(getLastUpdated());
	}
	@JsonIgnore
	public JSONObject nameInfo() throws JSONException {
		JSONObject json = new JSONObject();
		
		if(name!=null && name!="") {
			json.put("name", this.name);
		}
		json.put("id", id);
		
		return json;
	}
	
	@Override
	public int compareTo(Client client) {
		return getName().toLowerCase().compareTo(client.getName().toLowerCase());
	}

	public String getName() {
		String employeeName = "";
		if(name!=null && name!="") {
			employeeName=name;
		}
		return employeeName;
	}
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    
    public void setFavoriteStaffId(String id){
    	this.favoriteStaffId=id;
    }
    
    public String getFavoriteStaffId(){
    	return favoriteStaffId;
    }
    
    public void setOwnCats(boolean ownsCats) {
        this.ownsCats = ownsCats;
    }

    public boolean getOwnsCats() {
        return ownsCats;
    }
    
    public void setNoSmokers(boolean noSmokers) {
        this.noSmokers = noSmokers;
    }

    public boolean getNoSmokers() {
        return noSmokers;
    }
    
    public void setNoMale(boolean noMaleStaff) {
        this.noMaleStaff = noMaleStaff;
    }

    public boolean getNoMaleStaff() {
        return noMaleStaff;
    }
    
    public void setNoFemale(boolean noFemaleStaff) {
        this.noFemaleStaff = noFemaleStaff;
    }

    public boolean getNoFemaleStaff() {
        return noFemaleStaff;
    }
    
    public void setSigningOnly(boolean signingOnly) {
        this.signingOnly = signingOnly;
    }

    public boolean getSigningOnly() {
        return signingOnly;
    }
    
    public void setMedPass(boolean medPass) {
        this.medPass = medPass;
    }

    public boolean getMedPass() {
        return medPass;
    }
    
    public void setPreferSigning(boolean preferSigning) {
        this.preferSigning = preferSigning;
    }

    public boolean getPreferSigning() {
        return preferSigning;
    }
    
    public void setFixedSchedule(boolean fixedSchedule) {
        this.fixedSchedule = fixedSchedule;
    }

    public boolean getFixedSchedule() {
        return fixedSchedule;
    }

	public String getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(String lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Override
	public String toString() {
		return "Client [id=" + id + ", name="+name + ", favoriteStaffId=" + favoriteStaffId
				+ ", ownsCats=" + ownsCats + ", noSmokers=" + noSmokers + ", noMaleStaff=" + noMaleStaff
				+ ", noFemaleStaff=" + noFemaleStaff + ", preferSigning=" + preferSigning + ", signingOnly="
				+ signingOnly + ", medPass=" + medPass + ", fixedSchedule=" + fixedSchedule + ", lastUpdated="
				+ lastUpdated + "]";
	}

	public void setName(String string) {
		this.name=string;
		
	}	
}
