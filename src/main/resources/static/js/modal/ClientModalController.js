function ClientModalController($scope, $modalInstance, $http, client, customFields, action) {
    $scope.clientAction = action;
    $scope.client = client;
    $scope.customFields = customFields;//TODO remove this
    $scope.customFieldDataEditing=true;//TODO remove this
    $scope.client.customFields=customFields;
    
    if(action=="add"){
        var values = [];
        
        if(customFields){
        	var size = customFields.length;
        	for(var i=0;i<size;i++){
            	values.push(false);
            }
        	client.customValue=values;
        }
    }
    
    $scope.ok = function () {
        $modalInstance.close($scope.client, $scope.employee);
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};