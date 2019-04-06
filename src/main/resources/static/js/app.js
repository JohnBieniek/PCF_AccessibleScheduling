angular.module('AccessibleScheduling', ['mainNavigation', 'errors', 'status', 'info', 'ngRoute', 'ui.directives']).
    config(function ($locationProvider, $routeProvider) {
        // $locationProvider.html5Mode(true);
    	$routeProvider
        .when('/errors', {
        	controller: 'ErrorsController',
            templateUrl: 'templates/util/errors.html'
        })
        .when('/clients', {
        	controller: 'ErrorsController',
            templateUrl: 'templates/util/errors.html'
        })
        .otherwise({
        	 controller: 'MainNavigationController',
             templateUrl: 'templates/adminView.html'
        });
    }
);
