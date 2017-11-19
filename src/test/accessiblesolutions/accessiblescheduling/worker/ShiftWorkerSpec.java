package accessiblesolutions.accessiblescheduling.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;

public class ShiftWorkerSpec {
	@Test
	public void assignRequestedStaffGivesEmployeesThierShifts() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts1 = new ArrayList<Shift>();
		ArrayList<Shift> shifts2 = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		Shift shift2 = new Shift();
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		String id1 ="employeeId1";
		String name1 = "TestA";
		
		String id2 ="employeeId2";
		String name2 = "TestB";
		
		HashMap<String, ArrayList<Shift>> shiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
		
		shift1.setRequestedStaffId(id1);
		shift1.setRequestedStaffName(name1);
		shifts1.add(shift1);
		
		shift2.setRequestedStaffId(id1);
		shift2.setRequestedStaffName(name1);
		shifts1.add(shift2);
		shiftsPerEmployee.put(id1, shifts1);
		
		shift3.setRequestedStaffId(id2);
		shift3.setRequestedStaffName(name2);
		shifts2.add(shift3);
		
		shift4.setRequestedStaffId(id2);
		shift4.setRequestedStaffName(name2);
		shifts2.add(shift4);
		shiftsPerEmployee.put(id2, shifts2);
		
		try {
			assignedShifts=ShiftWorker.assignRequestedStaff(shiftsPerEmployee);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertNotNull(assignedShifts);
		
		assertTrue(assignedShifts.get(0).getAssigned());
		assertEquals(assignedShifts.get(0).getRequestedStaffId(),id2);
		assertEquals(assignedShifts.get(0).getRequestedStaffName(),name2);
		assertTrue(assignedShifts.get(1).getAssigned());
		assertEquals(assignedShifts.get(1).getRequestedStaffId(),id2);
		assertEquals(assignedShifts.get(1).getRequestedStaffName(),name2);
		
		assertTrue(assignedShifts.get(2).getAssigned());
		assertEquals(assignedShifts.get(2).getRequestedStaffId(),id1);
		assertEquals(assignedShifts.get(2).getRequestedStaffName(),name1);
		assertTrue(assignedShifts.get(3).getAssigned());
		assertEquals(assignedShifts.get(3).getRequestedStaffId(),id1);
		assertEquals(assignedShifts.get(3).getRequestedStaffName(),name1);
		
		assertFalse(exception);
	}
	
