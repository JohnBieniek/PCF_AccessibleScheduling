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
	 $scope.month=1;
	 $scope.generated="Generated";
	 $scope.assigned="Unassigned";
	 $scope.tab=1;
	 $scope.monthName="January";
	 $scope.setTab = function(newTab){
      $scope.tab = newTab;
      switch(newTab){
    	  case 1:
    		  $scope.monthName="January";
    		  break;
    	  case 2:
    		  $scope.monthName="Febuary";
    		  break;
    	  case 3:
    		  $scope.monthName="March";
    		  break;
    	  case 4:
    		  $scope.monthName="April";
    		  break;
    	  case 5:
    		  $scope.monthName="May";
    		  break;
    	  case 6:
    		  $scope.monthName="June";
    		  break;
    	  case 7:
    		  $scope.monthName="July";
    		  break;
    	  case 8:
    		  $scope.monthName="August";
    		  break;
    	  case 9:
    		  $scope.monthName="September";
    		  break;
    	  case 10:
    		  $scope.monthName="October";
    		  break;
    	  case 11:
    		  $scope.monthName="November";
    		  break;
    	  case 12:
    		  $scope.monthName="December";
    		  break;
      }
    };
	 
	 $scope.isSet = function(tabNum){
      return $scope.tab === tabNum;
    };
	    
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
    
	 $scope.setMonth = function setMonth(month) {
	        $scope.month = month;
	    }
    $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
    }
}
