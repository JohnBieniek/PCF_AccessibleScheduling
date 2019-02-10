angular.module('mainNavigation', ['ngResource', 'ui.bootstrap']).
    factory('Clients', function ($resource) {
        return $resource('clients');
    }).
    factory('Client', function ($resource) {
        return $resource('clients/:id', {id: '@id'});
    }).
    factory('Events', function ($resource) {
	    return $resource('events');
	}).
	factory('Event', function ($resource) {
	    return $resource('events/:id', {id: '@id'});
	}).
    factory('Employees', function ($resource) {
        return $resource('employees');
    }).
    factory('Employee', function ($resource) {
        return $resource('employees/:id', {id: '@id'});
    }).
    factory('CustomFields', function ($resource) {
        return $resource('customFields');
    }).
    factory('CustomField', function ($resource) {
        return $resource('customFields/:id', {id: '@id'});
    }).
    factory('Shifts', function ($resource) {
        return $resource('shifts');
    }).
    factory('Shift', function ($resource) {
        return $resource('shifts/:id', {id: '@id'});
    }).
    factory('Compatibility', function ($resource) {
        return $resource('compatibility');
    }).
    factory('RecurringShiftNeeds', function ($resource) {
        return $resource('recurringShiftNeeds');
    }).
    factory('RecurringShiftNeed', function ($resource) {
        return $resource('recurringShiftNeeds/:id', {id: '@id'});
    }).
    factory('ShiftRequests', function ($resource) {
        return $resource('shiftRequests');
    }).
    factory('ShiftRequest', function ($resource) {
        return $resource('shiftRequests/:id', {id: '@id'});
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

function MainNavigationController($scope, $modal, $http, Status) {
	 $scope.init = function() {
        $scope.setPage("scheduler");
        $scope.sortDescending = false;
        
 
        $scope.selectedMonth=1;
	   	$scope.selectedYear=2019;
	   	$scope.sortField="startDate";
	   	$scope.selectedWeek=null;
	   	$scope.assignedFilter=false;
	   	$scope.unassignedFilter= false;
	   	$scope.clientFilter = false;
	   	$scope.staffFilter = false;
	   	$scope.requestedFilter= false;
	   	$scope.unrequestedFilter=false;
	   	$scope.recurringFilter = false;
   	 	$scope.selectedClient= false;//Used by clientList.html to select a client for scheduling on scheduling.html
   	 	$scope.selectedEmployee= false;//Used by employeeList.html to select an employee for vacation on vacation.html
   	 	$scope.selectedShift= false;//Used by shiftList.html to select a shift for assignment on shift.html
   	 	$scope.multiTableEditing=false;//Have I fixed in table editing? No
   	 	$scope.customFieldDataEditing=true;//TODO factor out this flag
	 };
	 $scope.changeSortOrder = function(){
		 $scope.sortDescending = !$scope.sortDescending;
	 }
    $scope.setPage = function (viewName) {
        $scope.page = "templates/page/" + viewName + ".html";
    };
    $scope.setSortField = function(sortField){
    	$scope.sortField= sortField;
    }
    $scope.setWeekFilter = function(weekFilter){
    	$scope.weekFilter= weekFilter;
    }
    $scope.setClientFilter = function(clientFilter){
    	$scope.clientFilter= clientFilter;
    }
    $scope.setStaffFilter = function(staffFilter){
    	$scope.staffFilter= staffFilter;
    }
    $scope.setAssignedFilter = function(assigned){
    	$scope.assignedFilter= assigned;
    }
    $scope.setUnassignedFilter= function(unassigned){
    	$scope.unassignedFilter= unassigned;
    }
    $scope.setRequestedFilter= function(requested){
    	$scope.requestedFilter= requested;
    }
    $scope.setUnrequestedFilter= function(unrequested){
    	$scope.unrequestedFilter= unrequested;
    }
    $scope.setRecurringFilter= function(recurring){
    	$scope.recurringFilter= recurring;
    }
    $scope.setSelectedYear= function(year){
    	$scope.selectedYear= year;
    }
    $scope.setSelectedMonth= function(month){
    	$scope.selectedMonth= month;
    }
    $scope.setSelectedWeek= function(week){
    	$scope.selectedWeek= week;
    }
    $scope.setSelectedEmployee= function(employee){
    	$scope.selectedEmployee= employee;
    }
    $scope.setStaffFilter=function(boolean){
    	$scope.staffFilter=boolean;
    }
    
    $scope.setSelectedClient= function(client){
    	$scope.selectedClient= client;
    }
    $scope.setClientFilter=function(boolean){
    	$scope.clientFilter=boolean;
    }
    
    $scope.setSelectedShift= function(shift){
    	$scope.selectedShift= shift;
    }
}
