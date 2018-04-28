angular.module('settings', ['ngResource', 'ui.bootstrap']).
	factory('CustomFields', function ($resource) {
	    return $resource('customFields');
	}).
	factory('CustomFields', function ($resource) {
	    return $resource('customFields/:id', {id: '@id'});
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

function SettingsController($scope, $modal, $http, CustomFields, CustomField,Status) {
	 $scope.data = {
			    model: null
			   };//TODO delete this
	 
	 $scope.multiTableEditing=false;
	 
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 
	 function saveCustomField(customField) {
        CustomFields.save(customField,
            function () {
                Status.success("Custom Field saved");
                $scope.listCustomFields();
            },
            function (result) {
                Status.error("Error saving custom field: " + result.status);
            }
        );
     }
     
     $scope.addCustomField = function () {
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
             saveCustomField(customField);
         });
     };
    
     $scope.listCustomFields=function listCustomFields() {
         $scope.customFields = CustomFields.query();
     }
    
    $scope.deleteCustomField = function (customField) {
        CustomField.delete({id: customField.id},
            function () {
                Status.success("CustomField deleted");
                $scope.listCustomFields();
            },
            function (result) {
                Status.error("Error deleting Custom Field: " + result.status);
            }
        );
    };
}
