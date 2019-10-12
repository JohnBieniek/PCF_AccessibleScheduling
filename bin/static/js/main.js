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
		$scope.tab="Schedule";//client tab
		$scope.employeeTab="Schedule";
        $scope.showToast=false;
        $scope.alertMessage="";
        $scope.detailsChanged=false;//Used to toggle on/off ok&cancel buttons
        
        //Security data set on login
        $scope.profile = null;
        $scope.idToken = null;
        $scope.user=false;
        $scope.manager=false;
        $scope.admin=false;
        
        //Last updated info used to tell if we need to pull fresh data in autoUpdateData
        $scope.lastRequestUpdate=null;
        
        $scope.lastScheduleStatusUpdate=null;
        $scope.lastShiftUpdate=null;
        $scope.lastLocalCustomFieldUpdate=null;
        $scope.lastLocalShiftUpdate=null;
        $scope.lastClientsUpdate=null;
        $scope.lastClientUpdate=null;
        $scope.lastLocalClientUpdate=null;
        $scope.lastEmployeeUpdate=null;
        $scope.lastEmployeesUpdate=null;
        $scope.lastLocalEmployeeUpdate=null;
        
        //Table update info
        $scope.lastCustomFieldTableUpdate = null;
        $scope.lastAlertTableUpdate = null;
        $scope.lastStatusTableUpdate = null;
        $scope.lastShiftTableUpdate = null;
        $scope.lastClientTableUpdate=null;
        $scope.lastEmployeeTableUpdate=null;
        $scope.lastRequestTableUpdate=null;
        
        
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
        $scope.employee=null;//The full employee info for who we are looking at
        $scope.selectedEmployee=null;//The name and id of the employee we are looking at
        $scope.unmodifiedEmployee=null;
        $scope.employees=null;
        $scope.client=null;//The full client info for who we are looking at
        $scope.selectedClient = null;//The name and id of the client we are looking at TODO check to see if this is still used
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
        
		$scope.days=['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'];//Display strings for each day
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
		setTimeout($scope.autoUpdateData,15000);
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
	 
	 $scope.getEmployeeUpdatePayload = function (){
		 var employeeInfo = {}; 
		 
		 if($scope.selectedEmployee.id!=$scope.employee.id){
			 employeeInfo["lastUpdated"]=null;
			 employeeInfo["id"] = $scope.selectedEmployee.id;
		 }
		 else{
			 employeeInfo["lastUpdated"]=$scope.employee.lastUpdated;
			 employeeInfo["id"] = $scope.employee.id;	        					 
		 }
		 
		 return employeeInfo;
	 }
	 
	 
	 $scope.updateData = function updateData(){
		 var payload = {};
		 if($scope.page!="templates/page/scheduler.html" && $scope.page!="templates/page/alerts.html"){
			 var customFieldsInfo = {}; 
			 customFieldsInfo["tableLastUpdated"]=$scope.lastCustomFieldTableUpdate;
			 payload["customFields"] = customFieldsInfo;
			 
			 if($scope.page=="templates/page/employee.html" || $scope.page=="templates/page/client.html"){
				 var clientsInfo = {}; 
				 clientsInfo["tableLastUpdated"]=$scope.lastClientTableUpdate;
				 payload["clients"] = clientsInfo;
				 
				 var employeesInfo = {}; 
				 employeesInfo["tableLastUpdated"]=$scope.lastEmployeeTableUpdate;
				 payload["employees"] = employeesInfo;
				 
				 if($scope.page=="templates/page/employee.html" && $scope.employee!=null){
					 if(!$scope.detailsChanged){
						 payload["employee"] = $scope.getEmployeeUpdatePayload();
					 }
					 
					 if($scope.employeeTab == "Schedule"){
						 var shiftsInfo = {}; 
						 shiftsInfo["tableLastUpdated"]=$scope.lastShiftTableUpdate;
						 shiftsInfo["lastUpdated"]=$scope.lastShiftUpdate;
						 shiftsInfo["date"]=$scope.getDateString($scope.week);
						 shiftsInfo["id"] = $scope.selectedEmployee.id;
						 payload["shifts"] = shiftsInfo;
					 }
				 }
				 else if($scope.page=="templates/page/client.html"){
        			 if($scope.client!=null){
    					 if(!$scope.detailsChanged){
	        				 var clientInfo = {};
	        				 
	        				 if($scope.selectedClient.id!=$scope.client.id){
		    					 clientInfo["lastUpdated"]=null;
		    					 clientInfo["id"] = $scope.selectedClient.id;
	        				 }
	        				 else{
		    					 clientInfo["lastUpdated"]=$scope.client.lastUpdated;
		    					 clientInfo["id"] = $scope.client.id;	        					 
	        				 }

	    					 payload["client"] = clientInfo;
    					 }
    					 
        				 if($scope.tab == "Schedule"){
							 var shiftsInfo = {}; 
							 shiftsInfo["tableLastUpdated"]=$scope.lastShiftTableUpdate;
							 shiftsInfo["lastUpdated"]=$scope.lastShiftUpdate;
							 shiftsInfo["date"]=$scope.getDateString($scope.week);
							 shiftsInfo["id"] = $scope.selectedClient.id;
							 payload["shifts"] = shiftsInfo;
        				 }
        				 else if($scope.tab=="Requests"){
							 var requestsInfo = {}; 
							 requestsInfo["tableLastUpdated"]=$scope.lastRequestTableUpdate;
							 requestsInfo["lastUpdated"]=$scope.lastRequestUpdate;
							 requestsInfo["id"] = $scope.selectedClient.id;
							 payload["requests"] = requestsInfo;
        				 }
        			 }
				 }
			 }
		 }else if($scope.page=="templates/page/alerts.html"){
			 var alertsInfo = {}; 
			 alertsInfo["tableLastUpdated"]=$scope.lastAlertTableUpdate;
			 payload["alerts"] = alertsInfo;
		 }
		 else if($scope.page=="templates/page/scheduler.html"){
			 var statusInfo = {}; 
			 statusInfo["tableLastUpdated"]=$scope.lastStatusTableUpdate;
			 payload["status"] = statusInfo;

			// $scope.listStatusItems();
		 }

		 $scope.incrementCycle();
 		 $scope.getAllUpdates(payload);
	 }
	 
	 /**
	  * Infinite recursive loop used to ensure fresh data is always shown
	  * Calls UpdateData
	  * Uses the time since the last time the user interacted with the UI to determine how often to refresh
	  * The longer the user has been inactive the longer it is okay to wait to refresh data. 15sec-10min
	  * Users get slower base update speeds than managers and admins
	  */
	 $scope.autoUpdateData = function autoUpdateData(){
		 $scope.updateData();
		 var timeSinceInteraction = $scope.getMinutesSinceLastInteraction();
 		 if($scope.statusList !=undefined && $scope.statusList[$scope.monthTab-1] !=undefined &&
 		    $scope.statusList[$scope.monthTab-1].assigning && $scope.page=="templates/page/scheduler.html"){
			 setTimeout(autoUpdateData,5000);
 		 }
 		 else if(timeSinceInteraction>120){
			 setTimeout(autoUpdateData,600000);
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
        	$scope.updateData();
        });
	}
	 /**
	  * Takes in a payload of all updates that are needed and when that area last got its data
	  * Calls the allUpdates endpoint on the server to get all shifts, clients, etc that have changed.
	  * Only changed info comes back. When it does it overwrites existing info for that area.
	  */
	 $scope.getAllUpdates = function getAllUpdates(json){
		 if($scope.idToken!=null){
			 console.log("getting all updates");
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
	        			$scope.lastClientTableUpdate = response.data.clients.tableLastUpdated;
	        			
	        			if($scope.selectedClient==null){//On the first call set our initial selected Client to the first 
	        				$scope.selectedClient=$scope.clients[0];
	        				$scope.getClient($scope.selectedClient.id);
	        			}
	        		}
	        		
	        		//Don't update the client if the user has made changes as it's disruptive
	        		if(response.data.client && !$scope.detailsChanged){
	        			$scope.client = response.data.client;
	        			$scope.customValue=response.data.customFieldData;
	        		}
	        		
	        		if(response.data.employees){
	        			$scope.employees = response.data.employees.names;
	        			$scope.lastEmployeeTableUpdate = response.data.employees.tableLastUpdated;
	        		}
	        		
	        		//Don't update the employee if the user has made changes as it's disruptive
	        		if(response.data.employee && !$scope.detailsChanged){
	        			$scope.employee = response.data.employee;
	        			$scope.customValue=response.data.customFieldData;
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
	        			$scope.lastCustomFieldTableUpdate = response.data.customFields.tableLastUpdated;
	        		}
	        		
	        		if(response.data.requests){
	        			$scope.requests = response.data.requests.info;
	        			for(var index = 0; index< $scope.requests.length;index++){
	        				$scope.getDisplayValue($scope.requests[index]);
	        			}
	        			$scope.lastRequestTableUpdate = response.data.requests.tableLastUpdated;
	        			$scope.lastRequestUpdate = response.data.requests.lastUpdated;
	        		}
	        	}
	        })
            .catch(function(data, status) {
            	console.error('Gists error', data,status);
	    		if(data.status==404){
	    			$scope.warn("Failed to update schedule data. If connection trouble persits contact your representative.");
	    		}
	    		else if(!data.status==403){//Don't error on unauth, this is often the case after logging out
	    			$scope.warn("Failed to update schedule data");
	    		}
            });
		 }
	 }
	
	 /**
	  * Gets the full client model from the server for the selected client.
	  * Saves it in $scope.client and $scope.unmodifiedClient
	  */
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
        			$scope.unmodifiedClient=$scope.clone(response.data);
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

	 /**
	  * Gets the full employee model from the server for the $scope.selectedEmployee.
	  * Saves it in $scope.employee and $scope.unmodifiedEmployee
	  */
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
        			$scope.unmodifiedEmployee=response.data;
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
	 
	 /**
	  * Gets the employee list and save it in $scope.employees
	  * The list comes back sorted alphabeticly with each object containing employee.id and employee.name
	  */
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
	 
	 /**
	  * Gets the client list and save it in $scope.clients
	  * The list comes back sorted alphabeticly with each object containing client.id and client.name
	  * If no client is currently selected the first one in the list is chosen and its data is pulled
	  * Updates $scope.lastClientTableUpdate to response.data.tableLastUpdated
	  */
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
	        		console.log("got client names with selected client:",$scope.selectedClient);
        			$scope.clients = response.data;
        			if($scope.client==null ){
	        			$scope.selectedClient = $scope.clients[0];
		        		console.log("updated selected client to the first of the clients returned:",$scope.selectedClient);
		        		$scope.getClient($scope.selectedClient.id);
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
	 
	 /**
	  * Updates $scope.scheduled with the most recent number of assigned shifts for this month
	  * Updates $scope.total using the existing $scope.unscheduled number
	  */
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
    
	 /**
	  * Updates $scope.unscheduled with the most recent number of unassigned shifts for this month
	  * Updates $scope.total using the existing $scope.scheduled number
	  */
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
	     if($scope.selectedEmployee!=null && $scope.customFields !=null){
		      for(var index = 0; index<$scope.customFields.length;index++){
			      $scope.getEmployeeCustomFieldData($scope.employee,$scope.customFields[index],index);
		      }
	     }
	 }

	 $scope.getEmployeeCustomFieldData = function (employee,customField,index){
		if(null!=employee){
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
    	
       	$scope.getDisplayWeek();//TODO refactor this out
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
	    	
	    	if(null!=$scope.selectedClient){
	    		id=$scope.selectedClient.id;
	    		var tableLastUpdated =$scope.lastRequestTableUpdate;
	    		if(tableLastUpdated==null || tableLastUpdated==undefined){
	    			tableLastUpdated="null";
	    		}
	    		var lastUpdated =$scope.lastRequestUpdate;
	    		if(lastUpdated==null || lastUpdated==undefined){
	    			lastUpdated="null";
	    		}
		    	$http({
		            url: '/schedule/clientsRequests',
		            method: 'GET',
		            headers: {
			            'Authorization': $scope.idToken,
		                'Content-Type': 'application/x-www-form-urlencoded'
		            },
		            params: {
		            	clientId: id,
		            	tableLastUpdated: tableLastUpdated,
		            	lastUpdated:lastUpdated
		            }
		        })
		        .then(function(response) {
		        	if(response.data){
			    		$scope.requests = response.data.info;
			    		if(response.data.info){
		        			for(var index = 0; index< $scope.requests.length;index++){
		        				$scope.getDisplayValue($scope.requests[index]);
		        			}
			    		}
	        			console.log("got requests",$scope.requests);
	        			$scope.lastRequestTableUpdate = response.data.tableLastUpdated;
	        			$scope.lastRequestUpdate = response.data.lastUpdated;
		        	}
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
			        	
			        	if($scope.page=="templates/page/employee.html"){
			        		$scope.getAllEmployeeCustomFieldData();
			        	}
			        	else if($scope.page=="templates/page/client.html"){
			        		$scope.getAllClientCustomFieldData();
			        	}
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
	 
	 /**
	     * Updates the provided object with .displayValue containing a human readable string of what this request is for.
	     */
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
	$scope.setSelectedEmployeeWithoutGetEmployee = function(employee){
		 $scope.selectedEmployee=employee;
	}
	$scope.setSelectedEmployee = function(employee){
		 $scope.lastShiftUpdate=null;
		 $scope.lastShiftTableUpdate=null;
		 $scope.selectedEmployee=employee;
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
    $scope.setDetailsChanged = function setDetailsChanged(detailsChanged){
		 $scope.detailsChanged=detailsChanged;
	}
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
		 $scope.unmodifiedClient=client;
	}
	$scope.setSelectedClient = function(client){
		if(client!=$scope.selectedClient){
		 $scope.selectedClient=client;
		 $scope.lastRequestUpdate=null;
		 $scope.lastShiftUpdate=null;
		 $scope.lastShiftTableUpdate=null;
		 $scope.lastRequestTableUpdate=null;
		}
//		 $scope.requests=null;//Clear to ease visual transition
//		 $scope.shifts=null;//Clear to ease visual transition
		 //$scope.getClient(client.id);//Re-enable if no longer using only allUpdates
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
	
	/**
	 * Selects a different month to view the state of shifts for
	 * Pulls the count of assigned and unassigned shifts for the selectetd month
	 */
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
    
    /**
     * Updates the page to the selected page from the templates folder.
     * Removes any shifts and requests from the user to ensure one users data doesn't appear with another name for a moment
     * Pulls data for the given months schedule when moving to the scheduler before changing pages to be present when they arrive
     * Notes interaction as every other click
     */
	$scope.setPage = function (viewName) {
	    $scope.updateLastInteractionTime();
	    
		var newPage = "templates/page/" + viewName + ".html"
		
		if(newPage!=$scope.page){
			$scope.detailsChanged=false;
			
			$scope.lastShiftUpdate=null;
			$scope.lastShiftTableUpdate=null;
			
	        $scope.shifts=null;//Clear to ease visual transition
	        
	        if(newPage=="templates/page/scheduler.html"){
	        	var month = $scope.monthTab;
	        	if(month ==null){
	        		month = $scope.week.getMonth()+1;
	        	}
	    		$scope.setScheduleTab(month);//Update early to ease visual transition
	        }
	    	$scope.page = newPage;
	    	
	    	$scope.updateData();
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
