function ShiftModalController($scope, $modalInstance, $http, shift, clients, action) {
    $scope.shiftAction = action;
    $scope.shift = shift;
    $scope.clients = clients;
    $scope.valid=false;
    
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