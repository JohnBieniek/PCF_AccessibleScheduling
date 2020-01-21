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
	//Set our initial tab as this month
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
    /**Currently shows the tab as generated when generated and not assigned**/
    $scope.isTabGenerated = function(month){
    	if($scope.statusList && $scope.statusList[month-1]) return $scope.statusList[month-1].generated && $scope.monthTab != month && !$scope.statusList[month-1].assigned;
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
    //I've currently disabled showing which month is scheduling on the side as only one should be and the colors look odd
    $scope.isWorking = function(month){
    	  //if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].assigning  && $scope.monthTab != month;
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
    $scope.isDeleting = function(month){
    	if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].deleting
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
    $scope.isStopping = function(month){
  	  if($scope.statusList && $scope.statusList[month-1])return $scope.statusList[month-1].stopping
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
        	$scope.updateScheduleDisplay();
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
            	if($scope.monthTab==month){
            		$scope.setTotal(response.data);
            	}
            	$scope.updateData();
            }) 
            .catch(function(data, status) {
            	console.error('Gists error', data,status);
        		if(data.status==404){
        			$scope.warn("Failed to generate shifts. If connection trouble persits contact your representative.");
        		}
        		else if (data.status!==429){
        			$scope.warn("Failed to generate shifts");
	    		}
        		else{
                	$scope.updateData();
        		}
            });
    	}
    }

    /**
     * Checks to ensure we aren't ungenerated or assigning
     * Sets the assigned box text to "Assigning"
     * Sets this months status to assigning
     * Updates last interaction
     * Calls staffShiftsSafely on the server with all our selected options
     * Sets this months status to assigned on completion 
     * Sets the generated box text to "Assigned"
     * Updates the status list on completion
     * Warns "Failed to assign shifts on failure"
     */
    $scope.assignShifts = function(month, allowOvertime,allowInactive,allowUnavailable,prioritizeSecondShift,dailyMax,weeklyMax){
    	if($scope.statusList[month-1].generated==true && $scope.statusList[month-1].assigning==false){
	   	 	$scope.updateLastInteractionTime();
	    	$scope.statusList[month-1].assigning=true;
	    	$scope.statusList[month-1].stopped=false;
	    	$scope.statusList[month-1].assigned=false;
	    	$scope.statusList[month-1].errored=false;
	    	$scope.updateScheduleDisplay();
	    	$http({
	            url: '/schedule/staffShiftsSafely',
	            method: 'GET',
	            headers: {
	                'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	            },
	            params: {
	                month: month,
	                year: $scope.year,
	                allowOvertime: allowOvertime,
	                allowInactive: allowInactive,
	                allowUnavailable: allowUnavailable,
	                prioritizeSecondShift: prioritizeSecondShift,
	                dailyMax: dailyMax,
	                weeklyMax: weeklyMax
	            }
	        })
	        .then(function(response) {
		    	$scope.updateData();
	        }) 
	        .catch(function(data, status) {
	        	$scope.updateData();
	        	console.error('Gists error', data,status);
	    		if(data.status==404){
	    			$scope.warn("Failed to assign shifts. If connection trouble persits contact your representative.");
	    		}
	    		else if (data.status==429){
	    			$scope.warn("Assignment has just been started by another user and is already running for this month.");
	    		}
	    		else if (data.status==428){
	    			$scope.warn("Failled to assign shifts because they were just deleted by another user.");
	    		}
	        });
    	}
    }
    
    /**
     * Notes interaction
     * Checks to ensure we are currently assigning
     * Asks the user if they're certain they want to stop assignment
     */
    $scope.stopAssignment = function(month){
   	 	$scope.updateLastInteractionTime();
   	 	if($scope.statusList[month-1] && $scope.statusList[month-1].assigning==true){
	    	if(confirm("Are you sure you want to stop assigning shifts for "+$scope.monthName+"?")){
	        	$scope.statusList[month-1].stopping=true;
	        	$scope.updateScheduleDisplay();
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
		        	$scope.updateData();
		        	//$scope.statusList = response.data.sort(function(a, b){return a.month-b.month});
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
    }
}
