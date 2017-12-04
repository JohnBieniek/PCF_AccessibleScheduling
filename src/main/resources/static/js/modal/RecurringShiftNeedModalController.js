function RecurringShiftNeedModalController($scope, $modalInstance, $http, selectedClient, selectedEmployee, employees, recurringShiftNeed, action) {
    $scope.recurringShiftNeedAction = action;
    $scope.selectedClient=selectedClient;
    $scope.employees=employees;
    $scope.recurringShiftNeed = recurringShiftNeed;
	$scope.selectedEmployee = selectedEmployee;
	
	$scope.valid=false;
    
    $scope.isValid = function(shiftRequest){
    	$http({
            url: '/reccurringShiftNeeds/validity',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	reccurringShiftNeed: reccurringShiftNeed
            }
        })
        .then(function(response) {
    		$scope.valid = response.data;
        });
    }
    
    if(!$scope.recurringShiftNeed.clientId){
    	$scope.recurringShiftNeed.clientId=selectedClient.id;
    }
    if(!$scope.recurringShiftNeed.clientName){
    	$scope.recurringShiftNeed.clientName=selectedClient.first;
    }
    
    $scope.ok = function () {
        $modalInstance.close($scope.recurringShiftNeed);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};