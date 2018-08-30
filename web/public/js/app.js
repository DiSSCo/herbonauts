var herbonautesApp = angular.module('herbonautesApp',

    ['ui.bootstrap', 'ngRoute',
     'ngResource', 'ngTagsInput', 'ngCookies',
     'elasticsearch', 'angularMoment',
     'ngSanitize', 'monospaced.elastic',
     'truncate', 'lr.upload']);

herbonautesApp.config(['$httpProvider', function($httpProvider) {
    $httpProvider.defaults.headers.common["X-Requested-With"] = 'XMLHttpRequest';
}]);



herbonautesApp.directive('hTimeAgo', ['$compile', function ($compile) {

        function pluralize(n) {
            if (n > 1) {
                return "s";
            } else {
                return "";
            }
        }

        function trunc(n) {
            return Math.floor(n);
        }

        function deltaToAgo(delta) {
            if (delta < 5) {
                return i18n("since.now");
            }
            if (delta < 60) {
                var s = trunc(delta);
                return i18n('message.since', i18n("since.seconds", s, pluralize(delta)));
            }
            if (delta < 60 * 60) {
                var minutes = trunc(delta / 60);
                return i18n('message.since', i18n("since.minutes", minutes, pluralize(minutes)));
            }
            if (delta < 24 * 60 * 60) {
                var hours = trunc(delta / (60 * 60));
                return i18n('message.since', i18n("since.hours", hours, pluralize(hours)));
            }
            if (delta < 30 * 24 * 60 * 60) {
                var days = trunc(delta / (24 * 60 * 60));
                return i18n('message.since', i18n("since.days", days, pluralize(days)));
            }

            if (delta < 365 * 24 * 60 * 60) {
                var months = trunc(delta / (30 * 24 * 60 * 60));
                return i18n('message.since', i18n("since.months", months, pluralize(months)));
            }
            var years = trunc(delta / (365 * 24 * 60 * 60));
            return i18n('message.since', i18n("since.years", years, pluralize(years)));
        }

        var diff;

        var linker = function(scope, element, attrs) {

            if (!diff) {
                diff = scope.serverTime - new Date().getTime();
            }

            var delta = new Date().getTime() - diff - new Date(scope.hTimeAgo).getTime();


            scope.agoText = deltaToAgo(delta / 1000);


        }

        return {
            restrict: "EA",
            link: linker,
            scope: {
                hTimeAgo:'=',
                serverTime: '='
            },
            template: '{{ agoText }}'
        };
    }]);

function customTimeago(ts) {
    var now = new Date().getTime();
    if (now < ts) {
        return "";
    }
    return "il y a " + (now - ts) + " ms";
    /*
    long delta = (now.getTime() - date.getTime()) / 1000;
    if (delta < 60) {
        return Messages.get("since.seconds", delta, pluralize(delta));
    }
    if (delta < 60 * 60) {
        long minutes = delta / 60;
        return Messages.get("since.minutes", minutes, pluralize(minutes));
    }
    if (delta < 24 * 60 * 60) {
        long hours = delta / (60 * 60);
        return Messages.get("since.hours", hours, pluralize(hours));
    }
    if (delta < 30 * 24 * 60 * 60) {
        long days = delta / (24 * 60 * 60);
        return Messages.get("since.days", days, pluralize(days));
    }
    if (stopAtMonth) {
        return asdate(date.getTime(), Messages.get("since.format"));
    }
    if (delta < 365 * 24 * 60 * 60) {
        long months = delta / (30 * 24 * 60 * 60);
        return Messages.get("since.months", months, pluralize(months));
    }
    long years = delta / (365 * 24 * 60 * 60);
    return Messages.get("since.years", years, pluralize(years));
    */
}


herbonautesApp    .directive("popoverHtmlUnsafePopup", function () {
    return {
        restrict: "EA",
        replace: true,
        scope: { title: "@", content: "@", placement: "@", animation: "&", isOpen: "&" },
        templateUrl: "template/popover/popover-html-unsafe-popup.html"
    };
})

    .directive("popoverHtmlUnsafe", [ "$tooltip", function ($tooltip) {
        return $tooltip("popoverHtmlUnsafe", "popover", "click");
    }]);

herbonautesApp.filter('htmlLinky', function($sanitize, linkyFilter) {
    var ELEMENT_NODE = 1;
    var TEXT_NODE = 3;
    var linkifiedDOM = document.createElement('div');
    var inputDOM = document.createElement('div');

    var linkify = function linkify(startNode) {
        var i, ii, currentNode;

        for (i = 0, ii = startNode.childNodes.length; i < ii; i++) {
            currentNode = startNode.childNodes[i];

            switch (currentNode.nodeType) {
                case ELEMENT_NODE:
                    linkify(currentNode);
                    break;
                case TEXT_NODE:
                    linkifiedDOM.innerHTML = linkyFilter(currentNode.textContent);
                    i += linkifiedDOM.childNodes.length - 1
                    while(linkifiedDOM.childNodes.length) {
                        startNode.insertBefore(linkifiedDOM.childNodes[0], currentNode);
                    }
                    startNode.removeChild(currentNode);
            }
        }

        return startNode;
    };

    return function(input) {
        inputDOM.innerHTML = input;
        return linkify(inputDOM).innerHTML;
    };
});

herbonautesApp.config(function($locationProvider) {
  $locationProvider.html5Mode({
        enabled: true,
        requireBase: false,
        rewriteLinks: false
    });
});


herbonautesApp.service('elasticService', function (esFactory) {
    return esFactory({
        host: elasticHostHerbonautes
    });

});

