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
//	    var id_token = googleUser.getAuthResponse().id_token;
//	    $scope.setIdToken(id_token);
//	    $scope.$apply();
//	    
//	    var xhr = new XMLHttpRequest();
//	    xhr.open('POST', 'https://accessiblescheduling-dev.cfapps.io/auth/tokensignin');
////	    xhr.open('POST', 'https://accessiblescheduling.cfapps.io/auth/tokensignin');
//	    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
//	    xhr.onload = function() {
//	        $scope.setProfile(JSON.parse(xhr.responseText));
//	        $scope.setUser($scope.profile.user);
//	        $scope.setManager($scope.profile.manager);
//	        $scope.setAdmin($scope.profile.admin);
//		    $scope.$apply();
//		    if($scope.profile.user){
//			    $scope.setPage('employee');
//			    $scope.$apply();
//		    }
//		    else{
//		    	$http({
//		            url: '/auth/signedup',
//		            method: 'GET',
//		            headers: {
//		                'Content-Type': 'application/x-www-form-urlencoded'
//		            },
//		            params: {
//		            	idtoken:$scope.idToken
//		            }
//		        })
//		        .then(function(response) {
//		        	if(response.data=="true"){
//		        		$scope.setPage('awaitingAccess');
//		        	}
//		        	else{
//					    $scope.setPage('signUp');		        		
//		        	}
//		        	$scope.$apply();
//		        });
//		    }
//	    };
//	    xhr.send('idtoken=' + id_token);
	  }
	window.onSignIn = onSignIn;
}
