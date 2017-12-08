function RecurringShiftNeedModalController($scope, $modalInstance, $http, selectedClient, selectedEmployee, employees, recurringShiftNeed, action) {
    $scope.recurringShiftNeedAction = action;
    $scope.selectedClient=selectedClient;
    $scope.employees=employees;
    $scope.recurringShiftNeed = recurringShiftNeed;
	$scope.selectedEmployee = selectedEmployee;
	$scope.valid=false;
    
	// Will execute myCallback every 5 seconds 
	var intervalID = setInterval(function(){ myCallback(recurringShiftNeed)}, 1000);

	function myCallback(recurringShiftNeed) {
		$http({
            url: '/recurringShiftNeeds/validity',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	recurringShiftNeed: recurringShiftNeed
            }
        })
        .then(function(response) {
    		$scope.valid = response.data;
        });
	}
    $scope.isValid = function(recurringShiftNeed){
    	$http({
            url: '/recurringShiftNeeds/validity',
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	recurringShiftNeed: recurringShiftNeed
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
    	clearInterval(intervalID);
        $modalInstance.close($scope.recurringShiftNeed);
    };

    $scope.cancel = function () {
    	clearInterval(intervalID);
        $modalInstance.dismiss('cancel');
    };
};