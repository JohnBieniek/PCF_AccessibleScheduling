angular.module('events', ['ngResource', 'ui.bootstrap']).
	factory('Events', function ($resource) {
	    return $resource('events');
	}).
	factory('Event', function ($resource) {
	    return $resource('events/:id', {id: '@id'});
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

function EventsController($scope, $modal, $http, Events, Event,Status) {
	 $scope.multiTableEditing=false;
	 $scope.customFieldDataEditing=true;
	 $scope.sortField="startDate";
	 function clone (obj) {
	        return JSON.parse(JSON.stringify(obj));
     }
	 function saveEvent(event) {
        Events.save(event,
            function () {
                Status.success("Event saved");
                $scope.listEvents();
            },
            function (result) {
                Status.error("Error saving event: " + result.status);
            }
        );
    }
	   
	 $scope.addEvent = function () {
        var addModal = $modal.open({
            templateUrl: 'templates/modal/eventForm.html',
            controller: EventModalController,
            resolve: {
                event: function () {
                    return {};
                },
                action: function() {
                    return 'add';
                }
            }
        });

        addModal.result.then(function (event) {
            saveEvent(event);
        });
    };

    $scope.updateEvent = function (event) {
        var updateModal = $modal.open({
            templateUrl: 'templates/modal/eventForm.html',
            controller: EventModalController,
            resolve: {
                event: function() {
                    return clone(event);
                },
                action: function() {
                    return 'update';
                }
            }
        });

        updateModal.result.then(function (event) {
            saveEvent(event);
        });
    };

    $scope.listEvents = function listEvents() {
        $scope.events = Events.query();
    }

    $scope.deleteEvent = function (event) {
        Event.delete({id: event.id},
            function () {
                Status.success("Event deleted");
                $scope.listEvents();
            },
            function (result) {
                Status.error("Error deleting event: " + result.status);
            }
        );
    };
}
