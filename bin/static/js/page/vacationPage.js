angular.module('vacation', ['ngResource', 'ui.bootstrap']).
	factory('Employees', function ($resource) {
	    return $resource('employees');
	}).
	factory('Employee', function ($resource) {
	    return $resource('employees/:id', {id: '@id'});
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

function VacationController($scope, $modal, $http, Employees, Employee,Status) {
	 $scope.multiTableEditing=false;

	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 
	 $scope.addAbsence = function(selectedEmployee,newDate){
	        selectedEmployee.requestedOff.push(newDate);
	        saveEmployee(selectedEmployee);
    };
	    
    $scope.removeAbsence=function(selectedEmployee,item){ 
    	 if(confirm("Are you sure you want to delete this request?")){
	        var index=selectedEmployee.requestedOff.indexOf(item)
	        selectedEmployee.requestedOff.splice(index,1);     
	        saveEmployee(selectedEmployee);
	    }
      }
	    
	 function saveEmployee(employee) {
        Employees.save(employee,
            function (response) {
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
                $scope.listEmployees();
            },
            function (result) {
                Status.error("Error saving employee: " + result.status);
            }
        );
    }
    
    $scope.listEmployees = function listEmployees() {
        $scope.employees = Employees.query();
    }
}
