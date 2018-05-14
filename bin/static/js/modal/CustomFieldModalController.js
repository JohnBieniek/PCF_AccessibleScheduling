function CustomFieldModalController($scope, $modalInstance, customField, action) {
    $scope.customFieldAction = action;
    $scope.customField= {};

    if(customField){
    	$scope.customField = customField;
    }
    
    $scope.ok = function () {
        $modalInstance.close($scope.customField);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};