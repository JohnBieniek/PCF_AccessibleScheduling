angular.module('shifts', ['ngResource', 'ui.bootstrap']).
	factory('Shifts', function ($resource) {
	    return $resource('shifts');
	}).
	factory('Shift', function ($resource) {
	    return $resource('shifts/:id', {id: '@id'});
	}).
	factory('Clients', function ($resource) {
        return $resource('clients');
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

function ShiftsController($scope, $filter, $modal, $http,  Shifts, Shift, Clients, Employees, Status) {
	 $scope.multiTableEditing=false;
	 $scope.selectedMonth=4;
	 $scope.selectedYear=2018;
	 $scope.sortField="startDate";
	 $scope.dates="Something";
	 $scope.tab = 1;
	 
	 $scope.changeSortOrder = function(){
		 $scope.sortDescending = !$scope.sortDescending;
	 }
    $scope.setTab = function(newTab){
      $scope.tab = newTab;
      if(newTab==1){
    	  $scope.sortField="startDate";
      }
      if(newTab==2){
    	  $scope.sortField="startTime";
    	  $scope.getDatesForMonth($scope.selectedYear,$scope.selectedMonth);
      }
    };

    $scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    };
	    
    $scope.getDatesForMonth = function(year,month){
    	$http({
            url: '/calendar/getDatesForMonth',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	year: year,
                month: month
            }
        })
        .then(function(response) {
    		$scope.dates = response.data;
        });
    }
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	    
	 $scope.viewShift = function(shift){
   	 	$scope.setSelectedShift(shift);
   	 	$scope.setPage("shift");
    }
	    
	 
    function saveShift(shift) {
        Shifts.save(shift,
            function () {
                Status.success("Shift saved");
                $scope.listShifts();
            },
            function (result) {
                Status.error("Error saving shift: " + result.status);
            }
        );
    }
    
    $scope.addShift = function (clients) {
        var addModal = $modal.open({
            templateUrl: 'templates/modal/shiftForm.html',
            controller: ShiftModalController,
            resolve: {
            	shift: function(){
            		return {};
            	},
            	clients: function(){
            		return clone(clients);
            	},
                action: function() {
                    return 'add';
                }
            }
        });

        addModal.result.then(function (shift) {
            saveShift(shift);
        });
    };
    
    $scope.updateShift = function (shift) {
        var updateModal = $modal.open({
            templateUrl: 'templates/modal/shiftForm.html',
            controller: ShiftModalController,
            resolve: {
                shift: function() {
                    return clone(shift);
                },
                clients: function(){
            		return {};
            	},
                action: function() {
                    return 'update';
                }
            }
        });

        updateModal.result.then(function (shift) {
            saveShift(shift);
        });
    };
    
    $scope.list = function list(){
    	$scope.listShifts();
    	listClients();
    	listEmployees();
    	$scope.getDatesForMonth($scope.selectedYear,$scope.selectedMonth);
    }
    $scope.listShifts = function listShifts() {
        $scope.shifts = Shifts.query();
    }
    
    function listClients() {
        $scope.clients = Clients.query();
    }
	
	function listEmployees() {
        $scope.employees = Employees.query();
    }

    $scope.deleteShift = function (shift) {
        Shift.delete({id: shift.id},
            function () {
                Status.success("Shift deleted");
                $scope.listShifts();
            },
            function (result) {
                Status.error("Error deleting shift: " + result.status);
            }
        );
    };
}
