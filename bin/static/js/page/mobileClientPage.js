angular.module('client', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('clients');
	}).
	factory('Client', function ($resource) {
	    return $resource('client/:id', {id: '@id'});
	}).
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

function MobileClientController($scope, $modal, $http, Clients, Client,Employee, Employees, Status) {
	 $scope.multiTableEditing=false;
	 $scope.month=1;
	 $scope.allowOvertime=false;
	 $scope.allowUnavailable=false;
	 $scope.prioritizeSecondShift=false;
	 $scope.useDailyMax=true;
	 $scope.useWeeklyMax=true;
	 $scope.allowInactive=false;
	 $scope.generatedBool = false;
	 $scope.generated="Generated";
	 $scope.assigned="Unassigned";
	 $scope.tab=1;
	 $scope.monthName="January";
	 $scope.statusList=[];
	 $scope.unscheduled=0;
	 $scope.scheduled=0;
	 $scope.selectedInterval="days";
	$scope.setTab = function(newTab){
      $scope.tab = newTab;
	}
	$scope.setClient = function(newClient){
	      $scope.client = newClient;
		}
	
	$scope.setInterval = function(newInterval){
	      $scope.interval = newInterval;
		}
    
    $scope.toggleInactive = function(){
    	$scope.allowInactive = !$scope.allowInactive;
    }
    
    $scope.toggleUnavailable = function(){
    	$scope.allowUnavailable = !$scope.allowUnavailable;
    }
    $scope.toggleOvertime = function(){
    	$scope.allowOvertime = !$scope.allowOvertime;
    }
    $scope.toggleDailyMax = function(){
    	$scope.useDailyMax = !$scope.useDailyMax;
    }
    $scope.toggleWeeklyMax = function(){
    	$scope.useWeeklyMax = !$scope.useWeeklyMax;
    }
    $scope.togglePrioritizationHistory = function(){
    	$scope.prioritizeSecondShift = !$scope.prioritizeSecondShift;
    }
    
    $scope.isTabGenerated = function(month){
        return $scope.statusList[month-1].generated && $scope.tab != month && !$scope.statusList[month-1].assigned&& !$scope.statusList[month-1].assigning;
      };
      $scope.isTabAssigned = function(month){
          return $scope.statusList[month-1].assigned && $scope.tab != month&& !$scope.statusList[month-1].assigning ;
        };
    $scope.isGenerated = function(month){
        return $scope.statusList[month-1].generated;
      };
      
      $scope.isWorking = function(month){
          return $scope.statusList[month-1].assigning  && $scope.tab != month;
        };
      
      $scope.isAssigned = function(month){
          return $scope.statusList[month-1].assigned;
        };
    $scope.isErrored = function(month){
        return $scope.statusList[month-1].errored;
      };
    $scope.isAssigning = function(month){
    	return $scope.statusList[month-1].assigning
      };
      
      $scope.isStopped = function(month){
      	return $scope.statusList[month-1].stopped
        };
          
      $scope.isNotAssignable = function(month){
    	  assignable = true;
    	  
    	  console.log("assignable:"+assignable);
    	  if($scope.statusList[month-1].generated=="false"){
    		  assignable=false;
    	  }
    	  
	   	  console.log("assignable:"+assignable);
	   	  
		  if($scope.statusList[month-1].assigning=="true"){
	  		assignable=false;
		  }
  	  
	   	  console.log("assignable:"+assignable);
	   	  
	   	if($scope.statusList[month-1].assigned=="true"){
  		  assignable=false;
  	  }
  	  unassignable = !assignable;
	   	  console.log("unassignable:"+unassignable);
    	  console.log("$scope.statusList[month-1].generated" + $scope.statusList[month-1].generated);
    	  console.log("$scope.statusList[month-1].assigning "+$scope.statusList[month-1].assigning );
    	  console.log("$scope.statusList[month-1].assigned"+$scope.statusList[month-1].assigned);
      	return unassignable;
       };
      
	 $scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    };
    
    $scope.isClientSet = function(client){
        return $scope.client === client;
      };
    
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
	 $scope.setMonth = function setMonth(month) {
	        $scope.month = month;
	    }
    $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
        $scope.employees = Employees.query();
    }
    
    $scope.listEmployees = function listEmployees() {
        $scope.employees = Employees.query();
    }
    
    $scope.deleteShifts = function(month){
    	if(confirm("Are you sure to delete the shifts for "+$scope.monthName+"?")) {
	    	$http({
	            url: '/schedule/byMonth',
	            method: 'DELETE',
	            headers: {
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	            	month: month
	            }
	        })
	        .then(function(response) {
	        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
	        	$scope.getUnscheduled(month);
	            $scope.getScheduled(month);
	        });
    	}
    }
    
    $scope.listStatusItems = function listStatusItems(){
    	$http({
            url: '/schedule/statusList',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
    		$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
    		console.log(response.data);
    		console.log($scope.statusList[0].generated);
    		$scope.setTab($scope.tab);
    		setTimeout(listStatusItems,12500);
        });
    }
    
    $scope.getScheduled = function getScheduled(month){
    	$http({
            url: '/schedule/scheduled',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	month: month
            }
        })
        .then(function(response) {
        	$scope.scheduled=response.data;
        });
    }
    
    $scope.getUnscheduled = function getUnscheduled(month){
    	$http({
            url: '/schedule/unscheduled',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	month: month
            }
        })
        .then(function(response) {
        	$scope.unscheduled=response.data;
        });
    }
    
    $scope.finishAssignment = function finishAssignment(){
    	$http({
            url: '/schedule/finishAssignment',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                month: $scope.tab
            }
        })
        .then(function(response) {
        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
        });
    }

    $scope.stopAssignment = function(month){
    	if(confirm("Are you sure you want to stop assigning shifts for "+$scope.monthName+"?")){
	    	// $scope.assigned="Stopping";
	    	$http({
	            url: '/schedule/stopAssignment',
	            method: 'GET',
	            headers: {
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	                month: month
	            }
	        })
	        .then(function(response) {
	        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
	    		console.log(response.data);
	    		console.log($scope.statusList[0].generated);
	    		$scope.setTab($scope.tab);
	    		// setTimeout($scope.finishAssignment,10000);
	        });
    	}
    }
    
    $scope.generateShifts = function(month){
    	$http({
            url: '/schedule/generateShifts',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                month: month
            }
        })
        .then(function(response) {
        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
    		console.log(response.data);
    		console.log($scope.statusList[0].generated);
    		$scope.setTab($scope.tab);
    		
        });
    }
    
    
    
    $scope.addRequest = function (selectedClient,employees) {
		 console.log("attempting to open requestForm and RequestModalController");
       var addModal = $modal.open({
           templateUrl: 'templates/modal/requestForm.html',
           controller: RequestModalController,
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
       Requests.save(shiftRequest,
           function () {
               Status.success("Request saved");
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
           templateUrl: 'templates/modal/requestForm.html',
           controller: RequestModalController,
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
        Request.delete({id: shiftRequest.id},
            function () {
                Status.success("Shift Request deleted");
                $scope.listShiftRequests();
            },
            function (result) {
                Status.error("Error deleting shift Request: " + result.status);
            }
        );
    };
    
    
    
    
    
    $scope.assignShifts = function(month, allowOvertime,allowInactive,allowUnavailable,prioritizeSecondShift,dailyMax,weeklyMax){
    	$scope.statusList[month-1].assigning=true;
    	$scope.assigned="Assigning";
    	console.log("$scope.statusList" +$scope.statusList.toString());
    	$http({
            url: '/schedule/staffShiftsSafely',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                month: month,
                year: '2019',
                allowOvertime: allowOvertime,
                allowInactive: allowInactive,
                allowUnavailable: allowUnavailable,
                prioritizeSecondShift: prioritizeSecondShift,
                dailyMax: dailyMax,
                weeklyMax: weeklyMax
            }
        })
        .then(function(response) {
        	
        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
    		console.log(response.data);
    		console.log($scope.statusList[0].generated);
    		$scope.setTab($scope.tab);
        });
    }
    $scope.listStatusItems();
}
