(function() {
    'use strict';

    angular
        .module('backendApp')
        .controller('EventDetailController', EventDetailController);

    EventDetailController.$inject = ['$scope', '$rootScope', '$stateParams', 'previousState', 'entity', 'Event', 'User', 'Location', 'EventCategory'];

    function EventDetailController($scope, $rootScope, $stateParams, previousState, entity, Event, User, Location, EventCategory) {
        var vm = this;

        vm.event = entity;
        vm.previousState = previousState.name;

        var unsubscribe = $rootScope.$on('backendApp:eventUpdate', function(event, result) {
            vm.event = result;
        });
        $scope.$on('$destroy', unsubscribe);
    }
})();