herbonautesApp.service('searchService', ['elasticService', function(elasticService) {
    return {
        populateResults : function (query, type, fields, results, nbMaxResults, page) {
            var search = angular.lowercase(query);
            var from = 0;
            if(page != null)
                from = (nbMaxResults * (page - 1));
            var query = {
                "bool":{
                    "should":[
                        {
                            "query_string":{
                                "default_field":"_all",
                                "query": search,
                                "boost": 1.0
                            }
                        }
                    ]
                }
            };

            var length = fields.length;
            for(var i = 0 ; i < length ; i++) {
                var tmp = {};
                tmp.wildcard = {};
                tmp.wildcard[fields[i]] = {
                    "value": "*" + search + "*",
                    "boost": 10 * (length - i)
                }
                query.bool.should.push(tmp);
            }

            return elasticService.search({
                index: elasticIndex,
                type: type,
                from: from,
                size: nbMaxResults,
                body: {
                    "query": query,
                    "sort": "_score"
                }
            }).then(function(response) {
                var hits = response.hits.hits;
                for (i = 0; i < hits.length; i++) {
                    var tmp = {};

                    switch (type) {
                        case "specimens":
                            tmp.text = hits[i]._source.specimen.code;
                            tmp.url = showSpecimenAction({institute: encodeURIComponent(hits[i]._source.specimen.institute), collection: encodeURIComponent(hits[i]._source.specimen.collection), code: encodeURIComponent(hits[i]._source.specimen.code)});
                            tmp.group = 'Specimens';
                            break;
                        case "missions":
                            tmp.text = hits[i]._source.mission.title;
                            tmp.url = showMissionAction({id: hits[i]._source.mission.id});
                            tmp.group = 'Missions';
                            break;
                        case "discussions":
                            var text = hits[i]._source.discussion.title;
                            if(text == null || text == '') {
                                if( typeof hits[i]._source.discussion.messages === 'string' ) {
                                    text = hits[i]._source.discussion.messages;
                                } else {
                                    text = hits[i]._source.discussion.messages[0];
                                }
                            }
                            tmp.text = text;
                            tmp.url = showDiscussionAction({id: hits[i]._source.discussion.id});
                            tmp.group = 'Discussions';
                            break;
                        case "tags":
                            tmp.text = hits[i]._source.tag.tagLabel;
                            tmp.url = showTagAction({tagLabel: encodeURIComponent(hits[i]._source.tag.tagLabel)});
                            tmp.group = 'Tags';
                            break;
                        case "herbonautes":
                            tmp.text = hits[i]._source.herbonaute.login;
                            tmp.url = showUserAction({login: encodeURIComponent(hits[i]._source.herbonaute.login)});
                            tmp.group = 'Herbonautes';
                            break;
                        case "botanists":
                            tmp.text = hits[i]._source.botanist.name;
                            tmp.url = showBotanistAction({id: hits[i]._source.botanist.id});
                            tmp.group = 'Botanistes';
                            break;
                        case "scientificnames":
                            tmp.text = hits[i]._source.scientificname.genus + ' ' + hits[i]._source.scientificname.specificepithet;
                            tmp.url = listSpecimensSpAction({genus: encodeURIComponent(hits[i]._source.scientificname.genus), specificepithet: encodeURIComponent(hits[i]._source.scientificname.specificepithet)});
                            tmp.group = 'Noms scientifiques';
                            break;
                    }
                    results.push(tmp);
                }
                return results;
            });
        }
    }
}]);

herbonautesApp.service('DiscussionService', ['$http', function($http) {
    return {
        getDiscussionsCategories: function() {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/getDiscussionsCategories');
        },
        getNewMessages: function(tagLabel, tagType, lastMessageId) {
            var url = herbonautes.ctxPath + '/modules/discussionModule/getNewMessages?';
            if(!!tagLabel) {
                url += 'tagLabel=' + encodeURIComponent(tagLabel);
            }
            if(!!tagType) {
                if(!!tagLabel) url += '&';
                url += 'tagType=' + tagType;
            }
            if(!!tagLabel || !!tagType) url += '&';
            url += 'lastMessageId=' + lastMessageId;
            return $http.get(url);
        },
        getNewMessagesByDiscussionId: function(discussionId, lastMessageId) {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/getNewMessagesByDiscussionId?discussionId=' + discussionId + '&lastMessageId=' + lastMessageId);
        },
        getDiscussionsFilteredByCategories: function(tagLabel, tagType, categories, loadedDiscussions) {
            var url = herbonautes.ctxPath + '/modules/discussionModule/getDiscussionsFilteredByCategories?';
            if(!!tagLabel) {
                url += 'tagLabel=' + encodeURIComponent(tagLabel);
            }
            if(!!tagType) {
               if(!!tagLabel) url += '&';
               url += 'tagType=' + tagType;
            }
            if(!!tagLabel || !!tagType) url += '&';
            url += 'categories=' + encodeURIComponent(categories);
            url += '&loadedDiscussions=' + encodeURIComponent(loadedDiscussions);
            return $http.get(url);
        },
        getDiscussionById: function(discussionId) {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/getDiscussionById/' + discussionId);
        },
        getDiscussionsCount: function(tagLabel, tagType) {
            var url = herbonautes.ctxPath + '/modules/discussionModule/getDiscussionsCount';
            if(!!tagLabel || !!tagType) url += '?';
            if(!!tagLabel) url += 'tagLabel=' + encodeURIComponent(tagLabel);
            if(!!tagType) {
                if(!!tagLabel) url += '&';
                url += 'tagType=' + tagType;
            }
            return $http.get(url);
        },
        createDiscussion: function(newDiscussion) {
            return $http.post(herbonautes.ctxPath + '/modules/discussionModule/createDiscussion', newDiscussion);
        },
        postMessage: function(newMessage) {
            return $http.post(herbonautes.ctxPath + '/modules/discussionModule/postMessage', newMessage);
        },
        solveDiscussion: function(messageId) {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/solveDiscussion?messageId=' + messageId);
        },
        deleteMessage: function(messageId) {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/deleteMessage?messageId=' + messageId);
        },
        getLastMessages: function(tagLabel, tagType) {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/getLastMessages?tagLabel=' + encodeURIComponent(tagLabel) + '&tagType=' + tagType);
        },
        getServerTime: function() {
            return $http.get(herbonautes.ctxPath + '/modules/discussionModule/getServerTime');
        },
        saveDiscussionTags: function(saveTagsForm) {
            return $http.post(herbonautes.ctxPath + '/modules/discussionModule/saveTags', saveTagsForm);
        },
    }
}]);

