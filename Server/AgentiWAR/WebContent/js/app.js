var app = angular.module("agentClient", ["ngRoute"]);

app.config(['$routeProvider', function($routeProvider) {
	  $routeProvider
	  .when("/", {
	    templateUrl : "parts/agents.html"
	  })
	  .when("/somethingElse", {
	    templateUrl : "parts/agents.html"
	  });
	}]);
