app.controller('agentController', function($scope, agentService, messageService, $websocket) {
	$scope.test = "John";
	agentService.getClasses().then(function(response) {
		$scope.agentTypes = response.data;
	});

	$scope.startAgent = function(agentType) {
		if(agentType.agentName && agentType.agentName.trim().length != 0) {
			agentService.startAgent(agentType.name, agentType.agentName).then(function(response) {
				agentType.agentName = "";
			});
		} 
	}
	
	$scope.stopAgent = function(str) {
		agentService.stopAgent(str).then(function(response) {
			// success
		}, function() {
			// error
		});
	}
	
	messageService.getPerformatives().then(function(response) {
		$scope.performatives = response.data;
	});
	
	setInterval(function() {getRunningAgents()}, 1000);
	
	function getRunningAgents() {
		agentService.getRunningAgents().then(function(response) {
			$scope.runningAgents = angular.copy(response.data);
			if(!$scope.$$phase) {
				$scope.$apply();
			}		
		});
	}
	
	var dataStream = $websocket('ws://localhost:8080/AgentiWAR/runningAgents');
	
    dataStream.onMessage(function(response) {
    	$scope.runningAgents = JSON.parse(response.data);
    	if(!$scope.$$phase) {
			$scope.$apply();
		}	
    });

});
