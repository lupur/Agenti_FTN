app.factory('agentService', function($http) {
    return {
        someFunction: function() {
        	return tempest.invoke('GET', 'www.google.com');
        },
        getClasses: function() {
            return $http({
                method: 'GET',
                url: 'api/agents/classes'
            });
        },
        getRunningAgents: function() {
        	return $http({
        		method: 'GET',
        		url: 'api/agents/running'
        	});
        },
        startAgent: function(type, name) {
        	return $http({
        		method: 'PUT',
        		url: 'api/agents/running/' + type + '/' + name
        	});
        },
        stopAgent: function(aid) {
        	return $http({
        		method: 'DELETE',
        		url: 'api/agents/running/' + aid
        	});
        }
    };
});