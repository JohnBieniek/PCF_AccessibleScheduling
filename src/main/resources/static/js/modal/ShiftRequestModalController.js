function ShiftRequestModalController($scope, $modalInstance, $http, selectedClient, selectedEmployee, employees,shiftRequest, action) {
    $scope.shiftRequestAction = action;
    $scope.selectedClient=selectedClient;
    $scope.selectedEmployee=selectedEmployee;
    $scope.employees=employees;
    $scope.shiftRequest = shiftRequest;

    $scope.valid=false;
    
    $scope.isValid = function(shiftRequest){
    	$http({
            url: '/shiftRequests/validity',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	shiftRequest: shiftRequest
            }
        })
        .then(function(response) {
    		$scope.valid = response.data;
        });
    }
    
    if(!$scope.shiftRequest.clientId){
    	$scope.shiftRequest.clientId=selectedClient.id;
    }
    if(!$scope.shiftRequest.clientName){
    	$scope.shiftRequest.clientName=selectedClient.first;
    }

    $scope.ok = function () {
        $modalInstance.close($scope.shiftRequest);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};