herbonautesApp.service('TagService', ['$http', 'elasticService', function($http, elasticService) {
    return {
        subscribeTag: function(tagLabel) {
            return $http.get(herbonautes.ctxPath + '/modules/tagModule/subscribeTag/' + encodeURIComponent(tagLabel));
        },
        unsubscribeTag: function(tagLabel) {
            return $http.get(herbonautes.ctxPath + '/modules/tagModule/unsubscribeTag/' + encodeURIComponent(tagLabel));
        },
        getAllSubscribedTags: function() {
            return $http.get(herbonautes.ctxPath + '/modules/tagModule/getAllSubscribedTags');
        },
        getSpecimensByTags: function(tagLabel) {
            return $http.get(herbonautes.ctxPath + '/modules/tagModule/getSpecimensByTag/' + encodeURIComponent(tagLabel));
        },
        saveElementTags: function(saveTagsForm) {
            return $http.post(herbonautes.ctxPath + '/modules/tagModule/saveTags', saveTagsForm);
        },
        loadTagsByElement: function(tagLinkType, targetId) {
            return $http.get(herbonautes.ctxPath + '/modules/tagModule/loadTagsByElement/' + tagLinkType + '/' + targetId);
        },
        deleteTagLink: function(tagId, tagLinkType, targetId, principalTagLabel) {
            return $http.get(herbonautes.ctxPath + '/modules/tagModule/deleteTagLink/' + tagId + '/' + tagLinkType + '/' + targetId + '/' + encodeURIComponent(principalTagLabel));
        },
        loadTags: function($query, exclusions) {
            var result = [];

            if (!$query) {
                return;
            }

            var search = '*' + angular.lowercase($query).trim() + '*';


            //var query = {
            //    "bool" : {
            //        "must" : [
            //            {
            //                "wildcard": {
            //                    "tagLabel": {
            //                        "value": search
            //                    }
            //                }
            //            }
            //        ],
            //        "must_not" : []
            //    }
            //};

            var query = {
                "bool":{
                    "should":[
                        {
                            "query_string":{
                                "default_field":"tagLabel",
                                "query": search,
                                "boost": 1.0
                            }
                        }
                    ]
                }
            };

            if(!!exclusions) {
                var length = exclusions.length;
                for(var i = 0 ; i < length ; i++) {
                    var tmp = {};
                    tmp.term = {};
                    tmp.term.tagLabelRaw = exclusions[i].tagLabel;
                    query.bool.must_not.push(tmp);
                }
            }

            return elasticService.search({
                index: elasticIndex,
                type: 'tags',
                from: 0,
                size: 100,
                body: {
                    "query": query,
                    "sort": { "tagType" : "desc" }
                }
            }).then(function(response) {
                var hits = response.hits.hits;
                for (i = 0; i < hits.length; i++) {
                    var tmp = {};
                    tmp.tagLabel = hits[i]._source.tag.tagLabel;
                    tmp.tagType = hits[i]._source.tag.tagType;
                    result.push(tmp);
                }
                return result;
            });

        }
    }
}]);


herbonautesApp.controller('contributionDiscussionsCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {
    $scope.isDiscussionModuleOpened = false;

    $scope.showDiscussionModule = function() {
        $scope.isDiscussionModuleOpened = true;
    }

    $scope.hideDiscussionModule = function() {
        $scope.isDiscussionModuleOpened = false;
    }
}]);

