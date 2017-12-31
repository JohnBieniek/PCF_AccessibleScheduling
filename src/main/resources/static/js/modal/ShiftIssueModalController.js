function ShiftIssueModalController($scope, $modalInstance, $http, shiftIssue, employees, clients) {
    $scope.shiftIssue = shiftIssue;
    $scope.employees=employees;

    $scope.ok = function () {
    	//This will be a move to the shift page
        $modalInstance.close($scope.shiftRequest);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};