function RequestModalController($scope, $modalInstance, $http, selectedClient, selectedEmployee, employees,shiftRequest, action) {
    $scope.shiftRequestAction = action;
    $scope.selectedClient=selectedClient;
    $scope.selectedEmployee=selectedEmployee;
    $scope.employees=employees;
    $scope.shiftRequest = shiftRequest;

    // Will execute myCallback every 5 seconds 
	var intervalID = setInterval(function(){ myCallback(shiftRequest)}, 500);

	function myCallback(shiftRequest) {
		$http({
            url: '/requests/validity',
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
    $scope.valid=false;
    
    $scope.isValid = function(shiftRequest){
    	$http({
            url: '/requests/validity',
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
    	clearInterval(intervalID);
        $modalInstance.close($scope.shiftRequest);
    };

    $scope.cancel = function () {
    	clearInterval(intervalID);
        $modalInstance.dismiss('cancel');
    };
};