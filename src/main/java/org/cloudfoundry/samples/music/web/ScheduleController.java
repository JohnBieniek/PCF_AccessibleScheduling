package org.cloudfoundry.samples.music.web;//Ignore complaints

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cloudfoundry.samples.music.managers.EmployeeShiftMapManager;
import org.cloudfoundry.samples.music.managers.ScheduleManager;
import org.cloudfoundry.samples.music.managers.ShiftAssignmentManager;
import org.cloudfoundry.samples.music.managers.ShiftGenerationManager;
import org.cloudfoundry.samples.music.managers.ShiftManager;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoClientRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoClientRequestRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.MongoEmployeeRepository;
import org.cloudfoundry.samples.music.repositories.mongodb.ScheduleStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.core.JsonParseException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.cloud.cloudfoundry.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.repository.CrudRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import accessiblesolutions.accessiblescheduling.domain.Client;
import accessiblesolutions.accessiblescheduling.domain.ClientRequest;
import accessiblesolutions.accessiblescheduling.domain.Employee;
import accessiblesolutions.accessiblescheduling.domain.ScheduleStatus;
import accessiblesolutions.accessiblescheduling.domain.Shift;
import accessiblesolutions.accessiblescheduling.exception.CorruptDataException;
import accessiblesolutions.accessiblescheduling.exception.ProccessingException;
import accessiblesolutions.accessiblescheduling.to.ScheduleOptions;

@RestController
@RequestMapping(value = "/schedule")
public class ScheduleController {
    private ScheduleManager manager;
    
    @Autowired
    ShiftManager shiftManager;
    
    @Autowired
    ShiftGenerationManager generationManager;
    
    @Autowired
    ShiftAssignmentManager assignmentManager;
    
    @Autowired
    EmployeeShiftMapManager employeeShiftMapManager;
    
    @Autowired
    private CrudRepository<ScheduleStatus, String> scheduleStatusCrud;
    
    @Autowired
    ScheduleManager scheduleManager;
    
    @Autowired
    private ScheduleStatusRepository scheduleStatusRepository;   

    @Autowired
    private MongoClientRequestRepository mongoRepository;
 
    @Autowired
    private MongoClientRepository clientRepository;
    
    @Autowired
    private MongoEmployeeRepository employeeRepository;
    
    @Autowired
    public ScheduleController(ScheduleManager manager) {
        this.manager=manager;
    }
    
    @RequestMapping(value = "/clientShiftsForWeek",method = RequestMethod.GET)
    public Iterable<Shift> clientShiftsForWeek(@RequestParam String clientId, @RequestParam String month, @RequestParam String day, @RequestParam String year) {
    	return manager.getClientShiftsForWeek(clientId,month,day,year);
    }
     
    @RequestMapping(value = "/employeeShiftsForWeek",method = RequestMethod.GET)
    public Iterable<Shift> employeeShiftsForWeek(@RequestParam String employeeId, @RequestParam String month, @RequestParam String day, @RequestParam String year) {
    	return manager.getEmployeeShiftsForWeek(employeeId,month,day,year);
    }
    
//    @RequestMapping(value = "/currentWeek",method = RequestMethod.GET)
//    public String currentWeek() {
//    	return manager.getCurrentWeek();
//    }
    @RequestMapping(value = "/createEmployee",method = RequestMethod.POST)
    public Iterable<Employee> createEmployee() {
    	Employee employee =new Employee();
    	
    	employee.setFirst("An employee");
    	
    	employeeRepository.save(employee);

        List<Employee> employees = employeeRepository.findAll();
        Collections.sort(employees);
		return employees;
    }    
    @RequestMapping(value = "/createClient",method = RequestMethod.POST)
    public Iterable<Client> createClient() {
    	Client client =new Client();
    	
    	client.setFirst("A client");
    	
        clientRepository.save(client);

        List<Client> clients = clientRepository.findAll();
        Collections.sort(clients);
		return clients;
    }

