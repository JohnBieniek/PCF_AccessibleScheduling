angular.module('AccessibleScheduling', ['mainNavigation', 'info', 'ngRoute', 'ui.directives']).
    config(function ($locationProvider, $routeProvider) {
        // $locationProvider.html5Mode(true);
    	$routeProvider
        .otherwise({
        	 controller: 'MainNavigationController',
             templateUrl: 'templates/page/badUser.html'
        });
    }
);
