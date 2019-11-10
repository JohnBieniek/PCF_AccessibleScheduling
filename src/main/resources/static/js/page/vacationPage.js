function VacationController($scope, $modal, $http) {
	 $scope.addAbsence = function(selectedEmployee,newDate){
		 $scope.updateLastInteractionTime();
         $http({
 	           url: '/employees/addAbsence',
 	           method: 'POST',
 	           headers: {
 	               'Authorization': $scope.idToken,
 	               'Content-Type': 'application/x-www-form-urlencoded'
 	           },
 	           params: {
 	        	   id: $scope.employee.id,
 	        	   date: newDate
 	           }
 	     })
 	     .then(function (response) {//TODO handle error state
 	    	   $scope.setEmployee(response.data);

 	    	   $scope.notify("Employee saved");
 	     });
    };
	    
    $scope.removeAbsence=function(selectedEmployee,item){ 
    	$scope.updateLastInteractionTime();
    	 if(confirm("Are you sure you want to delete this request?")){
    		 $http({
    	           url: '/employees/removeAbsence',
    	           method: 'POST',
    	           headers: {
    	               'Authorization': $scope.idToken,
    	               'Content-Type': 'application/x-www-form-urlencoded'
    	           },
    	           params: {
    	        	   id: selectedEmployee.id,
    	        	   date: item
    	           }
    	       })
    	       .then(function (response) {//TODO handle error state
    	    	   $scope.setEmployee(response.data);

    	    	   $scope.notify("Employee saved");
    	       });
	    }
     }
}
