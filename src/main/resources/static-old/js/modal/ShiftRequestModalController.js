function ShiftRequestModalController($scope, $modalInstance,   selectedClient, selectedEmployee, employees,shiftRequest, action) {
    $scope.shiftRequestAction = action;
    $scope.selectedClient=selectedClient;
    $scope.selectedEmployee=selectedEmployee;
    $scope.employees=employees;
    $scope.shiftRequest = shiftRequest;

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