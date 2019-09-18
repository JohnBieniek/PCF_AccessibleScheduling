angular.module('employees', ['ngResource', 'ui.bootstrap']).
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

function AlertsController($scope, $modal, $http, Status) {
	 $scope.multiTableEditing=false;
	 $scope.customFieldDataEditing=true;
	 $scope.sortField="first";
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
 
	  $scope.listAlerts = function (){
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
	        	console.log(response.data);
	        	$scope.alerts=response.data;
	        });
	    }
	  $scope.noAlertsFound = function (){
		  let enable = true;
		  
		  if(typeof $scope.alerts !== 'undefined' && $scope.alerts !=null && $scope.alerts.length>0){
			  enable=false;
		  }
		  return enable;
	  }
	
	  $scope.assignSelectedEmployee = function(alert,employee){
		  if(typeof employee !== 'undefined' && employee !=null && employee != undefined && employee !=false){
			  if(confirm("Are you sure you want to give "+alert.name +" access to the profile of " +employee.first+"?")){
		    	$http({
		            url: '/auth/approve',
		            method: 'GET',
		            headers: {
    		            'Authorization': $scope.idToken,
		                'Content-Type': 'application/x-www-form-urlencoded'
		            },
		            params: {
		            	userId: alert.id,
		            	employeeId: employee.id
		            }
		        })
		        .then(function(response) {
		        	Status.success("Access granted.");
		        	$scope.alerts=response.data;
		        });
		     }
		  }
		  else{
			  confirm("Select an employee to grant "+alert.name +" access to.");
		  }
	  }
	  
	  $scope.saveEmployee = function saveEmployee(employee) {
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
	 	   $http({
	           url: '/employees',
	           method: 'POST',
	           headers: {
	               'Authorization': $scope.idToken,
	               'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	        	   employee: employee
	           }
	       })
	       .then(function (response) {//TODO handle error state
	    	   setEmployee(response.data);
	       	   employee.id=response.data.id;
	           if(employee.customFields){
		            var size = employee.customFields.length;
		           
		            for(var i = 0; i < size ;i++){
		                $http({
		                    url: 'https://scheduleaccessqa.cfapps.io/compatibility/setCustomFieldData',
		                    method: 'POST',
		                    headers: {
		    		            'Authorization': $scope.idToken,
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
	       });
	    }
	    
    $scope.list = function list(){
    	$scope.listEmployees();
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
	        });
    	}
    }

    $scope.deleteAlert = function (alert) {
    	let alertName = alert.name;
    	if(confirm("Are you sure you want to deny access for "+alertName+"?")){
        	$http({
                url: '/auth/deny',
                method: 'GET',
                headers: {
		            'Authorization': $scope.idToken,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                params: {
                    userId: alert.id
                }
            })
	        .then(function(response) {
	        	Status.success("Access denied to "+alertName+".");
	        	$scope.alerts=response.data;
	        });
    	}
    };
}
