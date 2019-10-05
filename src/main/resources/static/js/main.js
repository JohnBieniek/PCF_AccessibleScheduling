angular.module('mainNavigation', ['ngResource', 'ui.bootstrap']).
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

function MainNavigationController($scope, $modal, $http) {
	 $scope.init = function() {
		$scope.lastInteraction=new Date();
        $scope.setPage("login");
		$scope.tab="Schedule";
		$scope.employeeTab="Schedule";
        $scope.showToast=false;
        $scope.alertMessage="";
        
        //Security data set on login
        $scope.profile = null;
        $scope.idToken = null;
        $scope.user=false;
        $scope.manager=false;
        $scope.admin=false;
        
        $scope.lastScheduleStatusUpdate=null;
        $scope.lastShiftUpdate=null;
        $scope.lastRequestUpdate=null;
        $scope.lastLocalCustomFieldUpdate=null;
        $scope.lastLocalShiftUpdate=null;
        $scope.lastClientsUpdate=null;
        $scope.lastClientUpdate=null;
        $scope.lastLocalClientUpdate=null;
        $scope.lastEmployeeUpdate=null;
        $scope.lastEmployeesUpdate=null;
        $scope.lastLocalEmployeeUpdate=null;
        $scope.lastCustomFieldTableUpdate = null;
        $scope.lastAlertTableUpdate = null;
        $scope.lastStatusTableUpdate = null;
        $scope.lastShiftTableUpdate = null;
        $scope.lastClientTableUpdate=null;
        $scope.lastEmployeeTableUpdate=null;
        if($scope.week==undefined || $scope.week ==null){
			 $scope.week = new Date();
		}
        $scope.monthTab=null;//Scheduler tab
        $scope.year = $scope.week.getYear()+1900;

        $scope.shiftsForMonth=null;
        $scope.lastGeneratedStatus=false;
        $scope.lastAssignedStatus=false;
		$scope.unscheduled=0;//Number of shifts for the selected month in the scheduler page
		$scope.scheduled=0;//Number of shifts for the selected month in the scheduler page
	  	$scope.total= +$scope.scheduled + +$scope.unscheduled;//Number of shifts for the selected month in the scheduler page
	  	
		$scope.statusList=[];//Holds the info to color the month tabs of the scheduler
    	$scope.customValue=[];//Holds custom field data in the details tab
    	$scope.unmodifiedCustomValue=[];//Holds custom field data in the details tab
        $scope.employee=null;
        $scope.selectedEmployee=null;
        $scope.unmodifiedEmployee=null;
        $scope.employees=null;
        $scope.client=null;
        $scope.selectedClient = null;
        $scope.unmodifiedClient=null;
        $scope.clients=null;
        $scope.customFields=null;
        $scope.shifts=null;
        $scope.requests=null;
        $scope.alerts=null;
        
        //Scheduler options
		$scope.allowOvertime=false;
		$scope.allowUnavailable=false;
		$scope.prioritizeSecondShift=false;
		$scope.useDailyMax=true;
		$scope.useWeeklyMax=true;
		$scope.allowInactive=false;
		
		//Scheduler messages
		$scope.generated="Generated";
		$scope.assigned="Assigned";
        
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
		
		$scope.updateCycle=1;
		$scope.autoUpdateData();
	 };
	 
	 $scope.getMinutesSinceLastInteraction = function(){
		 var now = new Date();
		 var diffMs = (now-$scope.lastInteraction); // milliseconds between now & last interaction
		 var diffMins = Math.round(((diffMs % 86400000) % 3600000) / 60000); // minutes
		 return diffMins;
	 }

	 $scope.updateLastInteractionTime = function(){
		$scope.lastInteraction = new Date();
	 }
	 
	 $scope.incrementCycle = function(){
		 $scope.updateCycle=$scope.updateCycle+1;
		 if($scope.updateCycle=5){
			 $scope.updateCycle=1;
		 }
	 }
	 
	 $scope.getDateString = function getDateString(date){
		return  date.getFullYear() +"-" +(date.getMonth()+1)+"-"+date.getDate();
	 }
	 
	 $scope.updateData = function updateData(){
		 var payload = {};
		 if($scope.page!="templates/page/scheduler.html" && $scope.page!="templates/page/alerts.html"){
			 var customFieldsInfo = {}; 
			 customFieldsInfo["tableLastUpdated"]=$scope.lastCustomFieldUpdate;
			 payload["customFields"] = customFieldsInfo;
			 //$scope.listCustomFields();
			 
			 if($scope.page=="templates/page/employee.html" || $scope.page=="templates/page/client.html"){
				 var clientsInfo = {}; 
				 clientsInfo["tableLastUpdated"]=$scope.lastClientTableUpdate;
				 payload["clients"] = clientsInfo;
				 
				 //$scope.listClients();//deprecated
				 
				 var employeesInfo = {}; 
				 employeesInfo["tableLastUpdated"]=$scope.lastEmployeeTableUpdate;
				 payload["employees"] = employeesInfo;
				 
				 //$scope.listEmployees();//deprecated
				 //$scope.listShifts();//TODO Make this only update when new
				 
				 if($scope.page=="templates/page/employee.html"){
					 var shiftsInfo = {}; 
					 shiftsInfo["tableLastUpdated"]=$scope.lastShiftTableUpdate;
					 shiftsInfo["lastUpdated"]=$scope.lastShiftUpdate;
					 shiftsInfo["date"]=$scope.getDateString($scope.week);
					 shiftsInfo["id"] = $scope.employee.id;
					 payload["shifts"] = shiftsInfo;
				 }
				 else if($scope.page=="templates/page/client.html"){
        			 if($scope.selectedClient!=null){
						 var shiftsInfo = {}; 
						 shiftsInfo["tableLastUpdated"]=$scope.lastShiftTableUpdate;
						 shiftsInfo["lastUpdated"]=$scope.lastShiftUpdate;
						 shiftsInfo["date"]=$scope.getDateString($scope.week);
						 shiftsInfo["id"] = $scope.selectedClient.id;
						 payload["shifts"] = shiftsInfo;
						 
						 var requestsInfo = {}; 
						 requestsInfo["tableLastUpdated"]=$scope.lastRequestTableUpdate;
						 requestsInfo["lastUpdated"]=$scope.lastRequestUpdate;
						 requestsInfo["id"] = $scope.selectedClient.id;
						 payload["requests"] = requestsInfo;
        			 }
//						 $scope.listRequests();//TODO Make this only update when new
				 }
			 }
		 }else if($scope.page=="templates/page/alerts.html"){
			 var alertsInfo = {}; 
			 alertsInfo["tableLastUpdated"]=$scope.lastAlertTableUpdate;
			 payload["alerts"] = alertsInfo;
			 $scope.listAlerts();
		 }
		 else if($scope.page=="templates/page/scheduler.html"){
			 var statusInfo = {}; 
			 alertsInfo["tableLastUpdated"]=$scope.lastStatusTableUpdate;
			 payload["status"] = statusInfo;

			 $scope.listStatusItems();
		 }

		 $scope.incrementCycle();
 		 $scope.getAllUpdates(payload);
	 }
	 
	 $scope.autoUpdateData = function autoUpdateData(){
		 $scope.updateData();
		 var timeSinceInteraction = $scope.getMinutesSinceLastInteraction();
 		 if($scope.statusList !=undefined && $scope.statusList[$scope.monthTab-1] !=undefined &&
 		    $scope.statusList[$scope.monthTab-1].assigning && $scope.page=="templates/page/scheduler.html"){
			 setTimeout(autoUpdateData,5000);
 		 }
 		 else if(timeSinceInteraction>120){
			 setTimeout(autoUpdateData,12000000);
		 }
 		 else if(!$scope.manager && !$scope.admin){
 			setTimeout(autoUpdateData,30000);
 		 }
		 else if(timeSinceInteraction>60){
			 setTimeout(autoUpdateData,120000);
		 }
		 else if(timeSinceInteraction>30){
			 setTimeout(autoUpdateData,60000);
		 }
		 else if(timeSinceInteraction>=5){
			 setTimeout(autoUpdateData,30000);
		 }
		 else if(timeSinceInteraction<5){
			 setTimeout(autoUpdateData,15000);
		 }
	 }
	 
	 //API Access
	 $scope.getAllUpdates = function getAllUpdates(json){
		 if($scope.idToken!=null){
	    	$http({
	            url: '/schedule/allUpdates',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	json: json
	            }
	        })
	        .then(function(response) {
	        	if(response.data){
		        	console.log("get all updates response");
		        	console.log(response.data);

	        		if(response.data.clients){
	        			$scope.clients = response.data.clients.names;
	        			if($scope.selectedClient==null){
	        				$scope.selectedClient = $scope.clients[0];
	        			}
	        			$scope.lastClientTableUpdate = response.data.clients.tableLastUpdated;
	        		}
	        		
	        		if(response.data.employees){
	        			$scope.employees = response.data.employees.names;
	        			$scope.lastEmployeeTableUpdate = response.data.employees.tableLastUpdated;
	        		}
	        		
	        		if(response.data.shifts){
	        			$scope.shifts = response.data.shifts.info;
	        			$scope.lastShiftTableUpdate = response.data.shifts.tableLastUpdated;
	        		}
	        		
	        		if(response.data.alerts){
	        			$scope.alerts = response.data.alerts.info;
	        			$scope.lastAlertTableUpdate = response.data.alerts.tableLastUpdated;
	        		}
	        		
	        		if(response.data.customFields){
	        			$scope.customFields = response.data.customFields.info;
	        			$scope.lastCustomFieldTableUpdate = response.data.customFieldData.tableLastUpdated;
	        		}
	        		
	        		if(response.data.requests){
	        			$scope.requests = response.data.requests.info;
	        			$scope.lastRequestTableUpdate = response.data.requests.tableLastUpdated;
	        		}
	        	}
	        	else{
	        		if(response.status==404){
	        			$scope.warn("Failed to update schedule data. If connection trouble persits contact your representative.");
	        		}
	        		else{
	        			$scope.warn("Failed to update schedule data");
	        		}
	        	}
	        });
		 }
	 }
	
	 $scope.getClient = function getClient(clientInfo){
		 console.log("getting client");
		 console.log(clientInfo);

		 if($scope.idToken!=null){
	    	$http({
	            url: '/clients/'+clientInfo,
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
        			$scope.client = response.data;
	        	}
	        	else{
	        		if(response.status==404){
	        			$scope.warn("Failed to update client data. If connection trouble persits contact your representative.");
	        		}
	        		else{
	        			$scope.warn("Failed to update client data.");
	        		}
	        	}
	        });
		 }
	 }

	 $scope.getEmployee = function getEmployee(){
		 console.log("getting employee");
		 console.log($scope.selectedEmployee.id);
		 
		 if($scope.idToken!=null){
	    	$http({
	            url: '/employees/'+$scope.selectedEmployee.id,
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
        			$scope.employee = response.data;
	        	}
	        	else{
	        		if(response.status==404){
	        			$scope.warn("Failed to update employee data. If connection trouble persits contact your representative.");
	        		}
	        		else{
	        			$scope.warn("Failed to update employee data.");
	        		}
	        	}
	        });
		 }
	 }
	 $scope.getEmployeeNames = function getEmployeeNames(){
		 if($scope.idToken!=null){
	    	$http({
	            url: '/schedule/employeeNames',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	tableLastUpdated: $scope.lastEmployeeTableUpdate
	            }
	        })
	        .then(function(response) {
	        	console.log("get employee names");
	        	console.log(response.data);
	        	if(response.data){
        			$scope.employees = response.data;
        			if($scope.employee==null){
	        			$scope.employee = $scope.employees[0];
        			}
        			$scope.lastEmployeeTableUpdate = response.data.tableLastUpdated;
	        	}
	        	else{
	        		if(response.status==404){
	        			$scope.warn("Failed to update employee data. If connection trouble persits contact your representative.");
	        		}
	        		else{
	        			$scope.warn("Failed to update employee data.");
	        		}
	        	}
	        });
		 }
	 }
	 
	 $scope.getClientNames = function getClientNames(){
		 if($scope.idToken!=null){
	    	$http({
	            url: '/schedule/clientNames',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	tableLastUpdated: $scope.lastClientTableUpdate
	            }
	        })
	        .then(function(response) {
	        	if(response.data){
        			$scope.clients = response.data;
        			if($scope.selectedClient==null){
	        			$scope.selectedClient = $scope.clients[0];
        			}
        			$scope.lastClientTableUpdate = response.data.tableLastUpdated;
	        	}
	        	else{
	        		if(response.status==404){
	        			$scope.warn("Failed to update client data. If connection trouble persits contact your representative.");
	        		}
	        		else{
	        			$scope.warn("Failed to update client data.");
	        		}
	        	}
	        });
		 }
	 }
	 
	 $scope.getScheduled = function getScheduled(month){
    	if(month!=null && month!=undefined){
			$scope.shiftsForMonth=month;
	    	$http({
	            url: '/schedule/scheduled',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	month: month
	            }
	        })
	        .then(function(response) {
	        	$scope.scheduled=response.data;
	        	$scope.total= +$scope.scheduled + +$scope.unscheduled;
	        });
    	}
     }
    
     $scope.getUnscheduled = function getUnscheduled(month){
    	if(month!=null && month!=undefined){
			$scope.shiftsForMonth=month;
			$http({
		        url: '/schedule/unscheduled',
		        method: 'GET',
		        headers: {
		            'Authorization': $scope.idToken,
		            'Content-Type': 'application/x-www-form-urlencoded'
		        },
		        params: {
		        	month: month
		        }
		    })
		    .then(function(response) {
		    	$scope.unscheduled=response.data;
		    	$scope.total= +$scope.scheduled + +$scope.unscheduled;
		    });
    	}

     }
     
     $scope.listStatusItems = function listStatusItems(){
    	 var shiftNumbersUpdated = false;
    	 var month = $scope.monthTab;
		 if($scope.idToken!=null){
			 $http({
		           url: '/updateInfo/scheduleStatus',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
		     })
		     .then(function (response) {//TODO handle error state	    	 
		  	   if($scope.lastScheduleStatusUpdate==null || response.data==null || response.data.time.nano!=$scope.lastScheduleStatusUpdate.time.nano){
		  		   
		  		   	$scope.lastScheduleStatusUpdate=response.data;
			
		  		    $scope.getUnscheduled(month);
		  		    $scope.getScheduled(month);
		  		   	
			    	$http({
			            url: '/schedule/statusList',
			            method: 'GET',
			            headers: {
			                'Authorization': $scope.idToken,
			                'Content-Type': 'application/x-www-form-urlencoded'
			            },
			            params: {
			            }
			        })
			        .then(function(response) {
			    		$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
			    	    if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].generated){
			  	    	  $scope.setGenerated("Generated");
				  	    }
				  	      
				  	    if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].assigned){
				  	    	  $scope.setAssigned("Assigned");
				  	    }
				  	      
				  	    if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].assigning){
				      	  $scope.setAssigned("Assigning");
				      	  if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].stopped){
				      		  $scope.setAssigned("Stopping");
				      	  }
				  	    }
				        if($scope.statusList[month-1]!=undefined){
				    	   $scope.lastGeneratedStatus=$scope.statusList[month-1].generated;
					       $scope.lastAssignedStatus=$scope.statusList[month-1].assigned;
				        }
			        });
		  	   }
		  	   else{
		  		    if($scope.shiftsForMonth==null || $scope.shiftsForMonth!=month ||  
		 				$scope.statusList==undefined || $scope.statusList[month-1] == undefined ||
		 			    $scope.lastGeneratedStatus!=$scope.statusList[month-1].generated || 
		 			    $scope.lastAssignedStatus!=$scope.statusList[month-1].assigned||
		 			    $scope.statusList[month-1].assigning){
		 		  		   $scope.getUnscheduled(month);
		 		  		   $scope.getScheduled(month);
		 		  		   shiftNumbersUpdated=true;
		 	    	}
		  		    if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].generated){
		  	    	  $scope.setGenerated("Generated");
			  	    }
			  	      
			  	    if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].assigned){
			  	    	  $scope.setAssigned("Assigned");
			  	    }
			  	      
			  	    if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].assigning){
			      	  $scope.setAssigned("Assigning");
			      	  if($scope.statusList!=undefined && $scope.statusList[month-1]!=undefined && $scope.statusList[month-1].stopped){
			      		  $scope.setAssigned("Stopping");
			      	  }
			  	    }
		  	   }
		     });
		 }
     }
	    
	 $scope.getAllEmployeeCustomFieldData = function () {
	     if($scope.employee!=null && $scope.customFields !=null){
		      for(var index = 0; index<$scope.customFields.length;index++){
			      $scope.getEmployeeCustomFieldData($scope.employee,$scope.customFields[index],index);
		      }
	     }
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
        	$scope.unmodifiedCustomValue[response.data.numericResponse] = response.data.booleanResponse;
        });
     }
	 
	 $scope.getAllClientCustomFieldData = function () {
	     if($scope.client!=null && $scope.customFields !=null){
		      for(var index = 0; index<$scope.customFields.length;index++){
			      $scope.getClientCustomFieldData($scope.client,$scope.customFields[index],index);
		      }
	     }
	 }

	 $scope.getClientCustomFieldData = function (client,customField,index){
     	$http({
            url: '/compatibility/clientCustomFieldData',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                client: client,
                customField: customField,
                index:index,
            }
        })
        .then(function(response) {
        	$scope.customValue[response.data.numericResponse] = response.data.booleanResponse;
        	$scope.unmodifiedCustomValue[response.data.numericResponse] = response.data.booleanResponse;
        });
     }
	 
	 $scope.listAlerts = function (){
    	if($scope.admin){
	    	$http({
	            url: '/alerts/findAll',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken
	            },
	            params: {
	            }
	        })
	        .then(function(response) {
	        	$scope.alerts=response.data;
	        });
    	}
	 }
	 
	 
     $scope.listShifts = function listShifts(){
    	let id = "-1";
    	
       	$scope.getDisplayWeek();
		 if($scope.page=="templates/page/employee.html"){
	    	if(null!=$scope.employee){
	    		id=$scope.employee.id;
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
		    		$scope.setShifts(response.data);
		    	});
	    	}
		 }
		 else if($scope.page=="templates/page/client.html"){
	    	if(null!=$scope.client){
		    	if($scope.manager || $scope.admin){
		    		id=$scope.client.id;
		    		
			    	$http({
			            url: '/schedule/clientShiftsForWeek',
			            method: 'GET',
			            headers: {
				            'Authorization': $scope.idToken,
			                'Content-Type': 'application/x-www-form-urlencoded'
			            },
			            params: {
			            	clientId:id,
			            	month:$scope.week.getMonth()+1,
			            	day: $scope.week.getDate(),
			            	year:$scope.week.getFullYear()
			            }
			        })
			        .then(function(response) {
			    		$scope.setShifts(response.data);
			    	});
		    	}
	    	}
		 }
     }
     
	 $scope.listRequests = function listRequests(){
    	if($scope.manager || $scope.admin){
	    	let id = "-1";
	    	
	    	if(null!=$scope.client){
	    		id=$scope.client.id;

	    	
		    	$http({
		            url: '/schedule/clientsRequests',
		            method: 'GET',
		            headers: {
			            'Authorization': $scope.idToken,
		                'Content-Type': 'application/x-www-form-urlencoded'
		            },
		            params: {
		            	clientId:id
		            }
		        })
		        .then(function(response) {
		    		$scope.requests = response.data;
		    	});
	    	}
    	}
     }
	 
	 //Deprecated
	 $scope.listClients = function listClients() {
	    	if($scope.manager || $scope.admin){
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
		    	   if($scope.lastLocalClientUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalClientUpdate.time.nano){
		    		   $scope.lastLocalClientUpdate=response.data;
		    		   
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
				        	$scope.clients =response.data;
				        });
		    	   }
		       })
	    	}
	 }
	 
	 $scope.listCustomFields = function listCustomFields() {
		 if($scope.idToken!=null){
			 $http({
		           url: '/updateInfo/customFields',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
		     })
		     .then(function (response) {//TODO handle error state	    	 
		  	   if($scope.lastLocalCustomFieldUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalCustomFieldUpdate.time.nano){
		  		   $scope.lastLocalCustomFieldUpdate=response.data;
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
		     })
		 }
     }
	 
	 //Deprecated
    $scope.listEmployees = function listEmployees() {
    	if($scope.manager || $scope.admin){
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
	    	   if($scope.lastLocalEmployeeUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalEmployeeUpdate.time.nano){
	    		   $scope.lastLocalEmployeeUpdate=response.data;
	    		   
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
			        	$scope.employees =response.data;
			        });
	    	   }
	       })
    	}
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
		var serverUpdate;
		
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
    	   serverUpdate=response.data;
        })
       
        return serverUpdate;
	}

	 //View Utils
	 $scope.getDisplayMonth = function(date){
		 return $scope.getDisplayMonthFromInt(parseInt(date.getMonth())+1);
     }
	 
	 $scope.getDisplayMonthFromInt= function(month){
		 var monthName= "";
		 
		 switch(month){
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
     	 
	 $scope.noShiftDay= function(day){
		var unscheduled =true;
		
		if(null!=$scope.shifts && $scope.shifts.length>0){
			$scope.shifts.forEach(function(shift) {
			  if(shift.startsLocalDate.dayOfWeek==day.toUpperCase().split(" ")[0]){
			    unscheduled=false;
			  }
			});
		}
		
		return unscheduled;
	}
	 
    $scope.isDay = function(shift, day){
    	return day.toUpperCase().includes(shift.startsLocalDate.dayOfWeek.toUpperCase());
    }
	 
    //View setters
    $scope.setScheduled=function(scheduled){
    	$scope.scheduled=scheduled;
    }
    $scope.setUnscheduled=function(unscheduled){
    	$scope.unscheduled=unscheduled;
    }
    $scope.setTotal=function(total){
    	$scope.total=total;
    }
	$scope.setEmployee = function(employee){
		 $scope.employee=employee;
	}
	$scope.setSelectedEmployee = function(employee){
		 $scope.selectedEmployee=employee;
		 $scope.getEmployee();
	}
	$scope.setEmployees = function(employees){
		 $scope.employees=employees;
	}
	$scope.setGenerated = function (generated) {
		$scope.generated = generated;
    };
	$scope.setAssigned = function (assigned) {
		$scope.assigned = assigned;
    };
	$scope.setWeek = function (isWeek) {
       $scope.week = isWeek;
    };
	$scope.setShifts = function setShifts(shifts){
		 $scope.shifts=shifts;
	}
	$scope.setLastLocalShiftUpdate = function setLastLocalShiftUpdate(time){
		 $scope.lastLocalShiftUpdate=time;
	}
	$scope.setLastShiftUpdate = function (time){
		 $scope.lastShiftUpdate = time;
	}
	$scope.setLastEmployeeUpdate = function (time){
		 $scope.lastEmployeeUpdate = time;
	}
	$scope.setLastClientUpdate = function (time){
		 $scope.lastClientUpdate = time;
	}
	$scope.setClient = function (client){
		 $scope.client = client;
	}
	$scope.setSelectedClient = function(client){
		 $scope.selectedClient=client;
		 $scope.getClient();
	}
	$scope.setClients = function (clients){
		 $scope.clients = clients;
	}
	$scope.setRequests = function(requests){
		$scope.requests=requests;
	}

	 
	//Navigation
	$scope.setTab = function(newTab){//ClientTab
		 $scope.updateLastInteractionTime();
	     $scope.tab = newTab;
	}
	$scope.setEmployeeTab = function(newTab){
		 $scope.updateLastInteractionTime();
	     $scope.employeeTab = newTab;
	     
	}
	$scope.setScheduleTab = function(newTab){
		if($scope.monthTab!=newTab){
			$scope.updateLastInteractionTime();
			$scope.unscheduled="0";//Clear to ease visual transition
			$scope.scheduled="0";//Clear to ease visual transition
			$scope.total="0";//Clear to ease visual transition
		    $scope.monthName=$scope.getDisplayMonthFromInt(newTab);
	        $scope.monthTab=newTab;
	        
	        $scope.listStatusItems();
		}
    };
	$scope.setPage = function (viewName) {
	    $scope.updateLastInteractionTime();
		var newPage = "templates/page/" + viewName + ".html"
		if(newPage!=$scope.page){
	        $scope.shifts=null;//Clear to ease visual transition
	        $scope.requests=null;//Clear to ease visual transition
	        
	        if(newPage=="templates/page/scheduler.html"){
	        	var month = $scope.monthTab;
	        	if(month ==null){
	        		month = $scope.week.getMonth()+1;
	        	}
	    		$scope.setScheduleTab(month);//Update early to ease visual transition
	        }
	    	$scope.page = "templates/page/" + viewName + ".html";
		}
    };
    
    $scope.decrementWeek = function(){
   	 $scope.updateLastInteractionTime();
   	 $scope.setWeek($scope.week.addDays(-7));
   	 
   	 $scope.getDisplayWeek();
   	 
   	 $scope.listShifts();
    }
    $scope.incrementWeek = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.setWeek($scope.week.addDays(7));
   	 
   	 	$scope.getDisplayWeek();
   	 
   	 	$scope.listShifts();
    }
	
     
    //Auth
	$scope.loggedIn = function loggedIn() {
		 if($scope.idToken!=null){
			 return true;
		 }
		 else{
			 return false;
		 }
	 }
	 
    $scope.signOut = function signOut() {
   	 	 $scope.updateLastInteractionTime();
		 $scope.manager=false;
		 $scope.admin=false;
		 $scope.user=false;
		 $scope.idToken=null;
		 $scope.profile=null;
		 $scope.employee=null;
		 $scope.tab="Schedule";
		 $scope.employeeTab="Schedule";
	    
		 var auth2 = gapi.auth2.getAuthInstance();
	     auth2.signOut().then(function () {});
    }
    $scope.setIdToken = function (idToken) {
        $scope.idToken = idToken;
        $scope.updateData();
    };
    $scope.setProfile = function (profile) {
        $scope.profile = profile;
    };
    $scope.setUser = function (isUser) {
        $scope.user = isUser;
    };
    $scope.setManager = function (isManager) {
        $scope.manager = isManager;
    };
    $scope.setAdmin = function (isAdmin) {
        $scope.admin = isAdmin;
    };
   
	 //Toast notifications 
	 $scope.notify = function(message){
		 $scope.alertMessage=message;
		 $scope.alertError=false;
		 $scope.showToast=true;
		 var d = new Date();
		 var n = d.getTime();
		 $scope.lastAlert=n;
		 setTimeout($scope.autoHideToast,4000);
	 }
	 
	 $scope.warn = function(message){
		 $scope.alertMessage=message;
		 $scope.alertError=true;
		 $scope.showToast=true;

		 var d = new Date();
		 var n = d.getTime();
		 $scope.lastAlert=n;
		 setTimeout($scope.autoHideToast,8000);
	 }
	 
	 $scope.hideToast = function(){
		 $scope.updateLastInteractionTime();
		 $scope.showToast=false;
	 }
	 
	 $scope.autoHideToast = function(){
		 var d = new Date();
		 var n = d.getTime();
		 var difference = n-$scope.lastAlert;
		 if(difference>=4000){
			 $scope.showToast=false;
			 $scope.$apply();
		 }
		 else{
			 setTimeout($scope.autoHideToast,500);
		 }
	 }
	 
	 
	 //Utils
	 //Allows for date.addDays. It's fucking(sic) awesome
     Date.prototype.addDays = function(days) {
 	    var date = new Date(this.valueOf());
 	    date.setDate(date.getDate() + days);
 	    return date;
 	 }
     
     $scope.clone = function clone (obj) {
    	 if(undefined!=obj){
             return JSON.parse(JSON.stringify(obj));
    	 }
    	 else{
    		 return null;
    	 }
     }
}
