app.controller('agentController', function($scope, agentService, messageService) {
	$scope.test = "John";
	agentService.getClasses().then(function(response) {
		$scope.agentTypes = response.data;
	});
	
	agentService.getRunningAgents().then(function(response) {
		$scope.runningAgents = response.data;
	});
	
	$scope.startAgent = function() {
		agentService.startAgent($scope.agentType, $scope.agentName).then(function(response) {
		});
	}
	
	$scope.stopAgent = function() {
		agentService.stopAgent($scope.aidToDelete).then(function(response) {
			// success
		}, function() {
			// error
		});
	}
	
	messageService.getPerformatives().then(function(response) {
		$scope.performatives = response.data;
	});

});
