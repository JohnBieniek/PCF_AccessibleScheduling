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

function AlertsController($scope, $modal, $http) {
	 $scope.noAlertsFound = function (){
		  let enable = true;
		  
		  if(typeof $scope.alerts !== 'undefined' && $scope.alerts !=null && $scope.alerts.length>0){
			  enable=false;
		  }
		  return enable;
	  }
	
	  $scope.assignSelectedEmployee = function(alert,employee){
		  $scope.updateLastInteractionTime();
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
		        	if(response.data=="APPROVED"){
			        	$scope.notify("Access granted.");
			        	$scope.listAlerts();
		        	}else if(response.data=="DELETED"){
			        	$scope.warn("Your update has not been applied as someone else just answered this request.");
			        	$scope.listAlerts();
		        	}
		        });
		     }
		  }
		  else{
			  confirm("Select an employee to grant "+alert.name +" access to.");
		  }
	}
	
    $scope.deleteAlert = function (alert) {
    	$scope.updateLastInteractionTime();
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
	        	if(response.data=="DENIED"){
		        	$scope.notify("Access denied to "+alertName+".");
		        	$scope.listAlerts();
	        	}else if(response.data=="DELETED"){
		        	$scope.warn("Your update has not been applied as someone else just answered this request.");
		        	$scope.listAlerts();
	        	}
	        });
    	}
    };
}
