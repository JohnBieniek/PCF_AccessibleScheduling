angular.module('vacation', ['ngResource', 'ui.bootstrap']).
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

function SchedulingController($scope, $modal, $http, Status) {
	$scope.allowOvertime=false;
	$scope.allowUnavailable=false;
	$scope.prioritizeSecondShift=false;
	$scope.useDailyMax=true;
	$scope.useWeeklyMax=true;
	$scope.allowInactive=false;
	$scope.generatedBool = false;
	$scope.generated="Generated";
	$scope.assigned="Unassigned";
	
	$scope.statusList=[];
	$scope.unscheduled=0;
	$scope.scheduled=0;
	$scope.total=0;
	$scope.year=2019;
	 
	$scope.setScheduleTab = function(newTab){
	if($scope.statusList && $scope.statusList[$scope.monthTab-1])$scope.generatedBool = $scope.statusList[$scope.monthTab-1].generated;
      $scope.setMonthTab(newTab);
      
      $scope.getUnscheduled(newTab);
      $scope.getScheduled(newTab);
  	  $scope.total= +$scope.scheduled + +$scope.unscheduled;
      
      if($scope.statusList && $scope.statusList[$scope.monthTab-1] && $scope.statusList[$scope.monthTab-1].generated){
    	  $scope.generated="Generated<br />"+$scope.total;
      }
      else{
    	  $scope.generated="Ungenerated";
      }
      
      if($scope.statusList && $scope.statusList[$scope.monthTab-1] && $scope.statusList[$scope.monthTab-1].assigned){
    	  $scope.assigned="Assigned<br/>" +$scope.scheduled + " of "+$scope.total;
      }
      else {
    	  $scope.assigned="Unassigned";
      }
      
      if($scope.statusList && $scope.statusList[$scope.monthTab-1] && $scope.statusList[$scope.monthTab-1].assigning){
    	  $scope.assigned="Assigning";
    	  if($scope.statusList && $scope.statusList[$scope.monthTab-1] && $scope.statusList[$scope.monthTab-1].stopped){
    		  $scope.assigned="Stopping";
    	  }
      }
      
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
    	if($scope.statusList && $scope.statusList[month-1]) return $scope.statusList[month-1].generated && $scope.monthTab != month && !$scope.statusList[month-1].assigned&& !$scope.statusList[month-1].assigning;
  	  	return false;
    };
    $scope.isTabAssigned = function(month){
    	  if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].assigned && $scope.monthTab != month&& !$scope.statusList[month-1].assigning ;
    	  return false;
    }
    $scope.isGenerated = function(month){
    	if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].generated;
  	    return false;
    };
      
    $scope.isWorking = function(month){
    	  if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].assigning  && $scope.monthTab != month;
    	  return false;
    };
      
    $scope.isAssigned = function(month){
    	  if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].assigned;
    	  return false;
    };
    $scope.isErrored = function(month){
    	if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].errored;
  	    return false;
    };
    
    $scope.isAssigning = function(month){
    	if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].assigning
  	    return false;
    };
      
    $scope.isStopped = function(month){
    	  if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].stopped
    	  return false;
    };
          
    $scope.isNotAssignable = function(month){
    	assignable = true;  	  

		if($scope.statusList && $scope.statusList[month-1]){
			if($scope.statusList[month-1].generated=="false"){
	    		  assignable=false;
	    	}
    	}
    	  
	   	if($scope.statusList && $scope.statusList[month-1]){ 
		   	if($scope.statusList[month-1].assigning=="true"){
		   		assignable=false;
		   	}
	   	}
  	  
	   	if($scope.statusList && $scope.statusList[month-1]){ 
		   	if($scope.statusList[month-1].assigned=="true"){
	  		  assignable=false;
		   	}
	   	}
	   	
	   	unassignable = !assignable;
	   	
	   	return unassignable;
    };
      
	$scope.isSet = function(tabNum){
      return $scope.monthTab === tabNum;
    };
    
	function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
    }
    
	$scope.setMonth = function setMonth(month) {
	        $scope.month = month;
	}
	
	$scope.listClients = function listClients() {
		$http({
	        url: '/clients',
	        method: 'GET',
	        headers: {
	            'Authorization': $scope.idToken
	        },
	        params: {
	        }
	    })
	    .then(function(response) {
	    	$scope.clients=response.data;
	    	
	        if($scope.client==null){
	        	$scope.client = $scope.clients[0];
	        }
	    });
    }
    
    $scope.deleteShifts = function(month){
    	if(confirm("Are you sure to delete the shifts for "+$scope.monthName+"?")) {
	    	$http({
	            url: '/schedule/byMonth',
	            method: 'DELETE',
	            headers: {
	                'Authorization': $scope.idToken,
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
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
    		$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
    		$scope.setMonthTab($scope.monthTab);
    		if($scope.page.includes("scheduler")){
    			setTimeout(listStatusItems,12500);
    		}
        });
    }
    
    $scope.getScheduled = function getScheduled(month){
    	$http({
            url: '/schedule/scheduled',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
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
                'Authorization': $scope.idToken,
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
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                month: $scope.monthTab
            }
        })
        .then(function(response) {
        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
        });
    }

    $scope.stopAssignment = function(month){
    	if(confirm("Are you sure you want to stop assigning shifts for "+$scope.monthName+"?")){
	    	$http({
	            url: '/schedule/stopAssignment',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	                month: month
	            }
	        })
	        .then(function(response) {
	        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
	    		$scope.setTab($scope.monthTab);
	        });
    	}
    }
    
    $scope.generateShifts = function(month){
    	$http({
            url: '/schedule/generateShifts',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
                month: month,
                year: $scope.year
            }
        })
        .then(function(response) {
        	$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
        	
    		$scope.setTab($scope.monthTab);
        });
    }
    
    $scope.assignShifts = function(month, allowOvertime,allowInactive,allowUnavailable,prioritizeSecondShift,dailyMax,weeklyMax){
    	$scope.statusList[month-1].assigning=true;
    	$scope.assigned="Assigning";
    	
    	$http({
            url: '/schedule/staffShiftsSafely',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
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
        	
    		$scope.setTab($scope.monthTab);
        });
    }
    
    $scope.listStatusItems();
}