herbonautesApp.controller('discussionCtrl', ['$scope', '$rootScope', '$interval', '$http', 'DiscussionService', 'elasticService', 'TagService', 'amMoment', function ($scope, $rootScope, $interval, $http, DiscussionService, elasticService, TagService, amMoment) {

    $scope.newDiscussionForm = {};
    $scope.newDiscussionForm.categories = [];
    $scope.categories = {};
    $scope.discussions = [];
    $scope.newDiscussionFormDisplayed=false;
    $scope.selectedCategoriesFilters = [];
    $scope.tags = [];
    $scope.modalTagsToAdd = [];
    $scope.modalTags = [];
    $scope.modalDiscussionId = null;
    $scope.lastMessageId = null;
    $scope.nbDiscussions = null;
    $scope.newMessageFields = {};
    $scope.newMessage = {};
    $scope.openedDiscussions = [];
    $scope.discussionId = null;
    $scope.showNewDiscussionFormTitle = false;
    $scope.showNewDiscussionFormCategories = false;
    $scope.showNewDiscussionFormTags = false;
    $scope.loadedDiscussions = [];
    $scope.buttonMoreDisplayed = false;
    $scope.saveInProgress = false;

    $scope.timeDifference = null;
    $scope.forceDiscussionId = null;

    $scope.displayNewDiscussionFormTitle = function() {
        $scope.showNewDiscussionFormTitle = true;
    }

    $scope.displayNewDiscussionFormCategories = function() {
        $scope.showNewDiscussionFormCategories = true;
    }

    $scope.displayNewDiscussionFormTags = function() {
        $scope.showNewDiscussionFormTags = true;
    }

    $scope.isOpenedDiscussion = function(discussionId) {
        return($scope.openedDiscussions.indexOf(discussionId) >= 0);
    }

    $scope.resolutionShowed = function(discussion) {
        if (discussion.resolved && discussion.messages.length == 3) {
            if (discussion.messages[1].resolution) {
                return true;
            }
        }
        return false;
    }

    $scope.openDiscussion = function(discussionId) {
        $scope.openedDiscussions.push(discussionId);
    }

    $rootScope.$on('reloadDiscussions', function(event, code) {
        //console.log("event ",  event);
        //console.log('specimenId:' + specimenId);
        //console.log('code:' + code);
        //
        $scope.init("SPECIMEN", code, null);
    });

    $scope.init = function(tagType, tagLabel, discussionId, displayAll, forceDiscussionId) {
        $scope.discussions = [];
        $scope.loadedDiscussions = [];
        $scope.buttonMoreDisplayed = false;
        $scope.forceDiscussionId = forceDiscussionId;

        modifyRootNbDiscussionsValue(null);
        DiscussionService.getServerTime().then(function(result) {
            $scope.timeDifference = new Date() - new Date(result.data);
        }, function(error) {
            console.log(error.data);
        });

        amMoment.changeLocale('fr');

        if (!!tagType) {
            $scope.tagType = tagType;
        }
        if (!!tagLabel) {
            $scope.tagLabel = tagLabel;
        }

        if(discussionId != null) {
            $scope.discussionId = discussionId;
            $scope.getDiscussionById(discussionId);
            $scope.nbDiscussions = 1;
            modifyRootNbDiscussionsValue($scope.nbDiscussions);
        } else if(!!displayAll || (!!tagLabel && !!tagType)) {
            DiscussionService.getDiscussionsCategories().then(function(result) {
                $scope.categories = result.data;
                $scope.hideNewDiscussionForm();
                $scope.getDiscussionsFilteredByCategories();
            }, function(error) {
                console.log(error.data);
            });

            DiscussionService.getDiscussionsCount($scope.tagLabel, $scope.tagType).then(function(result) {
                $scope.nbDiscussions = parseInt(result.data);
                modifyRootNbDiscussionsValue($scope.nbDiscussions);
            }, function(error) {
                console.log(error.data);
            });

        }

    }

    $scope.getUserPageUrl = function(login) {
        return showUserAction({login: encodeURIComponent(login)});
    }

    $scope.getMessageImageUrl = function(message) {
        var tmpId = message.imageId == null ? 'nopic' : message.imageId;
        return userImageAction({login: encodeURIComponent(message.author), imageId: tmpId})
    }

    $scope.getTagUrl = function(tagLabel) {
        return showTagAction({tagLabel: encodeURIComponent(tagLabel)})
    }

    $scope.initTags = function() {
        $scope.tags = [];
        var tmp = {};
        tmp.tagType = $scope.tagType;
        tmp.tagLabel = $scope.tagLabel;
        $scope.tags.push(tmp);
    }

    $scope.getDiscussionsFilteredByCategories = function () {
        var nbResults = nbDiscussionsToLoadPerCall;
        DiscussionService.getDiscussionsFilteredByCategories($scope.tagLabel, $scope.tagType, $scope.selectedCategoriesFilters.join(), $scope.loadedDiscussions.join()).then(function(result) {
            var discussions = {};
            discussions = result.data;
            if(discussions.length > nbDiscussionsToLoadPerCall) {
                $scope.buttonMoreDisplayed = true;
                discussions.pop();
            } else {
                $scope.buttonMoreDisplayed = false;
            }
            var forceDiscussionIndex = null;
            for(var i= 0 ; i < discussions.length ; i++) {
                discussions[i].nbMessagesInit = discussions[i].messages.length;
                $scope.discussions.push(discussions[i]);
                if (discussions[i].id == $scope.forceDiscussionId) {
                    forceDiscussionIndex = i;
                }
                $scope.loadedDiscussions.push(discussions[i].id);
            }

            if (forceDiscussionIndex) {
                var el = $scope.discussions.splice(forceDiscussionIndex, 1)[0];
                $scope.discussions.unshift(el);
                var elid = $scope.loadedDiscussions.splice(forceDiscussionIndex, 1)[0];
                $scope.loadedDiscussions.unshift(elid);
            }


            $scope.calculateLastMessageId();
            $scope.updateDiscussionsInterval();
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.getDiscussionById = function () {
        DiscussionService.getDiscussionById($scope.discussionId).then(function(result) {
            $scope.discussions = result.data;
            for(var i= 0 ; i < $scope.discussions.length ; i++) {
                $scope.discussions[i].nbMessagesInit = $scope.discussions[i].messages.length;
            }
            $scope.calculateLastMessageId();
            $scope.updateDiscussionsInterval();
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.calculateLastMessageId = function(messages) {
        if(messages == null) {
            for (var i = 0; i < $scope.discussions.length; i++) {
                for (var j = 0; j < $scope.discussions[i].messages.length; j++) {
                    if($scope.lastMessageId == null || $scope.discussions[i].messages[j].id > $scope.lastMessageId){
                        $scope.lastMessageId = $scope.discussions[i].messages[j].id;
                    }
                }
            }
        }else {
            if(messages != null && messages.length > 0) {
                for (var i = 0; i < messages.length; i++) {
                    if($scope.lastMessageId == null || messages[i].id > $scope.lastMessageId){
                        $scope.lastMessageId = messages[i].id;
                    }
                }
            }

        }
    }


    var stop;

    $scope.updateDiscussionsInterval = function() {
        // Don't start a new fight if we are already fighting
        if ( angular.isDefined(stop) ) return;

        // stop = $interval(function() {
        //     $scope.initDiscussionsUpdate();
        // }, 5000);
    };

    $scope.cancelUpdateDiscussionsInterval = function() {
        if (angular.isDefined(stop)) {
            $interval.cancel(stop);
            stop = undefined;
        }
    };

    $scope.initDiscussionsUpdate = function(newDiscussionId) {
        if($scope.lastMessageId != null) {
            if($scope.discussionId != null) {
                DiscussionService.getNewMessagesByDiscussionId($scope.discussionId, $scope.lastMessageId).then(function(result) {
                    $scope.updateDiscussions(result, newDiscussionId);
                }, function(error) {
                    console.log(error.data);
                });
            } else {
                DiscussionService.getNewMessages($scope.tagLabel, $scope.tagType, $scope.lastMessageId).then(function(result) {
                    $scope.updateDiscussions(result, newDiscussionId);
                }, function(error) {
                    console.log(error.data);
                });
            }


        }
    }


    $scope.updateDiscussions = function(result, newDiscussionId) {
        var messages = result.data;
        $scope.calculateLastMessageId(messages);
        for (var i = 0; i < messages.length; i++) {
            for (var j = 0; j < $scope.discussions.length; j++) {
                if(messages[i].discussionId == $scope.discussions[j].id) {
                    if(newDiscussionId == null || newDiscussionId != messages[i].id)
                        $scope.discussions[j].messages.push(messages[i]);
                    if(messages[i].resolution){
                        $scope.discussions[j].resolved = true;
                    }
                }
            }
        }
    }



    $scope.loadTags = function($query) {
        return TagService.loadTags($query);
    };

    $scope.showNewDiscussionForm = function() {
        $scope.newDiscussionFormDisplayed=true;
    }

    $scope.hideNewDiscussionForm = function() {
        $scope.newDiscussionForm = {};
        $scope.newDiscussionForm.categories = [];
        $scope.newDiscussionFormDisplayed=false;
        $scope.showNewDiscussionFormTitle = false;
        $scope.showNewDiscussionFormCategories = false;
        $scope.showNewDiscussionFormTags = false;
        $scope.initTags();
    }

    $scope.isSelectedCategoryFilter = function(categoryId) {
        return($scope.selectedCategoriesFilters.indexOf(categoryId) >= 0);
    }

    $scope.selectOrUnselectCategoryFilter = function(categoryId) {
        var index = $scope.selectedCategoriesFilters.indexOf(categoryId);
        if(index >= 0) {
            $scope.selectedCategoriesFilters.splice(index, 1);
        } else {
            $scope.selectedCategoriesFilters.push(categoryId);
        }
        $scope.discussions = [];
        $scope.loadedDiscussions = [];
        $scope.getDiscussionsFilteredByCategories();
    }

    $scope.isSelectedNewDiscussionCategory = function(categoryId) {
        return($scope.newDiscussionForm.categories.indexOf(categoryId) >= 0);
    }

    $scope.addOrDeleteNewDiscussionCategory = function(categoryId) {
        var index = $scope.newDiscussionForm.categories.indexOf(categoryId);
        if(index >= 0) {
            $scope.newDiscussionForm.categories.splice(index, 1);
        } else {
            $scope.newDiscussionForm.categories.push(categoryId);
        }
    }

    $scope.createDiscussion = function() {
        $scope.newDiscussionForm.tags = $scope.tags;
        $scope.newDiscussionForm.tagType = $scope.tagType;
        $scope.newDiscussionForm.tagLabel = $scope.tagLabel;
        $scope.cancelUpdateDiscussionsInterval();
        $scope.saveInProgress = true;
        DiscussionService.createDiscussion($scope.newDiscussionForm).then(function(result) {
            $scope.newDiscussionForm = {};
            $scope.newDiscussionForm.categories = [];
            $scope.newDiscussionFormDisplayed=false;
            $scope.showNewDiscussionFormTitle = false;
            $scope.showNewDiscussionFormCategories = false;
            $scope.showNewDiscussionFormTags = false;
            $scope.saveInProgress = false;
            $scope.initTags();
            if($scope.selectedCategoriesFilters.length == 0 || $scope.selectedCategoriesFilters.indexOf(result.data.categoryId) >= 0) {
                $scope.discussions.unshift(result.data);
                $scope.loadedDiscussions.push(result.data.id);
                $scope.initDiscussionsUpdate(result.data.messages[0].id);
            }
            $scope.nbDiscussions++;
            modifyRootNbDiscussionsValue($scope.nbDiscussions);
            $scope.updateDiscussionsInterval();
        }, function(error) {
            $scope.updateDiscussionsInterval();
            console.log(error.data);
        });

    }

    function modifyRootNbDiscussionsValue (value) {
        $rootScope.nbDiscussionsTab = value == null || value == 0 ? '0' : value;
        $rootScope.nbDiscussionsBubble = value == null || value == 0 ? '' : value;
        /*$('#spanNbDiscussionsTab').html(value);
        $('#spanNbDiscussionsIcon').html(value);*/
    }

    $scope.postMessage = function(discussionId) {
        $scope.cancelUpdateDiscussionsInterval();
        $scope.newMessage.tagType = $scope.tagType;
        $scope.newMessage.tagLabel = $scope.tagLabel;
        $scope.newMessage.text = $scope.newMessageFields[discussionId];
        $scope.newMessage.discussionId = discussionId;
        DiscussionService.postMessage($scope.newMessage).then(function(result) {
            $scope.newMessageFields[discussionId] = '';
            $scope.newMessage = {};
            $scope.lastMessageId = result.data.id;
            for(var i= 0; i < $scope.discussions.length; i++)
            {
                if($scope.discussions[i].id == discussionId) {
                    $scope.discussions[i].messages.push(result.data);
                }
            }
            $scope.updateDiscussionsInterval();
        }, function(error) {
            console.log(error.data);
            $scope.updateDiscussionsInterval();
        });
    }

    $scope.solveDiscussion = function(messageId, confirmMessage) {
        if(confirm(confirmMessage)) {
            DiscussionService.solveDiscussion(messageId).then(function(result) {
                var message = result.data;
                for (var i = 0; i < $scope.discussions.length; i++) {
                    if(message.discussionId == $scope.discussions[i].id) {
                        $scope.discussions[i].resolved = true;
                        for (var j = 0; j < $scope.discussions[i].messages.length; j++) {
                            if(message.id == $scope.discussions[i].messages[j].id) {
                                $scope.discussions[i].messages[j].resolution = true;
                            }
                        }
                    }
                }
            }, function(error) {
                console.log(error.data);
            });
        }
    }

    $scope.deleteMessage = function(messageId, confirmMessage) {
        if(confirm(confirmMessage)) {
            DiscussionService.deleteMessage(messageId).then(function(result) {
                var message = result.data;
                var index = null;
                var discussionId = null;
                for (var i = 0; i < $scope.discussions.length; i++) {
                    for (var j = 0; j < $scope.discussions[i].messages.length; j++) {
                        if (message.id == $scope.discussions[i].messages[j].id) {
                            if (message.first) {
                                index = i;
                            } else {
                                index = j;
                                discussionId = i;
                            }
                        }
                    }
                }
                if(index != null) {
                    if(message.moderator == '') {
                        if(message.first){
                            $scope.discussions.splice(index, 1);
                            $scope.nbDiscussions--;
                            modifyRootNbDiscussionsValue($scope.nbDiscussions);
                        } else {
                            $scope.discussions[discussionId].messages.splice(index, 1);
                        }
                    } else {
                        $scope.discussions[discussionId].messages[index].moderator = message.moderator;
                        $scope.discussions[discussionId].messages[index].text = '';
                    }
                }

            }, function(error) {
                console.log(error.data);
            });
        }
    }

    $scope.defaultTag = null;
    $scope.currentDiscussion = null;
    $scope.loadTagsModal = function(discussion) {
        $scope.currentDiscussion = discussion;
        $scope.defaultTag = $scope.currentDiscussion.tags[0];
        $scope.modalTags = discussion.tags.slice();
        $('#control-discussions').css('z-index', 'auto');
        $('#discussionTagsModal').modal({
            keyboard: false,
            backdrop: 'static'
        });
    }

    $scope.saveDiscussionTagsForm = {};

    $scope.saveDiscussionTags = function() {
        if($scope.modalTags.length == 0) {
            $scope.modalTags.push($scope.defaultTag);
        } else {
            $scope.saveDiscussionTagsForm.tagsToAdd = $scope.modalTags;
            $scope.saveDiscussionTagsForm.targetId = $scope.currentDiscussion.id;
            $scope.saveDiscussionTagsForm.tagLinkType = 'DISCUSSION';
            $scope.saveDiscussionTagsForm.principalTagLabel = $scope.defaultTag.tagLabel;
            DiscussionService.saveDiscussionTags($scope.saveDiscussionTagsForm).then(function(result) {
                $scope.currentDiscussion.tags = $scope.modalTags.slice();
                $('#discussionTagsModal').modal('hide');
                $('#control-discussions').css('z-index', '1000');
            }, function(error) {
                console.log(error.data);
            });
        }

    }

    $scope.cancelDiscussionTagsModification = function() {
        $('#discussionTagsModal').modal('hide');
        $('#control-discussions').css('z-index', '1000');
    }

}]);

herbonautesApp.controller('tagCtrl', ['$scope', '$http', 'TagService', function ($scope, $http, TagService) {

    $scope.currentTag = {};
    $scope.subscribedTag = false;

    $scope.init = function(tagType, tagLabel, subscribedTag, forceDiscussion) {
        $scope.currentTag.tagLabel = tagLabel;
        $scope.currentTag.tagType = tagType;
        $scope.subscribedTag = subscribedTag;
        $scope.forceDiscussion = forceDiscussion;
    }

    $scope.subscribeTag = function() {
        TagService.subscribeTag($scope.currentTag.tagLabel).then(function(result) {
            $scope.subscribedTag = true;
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.unsubscribeTag = function() {
        TagService.unsubscribeTag($scope.currentTag.tagLabel).then(function(result) {
            $scope.subscribedTag = false;
        }, function(error) {
            console.log(error.data);
        });
    }

}]);

herbonautesApp.controller('elementTagsCtrl', ['$scope', '$rootScope', '$http', '$window', 'TagService', function ($scope, $rootScope, $http, $window, TagService) {

    $scope.targetId = null;
    $scope.tagLinkType = null;
    $scope.principalTagLabel = null;
    $scope.tagsToAdd = null;
    $scope.eleMentTags = {};
    $scope.saveTagsForm = {};

    $rootScope.$on('reloadSpecimenTags', function(event, id, code) {
        $scope.init("SPECIMEN", id, code);
    });

    $scope.init = function(tagLinkType, targetId, principalTagLabel) {
        console.log(tagLinkType);
        console.log(targetId);
        console.log(principalTagLabel);
        modifyRootNbTagsValue(null);
        $scope.targetId = targetId;
        $scope.tagLinkType = tagLinkType;
        $scope.principalTagLabel = principalTagLabel;
        $scope.loadTags();
    }

    $scope.loadTagsAutoComplete = function($query) {
        //return TagService.loadTags($query, $scope.elementTags);
        return TagService.loadTags($query);
    };

    $scope.loadTags = function() {
        TagService.loadTagsByElement($scope.tagLinkType, $scope.targetId).then(function(result) {
            $scope.elementTags = result.data;
            $scope.tagsToAdd = null;
            modifyRootNbTagsValue($scope.elementTags.length);
        }, function(error) {
            console.log(error.data);
        });
    };

    function modifyRootNbTagsValue (value) {
        $rootScope.nbTagsTab = value == null || value == 0 ? '0' : value;
    }


    $scope.saveTags = function () {
        $scope.saveTagsForm.tagsToAdd = $scope.tagsToAdd;
        $scope.saveTagsForm.elementTags = [];
        for(var i= 0 ; i < $scope.elementTags.length ; i++) {
            $scope.saveTagsForm.elementTags.push($scope.elementTags[i].tagLabel);
        }
        $scope.saveTagsForm.targetId = $scope.targetId;
        $scope.saveTagsForm.tagLinkType = $scope.tagLinkType;
        $scope.saveTagsForm.principalTagLabel = $scope.principalTagLabel;
        TagService.saveElementTags($scope.saveTagsForm).then(function(result) {
            $scope.loadTags();
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.cancel = function () {
        $scope.tagsToAdd = null;
        $('#specimenTagsModal').modal('hide');
    }

    $scope.deleteTagLink = function (tagId) {
        TagService.deleteTagLink(tagId, $scope.tagLinkType, $scope.targetId, $scope.principalTagLabel).then(function(result) {
            $scope.loadTags();
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.showTag = function(tagLabel) {
        $window.location= showTagAction({tagLabel: encodeURIComponent(tagLabel)});
    }

}]);

herbonautesApp.controller('userTagsCtrl', ['$scope', '$http', '$window', 'TagService', function ($scope, $http, $window, TagService) {

    $scope.tags = {};

    $scope.getAllSubscribedTags = function() {
        TagService.getAllSubscribedTags().then(function(result) {
            $scope.tags = result.data;
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.unsubscribeTag = function(tagLabel) {
        TagService.unsubscribeTag(tagLabel).then(function(result) {
            var index = null;
            for (var i = 0; i < $scope.tags.length; i++) {
                if(tagLabel == $scope.tags[i].tagLabel) {
                    index = i;
                }
            }
            if(index != null) $scope.tags.splice(index, 1);

        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.showTag = function(tagLabel, tagType) {
        $window.location= showTagAction({tagLabel: encodeURIComponent(tagLabel), tagType: tagType});
    }

}]);

herbonautesApp.controller('QuickSearchCtrl', ['$scope', '$http', '$window', 'searchService', function ($scope, $http, $window, searchService) {
    $scope.selectedResult = '';
    $scope.results = [];

    $scope.getResults = function (query) {
        var results = new Array();
        var tmp = {};
        tmp.text = 'Tous les résultats';
        tmp.url = searchAction({term: encodeURIComponent(query)});
        tmp.group = 'allResults';
        results.push(tmp);
        return searchService.populateResults(query, 'specimens', ['code', 'tags', 'genus', 'family', 'collection', 'institute'], results, specimensSearchNumber).then(function(result) {
            return searchService.populateResults(query, 'missions', ['title', 'tags'], results, missionsSearchNumber).then(function(result) {
               return searchService.populateResults(query, 'scientificnames', ['genus', 'family'], results, scientificNamesSearchNumber).then(function(result) {
                    return searchService.populateResults(query, 'discussions', ['messages', 'tags', 'categories'], results, discussionsSearchNumber).then(function(result) {
                        return searchService.populateResults(query, 'tags', ['tagLabel'], results, tagsSearchNumber).then(function(result) {
                            return searchService.populateResults(query, 'botanists', ['name'], results, botanistsSearchNumber).then(function(result) {
                                return searchService.populateResults(query, 'herbonautes', ['login', 'firstName', 'lastName'], results, herbonautesSearchNumber).then(function(result) {
                                    return results = _(results)
                                        .groupBy('group')
                                        .map(function (g) {
                                            g[0].firstInGroup = true;  // the first item in each group
                                            return g;
                                        })
                                        .flatten()
                                        .value();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    $scope.onSelect = function ($item, $model, $label) {
        var url = $scope.selectedResult.url;
        $scope.selectedResult.text = '';
        $window.location= url;
    };
}]);

herbonautesApp.controller('specimensListCtrl', ['$scope', '$interval', '$http', 'TagService', 'elasticService', 'amMoment', function ($scope, $interval, $http, TagService, elasticService, amMoment) {
    $scope.specimens = {};
    $scope.init = function(tagLabel) {
        TagService.getSpecimensByTags(tagLabel).then(function(result) {
            $scope.specimens = result.data;
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.getSpecimensListUrl = function(specimen) {
        return listSpecimensAction({family: encodeURIComponent(specimen.family), genus: encodeURIComponent(specimen.genus)});
    }

    $scope.getSpecimenUrl = function(specimen) {
        return showSpecimenAction({institute: encodeURIComponent(specimen.institute), collection: encodeURIComponent(specimen.collection), code: encodeURIComponent(specimen.code)});
    }


}]);


herbonautesApp.controller('nbDiscussionsCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {

}]);

herbonautesApp.controller('nbTagsCtrl', ['$scope', '$rootScope', function ($scope, $rootScope) {

}]);

herbonautesApp.controller('lastMessagesBoxCtrl', ['$scope', '$rootScope', 'DiscussionService', 'amMoment', function ($scope, $rootScope, DiscussionService, amMoment) {

    $scope.messages = {};
    $scope.timeDifference = null;

    $scope.init = function(tagType, tagLabel) {

        amMoment.changeLocale('fr');
        DiscussionService.getServerTime().then(function(result) {
            $scope.timeDifference = new Date() - new Date(result.data);
        }, function(error) {
            console.log(error.data);
        });

        if (!!tagType) {
            $scope.tagType = tagType;
        }
        if (!!tagLabel) {
            $scope.tagLabel = tagLabel;
        }

        DiscussionService.getLastMessages($scope.tagLabel, $scope.tagType).then(function(result) {
            $scope.messages = result.data;
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.getMessageImageUrl = function(message) {
        var tmpId = message.imageId == null ? 'nopic' : message.imageId;
        return userImageAction({login: encodeURIComponent(message.author), imageId: tmpId})
    }

    $scope.getUserPageUrl = function(login) {
        return showUserAction({login: encodeURIComponent(login)});
    }

}]);

herbonautesApp.controller('searchCtrl', ['$scope', '$http', '$window', 'searchService', function ($scope, $http, $window, searchService) {
    $scope.page = 1;
    $scope.specimens = [];
    $scope.missions = [];
    $scope.scientificnames = [];
    $scope.discussions = [];
    $scope.tags = [];
    $scope.botanists = [];
    $scope.herbonautes = [];
    $scope.query = '';
    $scope.searchResultsNumber = 0;

    $scope.init = function (query) {
        $scope.page = 1;
        $scope.getResults(query, 1);
        $scope.query = query;
        $scope.searchResultsNumber = fullSearchResultsNumber;
    }

    $scope.getResults = function (query, page) {
        $scope.specimens = [];
        $scope.missions = [];
        $scope.scientificnames = [];
        $scope.discussions = [];
        $scope.tags = [];
        $scope.botanists = [];
        $scope.herbonautes = [];
        var nbResults = eval(fullSearchResultsNumber) + 1;
        return searchService.populateResults(query, 'specimens', ['code', 'tags', 'genus', 'family', 'collection', 'institute'], $scope.specimens, nbResults, page).then(function(result) {
            return searchService.populateResults(query, 'missions', ['title', 'tags'], $scope.missions, nbResults, page).then(function(result) {
                return searchService.populateResults(query, 'scientificnames', ['genus', 'family'], $scope.scientificnames, nbResults, page).then(function(result) {
                    return searchService.populateResults(query, 'discussions', ['messages', 'tags', 'categories'], $scope.discussions, nbResults, page).then(function(result) {
                        return searchService.populateResults(query, 'tags', ['tagLabel'], $scope.tags, nbResults, page).then(function(result) {
                            return searchService.populateResults(query, 'botanists', ['name'], $scope.botanists, nbResults, page).then(function(result) {
                                return searchService.populateResults(query, 'herbonautes', ['login', 'firstName', 'lastName'], $scope.herbonautes, nbResults, page).then(function(result) {
                                    return;
                                });
                            });
                        });
                    });
                });
            });
        });




    }
    
    $scope.isNextButtonActive = function (){
        return ($scope.specimens.length > fullSearchResultsNumber
        || $scope.missions.length > fullSearchResultsNumber
        || $scope.scientificnames.length > fullSearchResultsNumber
        || $scope.discussions.length > fullSearchResultsNumber
        || $scope.tags.length > fullSearchResultsNumber
        || $scope.botanists.length > fullSearchResultsNumber
        || $scope.herbonautes.length > fullSearchResultsNumber)

    }

    $scope.nextPage = function (){
        if($scope.isNextButtonActive()) {
            $scope.page++;
            $scope.getResults($scope.query, $scope.page);
        }
    }

    $scope.previousPage = function (){
        if($scope.page > 1) {
            $scope.page--;
            $scope.getResults($scope.query, $scope.page);
        }
    }

}]);

herbonautesApp.service('DiscussionCategoriesService', ['$http', 'elasticService', function($http, elasticService) {
    return {
        getAllCategories: function () {
            return $http.get(herbonautes.ctxPath + '/categories/getAllCategories');
        },
        createCategory: function (label) {
            return $http.get(herbonautes.ctxPath + '/categories/createCategory/' + label);
        },
        deleteCategory: function (id) {
            return $http.get(herbonautes.ctxPath + '/categories/deleteCategory/' + id);
        }
    }
}]);

herbonautesApp.controller('categoriesCtrl', ['$scope', '$http', '$window', 'DiscussionCategoriesService', function ($scope, $http, $window, DiscussionCategoriesService) {

    $scope.categories = {};
    $scope.addFormDisplayed = false;
    $scope.newCategoryForm = {};

    $scope.init = function () {
        $scope.hideAddForm();
        $scope.getAllCategories();
    }

    $scope.getAllCategories = function() {
        DiscussionCategoriesService.getAllCategories().then(function(result) {
            $scope.categories = result.data;
        }, function(error) {
            console.log(error.data);
        });
    }

    $scope.displayAddForm = function () {
        $scope.addFormDisplayed = true;
    }

    $scope.hideAddForm = function () {
        $scope.addFormDisplayed = false;
    }

    $scope.createCategory = function() {
        DiscussionCategoriesService.createCategory($scope.newCategoryForm.label).then(function(result) {
            $scope.newCategoryForm = {};
            $scope.getAllCategories();
        }, function(error) {
            console.log(error.data);
        });
        $('#addCategoryModal').modal('hide');
    }

    $scope.deleteCategory = function(id, confirmMessage, nbDiscussions) {
        confirmMessage = nbDiscussions + ' ' + confirmMessage;
        if(nbDiscussions == 0 || confirm(confirmMessage)) {
            DiscussionCategoriesService.deleteCategory(id).then(function(result) {
                $scope.getAllCategories();
            }, function(error) {
                console.log(error.data);
            });
        }
    }

}]);