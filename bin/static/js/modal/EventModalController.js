function EventModalController($scope, $modalInstance, event, action) {
    $scope.eventAction = action;
    $scope.event = event;
    
    if(action=="add"){
    	$scope.event.requestedStaff=0;
    }
    
    $scope.ok = function () {
        $modalInstance.close($scope.event);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};