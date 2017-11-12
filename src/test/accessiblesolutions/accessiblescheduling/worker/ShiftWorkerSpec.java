package accessiblesolutions.accessiblescheduling.worker;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import accessiblesolutions.accessiblescheduling.domain.Shift;
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
	
	public void getAssignedShiftsReturnsAssignedForArrayContainingAssignedAndUnassignedShifts() {
		boolean exception = false;
		ArrayList<Shift> assignedShifts = null;
		
		ArrayList<Shift> shifts = new ArrayList<Shift>();
		
		Shift shift1 = new Shift();
		Shift shift2 = new Shift();
		Shift shift3 = new Shift();
		
		String name1 = "TestA";
		String name2 = "TestB";
		String name3 = "TestC";
		
		shift1.setAssigned(true);
		shift1.setStaffName(name1);
		
		shift2.setAssigned(false);
		shift2.setStaffName(name2);
		
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
}
