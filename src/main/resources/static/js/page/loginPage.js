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
	    $scope.completeSignIn(idToken);
	}
	window.onSignIn = onSignIn;
}
