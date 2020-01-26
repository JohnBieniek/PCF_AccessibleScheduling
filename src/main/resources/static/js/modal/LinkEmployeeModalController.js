function LinkEmployeeModalController($scope, $modalInstance, $http, selectedEmployee,action,idToken) {
	$scope.idToken = idToken;
	$scope.valid=false;
	$scope.linkEmployeeAction = action;
    $scope.selectedEmployee=selectedEmployee;
    $scope.changed=false;
  
	$scope.formIsValid = function(){
		var valid = true;
		
		
		if(!$scope.changed){
			valid=false;
		}
		else{
		}

		return valid;
	}
	
	$scope.setChanged = function(){
		$scope.changed=true;
	};

	$scope.ok = function (){
	   $http({
            url: '/auth/linkEmployee',
            method: 'GET',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	email: $scope.selectedEmployee.email,
            	employeeId:$scope.selectedEmployee.id
            }
        })
        .then(function(response) {
            $modalInstance.close(response.data);
       });
    };

    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };
};