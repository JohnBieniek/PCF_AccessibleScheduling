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
	$scope.requestSignUp = function requestSignUp() { 
    	$http({
            url: '/auth/signup',
            method: 'GET',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            	idtoken:$scope.idToken,
            	name:$scope.profile.name
            }
        })
        .then(function(response) {
		    $scope.setPage('awaitingAccess');
		    $scope.$apply();
        });
	}
}
