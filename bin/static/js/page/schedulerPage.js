angular.module('vacation', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('employees');
	}).
	factory('Client', function ($resource) {
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

function SchedulingController($scope, $modal, $http, Clients, Client,Status) {
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
	 
	 
	$scope.setTab = function(newTab){
	  $scope.generatedBool = $scope.statusList[$scope.tab-1].generated;
      $scope.tab = newTab;
      
      if($scope.statusList[$scope.tab-1].generated){
    	  $scope.generated="Generated";
      }
      else{
    	  $scope.generated="Ungenerated";
      }
      
      if($scope.statusList[$scope.tab-1].assigned){
    	  $scope.assigned="Assigned";
      }
      else {
    	  $scope.assigned="Unassigned";
      }
      
      if($scope.statusList[$scope.tab-1].assigning){
    	  $scope.assigned="Assigning";
    	  if($scope.statusList[$scope.tab-1].stopped){
    		  $scope.assigned="Stopping";
    	  }
      }
      
      $scope.allowOvertime=false;
 	  $scope.allowUnavailable=false;
 	  $scope.prioritizeSecondShift=false;
 	  $scope.useDailyMax=true;
 	  $scope.useWeeklyMax=true;
 	  $scope.allowInactive=false;
 	  $scope.generatedBool = false;
      
      switch(newTab){
    	  case 1:
    		  $scope.monthName="January";
    		  break;
    	  case 2:
    		  $scope.monthName="Febuary";
    		  break;
    	  case 3:
    		  $scope.monthName="March";
    		  break;
    	  case 4:
    		  $scope.monthName="April";
    		  break;
    	  case 5:
    		  $scope.monthName="May";
    		  break;
    	  case 6:
    		  $scope.monthName="June";
    		  break;
    	  case 7:
    		  $scope.monthName="July";
    		  break;
    	  case 8:
    		  $scope.monthName="August";
    		  break;
    	  case 9:
    		  $scope.monthName="September";
    		  break;
    	  case 10:
    		  $scope.monthName="October";
    		  break;
    	  case 11:
    		  $scope.monthName="November";
    		  break;
    	  case 12:
    		  $scope.monthName="December";
    		  break;
      }
    };
    
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
    
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
	 $scope.setMonth = function setMonth(month) {
	        $scope.month = month;
	    }
    $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
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
	        	$scope.listStatusItems();
	        	
	        	$scope.allowOvertime=false;
		       	 $scope.allowUnavailable=false;
		       	 $scope.prioritizeSecondShift=false;
		       	 $scope.useDailyMax=true;
		       	 $scope.useWeeklyMax=true;
		       	 $scope.allowInactive=false;
		       	 $scope.generatedBool = false;
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
    		setTimeout(listStatusItems,10000);
        });
    }
    
    $scope.stopAssignment = function(month){
    	$scope.assigned="Stopping";
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
        });
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
}
