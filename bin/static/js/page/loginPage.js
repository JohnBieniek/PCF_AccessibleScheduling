angular.module('login', ['ngResource', 'ui.bootstrap']).
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

function LoginController($scope, $modal, $http) {
	function onSignIn(googleUser) {
	    // The ID token you need to pass to your backend:
	    var idToken = googleUser.getAuthResponse().id_token;
	    $scope.setIdToken(idToken);
	    
    	$http({
            url: '/auth/tokensignin',
            method: 'GET',
            headers: {
	            'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
	        $scope.setProfile(response.data);
	        $scope.setUser($scope.profile.user);
	        $scope.setManager($scope.profile.manager);
	        $scope.setAdmin($scope.profile.admin);
		    if($scope.profile.user){
			    $scope.setPage('employee');
			    $scope.$apply();
		    }
		    else{
		    	$http({
		            url: '/auth/signedup',
		            method: 'GET',
		            headers: {
    		            'Authorization': $scope.idToken,
		                'Content-Type': 'application/x-www-form-urlencoded'
		            },
		            params: {
		            }
		        })
		        .then(function(response) {
		        	if(response.data=="true"){
		        		$scope.setPage('awaitingAccess');
		        	}
		        	else{
					    $scope.setPage('signUp');		        		
		        	}
		        });
		    }
	    });
	}
	window.onSignIn = onSignIn;
}
