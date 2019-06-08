function ShiftModalController($scope, $modalInstance, $http, shift, client,clients, action) {
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