function ShiftModalController($scope, $modalInstance, $http, shift, client,clients,date, action) {
    $scope.shiftAction = action;
    $scope.shift = shift;
    $scope.client=client;
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
   
    $scope.isValid = function(shift){
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
    
    $scope.ok = function () {
        $modalInstance.close($scope.shift);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};