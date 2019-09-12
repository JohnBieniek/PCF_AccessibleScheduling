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

function EmployeesController($scope, $modal, $http, Status) {
	 $scope.multiTableEditing=false;
	 $scope.customFieldDataEditing=true;
	 $scope.sortField="first";
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
 
	 function saveEmployee(employee) {
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
	        	employee.id=response.id;
	            if(employee.customFields){
	            	var size = employee.customFields.length;
	            
		            for(var i = 0; i < size ;i++){
		                $http({
		                    url: '/compatibility/setCustomFieldData',
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
                $scope.listEmployees();
            });
        
        );
    }
	   
     $scope.viewEmployeeShifts = function(employee){
   	 	$scope.setSelectedEmployee(employee);
   	 	$scope.setStaffFilter(true);
   	 	$scope.setClientFilter(false);
   	 	$scope.setPage("shiftList");
     }
    
     $scope.callOffEmployee = function(employee){
   	 	$scope.setSelectedEmployee(employee);
   	 	$scope.setPage("vacation");
     }
    
    $scope.addEmployee = function (customFields) {
        var addModal = $modal.open({
            templateUrl: 'templates/modal/employeeForm.html',
            controller: EmployeeModalController,
            resolve: {
                employee: function () {
                    return {};
                },
                customFields: function(){
                	return clone(customFields);
                },
                action: function() {
                    return 'add';
                }
            }
        });

        addModal.result.then(function (employee) {
            saveEmployee(employee);
        });
    };

    $scope.updateEmployee = function (employee,customFields) {
    	employee.customFields=customFields;
        var updateModal = $modal.open({
            templateUrl: 'templates/modal/employeeForm.html',
            controller: EmployeeModalController,
            scope: $scope,
            resolve: {
                employee: function() {
                    return clone(employee);
                },
                customFields: function() {
                	return clone(customFields);
                },
                action: function() {
                    return 'update';
                }
            }
        });

        updateModal.result.then(function (employee) {
            saveEmployee(employee);
        });
    };
    
    $scope.list = function list(){
    	$scope.listCustomFields();
    	$scope.listEmployees();
    }
    $scope.listEmployees = function listEmployees() {
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
    
    $scope.getCustomFieldData = function (employee,customField,index){
    	$http({
            url: '/compatibility/customFieldData',
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
        	$.each($scope.employees, function() {
    		    if (this.id == response.data.employeeId) {
    		    	if(this.customValue===undefined){
    		    		this.customValue={};
    		    	}
    		    	this.customValue[response.data.numericResponse] = {};
    		        this.customValue[response.data.numericResponse] = response.data.booleanResponse;
    		    }
    		});
        });
    }
    
    $scope.setCustomFieldData = function (employee,customField,value){
    	value=!value;
    	$http({
            url: '/compatibility/setCustomFieldData',
            method: 'POST',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                employee: employee,
                customField: customField,
                value:value,
            }
        });
    }

    $scope.deleteEmployee = function (employee) {
    	if(confirm("Are you sure to remove information for "+employee.first+"?")){
    		$http({
	            url: '/employees/'+employee.id,
	            method: 'DELETE',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            }
	        })
	        .then(function (response) {//TODO handle error state
                Status.success("Employee removed.");
                $scope.listEmployees();
            });
    	}
    };
}
