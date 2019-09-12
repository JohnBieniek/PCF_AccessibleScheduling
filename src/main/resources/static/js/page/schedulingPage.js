angular.module('vacation', ['ngResource', 'ui.bootstrap']).
	factory("EditorStatus", function () {
        var editorEnabled = {};

        var enable = function (id, fieldName) {
            editorEnabled = { 'id': id, 'fieldName': fieldName };
        };

        var disable = function () {
            editorEnabled = {};
        };

        var isEnabled = function(id, fieldName) {
            return (editorEnabled['id'] == id && editorEnabled['fieldName'] == fieldName);
        };

        return {
            isEnabled: isEnabled,
            enable: enable,
            disable: disable
        }
    });

function SchedulingController($scope, $modal, $http,Status) {
	 $scope.multiTableEditing=false;
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
		$scope.listClients = function listClients() {
			$http({
		        url: '/clients',
		        method: 'GET',
		        headers: {
		            'Authorization': $scope.idToken
		        },
		        params: {
		        }
		    })
		    .then(function(response) {
		    	$scope.clients=response.data;
		    	
		        if($scope.client==null){
		        	$scope.client = $scope.clients[0];
		        }
		    });
	    }
}
