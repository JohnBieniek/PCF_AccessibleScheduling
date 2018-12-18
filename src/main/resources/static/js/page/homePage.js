angular.module('dashboard', ['ngResource', 'ui.bootstrap']).
	factory('Shifts', function ($resource) {
	    return $resource('shifts');
	}).
	factory('Shift', function ($resource) {
	    return $resource('shifts/:id', {id: '@id'});
	}).
	factory('Clients', function ($resource) {
	    return $resource('clients');
	}).
	factory('Client', function ($resource) {
	    return $resource('clients/:id', {id: '@id'});
	}).
	factory('Employees', function ($resource) {
	    return $resource('employees');
	}).
	factory('Employee', function ($resource) {
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

function HomeController($scope, $modal, $http, Shifts, Shift, Clients, Client, Employees, Employee, Status) {
	 $scope.multiTableEditing=false;
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    $scope.list = function list(){
    	$scope.listClients();
    	$scope.listEmployees();
    }
    $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
    }
    $scope.listEmployees = function listEmployees() {
        $scope.employees = Employees.query();
    }
}
