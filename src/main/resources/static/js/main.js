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

function MainNavigationController($scope, $modal, $http) {
	 $scope.init = function() {
        $scope.setPage("login");
		$scope.tab="Schedule";
		$scope.employeeTab="Schedule";
        
        $scope.showToast=false;
        $scope.alertMessage="";
        
        $scope.sortDescending = false;
        
        $scope.profile = null;
        $scope.idToken = null;
        $scope.user=false;
        $scope.manager=false;
        $scope.admin=false;
        
        $scope.lastShiftUpdate=null;
        $scope.lastClientUpdate=null;
        $scope.lastLocalClientUpdate=null;
        $scope.lastEmployeeUpdate=null;
        $scope.lastLocalEmployeeUpdate=null;
        $scope.lastCustomFieldUpdate = null;
        
        if($scope.week==undefined || $scope.week ==null){
			 $scope.week = new Date();
		}
        
        $scope.monthTab=$scope.week.getMonth()+2;
		
        $scope.employee=null;
        $scope.employees=null;
        $scope.client=null;
        $scope.clients=null;
        
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
		 $scope.employee=null;
		 $scope.tab="Schedule";
		 $scope.employeeTab="Schedule";
	    
		 var auth2 = gapi.auth2.getAuthInstance();
	     auth2.signOut().then(function () {});
	  }
	 
	 $scope.notify = function(message){
		 $scope.alertMessage=message;
		 $scope.alertError=false;
		 $scope.showToast=true;
		 var d = new Date();
		 var n = d.getTime();
		 $scope.lastAlert=n;
		 setTimeout($scope.autoHideToast,5000);
	 }
	 
	 $scope.warn = function(message){
		 $scope.alertMessage=message;
		 $scope.alertError=true;
		 $scope.showToast=true;

		 var d = new Date();
		 var n = d.getTime();
		 $scope.lastAlert=n;
		 setTimeout($scope.autoHideToast,5000);
	 }
	 
	 $scope.hideToast = function(){
		 $scope.showToast=false;
		 $scope.$apply();
	 }
	 
	 $scope.autoHideToast = function(){
		 var d = new Date();
		 var n = d.getTime();
		 var difference = n-$scope.lastAlert;
		 if(difference>=5000){
			 $scope.showToast=false;
			 $scope.$apply();
		 }
		 else{
			 setTimeout($scope.autoHideToast,500);
		 }
	 }
	 
	 $scope.listClients = function listClients() {
	    	if($scope.manager || $scope.admin){
	    		$http({
			           url: '/updateInfo/clients',
			           method: 'GET',
			           headers: {
			               'Authorization': $scope.idToken,
			               'Content-Type': 'application/x-www-form-urlencoded'
			           },
			           params: {
			           }
		       })
		       .then(function (response) {//TODO handle error state	    
		    	   console.log("client update info response:");
		    	   console.log(response);
		    	   console.log("$scope.lastLocalClientUpdate");
		    	   console.log($scope.lastLocalClientUpdate);
		    	   if($scope.lastLocalClientUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalClientUpdate.time.nano){
		    		   $scope.lastLocalClientUpdate=response.data;
		    		   
		    		   $http({
				            url: '/clients/',
				            method: 'GET',
				            headers: {
					            'Authorization': $scope.idToken,
				                'Content-Type': 'application/x-www-form-urlencoded'
				            },
				            params: {
				            }
				        })
				        .then(function(response) {
				        	$scope.clients =response.data;
				        });
		    	   }
		       })
	    	}
    }
	 
    $scope.listEmployees = function listEmployees() {
    	if($scope.manager || $scope.admin){
    		$http({
		           url: '/updateInfo/employees',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
	       })
	       .then(function (response) {//TODO handle error state	    	   
	    	   console.log(response);
	    	   if($scope.lastLocalEmployeeUpdate==null || response.data==null || response.data.time.nano!=$scope.lastLocalEmployeeUpdate.time.nano){
	    		   $scope.lastLocalEmployeeUpdate=response.data;
	    		   
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
			        	$scope.employees =response.data;
			        });
	    	   }
	       })
    	}
    }
	    
	 $scope.getLastShiftUpdate = function (){
			$http({
		           url: '/updateInfo/shifts',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
		       })
		       .then(function (response) {//TODO handle error state
		    	   $scope.setLastShiftUpdate(response.data);
		       })
		}
		
		$scope.getLastClientUpdate = function (){
			$http({
		           url: '/updateInfo/clients',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
		       })
		       .then(function (response) {//TODO handle error state
		    	   $scope.setLastClientUpdate(response.data);
		       })
		}
		
		$scope.getLastEmployeeUpdate = function (){
			var serverUpdate;
			
			$http({
		           url: '/updateInfo/employees',
		           method: 'GET',
		           headers: {
		               'Authorization': $scope.idToken,
		               'Content-Type': 'application/x-www-form-urlencoded'
		           },
		           params: {
		           }
		       })
		       .then(function (response) {//TODO handle error state
		    	   $scope.setLastEmployeeUpdate(response.data);
		    	   serverUpdate=response.data;
		       })
		       
		       return serverUpdate;
		}
	 
	 $scope.setLastShiftUpdate = function (time){
		 $scope.lastShiftUpdate = time;
	 }
	 $scope.setLastEmployeeUpdate = function (time){
		 $scope.lastEmployeeUpdate = time;
	 }
	 $scope.setLastClientUpdate = function (time){
		 $scope.lastClientUpdate = time;
	 }
	 $scope.setClient = function (client){
		 $scope.client = client;
	 }
	 $scope.setClients = function (clients){
		 $scope.clients = clients;
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
	 $scope.setEmployees = function(employees){
		 $scope.employees=employees;
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
