angular.module('signUp', ['ngResource', 'ui.bootstrap']).
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

function SignUpController($scope, $modal, $http) {
	function onSignIn(googleUser) {
	    // The ID token you need to pass to your backend:
	    var idToken = googleUser.getAuthResponse().id_token;
	    $scope.setIdToken(idToken);
		$scope.updateLastInteractionTime();
	}
	window.onSignIn = onSignIn;
	
	$scope.requestSignUp = function requestSignUp() { 
		console.log("$scope.profile:",$scope.profile);
	    var url_string = window.location.href; //window.location.href
	    var url = new URL(url_string);
	    var invitation = url.searchParams.get("invitation");
    	$http({
            url: '/auth/signup',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	idtoken:$scope.idToken,
            	invitation:invitation
            }
        })
        .then(function(response) {
		    $scope.setPage('awaitingAccess');
		    $scope.$apply();
        });
	}
}
