angular.module('vacation', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('employees');
	}).
	factory('Client', function ($resource) {
	    return $resource('employees/:id', {id: '@id'});
	}).
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

function SchedulingController($scope, $modal, $http, Clients, Client,Status) {
	 $scope.multiTableEditing=false;
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
    $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
    }
}
