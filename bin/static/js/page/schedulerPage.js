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

function SchedulingController($scope, $modal, $http) {
	$scope.init = function(){
		$scope.setScheduleTab($scope.monthTab);
	}
	
    //Checkbox toggles
    $scope.toggleInactive = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.allowInactive = !$scope.allowInactive;
    }
    $scope.toggleUnavailable = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.allowUnavailable = !$scope.allowUnavailable;
    }
    $scope.toggleOvertime = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.allowOvertime = !$scope.allowOvertime;
    }
    $scope.toggleDailyMax = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.useDailyMax = !$scope.useDailyMax;
    }
    $scope.toggleWeeklyMax = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.useWeeklyMax = !$scope.useWeeklyMax;
    }
    $scope.togglePrioritizationHistory = function(){
   	 	$scope.updateLastInteractionTime();
    	$scope.prioritizeSecondShift = !$scope.prioritizeSecondShift;
    }
    
    
    //Conditional class checks
    $scope.isTabGenerated = function(month){
    	if($scope.statusList && $scope.statusList[month-1]) return $scope.statusList[month-1].generated && $scope.monthTab != month && !$scope.statusList[month-1].assigned&& !$scope.statusList[month-1].assigning;
  	  	return false;
    };
    $scope.isTabAssigned = function(month){
    	  if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].assigned && $scope.monthTab != month&& !$scope.statusList[month-1].assigning ;
    	  return false;
    }
    $scope.isGenerating = function(month){
    	if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].generating;
  	    return false;
    };
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
			else if($scope.statusList[month-1].generating=="true"){
	    		  assignable=false;
	    	}
			else if($scope.statusList[month-1].assigning=="true"){
		   		assignable=false;
		   	}
		   	else if($scope.statusList[month-1].assigned=="true"){
		  		  assignable=false;
		   	}
    	}
    	  
	   	if($scope.statusList && $scope.statusList[month-1]){ 

	   	}
  	  
	   	if($scope.statusList && $scope.statusList[month-1]){ 
		   	
	   	}
	   	
	   	unassignable = !assignable;
	   	
	   	return unassignable;
    };
	$scope.isSet = function(tabNum){
      return $scope.monthTab === tabNum;
    };
    
	
    //Button handlers
    /**
     * Checks to ensure we aren't generating or generated
     * Sets the generated box text to "Generating"
     * Sets this months status to generating
     * Updates last interaction
     * Calls generateShifts on the server
     * Sets this months status to generated 
     * Sets the generated box text to "Generated"
     * Updates the total shifts with the generateShfits response
     * Warns "Failed to generate shifts on failure"
     */
    $scope.generateShifts = function(month){
    	if($scope.statusList[month-1].generating==false && $scope.statusList[month-1].generated==false){
        	$scope.statusList[month-1].generating=true;
        	$scope.setGenerated("Generating");
       	 	$scope.updateLastInteractionTime();
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
            	$scope.statusList[month-1].generating=false;
            	$scope.statusList[month-1].generated=true;
            	$scope.setGenerated("Generated");
            	if($scope.monthTab==month){
            		$scope.setTotal(response.data);
            	}
            }) 
            .catch(function(data, status) {
            	console.error('Gists error', data,status);
        		if(data.status==404){
        			$scope.warn("Failed to generate shifts. If connection trouble persits contact your representative.");
        		}
        		else{
        			$scope.warn("Failed to generate shifts");
        		}
            });
    	}
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
	        	$scope.setScheduled("0");
	        	$scope.setUnscheduled("0");
	        	$scope.setTotal("0");
	        	setTimeout($scope.updateData,2000);
	        }) 
	        .catch(function(data, status) {
	        	console.error('Gists error', data,status);
	    		if(data.status==404){
	    			$scope.warn("Failed to delete shifts. If connection trouble persits contact your representative.");
	    		}
	    		else{
	    			$scope.warn("Failed to delete shifts");
	    		}
	        });
    	}
    }
    $scope.assignShifts = function(month, allowOvertime,allowInactive,allowUnavailable,prioritizeSecondShift,dailyMax,weeklyMax){
   	 	$scope.updateLastInteractionTime();
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
        	
        	setTimeout($scope.updateData,2000);
        }) 
        .catch(function(data, status) {
        	console.error('Gists error', data,status);
    		if(data.status==404){
    			$scope.warn("Failed to assign shifts. If connection trouble persits contact your representative.");
    		}
    		else{
    			$scope.warn("Failed to assign shifts");
    		}
        });
    }
    $scope.stopAssignment = function(month){
   	 	$scope.updateLastInteractionTime();
    	if(confirm("Are you sure you want to stop assigning shifts for "+$scope.monthName+"?")){
        	$scope.statusList[month-1].assigning=false;
        	$scope.setAssigning("Stopping");
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
	    		setTimeout($scope.updateData,2000);
	        }) 
	        .catch(function(data, status) {
	        	console.error('Gists error', data,status);
	    		if(data.status==404){
	    			$scope.warn("Failed to stop assigning shifts. If connection trouble persits contact your representative.");
	    		}
	    		else{
	    			$scope.warn("Failed to stop assigning shifts");
	    		}
	        });
    	}
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
        	setTimeout($scope.updateData,2000);
        }) 
        .catch(function(data, status) {
        	console.error('Gists error', data,status);
    		if(data.status==404){
    			$scope.warn("Failed to assign shifts. If connection trouble persits contact your representative.");
    		}
    		else{
    			$scope.warn("Failed to assign shifts");
    		}
        });
    }
}
