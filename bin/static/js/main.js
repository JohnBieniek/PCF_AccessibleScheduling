angular.module('mainNavigation', ['ngResource', 'ui.bootstrap']).
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
        $scope.setPage("login");
        $scope.sortDescending = false;
        $scope.profile = null;
        $scope.idToken = null;
        $scope.user=false;
        $scope.manager=false;
        $scope.admin=false;
 
        if($scope.week==undefined || $scope.week ==null){
			 $scope.week = new Date();
		}
        
        $scope.monthTab=$scope.week.getMonth()+2;

		$scope.tab="Schedule";
		$scope.employeeTab="Schedule";
        $scope.employee=null;
        $scope.client=null;
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
	 };
	 
	 $scope.loggedIn = function loggedIn() {
		 if($scope.idToken!=null){
			 return true;
		 }
		 else{
			 return false;
		 }

	  }
	 $scope.signOut = function signOut() {
		 $scope.manager=false;
		 $scope.admin=false;
		 $scope.user=false;
		 $scope.idToken=null;
		 $scope.profile=null;
	    var auth2 = gapi.auth2.getAuthInstance();
	    auth2.signOut().then(function () {
	    });
	  }
	 $scope.setClient = function (client){
		 $scope.client = client;
	 }
	 $scope.setMonthTab = function(monthTab){
		 $scope.monthTab = monthTab;
	 }
	 $scope.setTab = function(newTab){
	      $scope.tab = newTab;
	 }
	 $scope.setEmployeeTab = function(newTab){
	      $scope.employeeTab = newTab;
	 }
	 
	 $scope.setEmployee = function(employee){
		 $scope.employee=employee;
	 }
	 $scope.changeSortOrder = function(){
		 $scope.sortDescending = !$scope.sortDescending;
	 }
	 $scope.setWeek = function (isWeek) {
        $scope.week = isWeek;
     };
	 $scope.setIdToken = function (idToken) {
	        $scope.idToken = idToken;
	    };
	    $scope.setProfile = function (profile) {
	        $scope.profile = profile;
	    };
	 $scope.setUser = function (isUser) {
	        $scope.user = isUser;
	    };
	    $scope.setManager = function (isManager) {
	        $scope.manager = isManager;
	    };
	    $scope.setAdmin = function (isAdmin) {
	        $scope.admin = isAdmin;
	    };
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
