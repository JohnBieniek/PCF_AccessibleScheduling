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

function LoginController($scope, $modal, $http,Status) {

	function onSignIn(googleUser) {
		console.log("$scope.page"+$scope.page);
	    // Useful data for your client-side scripts:
	    var profile = googleUser.getBasicProfile();
	    console.log("ID: " + profile.getId()); // Don't send this directly to your server!
	    console.log('Full Name: ' + profile.getName());
	    console.log('Given Name: ' + profile.getGivenName());
	    console.log('Family Name: ' + profile.getFamilyName());
	    console.log("Image URL: " + profile.getImageUrl());
	    console.log("Email: " + profile.getEmail());
	
	    // The ID token you need to pass to your backend:
	    var id_token = googleUser.getAuthResponse().id_token;
	    console.log("ID Token: " + id_token);
	    
	    var xhr = new XMLHttpRequest();
	    xhr.open('POST', 'https://accessiblescheduling-dev.cfapps.io/auth/tokensignin');
	    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
	    xhr.onload = function() {
	        console.log('Signed in as: ' + xhr.responseText);
		    $scope.setPage('employee');
		    $scope.$apply();
	    };
	    xhr.send('idtoken=' + id_token);
	  }
	window.onSignIn = onSignIn;
}
