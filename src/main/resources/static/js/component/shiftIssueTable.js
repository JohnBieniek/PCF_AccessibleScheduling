angular.module('shiftIssues', ['ngResource', 'ui.bootstrap']).
	factory('Shifts', function ($resource) {
	    return $resource('shifts');
	}).
	factory('Shift', function ($resource) {
	    return $resource('shifts/:id', {id: '@id'});
	}).
	factory('Employees', function ($resource) {
	    return $resource('employees');
	}).
	factory('Clients', function ($resource) {
	    return $resource('clients');
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

function ShiftIssueController($scope, $modal, $http, Shifts, Shift, Employees,Clients, Status) {
	 $scope.sortShiftIssueField="date";
	 $scope.sortScheduleIssueField="staff";
	 $scope.selectedMonth='1';
	 $scope.shiftIssues;
	 $scope.scheduleIssues;
	 $scope.list= function list(){
		 $scope.listShifts();
		 $scope.listEmployees();
		 $scope.listClients();
		 $scope.listScheduleIssues();
		 $scope.listShiftIssues();
	 }
	 $scope.listScheduleIssues = function(){
	    	$http({
	            url: '/cleaning/scheduleNotifications',
	            method: 'GET',
	            headers: {
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	         
	            }
	        })
	        .then(function(response) {
	    		$scope.scheduleIssues = response.data;
	    		console.log(response.data);
	        });
	    }
	 $scope.listShiftIssues = function(){
	    	$http({
	            url: '/cleaning/shiftNotifications',
	            method: 'GET',
	            headers: {
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	         
	            }
	        })
	        .then(function(response) {
	    		$scope.shiftIssues = response.data;
	    		console.log(response.data);
	        });
	    }
	 $scope.listShifts = function listShifts() {
         $scope.shifts = Shifts.query();
     }
	  
	 $scope.listEmployees = function listEmployees(){
		 $scope.employees = Employees.query();
	 }
	 $scope.listClients = function listClients(){
		 $scope.clients = Clients.query();
	 }
	 $scope.fixShift = function (selectedIssue,employees,clients) {
   	 	$scope.setSelectedShift(selectedIssue.issues[0].shift);
   	 	$scope.setPage("shift");
//        var shiftIssueModal = $modal.open({
//            templateUrl: 'templates/modal/shiftIssueForm.html',
//            controller: ShiftIssueModalController,
//            resolve: {
//            	selectedIssue: function(){
//            		return clone(selectedIssue);
//            	},
//            	employees: function(){
//            		return clone(employees);
//            	},
//            	clients: function(){
//            		return clone(clients);
//            	}
//            }
//        });
//
//        shiftIssueModal.result.then(function (shiftIssue) {
//            $scope.updateShiftIssue(shiftIssue);
//        });
    };
    
    function clone (obj) {
        return JSON.parse(JSON.stringify(obj));
    }
$scope.updateShiftIssue = function(shiftIssue){
	$http({
      url: '/cleaning/updateShiftIssue',
      method: 'POST',
      headers: {
          'Content-Type': 'application/x-www-form-urlencoded'
      },
      params: {
      	shiftIssue: shiftIssue
      }
  })
  .then(function(response) {
		$scope.shiftIssues = response.data;
  });
}
}
