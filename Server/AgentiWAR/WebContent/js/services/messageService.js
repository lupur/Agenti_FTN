app.factory('messageService', function($http) {
    return {
    	getPerformatives: function() {
        	return $http({
        		method: 'GET',
        		url: 'api/messages'
        	});
        },
        sendMessage: function(message) {
        	return $http({
        		method: 'POST',
        		url: 'api/messages',
        		data: message
        	});
        }   
    };
});