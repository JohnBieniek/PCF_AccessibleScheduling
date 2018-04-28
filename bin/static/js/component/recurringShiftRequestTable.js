angular.module('recurringShiftRequests', ['ngResource', 'ui.bootstrap']).
	factory('RecurringShiftNeeds', function ($resource) {
	    return $resource('recurringShiftNeeds');
	}).
	factory('RecurringShiftNeed', function ($resource) {
	    return $resource('recurringShiftNeeds/:id', {id: '@id'});
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

function RecurringShiftRequestsController($scope, $modal, $http, RecurringShiftNeeds, RecurringShiftNeed, Employees, Status) {
	 $scope.multiTableEditing=false;
	 $scope.sortRecurringField="day";
	 
	 $scope.list= function list(){
		 $scope.listRecurringShiftRequests();
		 $scope.listEmployees();
	 }
	 $scope.listEmployees = function listEmployees(){
		 $scope.employees = Employees.query();
	 }
	 $scope.listRecurringShiftRequests = function listRecurringShiftRequests() {
		 $scope.recurringShiftNeeds = RecurringShiftNeeds.query();//RecurringShiftRequests.query();
	 }
	   
	 $scope.addRecurringShiftRequest = function (selectedClient,employees) {
	        var addModal = $modal.open({
	            templateUrl: 'templates/modal/recurringShiftNeedForm.html',
	            controller: RecurringShiftNeedModalController,
	            resolve: {
	            	selectedClient: function(){
	            		return clone(selectedClient);
	            	},
	            	employees: function(){
	            		return clone(employees);
	            	},
	                recurringShiftNeed: function () {
	                    return {};
	                },
	                selectedEmployee: function(){
	            		return "";
	            	},
	                action: function() {
	                    return 'add';
	                }
	            }
	        });
	        
	        addModal.result.then(function (recurringShiftRequest) {
	            saveRecurringShiftNeed(recurringShiftRequest);
	        });
	    };

    
    function clone (obj) {
        return JSON.parse(JSON.stringify(obj));
    }
    
    function saveRecurringShiftNeed(recurringShiftRequest) {
    	RecurringShiftNeeds.save(recurringShiftRequest,
            function () {
                Status.success("Recurring Shift Request saved");
                $scope.listRecurringShiftRequests();
            },
            function (result) {
                Status.error("Error saving Recurring Shift Request: " + result.status);
            }
        );
    }
       
    $scope.updateRecurringShiftRequest = function (selectedClient,recurringShiftRequest,employees) {
    	var selectedEmployee =null;
    	selectedEmployee = employees.filter(function( employee ) {
    		  return employee.id == recurringShiftRequest.staffId;
    	});
        var updateModal = $modal.open({
            templateUrl: 'templates/modal/recurringShiftNeedForm.html',
            controller: RecurringShiftNeedModalController,
            resolve: {
            	selectedClient: function(){
            		return clone(selectedClient);
            	},
            	recurringShiftNeed: function() {
                    return clone(recurringShiftRequest);
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

        updateModal.result.then(function (recurringShiftRequest) {
            saveRecurringShiftNeed(recurringShiftRequest);
        });
    };
    
    $scope.deleteRecurringShiftRequest = function (recurringShiftRequest) {
    	RecurringShiftNeed.delete({id: recurringShiftRequest.id},
            function () {
                Status.success("Recurring Shift Request deleted");
                $scope.listRecurringShiftRequests();
            },
            function (result) {
                Status.error("Error deleting Recurring Shift Request: " + result.status);
            }
        );
    };
}
