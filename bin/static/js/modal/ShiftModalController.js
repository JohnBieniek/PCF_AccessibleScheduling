function ShiftModalController($scope, $modalInstance, $http, Status, shift, client,clients,employees,date, action) {
    $scope.shiftAction = action;
    $scope.shift = shift;
    $scope.client=client;
    $scope.employees=employees;
    $scope.showEmployee=true;
    $scope.assignable=false;
    if(shift.clientId==null||shift.clientName==undefined){
    	shift.clientId=client.id;
    }
    if(shift.clientName==null||shift.clientName==undefined){
    	shift.clientName=client.first;
    }
    $scope.clients = clients;
    $scope.valid=false;
    if(action=="add"){
    	$scope.shift.creationReason="Manual";
    	$scope.shift.startDate=date.getFullYear()+"-"+((date.getMonth()+1)<10?"0"+(date.getMonth()+1):(date.getMonth()+1))+"-"+date.getDate();
    	$scope.shift.endDate=date.getFullYear()+"-"+((date.getMonth()+1)<10?"0"+(date.getMonth()+1):(date.getMonth()+1))+"-"+date.getDate();
    	$scope.shift.startTime="12:00";
    	$scope.shift.endTime="20:00";
    	$scope.valid=true;
    }
    
	if(shift && shift.staffId != undefined){
		for(var index = 0; index<$scope.employees.length;index++){
			if($scope.employees[index].id==shift.staffId){
				$scope.selectedEmployee=$scope.employees[index];
			}
		}
	}
    
//    if($scope.shift.staffId==null || $scope.shift.staffId==undefined){
//    	$scope.selectedEmployee=$scope.employees[0];
//    }
   
	$scope.clearSelectedEmployee = function(){
		console.log("clearign selected");
		$scope.showEmployee=false;
		$scope.showEmployee=true;
		$scope.selectedEmployee="";
		$scope.employee="";
		$scope.shift.staffId="";
		$scope.shift.staffName="";
		document.getElementById("mySelect").value=null;
		$scope.isValid($scope.shift);
	}
    $scope.isValid = function(shift){
    	$scope.assignable=false;
    	$http({
            url: '/shifts/validity',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	shift: shift
            }
        })
        .then(function(response) {
    		$scope.valid = response.data;
    		
    		if($scope.valid){
    			$scope.getAssignable(shift);
    		}
        });
    }
    
    $scope.getAssignable = function (shift) {
    	$http({
            url: '/schedule/staffShift',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	param: shift
            }
        })
        .then(function(response) {
    		if(response.data==0){
        		$scope.assignable=false;
    		}
    		else{
        		$scope.assignable=true;
    		}

        });
    };
    
    $scope.isAssignDisabled = function(){
	   	if($scope.assignable){
	   		return false;
	   	}
	   	else{
	   		return true;
	   	}
     };
    $scope.assign = function (shift) {
    	console.log("assigning shift");
    	$http({
            url: '/schedule/staffShift',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	param: shift
            }
        })
        .then(function(response) {
    		$scope.selectedEmployee = response.data;
    		
    		if($scope.selectedEmployee==0){
        		$scope.assignable=false;
    		}
    		else{
        		$scope.assignable=true;
        		$http({
                    url: '/shifts/validity',
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    params: {
                    	shift: shift
                    }
                })
                .then(function(response) {
            		$scope.valid = response.data;
                });
    		}
        });
    };
    
    $scope.ok = function () {
        console.log("oking shift:"+$scope.shift);
        console.log("oking employee:"+$scope.employee);
        console.log("oking selectedEmployee:"+$scope.selectedEmployee);           
        $modalInstance.close($scope.shift);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};