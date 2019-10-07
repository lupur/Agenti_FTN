'use strict';
var app = angular.module("agentClient", ['ngRoute', 'angular-websocket']);


app.config(['$routeProvider', function($routeProvider) {
	  $routeProvider
	  .when("/", {
	    templateUrl : "parts/agents.html"
	  })
	  .when("/somethingElse", {
	    templateUrl : "parts/agents.html"
	  });
	}]);
