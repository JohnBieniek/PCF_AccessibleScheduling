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
        $scope.setPage("splash");
        $scope.sortDescending = false;
        
   	 	$scope.selectedClient= null;//Used by clientList.html to select a client for scheduling on scheduling.html
   	 	$scope.selectedEmployee= null;//Used by employeeList.html to select an employee for vacation on vacation.html
   	 	$scope.selectedShift= null;//Used by shiftList.html to select a shift for assignment on shift.html
   	 	$scope.multiTableEditing=false;//Have I fixed in table editing? No
   	 	$scope.customFieldDataEditing=true;//TODO factor out this flag
	 };
    
    $scope.setPage = function (viewName) {
        $scope.page = "templates/page/" + viewName + ".html";
    };
    
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
