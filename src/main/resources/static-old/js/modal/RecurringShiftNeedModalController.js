function RecurringShiftNeedModalController($scope, $modalInstance, selectedClient, selectedEmployee, employees, recurringShiftNeed, action) {
    $scope.recurringShiftNeedAction = action;
    $scope.selectedClient=selectedClient;
    $scope.employees=employees;
    $scope.recurringShiftNeed = recurringShiftNeed;
	$scope.selectedEmployee = selectedEmployee;
	
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