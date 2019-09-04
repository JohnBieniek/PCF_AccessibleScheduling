angular.module('clients', ['ngResource', 'ui.bootstrap']).
	factory('Clients', function ($resource) {
	    return $resource('clients');
	}).
	factory('Client', function ($resource) {
	    return $resource('clients/:id', {id: '@id'});
	}).
	factory('CustomFields', function ($resource) {
        return $resource('customFields');
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

function ClientsController($scope, $modal, $http, Clients, Client, CustomFields, Status) {
	 $scope.sortField = "first";
	 $scope.multiTableEditing=false;
	 $scope.customFieldDataEditing=true;
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 
	 function saveClient(client) {
	        Clients.save(client,
	            function (response) {
	        		client.id=response.id;
	                var size = client.customFields.length;
	                for(var i = 0; i < size ;i++){
	                    $http({
	                        url: '/compatibility/setClientCustomFieldData',
	                        method: 'POST',
	                        headers: {
	                            'Content-Type': 'application/x-www-form-urlencoded'
	                        },
	                        params: {
	                            client: client,
	                            customField: client.customFields[i],
	                            value:client.customValue[i],
	                        }
	                    });
	                }
	                Status.success("Client saved");
	                $scope.listClients();
	            },
	            function (result) {
	                Status.error("Error saving client: " + result.status);
	            }
	        );
	    }
	   
     $scope.viewClientShifts = function(client){
   	 	$scope.setSelectedClient(client);
   	 	$scope.setStaffFilter(false);
   	 	$scope.setClientFilter(true);
   	 	$scope.setPage("shiftList");
     }
    
     $scope.scheduleClient = function(client){
    	 $scope.setSelectedClient(client);
	 	$scope.setPage("scheduling");
     }
     
     $scope.setClientCustomFieldData = function (client,customField){
     	$http({
             url: '/compatibility/setClientCustomFieldData',
             method: 'POST',
             headers: {
                 'Content-Type': 'application/x-www-form-urlencoded'
             },
             params: {
                 client: client,
                 customField: customField,
                 index:0,
             }
         });
     }
     
       
     $scope.getClientCustomFieldData = function (client,customField,index){
     	$http({
             url: '/compatibility/clientCustomFieldData',
             method: 'POST',
             headers: {
                 'Content-Type': 'application/x-www-form-urlencoded'
             },
             params: {
                 client: client,
                 customField: customField,
                 index:index,
             }
         })
         .then(function(response) {
         	$.each($scope.clients, function() {
     		    if (this.id == response.data.id) {
     		    	if(this.customValue===undefined){
     		    		this.customValue={};
     		    	}
     		    	this.customValue[response.data.numericResponse] = {};
     		        this.customValue[response.data.numericResponse] = response.data.booleanResponse;
     		    }
     		});
         });
     }
     
     $scope.addClient = function (customFields) {
         var addModal = $modal.open({
             templateUrl: 'templates/modal/clientForm.html',
             controller: ClientModalController,
             resolve: {
                 client: function () {
                     return {};
                 },
                 customFields: function(){
                 	return clone(customFields);
                 },
                 action: function() {
                     return 'add';
                 }
             }
         });

         addModal.result.then(function (client) {
             saveClient(client);
         });
     };
     $scope.updateClient = function (client, customFields) {
         var updateModal = $modal.open({
             templateUrl: 'templates/modal/clientForm.html',
             controller: ClientModalController,
             resolve: {
                 client: function() {
                     return clone(client);
                 },
                 customFields: function(){
                 	return clone(customFields);
                 },
                 action: function() {
                     return 'update';
                 }
             }
         });

         updateModal.result.then(function (client) {
             saveClient(client);
         });
     };

    $scope.list = function list(){
    	$scope.listClients();
    	$scope.listCustomFields();
    }
     $scope.listClients = function listClients() {
        $scope.clients = Clients.query();
    }
     
     $scope.listCustomFields = function listCustomFields() {
         $scope.customFields = CustomFields.query();
     }
    
    $scope.deleteClient = function (client) {
        Client.delete({id: client.id},
            function () {
                Status.success("Client deleted");
                $scope.listClients();
            },
            function (result) {
                Status.error("Error deleting client: " + result.status);
            }
        );
    };
}
