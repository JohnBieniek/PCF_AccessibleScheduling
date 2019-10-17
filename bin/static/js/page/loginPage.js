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
	  var googleUser = {};
	  $scope.startApp = function() {
	    gapi.load('auth2', function(){
	      // Retrieve the singleton for the GoogleAuth library and set up the client.
	      auth2 = gapi.auth2.init({
	        client_id: '728962972693-5uudecgh1ir6if7imo2hb6cv9qa9nrk3.apps.googleusercontent.com',
	        cookiepolicy: 'single_host_origin',
	        samesite:'None; Secure'
	        // Request scopes in addition to 'profile' and 'email'
	        //scope: 'additional_scope'
	      });
	      attachSignin(document.getElementById('customBtn'));
	    });
	  };
	
	  function attachSignin(element) {
		    console.log(element.id);
		    auth2.attachClickHandler(element, {},
		        function(googleUser) {
			    var id_token = googleUser.getAuthResponse().id_token;
			    $scope.setIdToken(id_token);
			    $scope.$apply();
			    
			    var xhr = new XMLHttpRequest();
			    xhr.open('POST', 'https://accessiblescheduling-dev.cfapps.io/auth/tokensignin');
//			    xhr.open('POST', 'https://accessiblescheduling.cfapps.io/auth/tokensignin');
			    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
			    xhr.onload = function() {
			        $scope.setProfile(JSON.parse(xhr.responseText));
			        $scope.setUser($scope.profile.user);
			        $scope.setManager($scope.profile.manager);
			        $scope.setAdmin($scope.profile.admin);
				    $scope.$apply();
				    if($scope.profile.user){
					    $scope.setPage('employee');
					    $scope.$apply();
				    }
				    else{
				    	$http({
				            url: '/auth/signedup',
				            method: 'GET',
				            headers: {
				                'Content-Type': 'application/x-www-form-urlencoded'
				            },
				            params: {
				            	idtoken:$scope.idToken
				            }
				        })
				        .then(function(response) {
				        	if(response.data=="true"){
				        		$scope.setPage('awaitingAccess');
				        	}
				        	else{
							    $scope.setPage('signUp');		        		
				        	}
				        	$scope.$apply();
				        });
				    }
			    };
			    xhr.send('idtoken=' + id_token);
		        }, function(error) {
		          alert(JSON.stringify(error, undefined, 2));
		        });
		  }
	  
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
