'use strict';
var app = angular.module("agentClient", ['ngRoute', 'angular-websocket']);

app.run(function ($websocket) {
	
	var dataStream = $websocket('ws://localhost:8080/AgentiWAR/runningAgents');
	
    dataStream.onMessage(function(message) {
    	console.log(message);
      });
//    var ws = $websocket.$new('ws://localhost:8080/AgentiWAR/agentClasses'); // instance of ngWebsocket, handled by $websocket service
//
//    ws.$on('$open', function () {
//        console.log('Oh my gosh, websocket is really open! Fukken awesome!');
//
//        ws.$emit('ping', 'hi listening websocket server'); // send a message to the websocket server
//
//        var data = {
//            level: 1,
//            text: 'ngWebsocket rocks!',
//            array: ['one', 'two', 'three'],
//            nested: {
//                level: 2,
//                deeper: [{
//                    hell: 'yeah'
//                }, {
//                    so: 'good'
//                }]
//            }
//        };
//
//        ws.$emit('pong', data);
//    });
//
//    ws.$on('pong', function (data) {
//        console.log('The websocket server has sent the following data:');
//        console.log(data);
//
//        ws.$close();
//    });
//
//    ws.$on('$close', function () {
//        console.log('Noooooooooou, I want to have more fun with ngWebsocket, damn it!');
//    });
});

app.config(['$routeProvider', function($routeProvider) {
	  $routeProvider
	  .when("/", {
	    templateUrl : "parts/agents.html"
	  })
	  .when("/somethingElse", {
	    templateUrl : "parts/agents.html"
	  });
	}]);
