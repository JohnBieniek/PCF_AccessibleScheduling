function RequestModalController($scope, $modalInstance, $http, selectedClient, selectedEmployee, employees,shiftRequest, date, action) {

	$scope.shiftRequestAction = action;
    $scope.selectedClient=selectedClient;
    $scope.selectedEmployee=null;
    $scope.employees=employees;
    $scope.changed=false;
  
    if(action=="add"){
    	if(undefined==shiftRequest || null==shiftRequest){
    		shiftRequest={};
    	}
        $scope.changed=true;
    	
    	shiftRequest.interval="day(s)";
    	shiftRequest.monthInterval="days";
    	shiftRequest.yearInterval="days";
    	shiftRequest.repeatsEvery=1;  
    	shiftRequest.startDate=date.getFullYear()+"-"+((date.getMonth()+1)<10?"0"+(date.getMonth()+1):(date.getMonth()+1))+"-"+date.getDate();
    	shiftRequest.endDate=date.getFullYear()+"-"+((date.getMonth()+1)<10?"0"+(date.getMonth()+1):(date.getMonth()+1))+"-"+date.getDate();
    	shiftRequest.startTime="12:00";
    	shiftRequest.endTime="20:00";
    }
    $scope.shiftRequest = shiftRequest;

    $scope.selectedYearInterval=1;
    $scope.selectedMonthInterval = 1;
    $scope.dayOfMonth=-1;
    $scope.dayOfWeek="Fakeday";
    $scope.weekOfMonth=-1;
    

    $scope.numbers = new Array(52).fill().map((x,i)=>i); 
    $scope.selectedEmployee=$scope.employees[0];
    	if(shiftRequest && shiftRequest.staffId != undefined){
    		for(var index = 0; index<$scope.employees.length;index++){
    			if($scope.employees[index].id==shiftRequest.staffId){
    				$scope.selectedEmployee=$scope.employees[index];
    			}
    		}
    	}
        myCallback(shiftRequest);
    // Will execute myCallback every 5 seconds 
	var intervalID = setInterval(function(){ myCallback(shiftRequest)}, 500);
	
	function myCallback(shiftRequest) {
		var date = new Date($scope.shiftRequest.startDate);
		$scope.dayOfMonth = date.getDate()+1;//It comes out zero indexed
		var dayOfWeek = date.getDay();
		
	  switch(date.getMonth()+1){
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
		switch(dayOfWeek){
    	  case 6:
    		  $scope.dayOfWeek="Sunday";
    		  break;
		  case 0:
			  $scope.dayOfWeek="Monday";
    		  break;
    	  case 1:
    		  $scope.dayOfWeek="Tuesday";
    		  break;
    	  case 2:
    		  $scope.dayOfWeek="Wednesday";
    		  break;
    	  case 3:
    		  $scope.dayOfWeek="Thursday";
    		  break;
    	  case 4:
    		  $scope.dayOfWeek="Friday";
    		  break;
    	  case 5:
    		  $scope.dayOfWeek="Saturday";
    		  break;
        }
		
		var week=1;
		var day = date.getDate()+1;

		for(var i = 0; i<32;i++){
			day=day-1;
			
			if(day<=0){
				break;
			}
			else{
				if(dayOfWeek==0){
					dayOfWeek = 6;
				}
				else{
					if(dayOfWeek==6){
						week=week+1;
					}
					dayOfWeek=dayOfWeek-1;
				}
			}
		}
		
		$scope.weekOfMonth= week;
	}
    $scope.valid=false;
    
    $scope.addException = function(newDate){
        $scope.changed=true;
    	if(!$scope.shiftRequest.exceptions){
    		$scope.shiftRequest.exceptions=[];
    	}
    	
    	if(!$scope.shiftRequest.exceptions.includes(newDate) && newDate!=null && newDate!=""){
    		$scope.shiftRequest.exceptions.push(newDate);
    	}
    };
    
	$scope.removeException=function(item){ 
        $scope.changed=true;
	    var index= $scope.shiftRequest.exceptions.indexOf(item)
	     $scope.shiftRequest.exceptions.splice(index,1);     
	}

	$scope.requestIsValid = function(shiftRequest){
		var valid = true;
		
		
		if(!$scope.changed){
			valid=false;
		}
		else if(null==shiftRequest){
			valid=false;
		}
		else{
			if(shiftRequest.requestEmployee && null==shiftRequest.staffName){
				valid=false;//No employee when one was requested
			}
			else if(null==shiftRequest.startDate || null==shiftRequest.endDate){
				valid=false;//No start or end date
			}
			else if(null==shiftRequest.startTime || null==shiftRequest.endTime){
				valid=false;//No start or end time
			}
			else{
				var splitStartDate = shiftRequest.startDate.split('-');
				var splitEndDate = shiftRequest.endDate.split('-');
				
				var splitStartTime = shiftRequest.startTime.split(':');
				var splitEndTime = shiftRequest.endTime.split(':');
				
				if(shiftRequest.startDate ==shiftRequest.endDate){
					if(parseInt(splitStartTime[0])>parseInt(splitEndTime[0])){
						valid=false;//Starts in hours after it ends
					}
					else if(splitStartTime[0]==splitEndTime[0]){
						if(parseInt(splitStartTime[1])>=parseInt(splitEndTime[1])){
							valid=false;//Starts when it ends or minutes after
						}
					}
				}
				else if(parseInt(splitEndTime[1])>parseInt(splitStartTime[1])){
    				if(parseInt(splitEndTime[0])>=parseInt(splitStartTime[0])){
						valid=false;
						//reason = "greater than 24 hours";
    				}
				}
				else if(parseInt(splitEndTime[0])>parseInt(splitStartTime[0])){
					valid=false;//reason="greater than 24 hours";
				}
			}
		}

		return valid;
	}
	
	$scope.setChanged = function(){
		$scope.changed=true;
	}
	$scope.setSelectedInterval2=function(selectedInterval2){
        $scope.changed=true;
		$scope.selectedInterval2=selectedInterval2
	}
	
	$scope.setSelectedInterval3=function(selectedInterval3){
        $scope.changed=true;
		$scope.selectedInterval3=selectedInterval3
	}

	
	$scope.setSelectedMonthInterval=function(selectedMonthInterval){
        $scope.changed=true;
		$scope.selectedMonthInterval=selectedMonthInterval
	}
	
	
	$scope.setMonthInterval=function(monthInterval){
        $scope.changed=true;
		$scope.monthInterval=monthInterval
	}
	
	$scope.everyWeek=function(weekInterval){
		return weekInterval=='week(s)'
	}
	
	$scope.everyMonth=function(selectedInterval){
		return selectedInterval=='month(s)'
	}
	
	$scope.everyYear=function(selectedInterval){
		return selectedInterval=='year(s)'
	}

	 $scope.setInitialDays = function setInitialDays(request){
		 if(request.days==null || request.days==undefined){
			 request.days=[false,false,false,false,false,false,false];
		 }
	 }
	 
    if(!$scope.shiftRequest.clientId){
    	$scope.shiftRequest.clientId=selectedClient.id;
    }
    if(!$scope.shiftRequest.clientName){
    	$scope.shiftRequest.clientName=selectedClient.first;
    }

    $scope.ok = function () {
    	clearInterval(intervalID);
        $modalInstance.close($scope.shiftRequest);
    };

    $scope.cancel = function () {
    	clearInterval(intervalID);
        $modalInstance.dismiss('cancel');
    };
};