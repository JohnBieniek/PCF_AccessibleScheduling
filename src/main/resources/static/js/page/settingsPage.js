angular.module('settings', ['ngResource', 'ui.bootstrap']).
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

function SettingsController($scope, $modal, $http) {
	 $scope.multiTableEditing=false;
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 
	 function saveCustomField(customField) {
		 $http({
	           url: '/customFields',
	           method: 'POST',
	           headers: {
	               'Authorization': $scope.idToken,
	                'Content-Type': 'application/x-www-form-urlencoded'
	           },
	           params: {
	        	   param: customField
	           }
	       })
	       .then(function (response) {//TODO handle error state
               $scope.notify("Custom Field saved");
               
               $scope.listCustomFields();
	       });
     }
     
     $scope.addCustomField = function () {
    	 $scope.updateLastInteractionTime();
         var addModal = $modal.open({
             templateUrl: 'templates/modal/customFieldForm.html',
             controller: CustomFieldModalController,
             resolve: {
                 customField: function () {
                     return {};
                 },
                 action: function() {
                     return 'add';
                 }
             }
         });

         addModal.result.then(function (customField) {
             saveCustomField(customField);
         });
     };
   
     $scope.updateCustomField = function (customField) {
         var updateModal = $modal.open({
             templateUrl: 'templates/modal/customFieldForm.html',
             controller: CustomFieldModalController,
             resolve: {
                 customField: function() {
                     return clone(customField);
                 },
                 action: function() {
                     return 'update';
                 }
             }
         });

         updateModal.result.then(function (customField) {
        	 $scope.updateLastInteractionTime();
             saveCustomField(customField);
         });
     };

    
    $scope.deleteCustomField = function (customField) {
    	$scope.updateLastInteractionTime();
    	$http({
            url: '/customFields/'+customField.id,
            method: 'DELETE',
            headers: {
                'Authorization': $scope.idToken,
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            params: {
            }
        })
        .then(function(response) {
            $scope.notify("CustomField deleted");
            
            $scope.listCustomFields();
        });
    };
}
