package accessiblesolutions.accessiblescheduling.domain;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import accessiblescheduling.domain.Client;
public class ClientSpec {
	@Test
	public void emptyConstructorInitializesVariables() {
		Client emptyClient = new Client();
		
		assertTrue(null==emptyClient.getId());
		
		assertTrue(""==emptyClient.getName());
		
		assertTrue(null==emptyClient.getFavoriteStaffId());
		
		assertFalse(emptyClient.getOwnsCats());
		assertFalse(emptyClient.getNoSmokers());
		
		assertFalse(emptyClient.getNoMaleStaff());
		assertFalse(emptyClient.getNoFemaleStaff());
		
		assertFalse(emptyClient.getPreferSigning());
		assertFalse(emptyClient.getSigningOnly());
		
		assertFalse(emptyClient.getMedPass());
		
		assertFalse(emptyClient.getFixedSchedule());
	}
	
	
	@Test
	public void setersUpdateVariables() {
		String sampleString1 = "SampleString1";

		Client client = new Client();
		
		assertTrue(null==client.getId());
		client.setId(sampleString1);
		assertTrue(sampleString1.equals(client.getId()));
		
		assertTrue(null==client.getFavoriteStaffId());
		client.setFavoriteStaffId(sampleString1);
		assertTrue(sampleString1.equals(client.getFavoriteStaffId()));
		
		assertFalse(client.getOwnsCats());
		client.setOwnCats(true);
		assertTrue(client.getOwnsCats());
		
		assertFalse(client.getNoSmokers());
		client.setNoSmokers(true);
		assertTrue(client.getNoSmokers());
		
		assertFalse(client.getNoMaleStaff());
		client.setNoMale(true);
		assertTrue(client.getNoMaleStaff());
		
		assertFalse(client.getNoFemaleStaff());
		client.setNoFemale(true);
		assertTrue(client.getNoFemaleStaff());
		
		assertFalse(client.getPreferSigning());
		client.setPreferSigning(true);
		assertTrue(client.getPreferSigning());
		
		assertFalse(client.getSigningOnly());
		client.setSigningOnly(true);
		assertTrue(client.getSigningOnly());
		
		assertFalse(client.getMedPass());
		client.setMedPass(true);
		assertTrue(client.getMedPass());
		
		assertFalse(client.getFixedSchedule());
		client.setFixedSchedule(true);
		assertTrue(client.getFixedSchedule());
	}
}
