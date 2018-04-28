angular.module('singleShiftRequests', ['ngResource', 'ui.bootstrap']).
	factory('ShiftRequests', function ($resource) {
	    return $resource('shiftRequests');
	}).
	factory('ShiftRequest', function ($resource) {
	    return $resource('shiftRequests/:id', {id: '@id'});
	}).
	factory('Employees', function ($resource) {
	    return $resource('employees');
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

function SingleShiftRequestsController($scope, $modal, $http, ShiftRequests, ShiftRequest, Employees, Status) {
	 $scope.multiTableEditing=false;
	 $scope.sortRequestsField="startDate";
	 $scope.selectedMonth='02';
	 
	 $scope.list= function list(){
		 $scope.listShiftRequests();
		 $scope.listEmployees();
	 }
	 $scope.listShiftRequests = function listShiftRequests() {
         $scope.shiftRequests = ShiftRequests.query();
     }
	  
	 $scope.listEmployees = function listEmployees(){
		 $scope.employees = Employees.query();
	 }
	 $scope.addShiftRequest = function (selectedClient,employees) {
        var addModal = $modal.open({
            templateUrl: 'templates/modal/shiftRequestForm.html',
            controller: ShiftRequestModalController,
            resolve: {
            	selectedClient: function(){
            		return clone(selectedClient);
            	},
            	employees: function(){
            		return clone(employees);
            	},
            	selectedEmployee: function(){
            		return "";
            	},
                shiftRequest: function () {
                    return {};
                },
                action: function() {
                    return 'add';
                }
            }
        });

        addModal.result.then(function (shiftRequest) {
            saveShiftRequest(shiftRequest);
        });
    };
    
    function clone (obj) {
        return JSON.parse(JSON.stringify(obj));
    }
    
    function saveShiftRequest(shiftRequest) {
        ShiftRequests.save(shiftRequest,
            function () {
                Status.success("Shift Request saved");
                $scope.listShiftRequests();
            },
            function (result) {
                Status.error("Error saving shift Request: " + result.status);
            }
        );
    }
    
    $scope.updateShiftRequest = function (selectedClient, shiftRequest,employees) {
    	var selectedEmployee = employees.filter(function( employee ) {
  		  return employee.id == shiftRequest.staffId;
    	});
        var updateModal = $modal.open({
            templateUrl: 'templates/modal/shiftRequestForm.html',
            controller: ShiftRequestModalController,
            resolve: {
            	selectedClient: function(){
            		return clone(selectedClient);
            	},
                shiftRequest: function() {
                    return clone(shiftRequest);
                },
            	employees: function(){
            		return clone(employees);
            	},
            	selectedEmployee: function(){
            		if(selectedEmployee!=null && selectedEmployee.length>0){
            			return clone(selectedEmployee[0]);
            		}
            		return "";
            	},
                action: function() {
                    return 'update';
                }
            }
        });

        updateModal.result.then(function (shiftRequest) {
            saveShiftRequest(shiftRequest);
        });
    };
    
    $scope.deleteShiftRequest = function (shiftRequest) {
         ShiftRequest.delete({id: shiftRequest.id},
             function () {
                 Status.success("Shift Request deleted");
                 $scope.listShiftRequests();
             },
             function (result) {
                 Status.error("Error deleting shift Request: " + result.status);
             }
         );
     };
}
