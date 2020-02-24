angular.module('AccessibleScheduling', ['mainNavigation', 'info', 'ngRoute', 'ui.directives']).
    config(function ($locationProvider, $routeProvider) {
        $locationProvider.html5Mode(true);
    	$routeProvider
    	.when("/signup",
			{
	       	 	controller: 'MainNavigationController',
	            templateUrl: 'templates/page/signup.html'
			}
    	)
        .otherwise({
        	 controller: 'MainNavigationController',
             templateUrl: 'templates/page/home.html'
        });
    }
);
