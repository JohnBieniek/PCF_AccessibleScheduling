angular.module('client', ['ngResource', 'ui.bootstrap']).
	factory("EditorStatus", function () {
        var editorEnabled = {};

        var enable = function (id, fieldName) {
            editorEnabled = { 'id': id, 'fieldName': fieldName };
        };

        var disable = function () {
            editorEnabled = {};
        };

        var isEnabled = function(id, fieldName) {
            return (editorEnabled['id'] == id && editorEnabled['fieldName'] == fieldName);
        };

        return {
            isEnabled: isEnabled,
            enable: enable,
            disable: disable
        }
    });

function MobileEmployeeController($scope, $modal, $http) {
	$scope.init = function(){
		 $scope.newDate=$scope.week.getFullYear()+"-"+(($scope.week.getMonth()+1)<10?"0"+($scope.week.getMonth()+1):($scope.week.getMonth()+1))+"-"+$scope.week.getDate();
		 
		 if($scope.manager){
	    	 $scope.getClientNames();
		 }
		 
		 $scope.listCustomFields();
		 
		 if($scope.selectedEmployee==undefined || $scope.selectedEmployee == null){
            $scope.setEmployeeToUser();
		 }

		 $scope.getDisplayWeek();

		 $scope.listShifts();
	}
	
	/**
	 * $scope.updateLastInteractionTime() is called from the setEmployeeTab
	 * function
	 */
	$scope.setEmployeeTabAndInfo=function(tab){
		$scope.updateEmployee();
		$scope.getAllEmployeeCustomFieldData();
	    $scope.listShifts();
		$scope.setEmployeeTab(tab);
	}
	
	$scope.deleteAvailability=function(availability){ 
		$scope.updateLastInteractionTime();
		var currentEmployee = $scope.employee;
		if(confirm("Are you sure you want to delete the selected availability? for "+ $scope.employee.days[availability]+ "?")){
			currentEmployee.startTimes.splice(availability,1);    
			currentEmployee.days.splice(availability,1);
			currentEmployee.endTimes.splice(availability,1); 

	        $scope.saveEmployee(currentEmployee);
	    }
    }
	
	$scope.addAvailability= function(day){
		$scope.updateLastInteractionTime();
		var currentEmployee = $scope.employee;
		if($scope.employee){
			currentEmployee.days.push(day);
			currentEmployee.startTimes.push("08:00");
			currentEmployee.endTimes.push("16:00");
	        $scope.saveEmployee(currentEmployee);		
		}
	}
	
	$scope.isAvailabilityDay= function(availability,day){
		return day==$scope.employee.days[availability];
	}
	
	$scope.noAvailabilityDay= function(availability,day){
		var unavailable =true;
		
		if($scope.emplyoee && $scope.employee.days){
			$scope.employee.days.forEach(function(selectedDay) {
			  if(selectedDay==day){
			    unavailable=false;
			  }
			});
		}
		
		return unavailable;
	}
	
	$scope.setEmployeeToUser = function(){
		$http({
            url: '/employees/'+$scope.profile.employeeId,
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(typeof $scope.employee !== 'undefined' && $scope.employee!=null && $scope.employee.role==null){
  			  response.data.role="user";
  		  	}

        	$scope.setEmployee(response.data);
        	$scope.setSelectedEmployeeWithoutGetEmployee(response.data);
        });
	}
	
	/**
	 * Changes which employee we are looking at
	 */
	$scope.setEmployeeAndInfo = function(newEmployee){		
	  $scope.detailsChanged=false;
	  
	  if(newEmployee.role==null){
		  newEmployee.role="user";
	  }
      $scope.setSelectedEmployee(newEmployee);
      $scope.getAllEmployeeCustomFieldData();
      $scope.listShifts();
	}
	 
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
	
	/**
	 * Toggled whenever a user changes something in the details section. Used to
	 * determine if the ok and cancel buttons should show.
	 */
	$scope.setDetailsToChanged = function(){
	  $scope.updateLastInteractionTime();
      $scope.detailsChanged=true;;
	}
	
     
	/**
	 * Updates the employee and its custom field data. Checks for updates to the
	 * employee names list afterwards and updates if needbe
	 */
     $scope.saveEmployee = function saveEmployee(employee) {
     	if(employee.availability){
	     	for(var index = 0; index<employee.availability.length;index++){
				if(!employee.availabilityStartTimes){
					employee.availabilityStartTimes=[];
				}
				if(!employee.availabilityEndTimes){
					employee.availabilityEndTimes=[];
				}			
				if(!employee.availabilityDays){
					employee.availabilityDays=[];
				}
				employee.availabilityStartTimes.push(employee.availability[index].startTime);
				employee.availabilityEndTimes.push(employee.availability[index].endTime);
				employee.days.push(employee.availability[index].day);	
			}
      	}
 	   $http({
           url: '/employees',
           method: 'POST',
           headers: {
               'Authorization': $scope.idToken,
               'Content-Type': 'application/x-www-form-urlencoded'
           },
           params: {
        	   param: employee
           }
       })
       .then(function (response) {// TODO handle error state
    	   if(response.data){
	    	   $scope.setEmployeeAndInfo(response.data);
	       	   employee.id=response.data.id;
	           if(employee.customFields){
		            var size = employee.customFields.length;
		           
		            for(var i = 0; i < size ;i++){
		                $http({
		                    url: 'https://scheduleaccessqa.cfapps.io/compatibility/setCustomFieldData',
		                    method: 'POST',
		                    headers: {
		                        'Content-Type': 'application/x-www-form-urlencoded'
		                    },
		                    params: {
		                        employee: employee,
		                        customField: employee.customFields[i],
		                        value:employee.customValue[i]
		                    }
		                });
		            }
	           }
	           $scope.listEmployeesAndInfo();
			   $scope.notify("Employee saved.");
    	   }
    	   else{
    		   $scope.warn("Failed to update employee. Please try again shortly. If problems persist contact your representative.");
    	   }
       });
    }
     
     /**
		 * Updates the provided object with .displayValue containing a human
		 * readable string of what this shift is for. Differs from the client
		 * version in its description of who this is for.
		 */
     $scope.setShiftDisplay = function(shift){
    	if(shift.startsLocalDateTime==null || shift.startsLocalDateTime==undefined){
			 return null;
		}
		 
		let startHour = parseInt(shift.startsLocalDateTime.hour);
	 	let endHour= parseInt(shift.endsLocalDateTime.hour);
	 	
	 	let startMinute = parseInt(shift.startsLocalDateTime.minute);
	 	let endMinute= parseInt(shift.endsLocalDateTime.minute);
	 	
	 	let startModifier = "AM";
	 	let endModifier = "AM";
	 	
	 	if(startHour>11){
	 		startHour-=12;
	 		startModifier="PM"
	 	}
	 	
	 	if(endHour>11){
	 		endHour-=12;
	 		endModifier="PM"
	 	}
	 	
	 	if(startMinute<10){
	 		startMinute="0"+startMinute
	 	}
	 	
	 	if(endMinute<10){
	 		endMinute="0"+endMinute
	 	}
        shift.displayValue = (startHour!=0?startHour:"12")+":"+startMinute+startModifier+"-";
        shift.displayValue += (endHour!=0?endHour:"12")+":"+endMinute+endModifier;
		 
		shift.displayValue+=" with "+shift.clientName;
		 
		 shift.displayValue+= ".";
    }
     
	$scope.isSet = function(tabNum){
		return $scope.employeeTab === tabNum;
    };
    
    $scope.isEmployeeSet = function(employee){
        return employee !==null && $scope.employee !==null && employee !==undefined && $scope.employee !==undefined && $scope.employee.id === employee.id;
    };
      
	$scope.isEmployeeChangeValid = function(employee){
	      let valid = true;
	      
	      if(employee == undefined || employee == null || employee.first ==undefined || employee.first.length<1){
	    	  valid=false;
	      }
	      
	      if(!$scope.detailsChanged){
	    	  valid=false;
	      }
	
	      return valid;
	};
    
	$scope.setInitialDays = function setInitialDays(request){
		 if(request.days==null || request.days==undefined){
			 request.days=[false,false,false,false,false,false,false];
		 }
	}
	 	    
	$scope.getDisplayValue = function getDisplayValue(request) {
		if(request.startsLocalDateTime==null || request.startsLocalDateTime==undefined){
			 return null;
		}
		 
		let startHour = parseInt(request.startsLocalDateTime.hour);
	 	let endHour= parseInt(request.endsLocalDateTime.hour);
	 	
	 	let startMinute = parseInt(request.startsLocalDateTime.minute);
	 	let endMinute= parseInt(request.endsLocalDateTime.minute);
	 	
	 	let startModifier = "AM";
	 	let endModifier = "AM";
	 	
	 	if(startHour>11){
	 		startHour-=12;
	 		startModifier="PM"
	 	}
	 	
	 	if(endHour>11){
	 		endHour-=12;
	 		endModifier="PM"
	 	}
	 	
	 	if(startMinute<10){
	 		startMinute="0"+startMinute
	 	}
	 	
	 	if(endMinute<10){
	 		endMinute="0"+endMinute
	 	}
	 	
        request.displayValue = (startHour!=0?startHour:"12")+":"+startMinute+startModifier+"-";
        request.displayValue += (endHour!=0?endHour:"12")+":"+endMinute+endModifier;
		 
		if(request.requestEmployee){
			 request.displayValue+=" with "+request.staffName;
		}
		if(request.repeats){
			 request.displayValue+= " every "+request.repeatsEvery + " " +request.interval; 
		}
		
		if(request.days && request.interval=="week(s)"){
			 request.displayValue+=" [";
			 if(request.days[0]){
				 request.displayValue+="Su";
			 }
			 if(request.days[1]){
				 request.displayValue+="M";
			 }
			 if(request.days[2]){
				 request.displayValue+="Tu";
			 }
			 if(request.days[3]){
				 request.displayValue+="W";
			 }
			 if(request.days[4]){
				 request.displayValue+="Th";
			 }
			 if(request.days[5]){
				 request.displayValue+="F";
			 }
			 if(request.days[6]){
				 request.displayValue+="Sa";
			 }
			 request.displayValue+="]";
		}
		var weekOfMonth = 0;
		var dateCursor = request.startsLocalDate.dayOfMonth;
		 
		for(var i =0; i<8;i++){
			 if(dateCursor>0){
				 dateCursor-=7;
				 weekOfMonth+=1;
			 }
		}
		 
		if(request.interval=="month(s)"){
			 if(request.monthInterval=="days"){
				 request.displayValue+=" on day "+request.startsLocalDate.dayOfMonth;
			 }
			 else{
				 request.displayValue+=" on "+request.startsLocalDate.dayOfWeek + " of week "+weekOfMonth;
			 }
		}
		if(request.interval=="year(s)"){
			 request.displayValue+= " in " +request.startsLocalDate.month;
			 if(request.yearInterval=="days"){
				 request.displayValue+=" on day "+request.startsLocalDate.dayOfMonth;
			 }
			 else{
				 request.displayValue+=" on "+request.startsLocalDate.dayOfWeek + " of week "+weekOfMonth;
			 }
		}
		 
		if(request.repeats){
			 request.displayValue+= " starting "+request.startDate; 
		}
		else{
			 request.displayValue+=" on "+ request.startDate;
		}

		// Add exceptions
		if(request.exceptions !=undefined && request.exceptions!=null && request.exceptions.length>0){
			 request.displayValue+=" except ";
			 
			 if(request.exceptions.length<4){
				 for(var index = 0; index <request.exceptions.length;index++){
					 if(index==request.exceptions.length-1 && request.exceptions.length>1){
						 request.displayValue+=" and " 
					 }
					 else if(index>0){
						 request.displayValue+=" ,"
					 }
					 request.displayValue+=request.exceptions[index];
				 }
			 }
			 else{
				 request.displayValue+=" as noted";
			 }
		}
		 
		request.displayValue+=".";
	}

	$scope.listEmployeesAndInfo = function listEmployeesAndInfo(){
		$scope.getEmployeeNames();
    	$scope.getClientNames();
		$scope.getDisplayWeek();
	}
    
	/**
	 * Enters or updates the provided shift in the database. Requires a unique
	 * version per page due to the differences in how shifts are listed
	 * afterward.
	 */
    function saveShift(shift) {
    	$http({
            url: '/shifts',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	param: shift
            }
        })
        .then(function(response) {
        	$scope.notify("Shift saved");
        	
            $scope.listShifts();
        });
    }

    /**
	 * When you click on a shift see if it's around. If it is bring them to an
	 * edit modal. When accepting changes in the modal we check to ensure we
	 * aren't overwriting any changes that just happened while we were editing.
	 * If changes occurred check if the user wants to view the changes or
	 * overwrite with their edits. Accept their input then refresh the list of
	 * shifts to reflect any changes.
	 */
    $scope.editShift = function (shift) {
       $scope.updateLastInteractionTime();
       // Only managers and above can currently edit shifts
	   if($scope.profile && $scope.profile.manager){
		   var lastUpdated = (shift.lastUpdated!=null?shift.lastUpdated:"null");// Account
																				// for
																				// old
																				// data
																				// having
																				// no
																				// lastUpdated
																				// info
		   $http({// Ensure this shift hasn't been deleted out from under us
					// before we start editing
	            url: '/schedule/shiftWasUpdated',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	lastUpdated:lastUpdated,
	            	shiftId:shift.id// If it can be deleted it already exists
									// and has an id, unlike new shifts
	            }
	        })
	        .then(function(response) {
	        	if(response.data=="DELETED"){// If this was delted out from
												// under us let the user know
												// and refresh our dated shift
												// list
	        	   $scope.warn("This shift has just been deleted by another user. If you still wish to make edits please make a new shift.");
	        	   $scope.listShifts();
	   	    	}
	        	else{// If, as usual, this shift is around, edit it
			       var updateModal = $modal.open({
			           templateUrl: 'templates/modal/shiftForm.html',
			           controller: ShiftModalController,
			           resolve: {
			        	   idToken: function(){
			        		   return $scope.clone($scope.idToken);
			        	   },
			               shift: function() {
			                   return $scope.clone(shift);
			               },
			               client: function(){
			            	   return {};
			               },
			               clients: function(){
			           		return {};
				           	},
				           	employees:function(){
				        		return $scope.clone($scope.employees);
				        	},
				           	date: function(){
				         	   return $scope.week;
				            },
			               action: function() {
			                   return 'update';
			               }
			           }
			       });
			
			       updateModal.result.then(function (shift) {
		    		   saveShift(shift);// Update this shift on the server if ok
										// was pressed
			           $scope.listShifts();// Refresh our list to show the
											// update and any others that may
											// have occurred while editing
			       }, function () {
			           $scope.listShifts();// If we canceled we still want to
											// make sure we come back to the
											// latest shift data
			       });
	        	}
	        });
	   }
   };


   /**
	 * Creates a new employee with name An Employee and refreshes the employee
	 * list to contain the change
	 */
    $scope.newEmployee = function () {
    	$scope.updateLastInteractionTime();
    	$http({
            url: '/schedule/createEmployee',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
                $scope.notify("Employee created");
                
        		$scope.listEmployeesAndInfo();// TODO refactor to ensure the
												// new employee is selected
        		
        		$scope.setEmployeeTab("Schedule");
        	}
        	else{
        		$scope.warn("Failed to save employee info.")
        	}
        });
    };

    /**
	 * Checks to see if we have the latest info in this employee. If we don't we
	 * ask if they want to overwrite what's on the server or see what the
	 * changes are. If the choose to proceed or if we had the latest data we
	 * save the employee and custom field data.
	 */
    $scope.ok = function () {
    	$scope.updateLastInteractionTime();
      	var originalEmployee = $scope.clone($scope.unmodifiedEmployee);
    	var modifiedEmployee = $scope.clone($scope.employee);
    	var lastUpdated = originalEmployee.lastUpdated;
    	if(null==lastUpdated || undefined == lastUpdated){
    		lastUpdated="null";
    	}
    	if(modifiedEmployee!=$scope.unmodifiedEmployee){
		   $http({
	            url: '/schedule/employeeWasUpdated',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	lastUpdated:lastUpdated,
	            	employeeId:originalEmployee.id
	            }
	        })
	        .then(function(response) {
	        	var updateData=false;
    	    	if(response.data=="UPDATED"){
    	    		   if(confirm(modifiedEmployee.first+" has just been modified by another user. Saving your changes will overwrite thier updates. Would you " +
    	    		   				"still like to save your changes?")){
    	    			   updateData=true;
    	    		   }else{
    	    			   $scope.cancel();
    	    		   }
    	    	}
    	    	else{
    	    		updateData=true;
    	    	}
    	    	
    	    	if(updateData){
    		  		$scope.detailsChanged=false;
    	
			    	if($scope.customFields && $scope.customFields.length>0){
				        var size = $scope.customFields.length;
				        for(var i = 0; i < size ;i++){
				            $http({
				                url: '/compatibility/setEmployeeCustomFieldData',
				                method: 'POST',
				                headers: {
					                'Authorization': $scope.idToken,
				                    'Content-Type': 'application/x-www-form-urlencoded'
				                },
				                params: {
				                    employee: modifiedEmployee,
				                    customField: $scope.customFields[i],
				                    value:$scope.customValue[i],
				                }
				            });
				        }
				    }
			    	
			    	$http({// TODO refactor to saveEmployee method call
			            url: '/schedule/updateEmployee',
			            method: 'POST',
			            headers: {
			                'Authorization': $scope.idToken,
			                'Content-Type': 'application/x-www-form-urlencoded'
			            },
			            params: {
			                param: modifiedEmployee
			            }
			        })
			        .then(function(response) {
			        	if(response.data){
			        		$scope.notify("Employee saved");
    		                
    		            	$scope.detailsChanged=false;
    		            	$scope.updateEmployee();
    		            	$scope.getAllEmployeeCustomFieldData();
			        	}
			        	else{
			        		$scope.warn("Failed to save employee info.")
			        	}
			        });
    	    	}
	        });
    	}
    };
    
    /**
	 * Updates $scope.employee and $scope.unmodifiedEmployee to be the most
	 * recent copy of this employee from the server
	 */
    $scope.updateEmployee = function () {
        $http({
            url: '/employees/'+$scope.employee.id,
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
        		$scope.setSelectedEmployee(response.data);
        	}
        });
    }
     
    $scope.delete = function () {
    	$scope.updateLastInteractionTime();
 	   if(confirm("Are you sure you want to delete info for "+$scope.employee.first + " "+$scope.employee.initial+"?")){
    	$http({
            url: '/employees/'+$scope.employee.id,
            method: 'DELETE',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response){
                $scope.notify("Employee deleted.");
        		$scope.listEmployeesAndInfo();
     			$scope.setEmployeeAndInfo(response.data[0]);
        		$scope.setEmployeeTab("Schedule");
        	}
        	else{
        		$scope.warn("Failed to delete employee info.")
        	}
        });
 	   }
    };
    
    $scope.cancel = function () {
    	$scope.updateLastInteractionTime();
	  	$http({
            url: '/employees/'+$scope.employee.id,
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	$scope.detailsChanged=false;
        	if(response.data){
        		$scope.setEmployeeAndInfo(response.data);
        	}
        });
    };
}
