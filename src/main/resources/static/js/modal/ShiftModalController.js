function ShiftModalController($scope, $modalInstance, shift, clients, action) {
    $scope.shiftAction = action;
    $scope.shift = shift;
    $scope.clients = clients;
    
    $scope.valid=false;
    $scope.getDatesForMonth = function(year,month){
    	$http({
            url: '/shifts/getValidity',
            method: 'GET',
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