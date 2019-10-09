app.controller('agentController', function($scope, agentService, messageService, $websocket) {
	const ipAdress = window.location.host;
	$scope.test = "John";
	$scope.selectedSender = "";
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
	
	$scope.sendMessage = function(aid) {
		let message = {};
		message.performative = 'REQUEST',
		message.receivers = [{
			name: aid.name,
			str: aid.str,
			host: aid.host
		}];
		
		messageService.sendMessage(message).then(function(response) {
			console.log("Message sent sucessfully");
		});	
	}
	
	messageService.getPerformatives().then(function(response) {
		$scope.performatives = response.data;
	});
	
	
	
	getRunningAgents();
	

	
	function getRunningAgents() {
		agentService.getRunningAgents().then(function(response) {
			$scope.runningAgents = angular.copy(response.data);
			if(!$scope.$$phase) {
				$scope.$apply();
			}		
		});
	}
	
	var dataStream = $websocket('ws://' + ipAdress + '/AgentiWAR/runningAgents');
	
    dataStream.onMessage(function(response) {
    	$scope.runningAgents = JSON.parse(response.data);
    	if(!$scope.$$phase) {
			$scope.$apply();
		}	
    });
    
	var agentClasses = $websocket('ws://' + ipAdress + '/AgentiWAR/agentClasses');
	
	agentClasses.onMessage(function(response) {
    	$scope.agentTypes = JSON.parse(response.data);
    	if(!$scope.$$phase) {
			$scope.$apply();
		}	
    });
    
    var logStream = $websocket('ws://' + ipAdress + '/AgentiWAR/log');
    
    $scope.logs = [];
	
    logStream.onMessage(function(response) {
    	$scope.logs.push(response.data);
    	if(!$scope.$$phase) {
			$scope.$apply();
		}	
    });

});
