herbonautesApp.directive('sortLink', function () {

    function build($scope, attrs) {
        $scope.sortBy = function(type) {
            $scope.onClick(type);
        }
    }

    return {
        restrict: 'E',
        scope: {
            page: '=',
            field: '@',
            onClick: '=',
            title: '@'
        },
        transclude: true,
        template: '<a href="" ng-click="sortBy(field)">' +
            '{{title}}' +
            '<span ng-show="field == page.sortBy">' +
            '<span ng-show="\'asc\' == page.order" class="glyphicon glyphicon-chevron-down"></span>' +
            '<span ng-show="\'desc\' == page.order" class="glyphicon glyphicon-chevron-up"></span>' +
            '</span>' +
            '</a>',
        link: function link($scope, $elem, attrs) {
            $scope.$watchCollection('[page]', function () {
                build($scope, attrs);
            });
        }
    };
});

herbonautesApp.directive('pages', function () {

    function build($scope, attrs) {
        $scope.visiblePages = [1, 2, 3, 4];
        $scope.goToPage = function(pageIndex) {
            $scope.pageClick(pageIndex);
        }
    }

    return {
        restrict: 'E',
        scope: {
            page: '=',
            pageClick: '='
        },
        template: '<a ng-show="page.hasPrev" ng-click="goToPage(page.pageIndex-1)">Page précédente</a>' +
            '<span ng-hide="page.hasPrev">Page précédente</span>' +
            ' - Page {{page.pageIndex}}/{{page.pageCount}} - ' +
            '<a ng-show="page.hasNext" ng-click="goToPage(page.pageIndex+1)">Page suivante</a>' +
            '<span ng-hide="page.hasNext">Page suivante</span>',
        link: function link($scope, $elem, attrs) {
            $scope.$watchCollection('[page]', function () {
                build($scope, attrs);
            });
        }
    };
});

herbonautesApp.factory('UserAdminService', ['$resource', function($resource) {
    return $resource(herbonautes.ctxPath + '/admin/users/:id/',
        { id: '@id' },
        {
            find: { method: 'GET', url: herbonautes.ctxPath + '/admin/search/users', isArray: false, params: {
                page: '@page',
                filter: '@filter',
                sortBy: '@sortBy',
                order: '@order'
            }
            }
        }
    );
}]);

herbonautesApp.controller('AdminUsersCtrl', ['$scope', 'UserAdminService', function($scope, UserAdminService) {

    $scope.users = {};

    $scope.filter;
    $scope.filtering = false;

    $scope.changeFilter = function() {
        if (!$scope.filtering) {
            $scope.filtering = true;
            $scope.goToPage(1, function() {
                $scope.filtering = false;
            });
        }
    };

    $scope.updateUser = function(user) {
        UserAdminService.save(user);
    }

    $scope.goToPage = function(page, callback) {
        UserAdminService.find({ page: page,
            filter: !!$scope.filter ? $scope.filter : null,
            sortBy: $scope.users.sortBy,
            order: $scope.users.order
        }, function(page) {
            $scope.users = page;
            if (callback) callback();
        });
        return false;
    }

    $scope.goToPage(1);

    $scope.sortBy = function(field) {
        if ($scope.users.sortBy == field) {
            $scope.users.order = ($scope.users.order == "asc" ? "desc" : "asc");
        } else {
            $scope.users.sortBy = field;
            $scope.users.order = "asc";
        }
        $scope.goToPage($scope.users.pageIndex);
    }

}]);