angular.module('client', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('clients');
	}).
	factory('Client', function ($resource) {
	    return $resource('client/:id', {id: '@id'});
	}).
	factory('Shifts', function ($resource) {
	    return $resource('shifts');
	}).
	factory('Shift', function ($resource) {
	    return $resource('shift/:id', {id: '@id'});
	}).
	factory('Employees', function ($resource) {
	    return $resource('employees');
	}).
	factory('Employee', function ($resource) {
	    return $resource('employees/:id', {id: '@id'});
	}).
	factory('CustomFields', function ($resource) {
        return $resource('customFields');
    }).
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

function MobileEmployeeController($scope, $modal, $http, Clients, Client,Shifts,Shift,Employee, Employees, CustomFields, Status) {
	 $scope.multiTableEditing=false;
	 $scope.month=1;
	 $scope.allowOvertime=false;
	 $scope.allowUnavailable=false;
	 $scope.prioritizeSecondShift=false;
	 $scope.useDailyMax=true;
	 $scope.useWeeklyMax=true;
	 $scope.allowInactive=false;
	 $scope.generatedBool = false;
	 $scope.tab="Schedule";
	 $scope.monthName="January";
	 $scope.statusList=[];
	 $scope.customValue=[];
	 $scope.unscheduled=0;
	 $scope.scheduled=0;
	 $scope.selectedInterval="day(s)";
	 $scope.detailsChanged=false;
	 $scope.customFieldDateEditing=true;
	 if($scope.week==undefined || $scope.week ==null){
		 $scope.week = new Date();//.getTime();
	 }


	$scope.newDate=$scope.week.getFullYear()+"-"+(($scope.week.getMonth()+1)<10?"0"+($scope.week.getMonth()+1):($scope.week.getMonth()+1))+"-"+$scope.week.getDate();


	 $scope.days=['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
	$scope.setTab = function(newTab){
      $scope.tab = newTab;
	} 

	$scope.deleteAvailability=function(availability){ 
		if(confirm("Are you sure you want to delete the selected availability? for "+ $scope.employee.days[availability]+ "?")){
	        $scope.employee.startTimes.splice(availability,1);    
	        $scope.employee.days.splice(availability,1);
	        $scope.employee.endTimes.splice(availability,1); 
	        $scope.saveEmployee($scope.employee);
	    }
      }
	$scope.addAvailability= function(day){
		if($scope.employee){
			$scope.employee.days.push(day);
			$scope.employee.startTimes.push("08:00");
			$scope.employee.endTimes.push("16:00");
	        $scope.saveEmployee($scope.employee);		
		}
	}
	
	$scope.isAvailabilityDay= function(availability,day){
		return day==$scope.employee.days[availability];
	}
	$scope.getEmployeeCustomFieldData = function (employee,customField,index){
     	$http({
            url: '/compatibility/employeeCustomFieldData',
            method: 'POST',
            headers: {
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
		console.log("setting emplyoee to user");
		console.log($scope.profile);
		$http({
            url: '/employees/'+$scope.profile.employeeId,
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	console.log("setEmployeetoUIserReponse");
        	console.log(response);
        	$scope.employee=response.data;
        	if(typeof $scope.employee !== 'undefined' && $scope.employee!=null && $scope.employee.role==null){
  			  $scope.employee.role="user";
  		  }
        	$scope.employeeModel=clone($scope.employee);
        	$scope.setEmployee($scope.employee);
        });
	}
	$scope.setEmployee = function(newEmployee){
		//if($scope.employee == null || $scope.employee== undefined || $scope.employee.id !==newEmployee.id){
		  $scope.detailsChanged=false;
		  
		  if(newEmployee.role==null){
			  newEmployee.role="user";
		  }
	      $scope.employee = newEmployee;
	      $scope.employeeModel = clone(newEmployee);
	      if($scope.employee!=null && $scope.customFields !=null){
		      for(var index = 0; index<$scope.customFields.length;index++){
			      $scope.getEmployeeCustomFieldData($scope.employee,$scope.customFields[index],index);
		      }
	      }

	      $scope.listShifts();
		//}
	}
	 $scope.listCustomFields = function listCustomFields() {
         $scope.customFields = CustomFields.query();
     }
	$scope.setInterval = function(newInterval){
      $scope.interval = newInterval;
	}
	
	$scope.setDetailsToChanged = function(){
      $scope.detailsChanged=true;;
	}
     $scope.isDay = function(shift, day){
    	 return day.toUpperCase().includes(shift.startsLocalDate.dayOfWeek.toUpperCase());
     }
     
     Date.prototype.addDays = function(days) {
	    var date = new Date(this.valueOf());
	    date.setDate(date.getDate() + days);
	    return date;
	}
     
     $scope.decrementWeek = function(){
    	 $scope.week = $scope.week.addDays(-7);
    	 $scope.getDisplayWeek();
    	 $scope.listShifts();
     }
     
     $scope.incrementWeek = function(){
    	 $scope.week = $scope.week.addDays(7);
    	 $scope.getDisplayWeek();
    	 $scope.listShifts();
     }
     
     $scope.saveEmployee = function saveEmployee(employee) {
		 console.log("role");
		 console.log(employee);
		 console.log(employee.role)
     	if(employee.availability){
	     	for(var index = 0; index<employee.availability.length;index++){
	     		employee.availabilityStartTimes
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
        Employees.save(employee,
            function (response) {
            	$scope.employee=response;
	        	employee.id=response.id;
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
		                        value:employee.customValue[i],
		                    }
		                });
		            }
	            }
                Status.success("Employee saved");
                //$scope.listEmployees();
            },
            function (result) {
                Status.error("Error saving employee: " + result.status);
            }
        );
    }

     $scope.getDisplayMonth = function(date){
    	 var monthName = "January";
    	 
		 switch(parseInt(date.getMonth())+1){
		  	  case 1:
		  		  monthName="January";
		  		  break;
		  	  case 2:
		  		  monthName="Febuary";
		  		  break;
		  	  case 3:
		  		  monthName="March";
		  		  break;
		  	  case 4:
		  		  monthName="April";
		  		  break;
		  	  case 5:
		  		  monthName="May";
		  		  break;
		  	  case 6:
		  		  monthName="June";
		  		  break;
		  	  case 7:
		  		  monthName="July";
		  		  break;
		  	  case 8:
		  		  monthName="August";
		  		  break;
		  	  case 9:
		  		  monthName="September";
		  		  break;
		  	  case 10:
		  		  monthName="October";
		  		  break;
		  	  case 11:
		  		  monthName="November";
		  		  break;
		  	  case 12:
		  		  monthName="December";
		  		  break;
	 		}
		 return monthName;
     }
     
     $scope.getDisplayWeek = function(){
    	 var date = parseInt($scope.week.getDate());
    	 var day = parseInt($scope.week.getDay());
    	 
    	 var weekStart = $scope.week.addDays(-day);
    	 var weekEnd = weekStart.addDays(6);
    	 
    	 $scope.displayWeek = weekStart.getDate()+ " - " +weekEnd.getDate();

    	 $scope.year = parseInt(weekStart.getYear())+1900;
    	 $scope.monthName=$scope.getDisplayMonth(weekStart);
    	 $scope.displayDays=[
			'Sunday '+$scope.monthName + " "+weekStart.getDate(),
			'Monday '+$scope.getDisplayMonth(weekStart.addDays(1)) + " "+weekStart.addDays(1).getDate(),
			'Tuesday '+$scope.getDisplayMonth(weekStart.addDays(2)) + " "+weekStart.addDays(2).getDate(),
			'Wednesday '+$scope.getDisplayMonth(weekStart.addDays(3)) + " "+weekStart.addDays(3).getDate(),
			'Thursday '+$scope.getDisplayMonth(weekStart.addDays(4)) + " "+weekStart.addDays(4).getDate(),
			'Friday '+$scope.getDisplayMonth(weekStart.addDays(5)) + " "+weekStart.addDays(5).getDate(),
			'Saturday '+$scope.getDisplayMonth(weekStart.addDays(6)) + " "+weekStart.addDays(6).getDate()
		];
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
      return $scope.tab === tabNum;
    };
    
    $scope.isEmployeeSet = function(employee){
        return employee !==null && $scope.employee !==null && employee !==undefined && $scope.employee !==undefined && $scope.employee.id === employee.id;
      };
      
      $scope.isEmployeeChangeValid = function(employee){
          let valid = true;
          
          if(employee.first ==undefined || employee.first.length<1){
        	  valid=false;
          }
          if(!$scope.detailsChanged){
        	  valid=false;
          }

          return valid;
        };
    
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
	 $scope.setMonth = function setMonth(month) {
	        $scope.month = month;
	    }
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

	 $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
    }
    
    $scope.listEmployees = function listEmployees() {
    	var newEmployees=Employees.query();
        $scope.employees = newEmployees;
        
        if($scope.employee==null){
             $scope.setEmployeeToUser();
        }


        $scope.listClients();
		$scope.getDisplayWeek();
    }
    
    function saveShift(shift) {
        Shifts.save(shift,
            function () {
                Status.success("Shift saved");
                $scope.listShifts();
            },
            function (result) {
                Status.error("Error saving shift: " + result.status);
            }
        );
    }
    
    
    $scope.listShifts = function listShifts(){
    	let id = "-1";
    	
    	if(null!=$scope.employee){
    		id=$scope.employee.id;
    	}
    	$scope.getDisplayWeek();
    	$http({
            url: '/schedule/employeeShiftsForWeek',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	employeeId:id,
            	month:$scope.week.getMonth()+1,
            	day: $scope.week.getDate(),
            	year:$scope.week.getFullYear()
            }
        })
        .then(function(response) {
    		$scope.shifts = response.data;
    	});
    }
    
    $scope.listCurrentShifts = function listCurrentShifts(){
    	let id = "-1";
    	
    	if(null!=$scope.employee){
    		id=$scope.employee.id;
    	}
    	
    	$http({
            url: '/schedule/currentShifts',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	employeeId:id
            }
        })
        .then(function(response) {
    		$scope.currentShifts = response.data;
    	});
    }
    

  
   
   function clone (obj) {
       return JSON.parse(JSON.stringify(obj));
   }
   
   function saveShiftRequest(shiftRequest) {
       Requests.save(shiftRequest,
           function () {
               Status.success("Request saved");
               $scope.listShiftRequests();
           },
           function (result) {
               Status.error("Error saving shift Request: " + result.status);
           }
       );
   }
   
   $scope.editShift = function (shift) {
	   if($scope.profile && $scope.profile.manager){
	       var updateModal = $modal.open({
	           templateUrl: 'templates/modal/shiftForm.html',
	           controller: ShiftModalController,
	           resolve: {
	               shift: function() {
	                   return clone(shift);
	               },
	               client: function(){
	            	   return {};
	               },
	               clients: function(){
	           		return {};
		           	},
		           	employees:function(){
		        		return clone($scope.employees);
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
	       });
	   }
   };


    $scope.newEmployee = function () {
    	$http({
            url: '/schedule/createEmployee',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
                Status.success("Employee created");
        		$scope.listEmployees();
        		$scope.tab="Schedule";
        	}
        	else{
        		Status.error("Failed to save employee info.")
        	}
        });
    };
    
     $scope.toggleAvailabilityFor = function toggleAvailabilityFor(day,boolean) {
    	$scope.detailsChanged=true;	
    	var hoursAvailable=[];
    	for(var i=0;i<24;i++){
        	hoursAvailable.push(boolean);
    	}
    	if(day==0){//sunday
    		$scope.employee.sundaysAvailability=hoursAvailable;
    	}
    	if(day==1){//monday
    		$scope.employee.mondaysAvailability=hoursAvailable;
    	}
    	if(day==2){//tuesday
    		$scope.employee.tuesdaysAvailability=hoursAvailable;
    	}
    	if(day==3){//wednesday
    		$scope.employee.wednesdaysAvailability=hoursAvailable;
    	}
    	if(day==4){
    		$scope.employee.thursdaysAvailability=hoursAvailable;
    	}
    	if(day==5){
    		$scope.employee.fridaysAvailability=hoursAvailable;
    	}
    	if(day==6){
    		$scope.employee.saturdaysAvailability=hoursAvailable;
    	}
    }

    $scope.ok = function () {
    	$scope.detailsChanged=false;
    	if($scope.customFields && $scope.customFields.length>0){
	        var size = $scope.customFields.length;
	        for(var i = 0; i < size ;i++){
	            $http({
	                url: '/compatibility/setEmployeeCustomFieldData',
	                method: 'POST',
	                headers: {
	                    'Content-Type': 'application/x-www-form-urlencoded'
	                },
	                params: {
	                    employee: $scope.employee,
	                    customField: $scope.customFields[i],
	                    value:$scope.customValue[i],
	                }
	            });
	        }
	    }
    	$http({
            url: '/schedule/updateEmployee',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                param: $scope.employee
            }
        })
        .then(function(response) {
        	if(response.data){
                Status.success("Employee saved");
				//$scope.employee=response.data;
            	$scope.detailsChanged=false;
        	}
        	else{
        		Status.error("Failed to save employee info.")
        	}
        });
    };
    
    $scope.deleteShift = function (shift) {
  	   if(confirm("Are you sure you want to delete the following shift? "+shift.display)){
     	$http({
             url: '/shifts/'+shift.id,
             method: 'DELETE',
             headers: {
                 'Content-Type': 'application/x-www-form-urlencoded'
             },
             params: {
             }
         })
         .then(function(response) {
         	console.log(response);
         	if(response){
                 Status.success("Shift deleted.");
                 $scope.listShifts();
         	}
         	else{
         		Status.error("Failed to delete employee info.")
         	}
         });
  	   }
     };
     
    $scope.delete = function () {
 	   if(confirm("Are you sure you want to delete info for "+$scope.employee.first + " "+$scope.employee.initial+"?")){
    	$http({
            url: '/employees/'+$scope.employee.id,
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response){
                Status.success("Employee deleted.");
        		$scope.listEmployees();
     			$scope.employee =response.data[0];
        		$scope.tab="Schedule";
        	}
        	else{
        		Status.error("Failed to delete employee info.")
        	}
        });
 	   }
    };
    
    $scope.cancel = function () {
	  	$http({
            url: '/employees/'+$scope.employee.id,
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	$scope.detailsChanged=false;
        	if(response.data){
        		$scope.setEmployee(response.data);
        	}
        });
    };
}
