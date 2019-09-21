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

function MobileEmployeeController($scope, $modal, $http, Status) {
	$scope.init = function(){
		 $scope.allowOvertime=false;
		 $scope.allowUnavailable=false;
		 $scope.prioritizeSecondShift=false;
		 $scope.useDailyMax=true;
		 $scope.useWeeklyMax=true;
		 $scope.allowInactive=false;
		 $scope.generatedBool = false;
		 $scope.monthName="January";
		 $scope.statusList=[];
		 $scope.customValue=[];
		 $scope.unscheduled=0;
		 $scope.scheduled=0;
		 $scope.selectedInterval="day(s)";
		 $scope.detailsChanged=false;
		 $scope.customFieldDateEditing=true;
		 $scope.days=['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];
		 
		 $scope.maxMonth=$scope.week.getMonth();
		 $scope.minMonth=$scope.maxMonth-1;
		 if($scope.minMonth<0){
			 $scope.minMonth=11;
		 }
		 $scope.minMonth=$scope.minMonth-1;
		 if($scope.minMonth<0){
			 $scope.minMonth=11;
		 }
		 $scope.maxMonth=$scope.maxMonth+1;
		 if($scope.maxMonth>11){
			 $scope.maxMonth=0;
		 }
		 $scope.newDate=$scope.week.getFullYear()+"-"+(($scope.week.getMonth()+1)<10?"0"+($scope.week.getMonth()+1):($scope.week.getMonth()+1))+"-"+$scope.week.getDate();
		 
		 if($scope.manager){
			 $scope.listEmployees();
		 }
		 
		 if($scope.employee==undefined || $scope.employee == null){
			 $scope.setEmployeeToUser();
		 }
		 
		 $scope.listShifts();
	}

	$scope.deleteAvailability=function(availability){ 
		var currentEmployee = $scope.employee;
		if(confirm("Are you sure you want to delete the selected availability? for "+ $scope.employee.days[availability]+ "?")){
			currentEmployee.startTimes.splice(availability,1);    
			currentEmployee.days.splice(availability,1);
			currentEmployee.endTimes.splice(availability,1); 

			$scope.setToast(true);
	        $scope.saveEmployee(currentEmployee);
            setTimeout($scope.hideToast,3000);
	    }
    }
	
	$scope.addAvailability= function(day){
		var currentEmployee = $scope.employee;
		if($scope.employee){
			currentEmployee.days.push(day);
			currentEmployee.startTimes.push("08:00");
			currentEmployee.endTimes.push("16:00");
			$scope.showToast();
	        $scope.saveEmployee(currentEmployee);		
            setTimeout($scope.hideToast,3000);
		}
	}
	
	$scope.isAvailabilityDay= function(availability,day){
		return day==$scope.employee.days[availability];
	}
	
	$scope.getLastShiftUpdate = function (){
		$http({
	           url: '/updateInfo/shifts',
	           method: 'GET',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	           }
	       })
	       .then(function (response) {//TODO handle error state
	    	   $scope.setLastShiftUpdate(response.data);
	       })
	}
	
	$scope.getLastClientUpdate = function (){
		$http({
	           url: '/updateInfo/clients',
	           method: 'GET',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	           }
	       })
	       .then(function (response) {//TODO handle error state
	    	   $scope.setLastClientUpdate(response.data);
	       })
	}
	
	$scope.getLastEmployeeUpdate = function (){
		$http({
	           url: '/updateInfo/employees',
	           method: 'GET',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	           }
	       })
	       .then(function (response) {//TODO handle error state
	    	   $scope.setLastEmployeeUpdate(response.data);
	       })
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
        	
        	$scope.employeeModel=clone($scope.employee);
        });
	}
	
	$scope.setEmployeeAndInfo = function(newEmployee){		
	  $scope.detailsChanged=false;
	  
	  if(newEmployee.role==null){
		  newEmployee.role="user";
	  }
      $scope.setEmployee(newEmployee);
      $scope.employeeModel = clone(newEmployee);
      if($scope.employee!=null && $scope.customFields !=null){
	      for(var index = 0; index<$scope.customFields.length;index++){
		      $scope.getEmployeeCustomFieldData($scope.employee,$scope.customFields[index],index);
	      }
      }
      $scope.listEmployees();
      $scope.listShifts();
	}
	
	 $scope.listCustomFields = function listCustomFields() {
		$http({
            url: '/customFields/',
            method: 'GET',
            headers: {
	            'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	$scope.customFields=response.data;
        });
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
    	 if($scope.manager || $scope.admin || $scope.week.getMonth()>$scope.minMonth){
	    	 $scope.setWeek($scope.week.addDays(-7));
	    	 $scope.getDisplayWeek();
	    	 $scope.listShifts();
    	 }
     }
     
     $scope.incrementWeek = function(){
    	 var weeksStart = $scope.week.addDays($scope.week.getDay()-1);
    	 var weeksEnd = weeksStart.addDays(6);
    	 var nextWeeksStart = weeksEnd.addDays(1);
    	 if($scope.manager || $scope.admin || nextWeeksStart.getMonth()<$scope.maxMonth){
    		 $scope.setWeek($scope.week.addDays(7));
	    	 $scope.getDisplayWeek();
	    	 $scope.listShifts();
    	 }
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
    	   console.log("updated employee and got");
    	   console.log(response.data);
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
           $scope.listEmployees();
           Status.success("Employee saved");
       });
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
    
	function clone (obj) {
		return JSON.parse(JSON.stringify(obj));
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
		$http({
            url: '/clients/',
            method: 'GET',
            headers: {
	            'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	$scope.clients=response.data;
        });
    }
    
    $scope.listEmployees = function listEmployees() {
    	if($scope.manager || $scope.admin){
	    	$http({
	            url: '/employees/',
	            method: 'GET',
	            headers: {
		            'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            }
	        })
	        .then(function(response) {
	        	$scope.employees=response.data;
	        	
	        	if($scope.employee==null){
	                 $scope.setEmployeeToUser();
	            }
	
	            $scope.listClients();
	    		$scope.getDisplayWeek();
	        });
    	}
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
        	Status.success("Shift saved");
        	
            $scope.listShifts();
        });
    }
    
    
    $scope.listShifts = function listShifts(){
    	let id = "-1";
    	
    	if(null!=$scope.employee){
    		id=$scope.employee.id;
    	}
    	$scope.getDisplayWeek();
    	if(id!=-1){
	    	$http({
	            url: '/schedule/employeeShiftsForWeek',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
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
                'Authorization': $scope.idToken,
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
	   $http({
           url: '/clientRequests',
           method: 'POST',
           headers: {
               'Authorization': $scope.idToken,
               'Content-Type': 'application/x-www-form-urlencoded'
           },
           params: {
           }
       })
       .then(function(response) {
           Status.success("Request saved");
           
           $scope.listShiftRequests();
       });
   }
   
   $scope.editShift = function (shift) {
	   if($scope.profile && $scope.profile.manager){
	       var updateModal = $modal.open({
	           templateUrl: 'templates/modal/shiftForm.html',
	           controller: ShiftModalController,
	           resolve: {
	        	   idToken: function(){
	        		   return clone($scope.idToken);
	        	   },
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
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response.data){
                Status.success("Employee created");
                
        		$scope.listEmployees();
        		
        		$scope.setEmployeeTab("Schedule");
        	}
        	else{
        		Status.error("Failed to save employee info.")
        	}
        });
    };
    
     $scope.toggleAvailabilityFor = function toggleAvailabilityFor(day,boolean) {
    	var existingEmployee = $scope.employee;
    	$scope.detailsChanged=true;	
    	var hoursAvailable=[];
    	
    	for(var i=0;i<24;i++){
        	hoursAvailable.push(boolean);
    	}
    	
    	if(day==0){//sunday
    		existingEmployee.sundaysAvailability=hoursAvailable;
    	}
    	if(day==1){//monday
    		existingEmployee.mondaysAvailability=hoursAvailable;
    	}
    	if(day==2){//tuesday
    		existingEmployee.tuesdaysAvailability=hoursAvailable;
    	}
    	if(day==3){//wednesday
    		existingEmployee.wednesdaysAvailability=hoursAvailable;
    	}
    	if(day==4){
    		existingEmployee.thursdaysAvailability=hoursAvailable;
    	}
    	if(day==5){
    		existingEmployee.fridaysAvailability=hoursAvailable;
    	}
    	if(day==6){
    		existingEmployee.saturdaysAvailability=hoursAvailable;
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
		                'Authorization': $scope.idToken,
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
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                param: $scope.employee
            }
        })
        .then(function(response) {
        	if(response.data){
                Status.success("Employee saved");
                
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
                'Authorization': $scope.idToken,
                 'Content-Type': 'application/x-www-form-urlencoded'
             },
             params: {
             }
         })
         .then(function(response) {
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
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
        	if(response){
                Status.success("Employee deleted.");
        		$scope.listEmployees();
     			$scope.setEmployeeAndInfo(response.data[0]);
        		$scope.setEmployeeTab("Schedule");
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