    @RequestMapping(value = "/updateEmployee",method = RequestMethod.POST)
    public Employee updateEmployee(@RequestParam String param) {
    	Employee employee =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			employee = mapper.readValue(param, Employee.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	employeeRepository.save(employee);
        
        return employeeRepository.findOne(employee.getId());
    }
    @RequestMapping(value = "/updateClient",method = RequestMethod.POST)
    public Client updateClient(@RequestParam String param) {
    	Client client =null;

    	ObjectMapper mapper = new ObjectMapper();
    	
    	try {
			client = mapper.readValue(param, Client.class);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        clientRepository.save(client);
        
        return clientRepository.findOne(client.getId());
    }

    @RequestMapping(value = "/deleteRequest", method = RequestMethod.GET)
    public List<ClientRequest> deleteById(@RequestParam String id) {
    	ClientRequest request = mongoRepository.findOne(id);
    	String clientId=request.getClientId();
    	mongoRepository.delete(id);
        
        return mongoRepository.findByClientId(clientId);
    }
    
    @RequestMapping(value = "/clientsRequests", method = RequestMethod.GET)
    public Iterable<ClientRequest> clientsRequests(@RequestParam String clientId) {
        return mongoRepository.findByClientId(clientId);
    }

    @RequestMapping(value = "/byMonth", method = RequestMethod.DELETE)
    public Iterable<ScheduleStatus> deleteByMonth(@RequestParam("month") String  month) {
    	 ScheduleStatus status = new ScheduleStatus();
         
     	status.setMonth(month);
     	    	
     	scheduleStatusRepository.deleteByMonth(month);
     	scheduleStatusCrud.save(status);
     	
     
        shiftManager.deleteShiftsForMonth(Integer.parseInt(month));
    	return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/staffSuggestion", method = RequestMethod.GET)
    public void staffSuggestion(@RequestParam("shiftId") String shiftId) throws CorruptDataException, ProccessingException {
        //return assignmentManager.getStaffSuggestion();
    }
    
    @RequestMapping(value = "/durationOfWeeksShifts", method = RequestMethod.GET)
    public float durationOfWeeksShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
        return shiftManager.getDurationOfShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
    }
    
//    @RequestMapping(value = "/staffWeekdayShift", method = RequestMethod.GET)
//    public String staffWeekdayShift(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekdayShiftStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeekdayShifts", method = RequestMethod.GET)
//    public String staffWeekdayShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekdayShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeekendShift", method = RequestMethod.GET)
//    public String staffWeekendShift(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekendShiftStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeekendShifts", method = RequestMethod.GET)
//    public String staffWeekendShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleWeekendShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
//    
//    @RequestMapping(value = "/staffWeeksShifts", method = RequestMethod.GET)
//    public String staffWeeksShifts(@RequestParam("week") String week,@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        return assignmentManager.scheduleShiftsStartingWeekOfMonth(Integer.parseInt(week),Integer.parseInt(month),Integer.parseInt(year));
//    }
    
    @RequestMapping(value = "/staffPreassignedShifts", method = RequestMethod.GET)
    public String staffPreassignedShifts(@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
    	ScheduleOptions options = new ScheduleOptions();
    	options.setMonth(month);
    	options.setYear(year);
    	return assignmentManager.staffPreassignedShifts(options);
    }
    
    @RequestMapping(value = "/staffShiftsSafely", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> staffShiftsSafely(@RequestParam("month") String month
    													,@RequestParam("year") String year
    													,@RequestParam("allowOvertime") boolean allowOvertime
    													,@RequestParam("allowInactive") boolean allowInactive
    													,@RequestParam("allowUnavailable") boolean allowUnavailable
    													,@RequestParam("prioritizeSecondShift") boolean prioritizeSecondShift
    													,@RequestParam("dailyMax") boolean dailyMax
    													,@RequestParam("weeklyMax") boolean weeklyMax) {
    	System.out.println("Starting assignment");
    	ScheduleStatus status = scheduleStatusCrud.findOne(month);
    	scheduleStatusRepository.deleteByMonth(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	if(status.isGenerated()) {
    		status.setAssigning(true);
        	scheduleStatusCrud.save(status);
    	}
    	
    	ScheduleOptions options = new ScheduleOptions(month, year, allowOvertime, allowInactive,allowUnavailable, 
    													prioritizeSecondShift, dailyMax,weeklyMax);
    	System.out.println("Schedule options:"+options.toString());
    	
		assignmentManager.scheduleShifts(options);

    	return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/assigning", method = RequestMethod.GET)
    public boolean assigning(@RequestParam("month") String month) {
    	return assignmentManager.scheduleStatus(month).isAssigning();
    }
    
    @RequestMapping(value = "/statusList", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> scheduleStatusList() {
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/unscheduled", method = RequestMethod.GET)
    public int unscheduled(@RequestParam("month") String month) throws NumberFormatException, ProccessingException {
        return shiftManager.getUnassignedShiftsForMonth(Integer.parseInt(month)).size();
    }
    
    @RequestMapping(value = "/scheduled", method = RequestMethod.GET)
    public int scheduled(@RequestParam("month") String month) throws NumberFormatException, ProccessingException {
        return shiftManager.getAssignedShiftsForMonth(Integer.parseInt(month)).size();
    }
    
    @RequestMapping(value = "/generateStatusList", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> generateStatusList() {
    	scheduleManager.generateStatusList();
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/finishAssignment", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> finishAssignment(@RequestParam("month") String month) throws CorruptDataException {
    	ScheduleStatus status = assignmentManager.scheduleStatus(month);
    	System.out.println("finishing assignment for "+month);
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	if(status.isAssigning()) {
    		status.setAssigning(false);
    		System.out.println("errored when trying to shut down");
    	}
    	if(status.isGenerated()) {
    		status.setAssigned(true);
    		scheduleStatusRepository.deleteByMonth(month);
        	scheduleStatusCrud.save(status);
    	}
    	
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/stopAssignment", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> stopAssignment(@RequestParam("month") String month) throws CorruptDataException, InterruptedException {
    	ScheduleStatus status = scheduleStatusCrud.findOne(month);
    	scheduleStatusRepository.deleteByMonth(month);
    	
    	if(null==status) {
    		status= new ScheduleStatus();
    		status.setMonth(month);
    	}
    	
    	status.setGenerated(true);
		status.setAssigning(true);
		status.setStopped(true);
    	scheduleStatusCrud.save(status);
    	Thread.sleep(5000);
    	finishAssignment(month);
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateShifts", method = RequestMethod.GET)
    public Iterable<ScheduleStatus> generateShifts(@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException {
    	generationManager.generateShifts(month,year);//manager.generateShifts(month);
        return scheduleStatusCrud.findAll();
    }
    
    @RequestMapping(value = "/generateSingleShifts", method = RequestMethod.GET)
    public String generateSingleShifts(@RequestParam("month") String month) throws CorruptDataException {
        return manager.generateSingleShifts(month);
    }
    
//    @RequestMapping(value = "/staffShifts", method = RequestMethod.GET)
//    public String staffShifts(@RequestParam("month") String month,@RequestParam("year") String year) throws CorruptDataException, ProccessingException {
//        ScheduleOptions options = new ScheduleOptions();
//        options.setMonth(month);
//        options.setYear(year);
//    	return manager.staffShifts(options);
//    }
    
    @RequestMapping(value = "/getShiftsForMonth", method = RequestMethod.GET)
    public String getShiftsForOfMonth(@RequestParam("month") String month) {
        return shiftManager.getShiftsForMonth(Integer.parseInt(month)).toString();
    }
    
    @RequestMapping(value = "/getShiftsPerEmployeeForMonth", method = RequestMethod.GET)
    public String getShiftsPerEmployeeForOfMonth(@RequestParam("month") String month) throws ProccessingException {
        return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).toString();
    }
    
    //TODO kill it
    @RequestMapping(value = "/getAssignedShiftsForEmployeeForMonth", method = RequestMethod.GET)
    public ArrayList<Shift> getAssignedShiftsForEmployeeForMonth(@RequestParam("employee") String employee) throws ProccessingException  {
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(4).get(employee);
    	//return manager.getAssignedShiftsForEmployeeForMonth("584890232b3acf554ef8d88f", Integer.parseInt(month));
    }
    
  //TODO kill it
    @RequestMapping(value = "/getAssignedShiftsForEmployeeForMonth2", method = RequestMethod.GET)
    public String getAssignedShiftsForEmployeeForMonth2(@RequestParam("month") String month) throws ProccessingException {
    	return employeeShiftMapManager.getAssignedShiftsPerEmployeeForMonth(Integer.parseInt(month)).keySet().toArray().toString();
    	//return manager.getAssignedShiftsForEmployeeForMonth("584890232b3acf554ef8d88f", Integer.parseInt(month));
    }
}