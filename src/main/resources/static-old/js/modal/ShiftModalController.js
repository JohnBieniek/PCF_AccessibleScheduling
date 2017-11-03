function ShiftModalController($scope, $modalInstance, shift, clients, action) {
    $scope.shiftAction = action;
    $scope.shift = shift;
    $scope.clients = clients;

    $scope.ok = function () {
        $modalInstance.close($scope.shift);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};