	@Test
	public void assignRequestedStaffGivesOneEmployeeThierShift() {
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		ArrayList<Shift> assignedShifts = null;
		Shift shift = new Shift();
		String id ="employeeId";
		String name = "TestName";
		HashMap<String, ArrayList<Shift>> shiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
		
		shift.setRequestedStaffId(id);
		shift.setRequestedStaffName(name);
		shifts.add(shift);
		shiftsPerEmployee.put(id, shifts);
		
		try {
			assignedShifts=ShiftWorker.assignRequestedStaff(shiftsPerEmployee);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertNotNull(assignedShifts);
		
		assertTrue(assignedShifts.get(0).getAssigned());
		assertEquals(assignedShifts.get(0).getRequestedStaffId(),id);
		assertEquals(assignedShifts.get(0).getRequestedStaffName(),name);
		
		assertFalse(exception);
	}
	
	@Test
	public void assignRequestedStaffReturnsEmptyForNullMap() {
		boolean exception=false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		ArrayList<Shift> assignedShifts = null;
		Shift shift = new Shift();
		String id ="employeeId";
		String name = "TestName";
		HashMap<String, ArrayList<Shift>> shiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
		
		shift.setRequestedStaffId(id);
		shift.setRequestedStaffName(name);
		shifts.add(shift);
		shiftsPerEmployee.put(id, shifts);
		
		try {
			assignedShifts=ShiftWorker.assignRequestedStaff(null);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(assignedShifts.size()==0);
		assertFalse(exception);
	}
	
	@Test
	public void assignRequestedStaffReturnsEmptyForNullShiftList() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		try {
			assignedShifts=ShiftWorker.assignRequestedStaff(null);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(assignedShifts.size()==0);
		assertFalse(exception);
	}
	
	@Test
	public void assignRequestedStaffReturnsEmptyForEmptyShiftList() {
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		ArrayList<Shift> assignedShifts = null;
		String id ="employeeId";
		HashMap<String, ArrayList<Shift>> shiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
		
		shiftsPerEmployee.put(id, shifts);
		
		try {
			assignedShifts=ShiftWorker.assignRequestedStaff(shiftsPerEmployee);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(assignedShifts.size()==0);
		assertFalse(exception);
	}
	
	@Test
	public void assignRequestedStaffThrowsExceptionForUnrequestedStaffId() {
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		String id ="employeeId";
		String name = "TestName";
		HashMap<String, ArrayList<Shift>> shiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
		
		shift.setRequestedStaffName(name);
		shifts.add(shift);
		shiftsPerEmployee.put(id, shifts);
		
		try {
			ShiftWorker.assignRequestedStaff(shiftsPerEmployee);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void assignRequestedStaffThrowsExceptionForUnrequestedStaffName() {
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		String id ="employeeId";
		HashMap<String, ArrayList<Shift>> shiftsPerEmployee = new HashMap<String, ArrayList<Shift>>();
		
		shift.setRequestedStaffId(id);
		shifts.add(shift);
		shiftsPerEmployee.put(id, shifts);
		
		try {
			ShiftWorker.assignRequestedStaff(shiftsPerEmployee);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getAssignedShiftsReturnsEmptyWithNothingAssigned() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		
		String id1 ="employeeId1";
		String name1 = "TestA";
		
		shift1.setRequestedStaffId(id1);
		shift1.setRequestedStaffName(name1);
		shifts.add(shift1);
		
		assignedShifts=ShiftWorker.getAssignedShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(assignedShifts);
		assertTrue(assignedShifts.size()==0);
	}
	
	@Test
	public void getAssignedShiftsReturnsEmptyForNullInput() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		
		String id1 ="employeeId1";
		String name1 = "TestA";
		
		shift1.setRequestedStaffId(id1);
		shift1.setRequestedStaffName(name1);
		shifts.add(shift1);
		
		assignedShifts=ShiftWorker.getAssignedShifts(null);
		
		assertFalse(exception);
		
		assertNotNull(assignedShifts);
		assertTrue(assignedShifts.size()==0);
	}
	
	@Test
	public void getAssignedShiftsReturnsEmptyForArrayContainingNull() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		
		String id1 ="employeeId1";
		String name1 = "TestA";
		
		shift1.setRequestedStaffId(id1);
		shift1.setRequestedStaffName(name1);
		shifts.add(null);
		
		assignedShifts=ShiftWorker.getAssignedShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(assignedShifts);
		assertTrue(assignedShifts.size()==0);
	}
	
	@Test
	public void getAssignedShiftsReturnsEmptyForArrayContainingNullAndUnassignedShift() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		
		String id1 ="employeeId1";
		String name1 = "TestA";
		
		shift1.setRequestedStaffId(id1);
		shift1.setRequestedStaffName(name1);
		shifts.add(shift1);
		shifts.add(null);
		
		assignedShifts=ShiftWorker.getAssignedShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(assignedShifts);
		assertTrue(assignedShifts.size()==0);
	}
	
	@Test
	public void getAssignedShiftsReturns1ForArrayContainingNullAndAssignedShift() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		
		String name1 = "TestA";
		
		shift1.setAssigned(true);
		shift1.setStaffName(name1);
		shifts.add(shift1);
		shifts.add(null);
		
		assignedShifts=ShiftWorker.getAssignedShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(assignedShifts);
		assertTrue(assignedShifts.size()==1);
	}
	
	@Test
	public void getAssignedShiftsReturnsAssignedForArrayContainingAssignedAndUnassignedShifts() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		Shift shift2 = new Shift();
		Shift shift3 = new Shift();
		
		String name1 = "TestA";
		String name3 = "TestC";
		
		shift1.setAssigned(true);
		shift1.setStaffName(name1);
		
		shift2.setAssigned(false);
		
		shift3.setAssigned(true);
		shift3.setStaffName(name3);
		
		shifts.add(shift1);
		shifts.add(shift2);
		shifts.add(shift3);
		
		assignedShifts=ShiftWorker.getAssignedShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(assignedShifts);
		assertTrue(assignedShifts.size()==2);
	}
	
	@Test
	public void getEmployeeShiftMapReturnsEmptyForNullInput(){
		boolean exception = false;
		HashMap<String, ArrayList<Shift>> map = null;

		try {
			map=ShiftWorker.getPrestaffedEmployeeShiftMap(null);
		} catch (ProccessingException e) {
			exception=true;
		}
		assertFalse(exception);
		
		assertNotNull(map);
		assertTrue(map.isEmpty());
	}
	
	@Test
	public void getEmployeeShiftMapReturnsEmptyForArrayContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = null;

		shifts.add(null);
		try {
			map=ShiftWorker.getPrestaffedEmployeeShiftMap(shifts);
		} catch (ProccessingException e) {
			exception=true;
		}
		assertFalse(exception);
		
		assertNotNull(map);
		assertTrue(map.isEmpty());
	}
	
