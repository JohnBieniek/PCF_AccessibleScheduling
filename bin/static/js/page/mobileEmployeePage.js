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
			 $scope.listEmployeesAndInfo();
		 }
		 
		 if($scope.employee==undefined || $scope.employee == null){
			 $scope.setEmployeeToUser();
		 }
		 
		 $scope.listShifts();
	}
	$scope.setEmployeeTabAndInfo=function(tab){
		$scope.updateEmployee();
	    if($scope.employee!=null && $scope.customFields !=null){
		      for(var index = 0; index<$scope.customFields.length;index++){
			      $scope.getEmployeeCustomFieldData($scope.employee,$scope.customFields[index],index);
		      }
	      }
	    $scope.listEmployeesAndInfo();
	    $scope.listShifts();
		$scope.setEmployeeTab(tab);
	}
	$scope.deleteAvailability=function(availability){ 
		var currentEmployee = $scope.employee;
		if(confirm("Are you sure you want to delete the selected availability? for "+ $scope.employee.days[availability]+ "?")){
			currentEmployee.startTimes.splice(availability,1);    
			currentEmployee.days.splice(availability,1);
			currentEmployee.endTimes.splice(availability,1); 

	        $scope.saveEmployee(currentEmployee);
	    }
    }
	
	$scope.addAvailability= function(day){
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

		$scope.employee.days.forEach(function(selectedDay) {
		  if(selectedDay==day){
		    unavailable=false;
		  }
		});
		
		return unavailable;
	}
	
	$scope.getEmployeeCustomFieldData = function (employee,customField,index){
     	$http({
            url: '/compatibility/employeeCustomFieldData',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                customField: customField,
                index:index,
            }
        })
        .then(function(response) {
        	$scope.customValue[response.data.numericResponse] = response.data.booleanResponse;
        });
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
        	
        	$scope.setEmployeeAndInfo(response.data);
        	
        	$scope.employeeModel=$scope.clone($scope.employee);
        });
	}
	
	$scope.setEmployeeAndInfo = function(newEmployee){		
	  $scope.detailsChanged=false;
	  
	  if(newEmployee.role==null){
		  newEmployee.role="user";
	  }
      $scope.setEmployee(newEmployee);
      $scope.employeeModel = $scope.clone(newEmployee);
      if($scope.employee!=null && $scope.customFields !=null){
	      for(var index = 0; index<$scope.customFields.length;index++){
		      $scope.getEmployeeCustomFieldData($scope.employee,$scope.customFields[index],index);
	      }
      }
      $scope.listEmployeesAndInfo();
      $scope.listShifts();
	}
	 
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
	
	$scope.setDetailsToChanged = function(){
      $scope.detailsChanged=true;;
	}
	
     
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
       .then(function (response) {//TODO handle error state
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
       });
    }

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

		//Add exceptions
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
		$scope.listEmployees();
		
    	if($scope.employee==null){
             $scope.setEmployeeToUser();
        }

        $scope.listClients();
		$scope.getDisplayWeek();
	}
    
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

    $scope.editShift = function (shift) {
	   if($scope.profile && $scope.profile.manager){
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
    		   saveShift(shift);
	           $scope.listShifts();
	       }, function () {
	           $scope.listShifts();
	       });
	   }
   };


    $scope.newEmployee = function () {
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
                
        		$scope.listEmployeesAndInfo();//TODO refactor to ensure the new employee is selected
        		
        		$scope.setEmployeeTab("Schedule");
        	}
        	else{
        		$scope.warn("Failed to save employee info.")
        	}
        });
    };

    $scope.ok = function () {
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
			    	
			    	$http({//TODO refactor to saveEmployee method call
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
        		$scope.setEmployee(response.data);
        	}
        });
    }
    
    $scope.deleteShift = function (shift) {
    	$http({
            url: '/schedule/shiftWasUpdated',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	lastUpdated:($scope.shift.lastUpdated!=null?$scope.shift.lastUpdated:"null"),
            	shiftId:id
            }
        })
        .then(function(response) {
        	var deleteShift=false;
        	
        	if(response.data=="UPDATED"){
    		   if(confirm("This shift has just been modified by another user. Deleteing this shift will overwrite thier updates. Would you " +
    		   				"still like to delete the following shift?"+shift.display)){
    			   deleteShift=true;
    		   }
    		   else{
    			   $scope.listShifts();
    		   }
        	}
    		else if(confirm("Are you sure you want to delete the following shift? "+shift.display)){
    			deleteShift=true;
    		}
        	
        	if(deleteShift){
             	$http({
                    url: '/shifts/'+shift.id,
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
                        $scope.notify("Shift deleted.");
                        $scope.listShifts();
                	}
                	else{
                		$scope.warn("Failed to delete shift info.")
                	}
                });
        	}
    	});
     };
     
    $scope.delete = function () {
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
