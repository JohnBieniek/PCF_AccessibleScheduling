angular.module('employees', ['ngResource', 'ui.bootstrap']).
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

function EmployeesController($scope, $modal, $http, Employees, Employee, CustomFields, Status) {
	 $scope.multiTableEditing=false;
	 $scope.customFieldDataEditing=true;
	 $scope.sortField="first";
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 
	 function saveEmployee(employee) {
        Employees.save(employee,
            function (response) {
	        	employee.id=response.id;
	            if(employee.customFields){
	            	var size = employee.customFields.length;
	            
		            for(var i = 0; i < size ;i++){
		                $http({
		                    url: '/compatibility/setCustomFieldData',
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
                $scope.listEmployees();
            },
            function (result) {
                Status.error("Error saving employee: " + result.status);
            }
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
        $scope.employees = Employees.query();
    }
    
    $scope.listCustomFields = function listCustomFields() {
        $scope.customFields = CustomFields.query();
    }
    
    $scope.getCustomFieldData = function (employee,customField,index){
    	$http({
            url: '/compatibility/customFieldData',
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
        Employee.delete({id: employee.id},
            function () {
                Status.success("Employee deleted");
                $scope.listEmployees();
            },
            function (result) {
                Status.error("Error deleting employee: " + result.status);
            }
        );
    };
}