	@Test
	public void getEmployeeShiftMapFailsWithNonPrestaffedShifts(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		Shift shift = new Shift();
		
		shifts.add(shift);
		
		try {
			ShiftWorker.getPrestaffedEmployeeShiftMap(shifts);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getEmployeeShiftMapPopulatesFor1PersonAndShift(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = null;
		Shift shift = new Shift();
		String id = "testA";
		
		shift.setRequestedStaffId(id);
		shifts.add(shift);
		
		try {
			map = ShiftWorker.getPrestaffedEmployeeShiftMap(shifts);
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
		assertNotNull(map);
		assertFalse(map.isEmpty());
		assertTrue(map.size()==1);
		assertTrue(map.keySet().toArray()[0].toString().equalsIgnoreCase(id));
	}
	
	@Test
	public void getEmployeeShiftMapPopulatesFor1PersonAnd2Shifts(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = null;
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		
		shift1.setRequestedStaffId(id1);
		shifts.add(shift1);
		
		shift2.setRequestedStaffId(id1);
		shift2.setRecurring(true);
		shifts.add(shift2);
		
		try {
			map = ShiftWorker.getPrestaffedEmployeeShiftMap(shifts);
		} catch (ProccessingException e) {
			exception=true;
		}
		assertFalse(exception);
		assertNotNull(map);
		assertFalse(map.isEmpty());
		assertTrue(map.size()==1);
		assertTrue(map.keySet().toArray()[0].toString().equalsIgnoreCase(id1));
		assertTrue(map.get(id1).size()==2);
	}
	
	@Test
	public void getEmployeeShiftMapPopulatesFor2PersonAnd1ShiftPer(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = null;
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		
		shift1.setRequestedStaffId(id1);
		shifts.add(shift1);
		
		shift2.setRequestedStaffId(id2);
		shift2.setRecurring(true);
		shifts.add(shift2);
		
		try {
			map = ShiftWorker.getPrestaffedEmployeeShiftMap(shifts);
		} catch (ProccessingException e) {
			exception=true;
		}
		assertFalse(exception);
		assertNotNull(map);
		assertFalse(map.isEmpty());
		assertTrue(map.size()==2);
		assertTrue(map.get(id1).size()==1);
		assertTrue(map.get(id2).size()==1);
	}
	
	@Test
	public void getEmployeeShiftMapPopulatesFor2PersonAnd2ShiftPer(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = null;
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setRequestedStaffId(id1);
		shifts.add(shift1);
		
		shift2.setRequestedStaffId(id1);
		shift2.setRecurring(true);
		shifts.add(shift2);
		
		shift3.setRequestedStaffId(id2);
		shifts.add(shift3);
		
		shift4.setRequestedStaffId(id2);
		shift4.setRecurring(true);
		shifts.add(shift4);
		
		try {
			map = ShiftWorker.getPrestaffedEmployeeShiftMap(shifts);
		} catch (ProccessingException e) {
			exception=true;
		}
		assertFalse(exception);
		assertNotNull(map);
		assertFalse(map.isEmpty());
		assertTrue(map.size()==2);
		assertTrue(map.get(id1).size()==2);
		assertTrue(map.get(id2).size()==2);
	}
	
	@Test
	public void getNonEventShiftsReturnsEmptyWithAllEvents() {
		boolean exception = false;
		ArrayList<Shift> nonEventShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		
		shift1.setEvent(true);
		shifts.add(shift1);
		
		nonEventShifts=ShiftWorker.getNonEventShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(nonEventShifts);
		assertTrue(nonEventShifts.size()==0);
	}
	
	@Test
	public void getNonEventShiftsReturnsEmptyForNullInput() {
		boolean exception = false;
		ArrayList<Shift> nonEventShifts = null;
		
		nonEventShifts=ShiftWorker.getNonEventShifts(null);
		
		assertFalse(exception);
		
		assertNotNull(nonEventShifts);
		assertTrue(nonEventShifts.size()==0);
	}
	
	@Test
	public void getNonEventShiftsReturnsEmptyForArrayContainingNull() {
		boolean exception = false;
		ArrayList<Shift> nonEventShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		shifts.add(null);
		
		nonEventShifts=ShiftWorker.getNonEventShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(nonEventShifts);
		assertTrue(nonEventShifts.size()==0);
	}
	
	
	@Test
	public void getNonEventShiftsReturnsForArrayContainingEventAndNonEventShifts() {
		boolean exception = false;
		ArrayList<Shift> nonEventShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		Shift shift2 = new Shift();
		Shift shift3 = new Shift();
		
		shift1.setEvent(true);
		
		shift2.setEvent(false);
		
		shift3.setEvent(true);
		
		shifts.add(shift1);
		shifts.add(shift2);
		shifts.add(shift3);
		
		nonEventShifts=ShiftWorker.getNonEventShifts(shifts);
		
		assertFalse(exception);
		
		assertNotNull(nonEventShifts);
		assertTrue(nonEventShifts.size()==1);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = null;
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setRequestedStaffId(id1);
		shifts.add(shift1);
		
		shift2.setRequestedStaffId(id1);
		shift2.setRecurring(true);
		shifts.add(shift2);
		
		shift3.setRequestedStaffId(id2);
		shifts.add(shift3);
		
		shift4.setRequestedStaffId(id2);
		shift4.setRecurring(true);
		shifts.add(shift4);
		
		try {
			map = ShiftWorker.getNonoverlapingShiftsPerEmployee(null);
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
		assertNotNull(map);
		assertTrue(map.isEmpty());
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeFailsWithNullShifts(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setRequestedStaffId(id1);
		shifts.add(shift1);
		
		shift2.setRequestedStaffId(id1);
		shift2.setRecurring(true);
		shifts.add(shift2);
		
		shift3.setRequestedStaffId(id2);
		shifts.add(shift3);
		
		shift4.setRequestedStaffId(id2);
		shift4.setRecurring(true);
		shifts.add(shift4);
		map.put(id1, null);
		
		try {
			ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeFailsWithSomeNullShifts(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setStartDate("2017-10-10");
		shift1.setEndDate("2017-10-10");
		shift1.setStartTime("10:10");
		shift1.setEndTime("11:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("11:10");
		shift2.setEndTime("12:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id2);
		shift3.setStartDate("2017-10-10");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("12:10");
		shift3.setEndTime("13:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("12:10");
		shift4.setEndTime("13:10");
		shifts.add(shift4);
		
		shifts.add(null);
		
		map.put(id1,shifts);
		
		try {
			ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeFailsWithMissingStartTime(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setStartDate("2017-10-10");
		shift1.setEndDate("2017-10-10");
		shift1.setEndTime("11:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("11:10");
		shift2.setEndTime("12:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id2);
		shift3.setStartDate("2017-10-10");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("12:10");
		shift3.setEndTime("13:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("12:10");
		shift4.setEndTime("13:10");
		shifts.add(shift4);
		
		map.put(id1,shifts);
		
		try {
			ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			exception=true;
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeFailsWithMissingStartDate(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setEndDate("2017-10-10");
		shift1.setStartTime("11:10");
		shift1.setEndTime("11:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("11:10");
		shift2.setEndTime("12:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id2);
		shift3.setStartDate("2017-10-10");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("12:10");
		shift3.setEndTime("13:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("12:10");
		shift4.setEndTime("13:10");
		shifts.add(shift4);
		
		map.put(id1,shifts);
		
		try {
			ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			exception=true;
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeFailsWithMissingEndDate(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setStartDate("2017-10-10");
		shift1.setStartTime("11:10");
		shift1.setEndTime("11:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("11:10");
		shift2.setEndTime("12:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id2);
		shift3.setStartDate("2017-10-10");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("12:10");
		shift3.setEndTime("13:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("12:10");
		shift4.setEndTime("13:10");
		shifts.add(shift4);
		
		map.put(id1,shifts);
		
		try {
			ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			exception=true;
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeFailsWithMissingEndTime(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		Shift shift2 = new Shift();
		String id2 = "testB";
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setStartDate("2017-10-10");
		shift1.setEndDate("2017-10-10");
		shift1.setStartTime("11:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("11:10");
		shift2.setEndTime("12:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id2);
		shift3.setStartDate("2017-10-10");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("12:10");
		shift3.setEndTime("13:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("12:10");
		shift4.setEndTime("13:10");
		shifts.add(shift4);
		
		map.put(id1,shifts);
		
		try {
			ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			exception=true;
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeReturnsAllForNoOverlapsForOneEmployeeSameClient(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		String id2= "testB";
		Shift shift2 = new Shift();
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setClientId(id2);
		shift1.setStartDate("2017-10-10");
		shift1.setEndDate("2017-10-10");
		shift1.setStartTime("01:10");
		shift1.setEndTime("02:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setClientId(id2);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("02:10");
		shift2.setEndTime("03:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id1);
		shift3.setClientId(id2);
		shift3.setStartDate("2017-10-10");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("03:10");
		shift3.setEndTime("04:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id1);
		shift4.setClientId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("00:10");
		shift4.setEndTime("01:10");
		shifts.add(shift4);
		
		map.put(id1,shifts);
		
		HashMap<String, ArrayList<Shift>> result=null;
		try {
			result = ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			exception=true;
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
		assertNotNull(result);
		assertNotNull(result.get(id1));
		assertTrue(result.get(id1).size()==4);
	}
	
	@Test
	public void getNonoverlapingShiftsPerEmployeeReturnsAllForNoOverlapsForTwoEmployeesWith1ClientPer(){
		boolean exception = false;
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
		Shift shift1 = new Shift();
		String id1 = "testA";
		String id2 = "testB";
		Shift shift2 = new Shift();
		Shift shift3 = new Shift();
		Shift shift4 = new Shift();
		
		shift1.setStaffId(id1);
		shift1.setClientId(id2);
		shift1.setStartDate("2017-10-10");
		shift1.setEndDate("2017-10-10");
		shift1.setStartTime("01:10");
		shift1.setEndTime("02:10");
		shifts.add(shift1);
		
		shift2.setStaffId(id1);
		shift2.setClientId(id2);
		shift2.setRecurring(true);
		shift2.setStartDate("2017-10-10");
		shift2.setEndDate("2017-10-10");
		shift2.setStartTime("02:10");
		shift2.setEndTime("03:10");
		shifts.add(shift2);
		
		shift3.setStaffId(id1);
		shift3.setClientId(id2);
		shift3.setStartDate("2017-10-09");
		shift3.setEndDate("2017-10-10");
		shift3.setStartTime("22:00");
		shift3.setEndTime("00:10");
		shifts.add(shift3);
		
		shift4.setStaffId(id1);
		shift4.setClientId(id2);
		shift4.setRecurring(true);
		shift4.setStartDate("2017-10-10");
		shift4.setEndDate("2017-10-10");
		shift4.setStartTime("00:10");
		shift4.setEndTime("01:10");
		shifts.add(shift4);
		
		map.put(id1,shifts);
		
		shifts = new ArrayList<Shift>();
		shift1.setStaffId(id2);
		shift1.setClientId(id1);
		shifts.add(shift1);
		
		shift2.setStaffId(id2);
		shift2.setClientId(id1);
		shifts.add(shift2);
		
		shift3.setStaffId(id2);
		shift3.setClientId(id1);
		shifts.add(shift3);
		
		shift4.setStaffId(id2);
		shift4.setClientId(id1);
		shifts.add(shift4);
		
		map.put(id2,shifts);
		
		HashMap<String, ArrayList<Shift>> result=null;
		try {
			result = ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
		} catch (CorruptDataException e) {
			exception=true;
		} catch (ProccessingException e) {
			exception=true;
		}
		
		assertFalse(exception);
		assertNotNull(result);
		assertTrue(result.keySet().size()==2);
		assertNotNull(result.get(id1));
		assertNotNull(result.get(id2));
		assertTrue(result.get(id1).size()==4);
		assertTrue(result.get(id2).size()==4);
	}
	
//	@Test
//	public void getNonoverlapingShiftsPerEmployeeReturnsSomeForSomeOverlapsForOneEmployeeWithMultipleClients(){
//		boolean exception = false;
//		ArrayList<Shift> shifts = new ArrayList<Shift>();
//		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
//		Shift shift1 = new Shift();
//		String id1 = "testA";
//		String id2 = "testB";
//		String id3 = "testC";
//		Shift shift2 = new Shift();
//		Shift shift3 = new Shift();
//		Shift shift4 = new Shift();
//		
//		shift1.setStaffId(id1);
//		shift1.setClientId(id2);
//		shift1.setStartDate("2017-10-09");
//		shift1.setEndDate("2017-10-10");
//		shift1.setStartTime("22:10");
//		shift1.setEndTime("01:10");
//		shifts.add(shift1);
//		
//		shift2.setStaffId(id1);
//		shift2.setClientId(id3);
//		shift2.setRecurring(true);
//		shift2.setStartDate("2017-10-10");
//		shift2.setEndDate("2017-10-10");
//		shift2.setStartTime("01:10");
//		shift2.setEndTime("02:10");
//		shifts.add(shift2);
//		
//		shift3.setStaffId(id1);
//		shift3.setClientId(id2);
//		shift3.setStartDate("2017-10-10");
//		shift3.setEndDate("2017-10-10");
//		shift3.setStartTime("02:00");
//		shift3.setEndTime("03:10");
//		shifts.add(shift3);
//		
//		shift4.setStaffId(id1);
//		shift4.setClientId(id2);
//		shift4.setRecurring(true);
//		shift4.setStartDate("2017-10-10");
//		shift4.setEndDate("2017-10-10");
//		shift4.setStartTime("03:10");
//		shift4.setEndTime("04:10");
//		shifts.add(shift4);
//		
//		map.put(id1,shifts);
//		
//		
//		HashMap<String, ArrayList<Shift>> result=null;
//		try {
//			result = ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
//		} catch (CorruptDataException e) {
//			exception=true;
//		} catch (ProccessingException e) {
//			exception=true;
//		}
//		
//		assertFalse(exception);
//		assertNotNull(result);
//		assertTrue(result.keySet().size()==1);
//		assertNotNull(result.get(id1));
//		System.out.println(result.toString());
//		assertTrue(result.get(id1).size()==1);
//	}
//	
//	@Test
//	public void getNonoverlapingShiftsPerEmployeeReturnsAllForNoOverlapsForTwoEmployeesWithMultipleClients(){
//		boolean exception = false;
//		ArrayList<Shift> shifts = new ArrayList<Shift>();
//		HashMap<String, ArrayList<Shift>> map = new HashMap<String, ArrayList<Shift>>();
//		Shift shift1 = new Shift();
//		String id1 = "testA";
//		String id2 = "testB";
//		String id3 = "testC";
//		Shift shift2 = new Shift();
//		Shift shift3 = new Shift();
//		Shift shift4 = new Shift();
//		
//		shift1.setStaffId(id1);
//		shift1.setClientId(id2);
//		shift1.setStartDate("2017-10-10");
//		shift1.setEndDate("2017-10-10");
//		shift1.setStartTime("01:10");
//		shift1.setEndTime("02:10");
//		shifts.add(shift1);
//		
//		shift2.setStaffId(id1);
//		shift2.setClientId(id3);
//		shift2.setRecurring(true);
//		shift2.setStartDate("2017-10-10");
//		shift2.setEndDate("2017-10-10");
//		shift2.setStartTime("02:10");
//		shift2.setEndTime("03:10");
//		shifts.add(shift2);
//		
//		shift3.setStaffId(id1);
//		shift3.setClientId(id2);
//		shift3.setStartDate("2017-10-09");
//		shift3.setEndDate("2017-10-10");
//		shift3.setStartTime("22:00");
//		shift3.setEndTime("00:10");
//		shifts.add(shift3);
//		
//		shift4.setStaffId(id1);
//		shift4.setClientId(id2);
//		shift4.setRecurring(true);
//		shift4.setStartDate("2017-10-10");
//		shift4.setEndDate("2017-10-10");
//		shift4.setStartTime("00:10");
//		shift4.setEndTime("01:10");
//		shifts.add(shift4);
//		
//		map.put(id1,shifts);
//		
//		shifts = new ArrayList<Shift>();
//		shift1.setStaffId(id2);
//		shift1.setClientId(id1);
//		shifts.add(shift1);
//		
//		shift2.setStaffId(id2);
//		shift2.setClientId(id1);
//		shifts.add(shift2);
//		
//		shift3.setStaffId(id2);
//		shift3.setClientId(id1);
//		shifts.add(shift3);
//		
//		shift4.setStaffId(id2);
//		shift4.setClientId(id1);
//		shifts.add(shift4);
//		
//		map.put(id2,shifts);
//		
//		HashMap<String, ArrayList<Shift>> result=null;
//		try {
//			result = ShiftWorker.getNonoverlapingShiftsPerEmployee(map);
//		} catch (CorruptDataException e) {
//			exception=true;
//		} catch (ProccessingException e) {
//			exception=true;
//		}
//		
//		assertFalse(exception);
//		assertNotNull(result);
//		assertTrue(result.keySet().size()==2);
//		assertNotNull(result.get(id1));
//		assertNotNull(result.get(id2));
//		System.out.println(result.toString());
//		assertTrue(result.get(id1).size()==2);
//		assertTrue(result.get(id2).size()==4);
//	}
	
	@Test
	public void getOvernightShiftsReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getOvernightShifts(null);
		} catch (CorruptDataException | ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getOvernightShiftsReturnsEmptyForEmpty(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getOvernightShifts(new ArrayList<Shift>());
		} catch (CorruptDataException | ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getOvernightShiftsFailsForListContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		shifts.add(null);
		
		try {
			ShiftWorker.getOvernightShifts(shifts);
		} catch (CorruptDataException e) {
			e.printStackTrace();
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getOvernightShiftsFailsForNoStart(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setEndDate("2017-11-10");
		shifts.add(shift);
		
		try {
			ShiftWorker.getOvernightShifts(shifts);
		} catch (CorruptDataException e) {
			exception = true;
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getOvernightShiftsFailsForNoEnd(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setStartDate("2017-11-10");
		shifts.add(shift);
		
		try {
			ShiftWorker.getOvernightShifts(shifts);
		} catch (CorruptDataException e) {
			exception = true;
		} catch (ProccessingException e) {
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getOvernightShiftsFailsReturnsStartDifferentFromEnd(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setStartDate("2017-11-10");
		shift.setEndDate("2017-11-11");
		shifts.add(shift);
		
		shift = new Shift();
		shift.setStartDate("2017-11-10");
		shift.setEndDate("2017-11-10");
		shifts.add(shift);
		
		try {
			shifts=ShiftWorker.getOvernightShifts(shifts);
		} catch (CorruptDataException e) {
			exception = true;
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertEquals(shifts.size(),1);
	}
	
	@Test
	public void getPrestaffedShiftsReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getPrestaffedShifts(null);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getPrestaffedShiftsReturnsEmptyForEmpty(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		try {
			shifts=ShiftWorker.getPrestaffedShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getPrestaffedShiftsFailsForArrayContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		shifts.add(null);
		try {
			shifts=ShiftWorker.getPrestaffedShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getPrestaffedShiftsReturnsThoseWithRequestedStaffId(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setRequestedStaffId("testA");
		shifts.add(shift);
		shift=new Shift();
		shifts.add(shift);
		try {
			shifts=ShiftWorker.getPrestaffedShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertTrue(shifts.size()==1);
	}
	
	@Test
	public void getRecurringShiftsReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getRecurringShifts(null);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getRecurringShiftsReturnsEmptyForEmpty(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		try {
			shifts=ShiftWorker.getRecurringShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getRecurringShiftsFailsForArrayContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		shifts.add(null);
		try {
			shifts=ShiftWorker.getRecurringShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getRecurringShiftsReturnsRecurring(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setRecurring(true);
		shifts.add(shift);
		shift=new Shift();
		shifts.add(shift);
		try {
			shifts=ShiftWorker.getRecurringShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertTrue(shifts.size()==1);
	}
	
	@Test
	public void getSingleShiftsReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getSingleShifts(null);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getSingleShiftsReturnsEmptyForEmpty(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		try {
			shifts=ShiftWorker.getSingleShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getSingleShiftsFailsForArrayContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		shifts.add(null);
		try {
			shifts=ShiftWorker.getSingleShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getSingleShiftsReturnsSingle(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setRecurring(true);
		shifts.add(shift);
		shift=new Shift();
		shifts.add(shift);
		try {
			shifts=ShiftWorker.getSingleShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertTrue(shifts.size()==1);
	}
	
	@Test
	public void getUnassignedShiftsReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getUnassignedShifts(null);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getUnassignedShiftsReturnsEmptyForEmpty(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		try {
			shifts=ShiftWorker.getUnassignedShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getUnassignedShiftsFailsForArrayContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		shifts.add(null);
		try {
			shifts=ShiftWorker.getUnassignedShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertTrue(exception);
	}
	
	@Test
	public void getUnassignedShiftsReturnsUnassigned(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		Shift shift = new Shift();
		shift.setAssigned(true);
		shifts.add(shift);
		shift=new Shift();
		shifts.add(shift);
		try {
			shifts=ShiftWorker.getUnassignedShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertTrue(shifts.size()==1);
		assertFalse(shifts.get(0).getAssigned());
	}
	
	@Test
	public void getWeekdayShiftsReturnsEmptyForNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= null;
		try {
			shifts=ShiftWorker.getWeekdayShifts(null);
		} catch (ProccessingException | CorruptDataException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getWeekdayShiftsReturnsEmptyForEmpty(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		try {
			shifts=ShiftWorker.getWeekdayShifts(shifts);
		} catch (ProccessingException | CorruptDataException e) {
			exception = true;
		}
		
		assertFalse(exception);
		assertNotNull(shifts);
		assertTrue(shifts.isEmpty());
	}
	
	@Test
	public void getWeekdayShiftsFailsForArrayContainingNull(){
		boolean exception = false;
		ArrayList<Shift> shifts= new ArrayList<Shift>();
		shifts.add(null);
		try {
			shifts=ShiftWorker.getWeekdayShifts(shifts);
		} catch (ProccessingException e) {
			exception = true;
		} catch (CorruptDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue(exception);
	}
	
//	@Test
//	public void getWeekdayShiftsReturnsWeekdays(){
//		boolean exception = false;
//		ArrayList<Shift> shifts= new ArrayList<Shift>();
//		Shift shift = new Shift();
//		shift.setStartDate(startDate);
//		shifts.add(shift);
//		shift=new Shift();
//		shifts.add(shift);
//		try {
//			shifts=ShiftWorker.getWeekdayShifts(shifts);
//		} catch (ProccessingException | CorruptDataException e) {
//			exception = true;
//		}
//		assertTrue(false);
//		assertFalse(exception);
//		assertTrue(shifts.size()==1);
//		assertFalse(shifts.get(0).getAssigned());
//	}
}
