
var SPLITS = ["/", "-", " "];

function getLastPathSegment(path) {
    var paths = path.split('/');
    return +paths[paths.length - 1];
}

function replaceLastPathSegment(path, segment) {
    var paths = path.split('/');
    paths[paths.length - 1] = segment;
    return paths.join('/');
}

function formatYear(d) {
    //var date = new Date(d);
    var date  = correctDateTZ(new Date(d));

    var year = date.getFullYear();

    return "" + year;
}

function formatMonth(d) {
    //var date = new Date(d);
    var date  = correctDateTZ(new Date(d));

    var monthNames = [
        "Janvier", "Février", "Mars",
        "Avril", "Mai", "Juin", "Juillet",
        "Août", "Septembre", "Octobre",
        "Novembre", "Décembre"
    ];

    var monthIndex = date.getMonth();
    var year = date.getFullYear();

    return monthNames[monthIndex] + " " + year;
}

function correctDateTZ(tzDate) {
    //var tz = herbonautes.timezoneOffset;
    //var date  = new Date(tzDate.getTime() + (tzDate.getTimezoneOffset() - tz)*60*1000);
    //return date;
    return tzDate;
}

function correctUTCShift(ts) {
    var d;
    if (typeof ts === "number") {
        d = new Date(ts);
    } else {
        d = ts;
    }
    var h = d.getUTCHours();
    var shift = 0;
    if (h > 12) {
        shift = 24 - h;
    } else {
        shift = -h;
    }
    return ts + (shift * 60 * 60 * 1000);
}

function formatDate(d) {
    //var date = new Date(d);

    //var date  = correctDateTZ(new Date(d));

    var date = new Date(correctUTCShift(d));

    var monthNames = [
        "Janvier", "Février", "Mars",
        "Avril", "Mai", "Juin", "Juillet",
        "Août", "Septembre", "Octobre",
        "Novembre", "Décembre"
    ];

    var day = date.getUTCDate();
    var monthIndex = date.getUTCMonth();
    var year = date.getUTCFullYear();

    return day + " " + monthNames[monthIndex] + " " + year;
}



function formatPeriod(start, end) {

    if (correctUTCShift(start) == correctUTCShift(end)) {
        return formatDate(start)
    }

    //var sd = new Date(start);
    var sd = new Date(correctUTCShift(start));

    if (sd.getUTCDate() == 1 && correctUTCShift(start) == firstDayOfMonth(correctUTCShift(end))) {
        return formatMonth(correctUTCShift(start));
    }

    if (sd.getUTCDate() == 1 && sd.getUTCMonth() == 0 && correctUTCShift(start) == firstDayOfYear(correctUTCShift(end))) {
        return formatYear(correctUTCShift(start));
    }

    return formatDate(correctUTCShift(start)) + " - " + formatDate(correctUTCShift(end));

}

function noAccent(str) {
    var accent = [
        /[\300-\306]/g, /[\340-\346]/g, // A, a
        /[\310-\313]/g, /[\350-\353]/g, // E, e
        /[\314-\317]/g, /[\354-\357]/g, // I, i
        /[\322-\330]/g, /[\362-\370]/g, // O, o
        /[\331-\334]/g, /[\371-\374]/g, // U, u
        /[\321]/g, /[\361]/g, // N, n
        /[\307]/g, /[\347]/g, // C, c
    ];
    var noaccent = ['A','a','E','e','I','i','O','o','U','u','N','n','C','c'];

    for(var i = 0; i < accent.length; i++){
        str = str.replace(accent[i], noaccent[i]);
    }

    return str;
}

function guessMonth(s) {
    var monthsPrefix = ["ja","fe","mar","av","mai","juin","juil","ao","se","oc","no", "de"];
    for (var i = 0 ; i < monthsPrefix.length ; i++) {
        var m = monthsPrefix[i];

        if (noAccent(s).substr(0, m.length) == m) {
            return i;
        }
    }
    return -1;
}

function bestSplit(str, seps) {
    var bestGroup, maxLength = 0;
    _.each(seps, function(sep) {
        var group = str.split(sep);
        if (maxLength < group.length) {
            bestGroup = group;
            maxLength = group.length;
        }
    });
    return bestGroup;
}

function tzDate(d, m, y) {
   //var date = new Date(y, m, d);
   //var tz = herbonautes.timezoneOffset;
   //date.setTime(date.getTime() - (date.getTimezoneOffset() - tz )*60*1000);

   //return date.getTime();
    return Date.UTC(y, m, d);
}

function lastDayOfMonth(dt) {
    var date = new Date(dt);
    date.setUTCMonth(date.getUTCMonth() + 1);
    date.setTime(date.getTime() - 86400000);
    return date.getTime();
}
function lastDayOfYear(dt) {
    var date = new Date(dt);
    date.setUTCFullYear(date.getUTCFullYear() + 1);
    date.setTime(date.getTime() - 86400000);
    return date.getTime();
}
function firstDayOfMonth(dt) {
    var date = new Date(dt);
    date.setTime(date.getTime() + 86400000);
    date.setUTCMonth(date.getUTCMonth() - 1);
    return date.getTime();
}
function firstDayOfYear(dt) {
    var date = new Date(dt);
    date.setTime(date.getTime() + 86400000);
    date.setUTCFullYear(date.getUTCFullYear() - 1);
    return date.getTime();
}

function guessPeriod(periodString) {
   if (periodString) {
       var p = bestSplit(periodString, ["/", "-", " "]);

       if (p.length == 1) {

           var start = tzDate(1, 0, p[0]);
           return [start, lastDayOfYear(start)];

       } else if (p.length == 2) {
           var m;
           if (isNaN(parseInt(p[0]))) {
               m = guessMonth(p[0]);
               if (m < 0) {
                   return null;
               }
           } else {
               m = p[0] - 1;
           }
           var start = tzDate(1, m, p[1]);
           return [start, lastDayOfMonth(start)];

       } else if (p.length == 3) {
           var d = guessDate(periodString);
           return [d, d];
       }

       return null;

   } else {
     return null;
   }
}


function guessDate(dateString) {

    if (dateString) {
        var p = bestSplit(dateString, ["/", "-", " "]);
        if (p.length != 3) {
            return null;
        }
        var m;
        if (isNaN(parseInt(p[1]))) {
            m = guessMonth(p[1]);
            if (m < 0) {
                return null;
            }
        } else {
            m = p[1] - 1;
        }

        return tzDate(p[0], m, p[2]);
    } else {
        return null;
    }
}


function removeAccents(s) {
    var r=s.toLowerCase();
    r = r.replace(new RegExp("\\s", 'g'),"");
    r = r.replace(new RegExp("[àáâãäå]", 'g'),"a");
    r = r.replace(new RegExp("æ", 'g'),"ae");
    r = r.replace(new RegExp("ç", 'g'),"c");
    r = r.replace(new RegExp("[èéêë]", 'g'),"e");
    r = r.replace(new RegExp("[ìíîï]", 'g'),"i");
    r = r.replace(new RegExp("ñ", 'g'),"n");
    r = r.replace(new RegExp("[òóôõö]", 'g'),"o");
    r = r.replace(new RegExp("œ", 'g'),"oe");
    r = r.replace(new RegExp("[ùúûü]", 'g'),"u");
    r = r.replace(new RegExp("[ýÿ]", 'g'),"y");
    r = r.replace(new RegExp("\\W", 'g'),"");
    return r;
};


herbonautesApp.controller('ShowSpecimenCtrl', ['$scope', function($scope) {

    $scope.context = {
        specimen: {
            genus: "GENUSTEST",
            specificEpithet: "SPEEPITEST"
        }
    }

}]);

herbonautesApp.controller('ReferencesCtrl', ['$scope', function($scope) {



}]);

herbonautesApp.factory('UserService', ['$resource', function($resource) {
    var urls = {
        current: herbonautes.actions.User.currentUser(),
        passedQuiz: herbonautes.actions.User.currentUserPassedQuiz()
    }

    return $resource('/modules/user/:id',
        { id: '@id' },
        {
            current: { method: 'GET', url: urls.current, isArray: false },
            passedQuiz: { method: 'GET', url: urls.passedQuiz, isArray: true },
        }
    );
}]);

herbonautesApp.factory('References', ['$resource', function($resource) {

    var urls = {
        getReference  : herbonautes.actions.References.getReference({referenceId: ':id' }),
        allReferences : herbonautes.actions.References.allReferences(),
        allRecords    : herbonautes.actions.References.allRecords({referenceId: ':id' }),
        search        : herbonautes.actions.References.searchRecord({referenceId: ':id' })
    }


    //console.log('URL REF', urls.allReferences, urls.getReference);

    return $resource(urls.getReference,
        { id: '@id' },
        {
            allReferences: { method: 'GET', url: urls.allReferences, isArray: true },
            allRecords: { method: 'GET', url: urls.allRecords, isArray: true },
            search: { method: 'POST', url: urls.search, isArray: true, headers: { 'Content-Type': 'application/x-www-form-urlencoded' } }
            // current: { method: 'GET', url: '/api/lots/current', isArray: true },
            // finished: { method: 'GET', url: '/api/lots/finished', isArray: false, params: {
            //     page: '@page'
            // } },
            // images: { method: 'GET', url: '/api/lots/:lotId/images', isArray: true },
            // stats: { method: 'GET', url: '/api/lots/:lotId/stats', isArray: false }
        }
    );
}]);

herbonautesApp.service('ReferencesSearch', ['$http', function($http) {

    this.search = function(referenceId, term, parentId) {
        return $http({
            method: 'POST',
            url: herbonautes.actions.References.searchRecord({ referenceId: referenceId}), //'/modules/admin/references/' + referenceId + '/search',
            data: $.param({ term: term, parentId: parentId }),
            headers: {'Content-Type': 'application/x-www-form-urlencoded'}
        })
    }

}]);

herbonautesApp.factory('MissionService', ['$resource', function($resource) {

    var urls = {
        questions: herbonautes.actions.Missions.allQuestions({id : ':id' }),
        reloadQuestions: herbonautes.actions.Missions.reloadQuestionsConfiguration({id : ':id' }),
        contributionsStats: herbonautes.actions.Missions.specimenWithContributionReports({id : ':id' }),
        statsContributions: herbonautes.actions.Missions.statsContributionsByQuestion({id : ':id' }),
        contributionsStatsDetails: herbonautes.actions.Missions.statsContributionsByUserDetails({id : ':id' }),
        joinMission: herbonautes.actions.Missions.joinMission({id : ':id' }),
        startRecolnatTransfer: herbonautes.actions.Missions.startRecolnatTransfer({id : ':id' }),
        recolnatTransferReport: herbonautes.actions.Missions.recolnatTransferReport({id : ':id' }),
    }

    console.log("init start transfer url", urls.startRecolnatTransfer);

    return $resource('/missions/:id',
        { id: '@id' },
        {
            questions: { method: 'GET', url: urls.questions, isArray: true },
            saveQuestions: { method: 'POST', url: urls.questions, isArray: true },
            reloadQuestions: { method: 'GET', url: urls.reloadQuestions, isArray: true },
            contributionsStats: { method: 'GET', url: urls.contributionsStats, isArray: true },
            statsContributions: { method: 'GET', url: urls.statsContributions, isArray: true },
            contributionsStatsDetails: { method: 'GET', url: urls.contributionsStatsDetails, isArray: true },
            joinMission: { method: 'GET', url: urls.joinMission, isArray: false},
            startRecolnatTransfer: { method: 'POST' , url: urls.startRecolnatTransfer, isArray: false},
            recolnatTransferReport: { method: 'GET' , url: urls.recolnatTransferReport, isArray: false},

        }
    );
}]);




herbonautesApp.controller('MissionCtrl', ['$scope', '$rootScope', 'MissionService', function($scope, $rootScope, MissionService) {

    $scope.questions = [];
    $scope.contributionsStats = [];
    $scope.contributionPage = 0;
    $scope.contributionWithConflictsOnly = false;

    $scope.ratingStats = [];
    $scope.ratingPage = 0;

    $scope.init = function(missionId) {
        //console.log("Init mission ", missionId);
        $scope.missionId = missionId;
        $scope.questions = MissionService.questions({id : missionId });
        $scope.nextContributionsPage();
        $scope.nextRatingPage();
    }

    $scope.contributionTooltip = function(q) {
        if (q.validated) {
            return q.answerCount + ' ' + (q.answerCount > 1 ? 'contributions en accord' : 'contribution en accord');
        } else if (q.conflicts) {
            return q.answerCount + ' ' + (q.answerCount > 1 ? 'contributions avec des différences' : 'contribution avec des différences');
        } else {
            return "à compléter"
        }
    }

    $scope.questionStat = function(stat, questionName) {
        return _.find(stat.questionStats, function(s) { return s.question.name == questionName });
    }

    $scope.answerCount = function(stat, questionName) {
        return _.find(stat.statsByQuestions, function(s) { return s.question.name == questionName });
    }

    $scope.userPic = function(user) {
        return herbonautes.ctxPath + '/users/' + user.login + '/' + (user.imageId || 'nopic' ) + '.jpg';
    }

    $scope.specimenUrl = function(stat, missionId) {
        return herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + stat.specimen.id;
    }

    $scope.specimenPageUrl = function(stat, missionId) {
        return herbonautes.ctxPath + '/specimens/' + stat.specimen.institute + '/' +  stat.specimen.collection + '/' + stat.specimen.code;
    }
    $scope.userImageUrl = function(user) {
        return herbonautes.ctxPath + "/img/users/" + user.id + ".jpg";
    }
    $scope.userProfileUrl = function(user) {
        return herbonautes.ctxPath + '/users/' + user.login;
    }

    $scope.resetContributionsPage = function() {
        $scope.contributionsStats = [];
        $scope.contributionPage = 0;
        $scope.noMoreContributions = false;
        $scope.contributionLoading = false;
        $scope.nextContributionsPage();
    }

    $scope.nextContributionsPage = function() {

        $scope.contributionLoading = true;
        MissionService.contributionsStats({ id: $scope.missionId, page: $scope.contributionPage + 1, onlyConflicts: $scope.contributionWithConflictsOnly }, function(stats) {
            //console.log(stats);
            if (stats.length == 0) {
                $scope.noMoreContributions = true;
            }
            $scope.contributionsStats = $scope.contributionsStats.concat(stats);
            $scope.contributionPage++;
            $scope.contributionLoading = false;
        });
    }

    $scope.nextRatingPage = function() {
        $scope.ratingLoading = true;

        MissionService.contributionsStatsDetails({ id: $scope.missionId, page: $scope.ratingPage + 1 }, function(stats) {
            //console.log(stats);
            if (stats.length == 0) {
                $scope.noMoreRatings = true;
            }
            $scope.ratingStats = $scope.ratingStats.concat(stats);
            $scope.ratingPage++;

            if (!$scope.topContributors && $scope.ratingStats.length > 0) {
                $scope.topContributors = [];
                for (var i = 0; i < 3 && i < $scope.ratingStats.length; i++) {
                    $scope.topContributors.push($scope.ratingStats[i].user)
                }
            }

            $scope.ratingLoading = false;

            //console.log($scope.ratingStats);
        });
    }


}]);


herbonautesApp.service('ContributionService', ['$http', function($http) {

    this.getUserAnswers = function(missionId, specimenId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/answers/user');
    }

    this.getStats = function(missionId, specimenId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/answers/stats');
    }

    this.submitAnswer = function(missionId, specimenId, questionId, answer, options) {
        var params = '';
        if (options && !!options.noConflicts) {
            params = '?noConflicts=true';
        } else {
            params = '?noConflicts=false';
        }
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/answers/' + questionId + params, answer);
    },

    this.keepAnswer = function(missionId, specimenId, questionId, answer, options) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/answers/' + questionId + '/keep');
    },

    this.answerReport = function(missionId, specimenId, questionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/answers/' + questionId);
    }

    this.cancelAnswer = function(missionId, specimenId, questionId) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/answers/' + questionId + '/cancel');
    }

    this.getSpecimen = function(missionId, specimenId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '.json');
    }

    this.getRandomSpecimen = function(missionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/random.json');
    }

    this.markSeen = function(missionId, specimenId) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/markseen');
    }

    this.markCurrent = function(missionId, specimenId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/' + specimenId + '/mark');
    }

}]);

herbonautesApp.service('QuestionUtils', [function() {

    function confByName(question, name) {
        return _.find(question.configuration, function(q) { return q.name == name });
    }

    function defaultAnswer(question) {
        var answer = {};

        if (question && question.configuration) {

            question.configuration.forEach(function(q) {
                if (q.type == 'checkbox') {
                    answer[q.name] = q.options && !!q.options.defaultValue;
                } else if (q.type == 'date') {
                    answer[q.name] = null;
                } else if (q.type == 'list' && q.options) {
                    answer[q.name] = q.options.defaultValue || null;
                } else {
                    answer[q.name] = null;
                }

                if (!answer[q.name] && q.options && q.options.multiple) {
                    answer[q.name] = { values: [], unknownOrder: true };
                }

                //console.log("set default value for " + q.name + " (" + q.type + ")", answer[q.name]);

                // console.log("set default to " + q.name + " = ", answer[q.name]);

                //$scope.initVisibleOptions(q);
            });

        }
        return answer;

    }


    function isVisible(question, conf, answer) {

        if (answer && (conf.visible == 'condition')) {

            var visible = true;
            _.each(conf.visibleOptions, function(opt) {
                if (opt.field) {
                    // Check parent visible
                    if (!isVisible(question, confByName(question, opt.field), answer)) {
                        visible = false;
                    }
                    //console.log("is visible ", question.name, conf.name, answer[opt.field] + " != " + opt.value, answer[opt.field] != opt.value);
                    var testVal = answer[opt.field];
                    if (confByName(question, opt.field).type == 'checkbox') { testVal = !!testVal }
                    if (answer && (testVal != opt.value)) {
                        visible = false;
                    }

                }
            });
            return visible;
        }

        /*if (questionLine.visible == 'condition') {
            console.log("is visible ", questionLine.name, " on field ", questionLine.visibleOptions.field, " ? ",
                questionLine.visibleOptions.value , " = ", $scope.question._answer[questionLine.visibleOptions.field]);
            if (!!questionLine.visibleOptions.field) {
                var parentVisible = $scope.isVisible($scope.confByName(questionLine.visibleOptions.field));
                if (!parentVisible) {
                    return false;
                }
                return $scope.question._answer[questionLine.visibleOptions.field] == questionLine.visibleOptions.value;
            }
            return true;

            //console.log(questionLine.visibleOptions.field, $scope.question._answer[questionLine.visibleOptions.field]);

        }*/

        return true;

        if (conf.visible == 'condition') {
            if (!!conf.visibleOptions.field) {
                var parentVisible = isVisible(question, confByName(question, conf.visibleOptions.field), answer);
                if (!parentVisible) {
                    return false;
                }
                return answer[conf.visibleOptions.field] == conf.visibleOptions.value;
            }
            return true;
            //return (!conf.visibleOptions.field || !!answer[conf.visibleOptions.field] == conf.visibleOptions.value);
        }
        return true;
    }

    function cleanAnswer(question, answer) {

        function cleanAutocomplete(conf, val) {
            // console.log("clean", conf.name, val);
            if (typeof val === 'string') {
                if (conf.options.allowUserCreation) {
                    return { label : val }
                } else {
                    return null;
                }
            }
            return val;
        }

        function toDate(dateString) {
            return guessDate(dateString);

        }

        function typedAnswer(conf, answer) {

            if (!answer) {
                return null;
            }

            if (conf.type == 'reference' && conf.options.display == 'autocomplete') {
                if (!conf.options.multiple) {
                    return cleanAutocomplete(conf, answer[conf.name])
                } else {
                    var cleanValues = !answer[conf.name] ? [] : _.map(answer[conf.name].values, function(val) { return cleanAutocomplete(conf, val) });
                        cleanValues = _.filter(cleanValues, function(val) { return !!val });
                    var unknownOrder = answer[conf.name] && answer[conf.name].unknownOrder;
                    //console.log("Clean values :", cleanValues ," from ", answer[conf.name].values);
                    return {
                        values: cleanValues,
                        unknownOrder: unknownOrder
                    };
                }
            } else if (conf.type == 'geo') {
                if (!answer[conf.name]) {
                    return null;
                }
                return {
                    lat: toDecimal(answer[conf.name].lat, 'lat'),
                    lng: toDecimal(answer[conf.name].lng, 'lng')
                }
            } else if (conf.type == 'date') {
                var d = toDate(answer[conf.name].raw);
                if (d) {
                    return {
                        raw: answer[conf.name].raw,
                        ts: toDate(answer[conf.name].raw)
                    }
                } else {
                    return null;
                }
            } else if (conf.type == 'period') {

                var ans = answer[conf.name];

                //console.log("clean period ", JSON.stringify(ans));

                if (!ans) {
                    return null;
                }
                if (!ans.end || !ans.end.raw) {
                    // 1 date only
                    if (ans.start && ans.start.raw) {
                        var p = guessPeriod(ans.start.raw);
                        if (!p || !p[0] || !p[1]) { return null }
                        ans.start.ts = p[0];
                        ans.end = { ts: p[1]};
                        return ans;
                    } else {
                        return false;
                    }
                } else {
                    if (ans.start && ans.start.raw) {
                        var startTs = guessDate(ans.start.raw),
                            endTs = guessDate(ans.end.raw)
                        if (!startTs && !endTs) { return null }
                        if (!endTs) {
                            ans.end.ts = startTs;
                        } else {
                            ans.end.ts = endTs;
                        }
                        ans.start.ts = startTs;

                        return ans;
                    } else {
                        return false;
                    }
                }


            } else {
                return answer[conf.name];
            }
        }

        var retAnswer = {};
        if (!!question.configuration) {
            question.configuration.forEach(function(conf) {
                if (isVisible(question, conf, answer)) {
                    retAnswer[conf.name] = typedAnswer(conf, answer); //answer[conf.name]
                }
            });
        }
        return retAnswer;
    }


    function validateAnswer(question, answer, rawAnswer) {

        var errors = {};
        _.each(question.configuration, function(conf) {
            var error = isVisible(question, conf, answer) && validateQuestionLine(conf, answer, rawAnswer);
            if (error) {
                //console.log("ERROR", error);
                errors[conf.name] = i18n(error);
            }
        });
        return errors;
    }

    function validDate(dateString) {
        if (!dateString) {
            return false;
        }
        if (dateString.split("/").length != 3) {
            return false;
        }
        return true;
    }

    function validateQuestionLine(questionLine, answer, rawAnswer) {

        var rawAns = rawAnswer[questionLine.name],
            ans = answer[questionLine.name],
            type = questionLine.type;

        // Mandatory
        //console.log("validate", questionLine);
        if (questionLine.mandatory) {
            if (type == 'text' || type == 'list') {
                if (!ans) {
                   return "error.mandatory";
                }
            } else if (type == 'reference') {

                if (!questionLine.options.multiple) {

                    if (!ans || !ans.label) {
                        return "error.mandatory";
                    }
                }

            } else if (type == 'date') {
                if (!rawAns || !rawAns.raw) {
                    return "error.mandatory";
                }
            } else if (type == 'period') {
                if (!rawAns || !rawAns.start || !rawAns.start.raw ) {
                    return "error.mandatory";
                }
            } else if (type == 'geo') {
                if (!rawAnswer[questionLine.name]) {
                    return "error.mandatory";
                } else {
                    if (!(rawAnswer[questionLine.name].lat) || !(rawAnswer[questionLine.name].lng)) {
                        return "error.mandatory";
                    }
                }
            }
        }
        if (questionLine.type == 'date') {
            if (!answer[questionLine.name]) {
                return "error.bad.format";
            }
        }
        if (questionLine.type == 'period') {

            if (!ans || !ans.start || !ans.start.ts || ans.start<ans.end || ans.start.ts>ans.end.ts) {
//            	console.log("this is test", rawAns.start, rawAns.end, ans.start, ans.end, type);
                return "error.bad.period";
            }
        }
        if (questionLine.type == 'geo') {
            if (isNaN(answer[questionLine.name].lat) || isNaN(answer[questionLine.name].lng)) {
                return "error.bad.format";
            }
        }
        return false;
    }

    function findQuestionWithReferenceId(referenceId, questions) {
        var ret = null;
        _.each(questions, function(q) {
            _.each(q.configuration, function(conf) {
                if (conf.type == 'reference' && conf.options.reference == referenceId) {
                    ret = q;
                }
            });
        });
        return ret;
    }

    function findParentReferencesIdInQuestion(references, question) {

        var referencesById = _.indexBy(references, "id");

        var ids = [];

        _.each(question.configuration, function(conf) {
            if (conf.type == 'reference') {
                var parent = referencesById[conf.options.reference].parent;
                if (parent) {
                    ids.push(parent.id);
                };

            }
        });

        return ids;
    }

    function getMissingParentReferencesByQuestionName(references, questions) {
        var missingParentRef = {};
        var referencesById = _.indexBy(references, "id");
        _.each(questions, function(q) {
            var refIds = findParentReferencesIdInQuestion(references, q);
            _.each(refIds, function(refId) {
                var question = findQuestionWithReferenceId(refId, questions);
                if (!question) {
                    missingParentRef[q.name] = referencesById[refId];
                }
            })
        });
        return missingParentRef;
    }

    function buildReferenceParentIndex(references, questions) {


            var referencesById = _.indexBy(references, "id");
            var questionsByRefId = {}
            var parentQuestionByRefId = {}
            var childrenQuestionsQuestionName = {}

            _.each(questions, function(q) {
                _.each(q.configuration, function(conf) {
                    if (conf.type == 'reference') {
                        questionsByRefId[conf.options.reference] = q;
                    }
                });
            });

            _.each(questions, function(q) {
                _.each(q.configuration, function(conf) {
                    if (conf.type == 'reference') {
                        var ref = referencesById[conf.options.reference];
                        if (!!ref.parent) {
                            //console.log("Question " + q.label + " needs parent ref");
                            var parentQuestion = questionsByRefId[ref.parent.id];
                            if (parentQuestion) {
                                //console.log("Found parent question for " + q.label + ": " + parentQuestion.label);
                                var parentConf =_.find(parentQuestion.configuration, function(c) {
                                    return c.type == 'reference' && "" + c.options.reference == "" + ref.parent.id;
                                });
                                parentQuestionByRefId[ref.id] = {
                                    answerName: parentConf.name,
                                    question: parentQuestion
                                };
                                if (!childrenQuestionsQuestionName[parentQuestion.name]) {
                                    childrenQuestionsQuestionName[parentQuestion.name] = [];
                                }
                                childrenQuestionsQuestionName[parentQuestion.name].push(q);
                            } else {
                                //console.log("No parent question for " + q.label + "!");
                            }
                        }
                    }
                })
            });


        return {
            questionsByRefId: questionsByRefId,
            parentQuestionByRefId: parentQuestionByRefId,
            childrenQuestionsQuestionName: childrenQuestionsQuestionName
        }

    }
    //

    this.isVisible = isVisible;

    this.cleanAnswer = cleanAnswer;

    this.validateAnswer = validateAnswer;

    this.buildReferenceParentIndex = buildReferenceParentIndex;

    this.getMissingParentReferencesByQuestionName = getMissingParentReferencesByQuestionName;

    this.defaultAnswer = defaultAnswer;

}]);


herbonautesApp.factory('QuestionTemplates', ['$resource', function($resource) {
    return $resource(herbonautes.ctxPath + '/admin/questions/templates/:id',
        { id: '@id' },
        {
           save: { method: 'POST', url:herbonautes.ctxPath + '/admin/questions/templates', isArray: false },
           saveSort: { method: 'POST', url:herbonautes.ctxPath + '/admin/questions/templates/sort', isArray: true },
            delete: { method: 'POST', url:herbonautes.ctxPath + '/admin/questions/templates/delete', isArray: false },
        }
    );
}]);


herbonautesApp.factory('QuizService', ['$resource', function($resource) {
    return $resource(herbonautes.ctxPath + '/modules/quiz/:id',
        { id: '@id' },
        {
            // current: { method: 'GET', url: '/api/lots/current', isArray: true },
        }
    );
}]);

herbonautesApp.directive('dynamicContent', ['$compile', function ($compile) {

    var linker = function(scope, element, attrs) {

        scope.specimen = scope.context.specimen;
        scope.$watch('template', function() {
            element.html(scope.template);
            $compile(element.contents())(scope);
        });

    }

    return {
        restrict: "E",
        link: linker,
        scope: {
            context:'=',
            template:'='
        }
    };
}]);


herbonautesApp.controller('QuestionsAdminCtrl', ['$scope',
                                                 '$interpolate',
                                                 '$location',
                                                'QuestionTemplates',
                                                'References',
                                                'ReferencesSearch',
                                                'QuizService',
                                                'QuestionUtils',
                                                function($scope, $interpolate, $location,
                                                         QuestionTemplates, References,
                                                         ReferencesSearch, QuizService,
                                                         QuestionUtils) {


    $scope.questions = QuestionTemplates.query();
    $scope.references = References.allReferences();

    $scope.referenceRecords = {}

    $scope.quizList = QuizService.query();

    $scope.isEditable = function() {
        if ($scope.currentQuestion && $scope.currentQuestion.editable) {
            return true;
        }
        return $scope.admin;
    }

    $scope.isDeletable = function() {
        if ($scope.currentQuestion && !$scope.currentQuestion.usedCount) {
            return true;
        }
        return false;
    }

    $scope.admin = false;
    $scope.init = function(admin) {
        $scope.admin = admin;
    }



    $scope.referenceRecordsByQuestion = {};

    $scope.updateReferenceRecords = function(question, conf) {

    }

    $scope.initReferenceRecords = function(question, conf) {
        //console.log("init reference records in admin ctrl ", question.name, conf.name, conf);

        if (!!conf.options && conf.type == 'reference' && conf.options.display != 'autocomplete') {

            var refs = $scope.referenceRecordsByQuestion;

            if (!refs[question.name]) {
                refs[question.name] = {};
            }

            refs[question.name][conf.name] =  $scope.getReferenceRecords(question, conf);
        }
    }

    $scope.getReferenceRecords = function(question, conf) {
        //console.log("get ref records in admin ctrl for ", question.name, conf.name);

        //childrenQuestionsQuestionName[question.name];

        var referenceId = conf.options.reference;

        return References.allRecords({ id: conf.options.reference, sample: true })
    }



    $scope.$on("$locationChangeStart",function(event, next, current) {

        var questionName = $location.search()['q'];

        // console.log("location change q", questionName);

        $scope.questions.$promise.then(function(questions) {
            var q = _.find(questions, function(q) { return questionName == q.name });
            loadQuestion(q);
        });
        //loadSpecimen(newSpecimenId, loadQuestions);
    });

    function loadQuestion(question) {
        console.log("load question admin")
        $scope.currentQuestion = question;
        $scope.currentQuestion._opened = true;
        $scope.answer = {}
        question.configuration.forEach(function(conf) {
            $scope.initReferenceRecords(question, conf);
        });
        $scope.initVisibleOptionFields();
    }

    var specimen = {
        id:1,
        family:"Malvaceae",
        genus:"Bombax",
        specificEpithet: "Spe Epit",
        institute:"MNHN",
        collection:"P",
        code:"P06664353",
        sonneratURL:"http://sonneratphoto.mnhn.fr/2012/07/24/4/P06664353.jpg"
    }


    $scope.specimen = specimen;
    $scope.context = {
        specimen: specimen
    }


    $scope.currentQuestion = {}

    function isEmpty(obj) {
        for(var prop in obj) {
            if(obj.hasOwnProperty(prop))
                return false;
        }

        return true;
    }

    $scope.newQuestion = function() {
        $scope.questions.push({
            name: "q_" + ($scope.questions.length + 1),
            label: "[Nouveau modèle]",
            configuration: [],
            sortIndex: $scope.questions.length,
            editable: true
        });
        $scope.editQuestion($scope.questions[$scope.questions.length - 1]);
    }

    $scope.showEditor = function() {
        return !isEmpty($scope.currentQuestion);
    }

    $scope.isCurrent = function(question) {
        return $scope.currentQuestion && question.name == $scope.currentQuestion.name;
    }

    $scope.questionLabel = function(conf) {
        return $interpolate(conf.label);
    }

    $scope.editQuestion = function(question) {
        //console.log("edit question", question);
        $location.search({q : question.name});
    }


    $scope.isVisible = function(question) {
       // console.log("is visible", question);
        if (question.visible == 'condition') {
            return (!question.visibleOptions.field || !!$scope.answer[question.visibleOptions.field] == question.visibleOptions.value);
        }
        return true;
    }

    $scope.addVisibleOption = function(questionLine) {
        //console.log("add visible option");
        if (!questionLine.visibleOptions) {
            questionLine.visibleOptions = []
        }
        questionLine.visibleOptions.push({
            field: '',
            value: ''
        });
    }

    $scope.addConfiguration = function() {
        $scope.currentQuestion.configuration.push({
            name: 'question_' + ($scope.currentQuestion.configuration.length + 1) ,
            label: '',
            type: 'text'
        });
    }

    $scope.removeConfiguration = function(i) {
        $scope.currentQuestion.configuration.splice(i, 1);
    }



    $scope.removeVisibleOption = function(conf, i) {
        conf.visibleOptions.splice(i, 1);
    }

    $scope.visibleOptions = function(conf) {
       // console.log("visible options", conf);
       // var options = $scope.currentQuestion.configuration.filter(function(q) {
       //     return q.type == 'checkbox' && q.name != conf.name;
       // }).map(function(q) { return { value: q.name, label: q.label } });
       // return options;
        return [{ value: 'no_info', label: 'pas infos'}];
    }

    $scope.confByName = function(name) {
        return _.find($scope.currentQuestion.configuration, function(q) { return q.name == name });
    }

    $scope.visibleOptionFields = [];

    $scope.initOptionFields = function(conf) {
        conf.options = {};
        $scope.initVisibleOptionFields();
    }

    $scope.initVisibleOptionFields = function() {
        //return ["a", "b", "c"];
        //console.log("init visivle options");
        $scope.visibleOptionFields = _.map(_.filter($scope.currentQuestion.configuration, function(c) {
            return c.type == 'checkbox' ||  c.type == 'list';
        }), function(c) {
            return {
                value: c.name,
                label: c.label ? c.label : c.name
            }
        });
        //console.log("visible options : ", $scope.visibleOptionFields);
    }


    /*$scope.initVisibleOptions = function(conf) {
        if (conf.visible == 'condition') {
            if (!conf.visibleOptions) {
                conf.visibleOptions = { field: '', value: false }
            }
            conf.visibleOptions.choice = $scope.currentQuestion.configuration
                                            .filter(function(q) {
                                                var visible = (q.type == 'checkbox' || q.type == 'list') && q.name != conf.name;
                                                console.log("Visible option ", q.type, q.name, conf.name, visible);
                                                return (q.type == 'checkbox' || q.type == 'list') && q.name != conf.name;
                                            })
                                            .map(function(q) {
                                                return { value: q.name, label: q.name, type: q.type }
                                            });
        }
        //console.log(conf);
    };*/



    //$scope.initReferenceRecords = function(conf) {
    //    console.log("init records admin", conf);
    //    if (conf.options && conf.options.reference) {
    //        if (!$scope.referenceRecords[conf.options.reference]) {
    //            $scope.referenceRecords[conf.options.reference] = References.allRecords({ id: conf.options.reference });
    //        }
    //    }
    //};

    $scope.suggestReferenceRecord = function(conf, val) {
        //console.log("search", conf, val);
        return ReferencesSearch.search(conf.options.reference, val, null).then(function(response) {
            return response.data;
        });
    };

    $scope.addChoice = function(conf) {
        if (!conf.options) {
            conf.options = {};
        }
        if (!conf.options.choice) {
            conf.options.choice = [];
        }
        conf.options.choice.push({value: ""});
    };

    $scope.removeChoice = function(conf, index) {
        conf.options.choice.splice(index, 1);
    }

    function defaultAnswer() {
        var answer = {};

       if ($scope.currentQuestion &&
            $scope.currentQuestion.configuration) {

            $scope.currentQuestion.configuration.forEach(function(q) {



                if (q.options) {
                    answer[q.name] = q.options.defaultValue || null;
                } else {
                    if (q.type == 'checkbox') {

                        answer[q.name] = false;
                    } else {
                        answer[q.name] = null;
                    }
                }

                $scope.initVisibleOptions(q);
            });



        }

        return answer;
    };

    $scope.answerHtml = function() {
        var html = "";
        $scope.currentQuestion.configuration.forEach(function(conf) {
            if ($scope.isVisible(conf)) {
               if (conf.type == 'reference') {
                   html += $scope.answer[conf.name];
               } else if (conf.type == 'checkbox') {
                   if (!!$scope.answer[conf.name]) {
                      html += '<span class="label">' + conf.label + '</span>';
                   }
               } else {
                   html += $scope.answer[conf.name];
               }
               html += "<br>";
            }
        });
        return html;
    };

    $scope.answerJson = function() {
        return JSON.stringify($scope.answer);
    };


    $scope.saveQuestion = function() {
        //console.log("save", $scope.currentQuestion);
        QuestionTemplates.save($scope.currentQuestion, function(question) {
            $scope.currentQuestion.id = question.id;
           alert("Question sauvegardée avec succés");
        });
    };


    $scope.deleteQuestion = function() {
       if ($scope.currentQuestion.id) {
           QuestionTemplates.delete($scope.currentQuestion, function() {
               $scope.currentQuestion = {};
               $scope.questions = QuestionTemplates.query();
           });
       } else {
           $scope.currentQuestion = {};
           $scope.questions = QuestionTemplates.query();
       }
    }


    $scope.submitPreview = function(question) {
        //console.log("submit preview")
        $scope.answer = question._answer;
        //console.log("submit", question._answer)

        var cleanAnswer = QuestionUtils.cleanAnswer(question, question._answer);
        //console.log("validate", cleanAnswer);
        var errors = QuestionUtils.validateAnswer(question, cleanAnswer, question._answer);
        //console.log("Validation", errors);

        $scope.currentAnswer = cleanAnswer;

        question._errors = errors;

    };

    $scope.cancelPreview = function(question) {
        //console.log("cancel preview")
        $scope.answer = question._answer;
    };

    $scope.isStatusPreview = function() {
        //console.log("is status preview")
        //$scope.answer = answer;
    };


    // SORTABLE TEMPLATES

    $scope.dragTemplateStart = function(e, ui) {
        ui.item.data('start', ui.item.index());

    };

    $scope.dragTemplateEnd = function(e, ui) {

        var start = ui.item.data('start'),
            end = ui.item.index();

        $scope.questions.splice(end, 0,
            $scope.questions.splice(start, 1)[0]);

        _.each($scope.questions, function(q, i) {
            q.sortIndex = i;
        });

        QuestionTemplates.saveSort($scope.questions, function(question) {

        }, function(error) {
            alert("Impossible de sauvegarder l'ordre")
        });
        $scope.$apply();
    };

    $("#template-list").sortable({
        start: $scope.dragTemplateStart,
        update: $scope.dragTemplateEnd,
        handle: '.sort-handle'
    });

    // SORTABLE QUESTIONS

    $scope.minified = false;

    $scope.minifyAll = function() {
        $scope.minified = true;
    };

    $scope.unminifyAll = function() {
        $scope.minified = false;
    };

    $scope.dragStart = function(e, ui) {
        ui.item.data('start', ui.item.index());

    };

    $scope.dragEnd = function(e, ui) {

        var start = ui.item.data('start'),
            end = ui.item.index();

        $scope.currentQuestion.configuration.splice(end, 0,
            $scope.currentQuestion.configuration.splice(start, 1)[0]);

        $scope.$apply();
    };

    $("#question-configuration").sortable({
        start: $scope.dragStart,
        update: $scope.dragEnd,
        handle: '.sort-handle'
    });

    $scope.$watch('currentQuestion', function() {
        $scope.currentAnswer = QuestionUtils.cleanAnswer($scope.currentQuestion, $scope.currentQuestion._answer);
    }, true);



}]);


herbonautesApp.directive('sortableList', [function() {

    return {
        restrict: 'A',
        link: function($scope, element, attrs) {

            $scope.dragStart = function(e, ui) {
                //console.log("BEFORE : ", $scope.items);
                ui.item.data('start', ui.item.index());

            };

            $scope.dragEnd = function(e, ui) {

                var start = ui.item.data('start'),
                    end =   ui.item.index();



                $scope.items.splice(end, 0, $scope.items.splice(start, 1)[0]);
                $scope.$apply();

                //console.log("AFTER : ", $scope.items);

            };

            $(element).sortable({
                start: $scope.dragStart,
                update: $scope.dragEnd,
                handle: $scope.handle
            });

        },
        scope: {
            items: '=',
            handle: '='
        }
    }


}]);


herbonautesApp.directive('answerBox', ['References', 'QuestionUtils', function(References, QuestionUtils) {

    return {
        restrict: 'E',
        link: function($scope, element, attrs) {

            function cleanAnswer(question, answer) {
                var cleanAnswer = {};
                question.configuration.forEach(function(conf) {
                   if ($scope.isVisible(conf)) {
                       cleanAnswer[conf.name] = answer[conf.name]
                   }
                });
                //var cleanAnswer = QuestionUtils.cleanAnswer(question, answer);
                return cleanAnswer;
            }

            //console.log("Answer box (" + $scope.question.name + ") with specimen", $scope.specimen);

            // Init context
            if (!$scope.context) {
                $scope.context = {
                    specimen: $scope.specimen
                }
            }
            _.each(_.keys($scope.specimen), function(attr) {
                $scope.context.specimen[attr] = $scope.specimen[attr];
            });

            if ($scope.specimen && !$scope.specimen['specificEpithet']) {
                $scope.context.specimen['specificEpithet'] = $scope.specimen['genus']
                $scope.context.specimen['genus'] = $scope.specimen['family']
                $scope.context.specimen['family'] = ''
            }
            // End context

            $scope.answerHtml = function() {
                //console.log("answer");
                return JSON.stringify(cleanAnswer($scope.question, $scope.answer));
            }

            $scope.isVisible = function(questionLine) {
                /*
                if (questionLine.visible == 'condition') {
                    return (!questionLine.visibleOptions.field || ($scope.answer && $scope.answer[questionLine.visibleOptions.field] == questionLine.visibleOptions.value));
                }
                return true;
                */
                return QuestionUtils.isVisible($scope.question, questionLine, $scope.answer);
            }

            $scope.formatPeriod = function(start, end) {
                return formatPeriod(start, end);
            }

            $scope.formatDate = function(d) {
                var date = new Date(d);

                var monthNames = [
                    "Janvier", "Février", "Mars",
                    "Avril", "Mai", "Juin", "Juillet",
                    "Août", "Septembre", "Octobre",
                    "Novembre", "Décembre"
                ];

                var day = date.getUTCDate();
                var monthIndex = date.getUTCMonth();
                var year = date.getUTCFullYear();

                return day + " " + monthNames[monthIndex] + " " + year;
            }


        },
        scope: {
            question: '=',
            answer: '=',
            specimen: '='
        },
        templateUrl: function(elem, attr) {
            return 'templates/answer-dynamic-html.html';
        }
    }
}]);

herbonautesApp.directive('answerBoxJson', ['References', 'QuestionUtils', function(References, QuestionUtils) {

    return {
        restrict: 'E',
        link: function($scope, element, attrs) {

            $scope.answerJson = function() {
                var cleanAnswer = QuestionUtils.cleanAnswer($scope.question, $scope.question._answer);
                return JSON.stringify(cleanAnswer);
            }

            $scope.isVisible = function(questionLine) {
                return QuestionUtils.isVisible($scope.question, questionLine, $scope.question._answer);

                //function isVisible(question, conf, answer) {

                /*
                if (questionLine.visible == 'condition') {
                    return (!questionLine.visibleOptions.field || !!$scope.question._answer[questionLine.visibleOptions.field] == questionLine.visibleOptions.value);
                }
                return true;  */
            }


        },
        scope: {
            question: '='
        },
        templateUrl: function(elem, attr) {
            return 'templates/answer-dynamic-json.html';
        }
    }
}]);


herbonautesApp.directive('questionUnusableBox', ['$modal',  function($modal) {

    return {
        restrict: 'E',
        link: function($scope, element, attrs) {

            $scope.openUnusableModal = function() {
                var modalInstance = $modal.open({
                    animation: false,
                    templateUrl: 'modal-unusable.html',
                    controller: 'ModalUnusableCtrl',
                    size: 'lg',
                    backdrop: true,
                    resolve: {
                        causes: function() {
                            return $scope.causes;
                        }
                    }

                });

                modalInstance.result.then(function(answer) {
                    //console.log("unusable", answer);
                    $scope.question._answer = answer;
                    $scope.onSubmit($scope.question);
                }, function () {
                    console.log('Modal dismissed at: ' + new Date());
                });
            }

            $scope.cancel = function() {
                $scope.onCancel($scope.question);
            }

            $scope.markedUnusable = function() {
                return $scope.question && $scope.question._submitted;
            }

        },
        scope: {
            question: '=',
            onSubmit: '=',
            onCancel: '=',
            context: '=',
            checkStatus: '=',
            user: '=',
            suggest: '=',
            disable: '=',
            causes: '='
        },
        templateUrl: function(elem, attr) {
            return 'templates/question-unusable.html';
        }
    }

}]);

herbonautesApp.directive('questionBox', ['References', 'ReferencesSearch', 'PlayActions', 'QuestionUtils',
    function(References, ReferencesSearch, PlayActions, QuestionUtils) {


    function setDefaultAnswer(question) {
        question._answer = {};
        question._errors = {};

        if (question && question.configuration) {

            question.configuration.forEach(function(q) {
                if (q.options) {
                    question._answer[q.name] = q.options.defaultValue || null;
                } else {
                    if (q.type == 'checkbox') {
                        question._answer[q.name] = false;
                    } else {
                        question._answer[q.name] = null;
                    }
                }

                //$scope.initVisibleOptions(q);
            });

        }

    }



    function initReferenceRecords($scope, conf, q) {

        //if (!!conf.options && conf.type == 'reference' && conf.options.display != 'autocomplete') {
        //    $scope.referenceRecordsByConfName[conf.name] = $scope.getReferenceRecords(q, conf);
        //}

        /*if (!!conf.options && !$scope.referenceRecords[conf.options.reference]) {
            if (conf.type == "reference" && conf.options.display != "autocomplete") {
                // console.log("init records", conf);
                $scope.referenceRecords[conf.options.reference] = References.allRecords({ id: conf.options.reference });
            }
        }*/
    }

    return {
        restrict: 'E',
        link: function($scope, element, attrs) {

            setDefaultAnswer($scope.question);

            console.log("Question box with specimen: ", $scope.specimen);

            //console.log("Set default answer for " + $scope.question.name, $scope.question._answer);

            //$scope.$watch('question', function() {

            //});
            $scope.startQuizAction = function(name) {
                return PlayActions.startQuiz({name: name});
            }

            $scope.clearError = function(confName) {
                $scope.question._errors[confName] = undefined;
            }

            $scope.popoverTemplate = 'templates/question-help.html';

            //$scope.referenceRecords = {};
            $scope.referenceRecordsByConfName = {};


            $scope.onUpdateReferenceRecords = function(conf) {
                //console.log("update records", conf);
                $scope.updateReferenceRecords($scope.question, conf);
                //
                //$scope.initAllReferenceRecords();
            }


            //$scope.initReferenceRecords = function(conf) {
            //    initReferenceRecords($scope, conf, $scope.question);
            //}

            $scope.initAllReferenceRecords = function() {
                $scope.question.configuration.forEach(function(questionLine) {
                    $scope.initReferenceRecords($scope.question, questionLine);
                    //initReferenceRecords($scope, questionLine, $scope.question);
                });
            }


            $scope.formatLabel = function(model) {
                if (!!model && !!model.label) {
                    return model.label;
                }
                return model;
            }

            $scope.initAllReferenceRecords();

            //$scope.question._answer = {};

            $scope.isOpened = function(question) {
                return !!question._opened;
            }

            $scope.toggleQuestion = function(question) {
                question._opened = !question._opened;
            }

            $scope.isSubmitted = function() {
                return !!$scope.question._submitted;
            }
            $scope.submit = function() {
                $scope.onSubmit($scope.question);
                //$scope.question._submitted = true;
            }
            $scope.cancel = function() {
                $scope.onCancel($scope.question);
                //$scope.question._submitted = false;
            },
            $scope.confByName = function(name) {
                return _.find($scope.question.configuration, function(q) { return q.name == name });
            }
            $scope.isVisible = function(questionLine) {
                return QuestionUtils.isVisible($scope.question, questionLine, $scope.question._answer);
                /*
                if (questionLine.visible == 'condition') {
                    //console.log("is visible ", questionLine.name, " on field ", questionLine.visibleOptions.field, " ? ",
                    //    questionLine.visibleOptions.value , " = ", $scope.question._answer[questionLine.visibleOptions.field]);
                    if (!!questionLine.visibleOptions.field) {
                        var parentVisible = $scope.isVisible($scope.confByName(questionLine.visibleOptions.field));
                        if (!parentVisible) {
                            return false;
                        }
                        return $scope.question._answer[questionLine.visibleOptions.field] == questionLine.visibleOptions.value;
                    }
                    return true;

                   //console.log(questionLine.visibleOptions.field, $scope.question._answer[questionLine.visibleOptions.field]);

                }
                return true;
                */
            },


            $scope.warningReferenceInput = function(questionLine, val) {
                if (questionLine.type == "reference" && val && !questionLine.options.allowUserCreation) {
                    return typeof val == "string";
                }
                return false;
            }

            $scope.isStatus = function(status) {
                return $scope.checkStatus(status, $scope.question);
                /*if (status == 'lock') {
                    console.log('is locked ', $scope.question.name, $scope.question.minLevel, $scope.user.level);
                    var locked = $scope.isLocked();
                    console.log("locked ? " + locked);
                    return locked;
                }
                $scope.onSubmit($scope.question);
                return false;  */
            }


            /*$scope.getReferenceRecordsFor = function(conf) {
                console.log("ref records");
                return [
                    {id: 1, label: "Test 1"}
                ]
            }*/

            $scope.suggestReferenceRecord = function(conf, val) {
                //console.log("suggest", referenceId, val);
                if ($scope.suggest) {
                    return $scope.suggest(conf, val);
                }
                var referenceId = conf.options.reference;
                return ReferencesSearch.search(referenceId, val, null).then(function(response) {
                    return response.data;
                });
            }

            $scope.isLocked = function() {
                return !!($scope.user) && $scope.question.minLevel > $scope.user.level;
            }

            $scope.addAnswerItem = function(conf) {
                //console.log("answer item ", $scope.question._answer[conf.name]);
                if (!$scope.question._answer) {
                    $scope.question._answer = {};
                }
                if (!$scope.question._answer[conf.name]) {
                    var defaultUnkwnowOrder = true;
                    if (conf.options.sortable) {
                        defaultUnkwnowOrder = false;
                    }
                    $scope.question._answer[conf.name] = {
                        values: [],
                        unknownOrder: defaultUnkwnowOrder
                    }
                }
                if (!$scope.question._answer[conf.name].values) {
                    $scope.question._answer[conf.name].values = [];
                }

                $scope.question._answer[conf.name].values.push(null);
            }

            $scope.removeAnswerItem = function(conf, i) {
                $scope.question._answer[conf.name].values.splice(i, 1);
            }

            $scope.showEndDate = function(periodAnswer) {
                var show = false;
                if (periodAnswer && periodAnswer.end && periodAnswer.end.raw) {
                    return true;
                }
                if (periodAnswer && periodAnswer.start && periodAnswer.start.raw) {
                    show = bestSplit(periodAnswer.start.raw, SPLITS).length >= 3;
                }
                return show;
            }

            $scope.clickShowConflicts = function() {
                $scope.showConflicts($scope.question);
            }

        },
        scope: {
            question: '=',
            onSubmit: '=',
            onCancel: '=',
            context: '=',
            checkStatus: '=',
            user: '=',
            referenceRecords: '=',
            updateReferenceRecords: '=',
            initReferenceRecords: '=',
            suggest: '=',
            popoverPlacement: '=',
            showMap: '=',
            showConflicts: '=',
            specimen: '='
        },
        templateUrl: function(elem, attr) {
            return 'templates/question-dynamic.html';
        }
    }
}]);

herbonautesApp.controller('ContributionBoard', ['$scope', '$rootScope', '$location', '$modal', '$window', '$cookieStore',
                                                'MissionService', 'ContributionService', 'UserService',
                                                'ReferencesSearch', 'References', 'QuestionUtils',
                                                function($scope, $rootScope, $location, $modal, $window, $cookieStore, MissionService, ContributionService,
                                                         UserService, ReferencesSearch, References, QuestionUtils) {


    // Variables
    var openedQuestions = [];

    $scope.firstLoading = true;
    $scope.questionLoading = true;

    $scope.specimenFamilyUrl = function(specimen) {
        if (!specimen) {
            return "#";
        }
        if (specimen.specificEpithet) {
            return herbonautes.ctxPath + '/specimens?genus=' + specimen.genus + '&specificEpithet=' + specimen.specificEpithet;
        } else {
            return herbonautes.ctxPath + '/specimens?genus=' + specimen.family + '&specificEpithet=' + specimen.genus;

        }
    }

    $scope.specimenUrl = function(specimen) {
        if (!specimen) {
            return "#";
        }
        return herbonautes.ctxPath + '/specimens/' + specimen.institute + '/' + specimen.collection + '/' + specimen.code;
    }

    // Utils

    $scope.emptyQuestionsLeft = function() {
        var unsubmittedQuestions = _.filter($scope.questions, function(q) { return !q._submitted && q.name != 'unusable' });
        var emptyQuestions = _.filter(unsubmittedQuestions, function(q) {
            return !$scope.checkStatus('lock', q);
        });
        return emptyQuestions.length > 0;
    }

    $scope.markedUnusable = function() {
        return $scope.questionUnusable && $scope.questionUnusable._submitted;
    }

    $scope.hasSubmittedAnswer = function() {
        var submittedQuestions = _.filter($scope.questions, function(q) { return q._submitted && q.name != 'unusable' });
        return submittedQuestions.length > 0;
    }

    // INIT
    function openNextQuestion(questions, prevQuestionId) {

        $scope.questionLoading = false;

        _.each(questions, function(q) {
            //console.log("locked " + q.name + " ? " + $scope.checkStatus('lock', q));
            q._opened = (q._submitted || $scope.checkStatus('lock', q));
        });

        var nextQuestions = [];
        var passedPrev = false;
        for (var i = 0 ; i < questions.length ; i++) {
            if (passedPrev) {
                nextQuestions.push(questions[i]);
            }
            if (questions[i].id == prevQuestionId) {
                passedPrev = true;
            }
        }

        // console.log("open first question " + questions)
        var firstNotSubmitted = _.find(nextQuestions, function(q) {

            return !q._submitted && !q._validated && q.name != 'unusable';
        });

        if (firstNotSubmitted) {
            firstNotSubmitted._opened = true;
            // console.log("###", $("#form-" + firstNotSubmitted.name + " input").first());
            setTimeout(function() {
                jQuery("#form-" + firstNotSubmitted.name + " input, #form-" + firstNotSubmitted.name + " select")
                    .first().focus();
            }, 200);

        } else {
            openFirstQuestion(questions);
        }

    }



    function openFirstQuestion(questions) {

        $scope.questionLoading = false;

        _.each(questions, function(q) {
            //console.log("locked " + q.name + " ? " + $scope.checkStatus('lock', q));
            q._opened = (q._submitted || $scope.checkStatus('lock', q));
        });

        // console.log("open first question " + questions)
        var firstNotSubmitted = _.find(questions, function(q) {

            return !q._submitted && !q._validated && q.name != 'unusable';
        });

        if (firstNotSubmitted) {
            firstNotSubmitted._opened = true;
            // console.log("###", $("#form-" + firstNotSubmitted.name + " input").first());
            setTimeout(function() {
                jQuery("#form-" + firstNotSubmitted.name + " input, #form-" + firstNotSubmitted.name + " select")
                    .first().focus();
            }, 200);

        }
        // Open submitted questions
        _.each(_.filter(questions, function(q) { return q._submitted }), function(q) {
            q._opened = true;
            //.get(0).focus();
        });
    }

    function resetAnswers(questions) {
        _.each(questions, function(q) {
            q._opened = false;
            q._submitted = false;
            q._answer = {};
            q._validated = false;
            q._stat = {};
            q._conflicts = false;
            q._loading = true;
            _.each(q.configuration, function(c) {
               if (c.type == 'geo') {
                   c._map = {
                       status: 'hidden'
                   }
               }
            });
        })
    }

    function setAllLoading(val) {
        //console.log("set all loading", val);
        _.each($scope.questions, function(q) {
            q._loading = val;
        })
    }

    function injectAnswers(questions, answers) {
        //setAllLoading(true);




        _.each(questions, function(q) {
            var answer = _.find(answers, function(a) { return a.questionId == q.id });
            if (answer) {
                q._answer = answer.jsonValue;
                q._submitted = true;

                _.each(q.configuration, function(questionLine) {
                    if (questionLine.type == "reference") {
                        $scope.updateReferenceRecords(q, questionLine);
                    }
                })


            }
        });



        var pending = $cookieStore.get("PENDING_QUESTION");
        if (!!pending) {
            var questionToSubmit = _.find(questions, function(q) { return q.id == pending.questionId });
            questionToSubmit._answer = pending.answer;
            //console.log("submit pending", questionToSubmit);
            $scope.submitQuestion(questionToSubmit);
            $cookieStore.remove("PENDING_QUESTION");
        }

        //setAllLoading(false);

        //buildMap(questions, answers);
    }

    $scope.markedUnusableByOthers = false;
    $scope.markedUnusableByOthersCount = 0;
    $scope.markedUsableByOthers = false;

    function injectStats(questions, stats) {

        if (!$scope.user.id) {
            return;
        }

        //setAllLoading(true);
        $scope.markedUnusableByOthers = false;
        $scope.markedUsableByOthers = false;
        // console.log("inject stats", stats)
        _.each(questions, function(q) {
            var stat = _.find(stats, function(a) { return a.question.id == q.id });
            if (stat) {
                //console.log("inject stat", stat, q);
                q._stat = stat
                q._validated = stat.validated;
                q._conflicts = stat.conflicts;

                if (stat.answerCount > 0) {
                    if (q.name == "unusable") {
                        $scope.markedUnusableByOthers = true;
                        $scope.markedUnusableByOthersCount += 1;
                    } else {
                        $scope.markedUsableByOthers = true;
                    }
                }

            }
        });
        //setAllLoading(false);
    }

    var questionsByRefId = {};
    var parentQuestionByRefId = {};
    var childrenQuestionsQuestionName = {};
    var referencesById = {};

    $scope.showMap = function(conf) {
        if (conf._map && conf._map.status == 'hidden') {
            conf._map.status = 'half';
        } else {
            conf._map.status = 'hidden';
        }
    }


    function buildGeoQuestions(questions) {
        var geoQuestions = [];

        _.each(questions, function(q) {
            _.each(q.configuration, function(conf) {
                if (conf.type == 'geo') {
                    conf._map = {
                       status: 'hidden'
                    }
                    geoQuestions.push({
                        question: q,
                        conf: conf
                    });
                }
            });
        });

        return geoQuestions;
    }

    function buildReferenceParentIndex(questions) {
        References.allReferences(function(references) {
            $scope.references = references;
            referencesById = _.indexBy(references, "id");
            //console.log(referencesById);

            _.each(questions, function(q) {
                _.each(q.configuration, function(conf) {
                    if (conf.type == 'reference') {
                        questionsByRefId[conf.options.reference] = q;
                    }
                });
            });

            _.each(questions, function(q) {
                _.each(q.configuration, function(conf) {
                    if (conf.type == 'reference') {
                       var ref = referencesById[conf.options.reference];
                        if (!!ref.parent) {
                            //console.log("Question " + q.label + " needs parent ref");
                            var parentQuestion = questionsByRefId[ref.parent.id];
                            if (parentQuestion) {
                                //console.log("Found parent question for " + q.label + ": " + parentQuestion.label);
                                var parentConf =_.find(parentQuestion.configuration, function(c) {
                                   return c.type == 'reference' && "" + c.options.reference == "" + ref.parent.id;
                                });
                                parentQuestionByRefId[ref.id] = {
                                    answerName: parentConf.name,
                                    question: parentQuestion
                                };
                                if (!childrenQuestionsQuestionName[parentQuestion.name]) {
                                    childrenQuestionsQuestionName[parentQuestion.name] = [];
                                }
                                childrenQuestionsQuestionName[parentQuestion.name].push(q);
                            } else {
                                //console.log("No parent question for " + q.label + "!");
                            }
                        }
                    }
                })
            });
        });
    }

    var referenceLinks = {};

    function initAnswers() {

        //console.log("init answers");
        _.each($scope.questions, function(q) {
            q._answer = QuestionUtils.defaultAnswer(q);
        });

        ContributionService.getUserAnswers($scope.missionId, $scope.specimenId).then(function(response) {
            // Merge answers
            // console.log("get user answers");
            $scope.questions.$promise.then(function(questions) {
                var answers = response.data;
                $scope.questionUnusable = _.find(questions, function(q) { return q.name == 'unusable' });
                //console.log("unusable =", $scope.questionUnusable);
                $scope.causesUnusable = $scope.questionUnusable.configuration[0].options.choice;

                //console.log("causes", $scope.causesUnusable);

                injectAnswers(questions, answers);
                openFirstQuestion($scope.questions);
            });
        });
        ContributionService.getStats($scope.missionId, $scope.specimenId).then(function(response) {
            // Merge answers
            // console.log("get user stats");
            $scope.questions.$promise.then(function(questions) {
                var stats = response.data;
                //console.log(stats);
                injectStats(questions, stats);
                openFirstQuestion($scope.questions);
                // Open conflict box if search
                var openConflicts = $location.search()["conflicts"];
                if (openConflicts) {
                    var conflictQuestion = _.find(questions, function(q) {
                        return q.name == openConflicts;
                    });
                    if (conflictQuestion) {
                        $scope.showConflicts(conflictQuestion);
                    }
                    //alert("Open conflicts " + openConflicts);
                }
            });
        });
    }

    //alert("Path : " + );
    function getSpecimenIdFromLocation(location) {
        var paths = location.split('/');
        var lastPath = paths[paths.length - 1].split('?')[0].split('#')[0];
        //console.log("Lastpath : " + lastPath)
        return lastPath;
    }

    function setSpecimenIdInLocation(newId) {
        var paths = $location.path().split('/');
        var specimenId = getSpecimenIdFromLocation($location.path());
        paths[paths.length - 1] = newId;

        $location.path(paths.join('/'));
        $location.search('conflicts', null);
    }


    var firstLoad = true;
    $scope.$on("$locationChangeStart",function(event, next, current){
        var locationNotValid = false;
        if (locationNotValid) {
            e.preventDefault();
        }
        var newSpecimenId = getSpecimenIdFromLocation(next);
        if (newSpecimenId == "random") {

            //console.log("Next specimen");
            if (firstLoad) {
                $scope.nextSpecimen();
            } else {
                // Back
                $window.history.back();
            }

            /*ContributionService.getRandomSpecimen($scope.missionId).then(function(resp) {

                if (resp.data) {
                    setSpecimenIdInLocation(resp.data.id);
                } else {
                    alert("plus de specimen pour vous");
                }

            });*/


        } else if (firstLoad || newSpecimenId != $scope.specimenId) {
            firstLoad = false;
            loadSpecimen(newSpecimenId, loadQuestions);
        }
    });


    function loadQuestions() {
         if (!$scope.questions || $scope.questions.length == 0) {
             $scope.questions = MissionService.questions({id: $scope.missionId}, function(questions) {

                 referenceLinks = buildReferenceParentIndex($scope.questions);

                 $scope.geoQuestions = buildGeoQuestions($scope.questions);

                 initAnswers();

             });
         } else {
             initAnswers();
         }
    }

    function initSpecimenBoard() {
        console.log("init specimen board");
        initializeSpecimenMap(
                $scope.tilesRootURL,
                $scope.specimen.institute,
                $scope.specimen.collection,
                $scope.specimen.code,
                $scope.specimen.tw,
                $scope.specimen.th,
                $scope.defaultZoom);
    }

    function loadSpecimen(specimenId, callback) {

        setAllLoading(true);
        resetAnswers($scope.questions);

        ContributionService.markSeen($scope.missionId, specimenId).then(function() {
            nextRandom = ContributionService.getRandomSpecimen($scope.missionId);
        });

        $scope.specimenId = specimenId;

        ContributionService.getSpecimen($scope.missionId, $scope.specimenId).then(function(response) {
            $scope.specimen = response.data;

            if (!$scope.context) {
                $scope.context = {
                    specimen: $scope.specimen
                }
            }
            _.each(_.keys($scope.specimen), function(attr) {
                $scope.context.specimen[attr] = $scope.specimen[attr];
            });

            if (!$scope.specimen['specificEpithet']) {
                $scope.context.specimen['specificEpithet'] = $scope.specimen['genus']
                $scope.context.specimen['genus'] = $scope.specimen['family']
                $scope.context.specimen['family'] = ''
            }

            if (callback) {
                callback();
                setAllLoading(false);
            }

            //console.log("SPECIMEN", $scope.specimen);
            //$scope.tilesRootURL;

            console.log("call init specimen board")
            initSpecimenBoard();

            //setAllLoading(false);

            $rootScope.$emit('reloadDiscussions', $scope.specimen.code);
            $rootScope.$emit('reloadSpecimenTags', $scope.specimen.id, $scope.specimen.code);

            $scope.firstLoading = false;

        }, function (error) {
            if (error.status = 404) {
                setAllLoading(false);

                openNoSpecimenModal($scope.mission, $scope.user);
                //alert("[TODO modal] Plus de spécimen pour vous, choisissez une autre mission svp.")
            }
        });

        //loadQuestions();
    }


    $scope.hasUserPassedQuiz = function(quizId) {
        return $scope.passedQuizList && _.find($scope.passedQuizList, function(q) {
            return quizId == q.quizId;
        });
    }

    var nextRandom;

    $scope.init = function(missionId, tilesRoot, defaultZoom) {

        //console.log(tilesRoot);

        $scope.user = UserService.current();
        $scope.passedQuizList = UserService.passedQuiz();
        $scope.tilesRootURL = tilesRoot;
        $scope.defaultZoom =  defaultZoom;

        $scope.missionId = missionId;

        nextRandom = ContributionService.getRandomSpecimen($scope.missionId);

        //loadSpecimen(specimenId);

    }


    $scope.resetReferenceRecords = function() {
        _.each(childrenQuestionsQuestionName, function(v, k) {
            _.each(v, function(q) {
                _.each(q.configuration, function(conf) {
                    //console.log(conf)
                    if (conf.type == 'reference' && conf.options.display == 'combobox') {
                        $scope.referenceRecordsByQuestion[q.name][conf.name] = [];
                    }
                });
            });
        });
    }

    $scope.nextSpecimen = function() {
        var specimenId = 1 + $scope.specimenId;

        //console.log("NEXT", nextRandom);

        nextRandom.then(function(resp) {

            //console.log("GOT NEXT", resp.data)
            if (resp.data && resp.data.id != $scope.specimenId) {
                setSpecimenIdInLocation(resp.data.id);
                $scope.resetReferenceRecords();
            } else {
                //alert("plus de specimen pour vous");

                openNoSpecimenModal($scope.mission, $scope.user);
            }

        });
        //setSpecimenIdInLocation(specimenId);
    }


    // GEOLOCALISATION
    /*$scope.geolocalisationMap = {}
    function buildMap(questions, answers) {
        //console.log("build map", questions, answers);
        var markers = _.map(questions, function(q) {
            var geoQuestions = _.filter(q.configuration, function(c) { return c.type == 'geo' });
            //console.log("build map with geo", geoQuestions);
            if (geoQuestions.length > 0) {
                //console.log("build map with ", geoQuestions);
            }
            var geoAnswers = _.map(geoQuestions, function(c) {
                var a = _.find(answers, function(a) { return a.questionId == q.id });
                //console.log("map a", a, c.name);
                if (a) {
                    var geo = a.jsonValue[c.name];
                    return { position: geo, questionId: q.id, confName: c.name, editable: true };
                } else {
                    return { noPosition: true, questionId: q.id, confName: c.name };
                }
            });
            //console.log("GEO ANSWERS", geoAnswers);
            return geoAnswers;
        });
        $scope.geolocalisationMap = {
            markers: _.flatten(markers)
        }
    } */


    /*function updateMarkers(question, answer) {
       _.each($scope.geolocalisationMap.markers, function(marker) {

           if (marker.questionId == question.id) {

               var conf = _.find(question.configuration, function(c) {
                   return c.name = marker.confName;
               });

               if (conf) {
                   marker.position = answer.jsonValue[conf.name];
                   marker.noPosition = false;
                   marker.editable = !question._submitted;
               }

           }

       });
    }*/

    // AFFICHAGE

    $scope.unusableOver = false;
    $scope.mouseoverUnusable = function() {
        $scope.unusableOver =  true;
    }
    $scope.mouseoutUnusable = function() {
        $scope.unusableOver =  false;
    }

    $scope.isOpened = function(question) {
        return !!question._opened;
    }

    $scope.toggleQuestion = function(question) {
        question._opened = !question._opened;
    }


    $scope.referenceRecordsByQuestion = {};

    $scope.updateReferenceRecords = function(question, conf) {
        //console.log("update reference records from", question.name, conf.name);

        var children = childrenQuestionsQuestionName[question.name]

        //console.log("update ref records for ", children);
        _.each(children, function(q) {
            _.each(q.configuration, function(c) {
                $scope.initReferenceRecords(q, c);
            })
        });


        //var refs  = $scope.referenceRecordsByQuestion;
        //
        //if (!refs[question.name]) {
        //    refs[question.name] = {};
        //}
        //
        //refs[question.name][conf.name] = $scope.getReferenceRecords(question, conf);

    }

    $scope.initReferenceRecords = function(question, conf) {
        //console.log("init reference records in ctrl ", question.name, conf.name, conf);

        if (!!conf.options && conf.type == 'reference' && conf.options.display != 'autocomplete') {

            var refs = $scope.referenceRecordsByQuestion;

            if (!refs[question.name]) {
                refs[question.name] = {};
            }

            refs[question.name][conf.name] =  $scope.getReferenceRecords(question, conf);
        }
    }

    $scope.getReferenceRecords = function(question, conf) {
        //console.log("get ref records in ctrl for ", question.name, conf.name);

        //childrenQuestionsQuestionName[question.name];

        var referenceId = conf.options.reference;
        var parentRecordId = null;
        var parentQuestion = parentQuestionByRefId[referenceId];
        if (parentQuestion) {
            var parentAnswer = parentQuestion.question._answer[parentQuestion.answerName];
            if (parentAnswer) {
                //console.log("Parent answer ", parentAnswer);
                parentRecordId = parentAnswer.id;

            } else {
                //parentAnswer
                //console.log("No parent answer");
                return [];
                //parentRecordId = conf.options.defaultParentRef.id;
                //suggest needs parent", parentQuestion);
            }

        } else {

            // Pas de question parent
            if (conf.options.defaultParentRef) {
                parentRecordId = conf.options.defaultParentRef.id;
            }
        }


        return References.allRecords({ id: conf.options.reference, parent: parentRecordId })
    }


    $scope.suggestReferenceRecord = function(conf, val)
    {
        //console.log("suggest in board", conf, val);

        var referenceId = conf.options.reference;
        var parentRecordId = null;
        var parentQuestion = parentQuestionByRefId[referenceId];
        if (parentQuestion) {
            var parentAnswer = parentQuestion.question._answer[parentQuestion.answerName];
            if (parentAnswer) {
               //console.log("Parent answer ", parentAnswer);
               parentRecordId = parentAnswer.id;
            } else {
               //parentAnswer
               //console.log("No parent answer");
               //parentRecordId = conf.options.defaultParentRef.id;
               //suggest needs parent", parentQuestion);
            }

        } else {
            if (conf.options.defaultParentRef) {
                parentRecordId = conf.options.defaultParentRef.id;
            }
        }

        return ReferencesSearch.search(referenceId, val, parentRecordId).then(function(response) {
            return response.data;
        });
    }


    $scope.checkStatus = function(status, question) {
        //console.log(status, question);
        if (status == 'lock') {


            if (!$scope.user.id) {
               return question.minLevel > 1;
            }

            /*console.log("Is question '" + question.label +
                "' [level:" + question.minLevel +
                "] locked for user [level:" + $scope.user.level + "] ?");*/
            //console.log('lock ' + question.label + ' : ' + (question.minLevel > $scope.user.level));
            if (question.minLevel > $scope.user.level) {
                // console.log("Lock level pour " + question.label, question);
                return true;
            }

            //console.log("quiz requis ", question.name, question.neededQuiz);
            if (question.neededQuiz && !$scope.hasUserPassedQuiz(question.neededQuiz.id)) {
                // console.log("Lock quiz pour " + question.label, question);
                return true;
            }

            return false;
        } else if (status == 'done') {
            return question._submitted;
        } else if (status == 'complete') {
            return question._validated;
        } else if (status == 'conflicts') {
            return question._conflicts;
        }
        return true;
    }

    // CONTRIBUTION

    $scope.submitUnusable = function(question) {
        //console.log("Submit unusable", question);
        $scope.submitQuestion(question);
    }

    $scope.cancelUnusable = function(question) {
        //console.log("Cancel unusable", question);

        $scope.cancelQuestion(question);
        $scope.markedUnusableByOthersCount--;
        if ($scope.markedUnusableByOthersCount  == 0) {
            $scope.markedUnusableByOthers = false;
        }
    }

    $scope.showConflicts = function(question) {
        //console.log("Show conflicts " + question.label);
        ContributionService.answerReport($scope.missionId, $scope.specimenId, question.id).then(function(response) {
            if (response.data.stat.conflicts) {
                openConflictsModal($scope.specimen, question, response.data, function(answer) {
                    question._answer = answer;
                    $scope.submitQuestion(question, { noConflicts: true });


                    //
                    if (childrenQuestionsQuestionName[question.name]) {
                        _.each(childrenQuestionsQuestionName[question.name], function(q) {
                            //console.log("Cancel " + q.label + " (depends on " + question.label + ")");
                            $scope.cancelQuestion(q);
                        });
                    }
                });
            }
        });
    }

    $scope.submitQuestion = function(question, submitOptions) {
        var options = submitOptions || {};


        // Check markedUnusableByOthers markedUsableByOthers

        if (!options.forceUnusable) {
            if (question.name == "unusable" && $scope.markedUsableByOthers) {
                openConflictsUsableModal(question, function (answer) {

                    //console.log("submit", answer);

                    // Resubmit
                    options.forceUnusable = true;
                    $scope.submitQuestion(question, options);



                    //question._answer = answer;
                    //$scope.submitQuestion(question, { noConflicts: true });
                });
                return;
            } else if (question.name != "unusable" && $scope.markedUnusableByOthers) {
                openConflictsUnusableModal(question, function (answer) {
                    //question._answer = answer;
                    //$scope.submitQuestion(question, { noConflicts: true });

                    options.forceUnusable = true;
                    $scope.submitQuestion(question, options);

                });
                return
            }
        }

        question._loading = true;

        var cleanAnswer = QuestionUtils.cleanAnswer(question, question._answer);
        var errors = QuestionUtils.validateAnswer(question, cleanAnswer, question._answer);

        if (!_.isEmpty(errors)) {
            question._errors = errors;
            question._loading = false;
            return;
        }

        //console.log("submit clean", cleanAnswer);
        $scope.pendingQuestion = {
            missionId: $scope.missionId,
            specimenId: $scope.specimenId,
            questionId: question.id,
            answer: cleanAnswer
        };

        ContributionService.submitAnswer($scope.missionId, $scope.specimenId, question.id, cleanAnswer, options).then(
            function(response) {

                $cookieStore.remove('PENDING_QUESTION');

                //console.log("REPORT : ", response.data);

                if (response.data.attributes.newLevel) {
                    var newLevel = +response.data.attributes.newLevel;
                    //alert("New level " + newLevel);
                    openLevelModal($scope.questions, newLevel);
                    $scope.user.level = newLevel;
                }

                // Ticket 272 Affichage conflit
                // if (response.data.stat.conflicts && !options.noConflicts) {
                if (response.data.conflictAnswers.length > 0 && !options.noConflicts) {
                    openConflictsModal($scope.specimen, question, response.data, function(answer) {
                        question._answer = answer;
                        $scope.submitQuestion(question, { noConflicts: true });





                    }, function() {

                        ContributionService.keepAnswer($scope.missionId, $scope.specimenId, question.id).then(function() {

                        });
                    });
                }

                // force update references
                //console.log("reload references");
                _.each(question.configuration, function(conf) {
                    $scope.updateReferenceRecords(question, conf);
                });


                question._stat = response.data.stat;
                question._validated = response.data.stat.validated;
                question._conflicts = response.data.stat.conflicts;
                question._submitted = true;
                question._loading = false;
                question._answer = cleanAnswer;




                //openFirstQuestion($scope.questions);

                openNextQuestion($scope.questions, question.id);


                //updateMarkers(question, response.data.answer);

            },
            function(error) {
                question._loading = false;
                if (error.status == 403) {

                    if (error.data == "not.connected") {

                        //console.log("Save", $scope.pendingQuestion);

                        $cookieStore.put('PENDING_QUESTION', $scope.pendingQuestion);

                        $scope.openConnectionModal();

                    } else if (error.data == "not.member") {
                        $scope.openJoinMissionModal(function() {
                            $scope.submitQuestion(question, { noConflicts: false });
                        });
                    }

                }
                //console.log("Error on submit", error);

            }
        );

    }

    $scope.cancelQuestion = function(question) {
        if (!question._submitted) {
            return;
        }
        //console.log("cancel in controller", question);
        question._loading = true;
        ContributionService.cancelAnswer($scope.missionId, $scope.specimenId, question.id).then(function(response) {
            question._stat = response.data.stat;
            question._validated = response.data.stat.validated;
            question._conflicts = response.data.stat.conflicts;
            question._submitted = false;
            // question._answer = {}
            question._loading = false;
        });

        if (childrenQuestionsQuestionName[question.name]) {
            _.each(childrenQuestionsQuestionName[question.name], function(q) {
              //console.log("Cancel " + q.label + " (depends on " + question.label + ")");
                $scope.cancelQuestion(q);
            });
        }

    }

    $scope.openConnectionModal = function() {
        var modalInstance = $modal.open({
            animation: false,
            templateUrl: 'modal-connection.html',
            controller: 'ModalConnectionCtrl',
            size: 'lg',
            backdrop: true
        });

        modalInstance.result.then(function(answer) {
            //console.log("unusable", answer);

        }, function () {
            //console.log('Modal dismissed at: ' + new Date());
        });
    }



    $scope.openJoinMissionModal = function(callback) {
        var modalInstance = $modal.open({
            animation: false,
            templateUrl: 'modal-join-mission.html',
            controller: 'ModalJoinMissionCtrl',
            size: 'lg',
            backdrop: true
        });

        modalInstance.result.then(function() {
            //console.log("join");
            MissionService.joinMission({ id: $scope.missionId, ajax: true }, function() {
                if (callback) {
                    callback();
                }
            });
        }, function () {
            //console.log('Modal dismissed at: ' + new Date());
        });
    }



    function openNoSpecimenModal(mission, user) {
        var modalInstance = $modal.open({
            animate: false,
            templateUrl: 'modal-no-specimen.html',
            controller: 'ModalNoSpecimenCtrl',
            size: 'lg',
            backdrop: true,
            resolve: {
                mission: function() {
                    return mission;
                },
                user: function() {
                    return user;
                }
            }

        });

        modalInstance.result.then(function() {
            //console.log("Copy answer");
        }, function () {
            //console.log("Keep mine");
        });
    }

    function openConflictsModal(specimen, question, stats, callback, onKeep) {
        //console.log("Open conflicts popup", specimen);
        var modalInstance = $modal.open({
            animate: false,
            templateUrl: 'modal-conflicts.html',
            controller: 'ModalConflictsCtrl',
            size: 'lg',
            backdrop: true,
            resolve: {
                question: function() {
                    return question;
                },
                report: function () {
                    return stats;
                },
                specimen: function() {
                    return specimen;
                }
            }

        });

        modalInstance.result.then(function(answerId) {
            //console.log("Copy answer", answerId);
            //alert("Copy answer");
            callback(answerId);
        }, function () {
            //console.log("Keep mine");
           // alert("Keep answer");
            if(onKeep) onKeep();
        });
    }

     function openConflictsUnusableModal(question, callback) {
         var modalInstance = $modal.open({
             animate: false,
             templateUrl: 'modal-conflicts-unusable.html',
             controller: 'ModalConflictsUnusableCtrl',
             size: 'lg',
             backdrop: true,
             resolve: {
                 question: function() {
                     return question;
                 }
             }

         });

         modalInstance.result.then(function(answerId) {
             //console.log("Copy answer", answerId);
             //callback(answerId);
         }, function () {
             //console.log("Keep mine");
             callback();
         });
     }

function openConflictsUsableModal(question, callback) {
    var modalInstance = $modal.open({
        animate: false,
        templateUrl: 'modal-conflicts-usable.html',
        controller: 'ModalConflictsUsableCtrl',
        size: 'lg',
        backdrop: true,
        resolve: {
            question: function() {
                return question;
            }
        }

    });

    modalInstance.result.then(function(answerId) {
        //console.log("Copy answer", answerId);
        //callback(answerId);
    }, function () {
        //console.log("Keep mine");
        callback();
    });
}

function openLevelModal(questions, level) {
    var modalInstance = $modal.open({
        animate: false,
        templateUrl: 'modal-level.html',
        controller: 'ModalLevelCtrl',
        size: 'lg',
        backdrop: true,
        resolve: {
            questions: function() {
                return questions;
            },
            level: function () {
                return level;
            }
        }

    });

    modalInstance.result.then(function(answerId) {
    }, function () {
    });
}

    // GOOGLE MAPS

    // $scope.mapStyle = 'full';
    // $scope.checkMapStyle = function(s) { return s == $scope.mapStyle }



}]);

/*
herbonautesApp.controller('ReferencesListCtrl', ['$scope', '$modal', '$window', 'ReferencesAdmin',
    function($scope, $modal, $window, ReferencesAdmin) {

        $scope.references = ReferencesAdmin.allReferences();

        $scope.toAdminEdit = function(ref) {
            //console.log("toAdminEdit", ref);
            $window.location = toReferenceAdminEdit({ id: ref.id });
        }

        $scope.open = function (size) {

            var modalInstance = $modal.open({
                animation: $scope.animationsEnabled,
                templateUrl: 'new-reference-modal.html',
                controller: 'ModalNewReferenceCtrl',
                size: size
            });

            modalInstance.result.then(function (ref) {
                //console.log("save ref", ref);
                ReferencesAdmin.saveReference(null, ref, function(ref) {
                    $scope.references.push(ref);
                });
            }, function () {
                //console.log('Modal dismissed at: ' + new Date());
            });
        };

    }]);

*/

herbonautesApp.controller('ModalUnusableCtrl', ['$scope', '$modalInstance', 'causes', function ($scope, $modalInstance, causes) {

    $scope.answer = {}
    $scope.causes = causes;

    $scope.submit = function() {
        $modalInstance.close($scope.answer);
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

}]);

herbonautesApp.controller('ModalConnectionCtrl', ['$scope', '$modalInstance', function ($scope, $modalInstance) {

    $scope.connect = function() {
        $modalInstance.close("connect");
    };

    $scope.register = function() {
        $modalInstance.close("register");
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

}]);

herbonautesApp.controller('ModalLevelCtrl', ['$scope', '$modalInstance', 'questions', 'level',
                        function ($scope, $modalInstance, questions, level) {

    $scope.questions = questions;
    $scope.level = level;

    $scope.ok = function() {
        $modalInstance.close("ok");
    };


}]);

herbonautesApp.controller('ModalJoinMissionCtrl', ['$scope', '$modalInstance', function ($scope, $modalInstance) {

    $scope.join = function() {
        $modalInstance.close("join");
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

}]);


herbonautesApp.controller('ModalNoSpecimenCtrl', ['$scope', '$modalInstance', 'mission', 'user', function ($scope, $modalInstance, mission, user) {

    $scope.mission = mission;
    $scope.user = user;

    $scope.cancel = function() {
        $modalInstance.dismiss();
    };

    $scope.ok = function() {
        $modalInstance.close();
    };

}]);

herbonautesApp.controller('ModalConflictsUnusableCtrl', ['$scope', '$modalInstance', 'question', function($scope, $modalInstance, question) {
    $scope.keep = function() {
        $modalInstance.dismiss("keep");
    };

    $scope.cancel = function() {
        $modalInstance.close();
    };
}]);

herbonautesApp.controller('ModalConflictsUsableCtrl', ['$scope', '$modalInstance', 'question', function($scope, $modalInstance, question) {
    $scope.keep = function() {
        $modalInstance.dismiss("keep");
    };

    $scope.cancel = function() {
        $modalInstance.close();
    };
}]);

herbonautesApp.controller('ModalConflictsCtrl', ['$scope', '$modalInstance', 'specimen', 'question', 'report', function ($scope, $modalInstance, specimen, question, report) {

    $scope.report = report;

    //console.log(report);

    function copyQuestion(question, answer) {
        var q = _.pick(question, "configuration");
        q._answer = answer.jsonValue;
        q.userId = answer.userId;
        q.userLogin = answer.userLogin;
        return q;
    }

    $scope.userImageUrl = function(ans) {
        return herbonautes.ctxPath + "/img/users/" + ans.userId + ".jpg";
    }

    $scope.userProfileUrl = function(ans) {
        return herbonautes.ctxPath + "/users/" + ans.userLogin;
    }

    //
    $scope.userQuestion = copyQuestion(question, report.answer);
    $scope.userQuestion.userId = report.answer.userId;
    $scope.userQuestion._answer = report.answer.jsonValue;
    $scope.specimen = specimen;
   // console.log("USER QUESTION", $scope.userQuestion);

    // Build conflicts
    $scope.conflicts = [];
    _.each(report.conflictAnswers, function(answer) {
        $scope.conflicts.push(copyQuestion(question, answer));

    });
    $scope.noConflicts = [];
    _.each(report.noConflictAnswers, function(answer) {
        $scope.noConflicts.push(copyQuestion(question, answer));

    });

    //console.log("CONFLICTS", $scope.conflicts);


    $scope.keep = function() {
        $modalInstance.dismiss("keep");
    };

    $scope.copyAnswer = function(answer) {
        $modalInstance.close(answer);
    };

}]);

function toDecimal(val, type) {

    if (!val) {
        return 0;
    }

    if (typeof val != "string") {
        return val;
    }

    var splits = val.split(/[^\d\.]/);
    var decimal = 0;
    var index = 0;
    var neg = 1;
    for (var i = 0, l = splits.length ; i < l ; i++) {
        if (!splits[i]) continue;
        decimal += parseFloat(splits[i]) / Math.pow(60, index);
        index++;
        if (index > 2) break;
    }

    if (val.indexOf('S') >= 0 ||
        val.indexOf('s') >= 0 ||
        val.indexOf('W') >= 0 ||
        val.indexOf('w') >= 0 ||
        val.indexOf('O') >= 0 ||
        val.indexOf('o') >= 0) {
        neg = -neg;
    }

    if (val.indexOf('-') >= 0) {
        neg = -neg;
    }

    return decimal * neg;
}

herbonautesApp.directive('geolocalisationMap', ['QuestionUtils', '$http', '$timeout', function(QuestionUtils, $http, $timeout) {

    var map,
        marker,
        autocomplete;


    function initSuggestionInput($scope) {

        $scope.selectedIndex = -1;

        $scope.onQueryBlur = function() {
            $timeout(function() {
                $scope.showSuggestions = false;
            }, 1000)

            return true;
        }

        $scope.onQueryKeyPress = function($event) {
            if ($event.keyCode == 27) {
                $event.target.blur();
                return;
            }

            if ($event.keyCode == 38) {
                // up
                $scope.selectedIndex = ($scope.selectedIndex - 1) % $scope.geolocationSuggestions.length;
                return;
            }

            if ($event.keyCode == 40) {
                // down
                $scope.selectedIndex = ($scope.selectedIndex + 1) % $scope.geolocationSuggestions.length;
                return;
            }


            if ($event.keyCode == 13 && $scope.selectedIndex > -1) {
                var suggestion = $scope.geolocationSuggestions[$scope.selectedIndex];
                $scope.placeSuggestionMarker(suggestion);
                return;
            }
        }

        $scope.geolocationSuggestions = [
        ];


        $scope.placeSuggestionMarker = function(suggestion) {

            console.log("Place marker");

            var lat = +suggestion.lat,
                lon = +suggestion.lon;


            $scope.selectedSuggestion = suggestion.display_name;
            $scope.geolocationQuery = suggestion.display_name;


            //if (place.geometry.viewport) {
            //    map.fitBounds(place.geometry.viewport);
            //} else {
            //    map.setCenter(place.geometry.location);
            //    map.setZoom(15);
            //}

            if (suggestion.boundingbox) {
                var bbox = suggestion.boundingbox;
                var bboxLeaflet = [[+bbox[0],+bbox[2]], [+bbox[1],+bbox[3]]];
                map.fitBounds(bboxLeaflet);

            } else {
                map.setView(new L.LatLng(lat, lon), 15);
            }



            $scope.showSuggestions = false;

//
            ////console.log("Place changed");
//
            //// marker.setPosition(place.geometry.location);
//
            if (!$scope.question._submitted) {
                $scope.question._answer.position = {
                    lat: lat,
                    lng: lon
                }
                //$scope.$digest();
            }

        }

        /* $scope.searchSuggestion = function(q) {

            $scope.geolocationSuggestions = [
                { name: ">> " + q + " <<", lat: 35.0, lon: 0 }
            ]

        } */

        $scope.searching = false;

        function searchSuggestions() {

            if ($scope.searching) {
                return;
            }

            if ($scope.selectedSuggestion == $scope.geolocationQuery) {
                // cancel first search after marker placed
                $scope.selectedSuggestion = "";
                return;
            }

            var q = $scope.geolocationQuery;

            $scope.searching = true;

            $http.get('https://nominatim.openstreetmap.org/search?format=json&q=' + q).then(function(response) {

                $scope.geolocationSuggestions = response.data;
                $scope.showSuggestions = true;
                $scope.searching = false;

                $scope.selectedIndex = -1;
            });



            //$scope.$apply(function() {
            //    $scope.geolocationSuggestions = [
            //        { name: ">> " + q + " <<", lat: 35.0, lon: 0 }
            //    ]
            //})
        }

        $scope.$watch('geolocationQuery', _.debounce(searchSuggestions, 1000));

    }



    function resetInput($scope, element) {
        $scope.$apply(function() {
            $scope.geolocationQuery = "";
            $scope.geolocationSuggestions = [];
            $scope.showSuggestions = false;
        });
    }




    function resetInput_google($scope, element) {
        //console.log("KEYWORDS ", keywordElement);
        var keywordElement = element.find("input")[0];
        keywordElement.value = "";
        autocomplete = new google.maps.places.Autocomplete(keywordElement);
        autocomplete.bindTo('bounds', map);

        google.maps.event.addListener(autocomplete, 'place_changed', function() {
            var place = autocomplete.getPlace();

            if (place.geometry.viewport) {
                map.fitBounds(place.geometry.viewport);
            } else {
                map.setCenter(place.geometry.location);
                map.setZoom(15);
            }

            //console.log("Place changed");

            // marker.setPosition(place.geometry.location);

            if (!$scope.question._submitted) {
                $scope.question._answer.position = {
                    lat: place.geometry.location.lat(),
                    lng: place.geometry.location.lng()
                }
                $scope.$apply();
            }

        });

    }

    function initializeGeolocalisationMap($scope, element) {

        var mapElement = element.find("div")[1];

        var topoAttribution = 'Kartendaten: &copy; <a href="https://openstreetmap.org/copyright">OpenStreetMap</a>-Mitwirkende, <a href="http://viewfinderpanoramas.org">SRTM</a> | Kartendarstellung: &copy; <a href="https://opentopomap.org">OpenTopoMap</a> (<a href="https://creativecommons.org/licenses/by-sa/3.0/">CC-BY-SA</a>)';

        map = L.map(mapElement).setView([45.18478, 5.73134], 7);
        L.tileLayer('http://{s}.tile.opentopomap.org/{z}/{x}/{y}.png', {
            maxZoom: 17,
            attribution: topoAttribution,
            zoomControl: false
        }).addTo(map);

        map.zoomControl.setPosition('bottomright');

        window.leafletMap = map;

        map.on('click', onClick($scope));

        initSuggestionInput($scope);
        resetInput($scope, element);
    }

    function initializeGeolocalisationMap_google($scope, element) {

        //console.log("INIT MAP ON ", element.find("div"));


        var mapElement = element.find("div")[1]; //$(element.children()[0]).find(".geolocalisation-map-container").first(); //.children()[0],
            // keywordElement = element.find("input")[0]; //$(element.children()[0]).find(".geolocalisation-keywords-container").first();

        // console.log("KEYWORDS ", keywordElement);

        var default_center;
        var default_zoom;
        var place_marker = false;

        var position = null;

        if (position) {
            default_center = new google.maps.LatLng(position.lat, position.lng);
            default_zoom = 8;
            place_marker = true;

        } else {
            // si pays
            default_center = new google.maps.LatLng(45, 2);
            default_zoom = 2;
        }

        var map_options = {
            center: default_center,
            zoom: default_zoom,
            mapTypeId: google.maps.MapTypeId.TERRAIN,
            streetViewControl: false,
            panControl: false,
            mapTypeControl: true,
            mapTypeControlOptions: {
                style: google.maps.MapTypeControlStyle.HORIZONTAL_BAR,
                position: google.maps.ControlPosition.TOP_CENTER
            },
            zoomControlOptions: {
                position: google.maps.ControlPosition.RIGHT_BOTTOM
            }
        };

        map = new google.maps.Map(mapElement, map_options);

        var defaultBounds = new google.maps.LatLngBounds(
            new google.maps.LatLng(-6, 106.6),
            new google.maps.LatLng(-6.3, 107)
        );

        google.maps.event.addListener(map, 'click', onClick($scope));


        resetInput($scope, element);
        //autocomplete = new google.maps.places.Autocomplete(keywordElement);
        //autocomplete.bindTo('bounds', map);




        return map;
    }

    function onClick($scope) {
        return function (event) {
            initMarker($scope, event);
            resetInput($scope);
        }
    }

    function onClick_google($scope) {
        return function (event) {
            initMarker_google($scope, event);
        }
    }

    function markerPositionListen($scope, listen) {
        if (marker) {
            if (listen) {

                marker.on('dragstart', function(e) {

                    // Cancel click event on map
                    if (map) {
                        console.log('remove click event');
                        map.off('click');
                    }
                });

                marker.on('dragend', function(e) {
                    if (!$scope.question._submitted) {
                        console.log("Marker", marker);
                        $scope.question._answer.position = {
                            lat: marker.getLatLng().lat,
                            lng: marker.getLatLng().lng
                        }
                        $scope.$apply();
                    }

                    // Put back click event on map
                    if (map) {
                        setTimeout(function() {
                            map.on('click', onClick($scope));
                        }, 200)

                    }
                });

            } else {
                // TODO CLEAR
            }
        }
    }

    function markerPositionListen_google($scope, listen) {
        if (marker) {
            if (listen) {
                google.maps.event.addListener(marker, 'dragend', function() {
                    if (!$scope.question._submitted) {
                        $scope.question._answer.position = {
                            lat: marker.getPosition().lat(),
                            lng: marker.getPosition().lng()
                        }
                        $scope.$apply();
                    }
                });
            } else {
                google.maps.event.clearListeners(marker, 'position_changed');
                google.maps.event.clearListeners(autocomplete, 'place_changed');
            }
        }
    }

    var changeCenter = true;

    function recenterMap($scope) {
        var answer = QuestionUtils.cleanAnswer($scope.question, $scope.question._answer);
        if (map && !!answer && !!answer.position && !isNaN(answer.position.lat) && !isNaN(answer.position.lng)) {
            pos =  new google.maps.LatLng(answer.position.lat, answer.position.lng);
            map.setCenter(pos);
        }
    }

    function initMarker($scope, event) {

        console.group("Init marker")
        console.log("event", event);

        // Find position
        var pos;
        if (!!event && !$scope.question._submitted) {
            pos = event.latlng;
            //console.log("change on click", event.latLng);
        } else {
            var answer = QuestionUtils.cleanAnswer($scope.question, $scope.question._answer);

            if (!!answer.position && !isNaN(answer.position.lat) && !isNaN(answer.position.lng)) {
                pos =  new L.latLng(answer.position.lat, answer.position.lng);
                // TODO LEAFLET
            }
        }

        console.log("pos", pos);
        console.groupEnd();

        if (!pos) {
            // Pas d'info, on supprime le marker
            if (marker) {
                markerPositionListen($scope, false);
                marker.setMap(null);
                marker = null;
            }

            return;
        }

        if (!marker) {
            pos = pos || event.latlng;
            console.log("Add marker", pos);
            marker = new L.marker(pos).addTo(map);
            markerPositionListen($scope, true);
        }

        var iconColor = !$scope.question._submitted ? 'yellow' : 'grey';

        if (!$scope.question._submitted) {
            marker.dragging.enable();
        } else {
            marker.dragging.disable();
        }

        var icon = new L.Icon({
            iconUrl: 'https://cdn.rawgit.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-' + iconColor + '.png',
            shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/0.7.7/images/marker-shadow.png',
            iconSize: [25, 41],
            iconAnchor: [12, 41],
            popupAnchor: [1, -34],
            shadowSize: [41, 41]
        });

        marker.setIcon(icon);

        marker.setLatLng(pos);

        if (!!event && !$scope.question._submitted) {
            $scope.question._answer.position = {
                lat: marker.getLatLng().lat,
                lng: marker.getLatLng().lng
            }
            $scope.$apply();
        }

    }

    function initMarker_google($scope, event) {



         var pos;

         //console.log('init marker', event, $scope);

         if (!!event && !$scope.question._submitted) {
            pos = event.latLng;
             //console.log("change on click", event.latLng);
         } else {
            var answer = QuestionUtils.cleanAnswer($scope.question, $scope.question._answer);

             if (!!answer.position && !isNaN(answer.position.lat) && !isNaN(answer.position.lng)) {
                 pos =  new google.maps.LatLng(answer.position.lat, answer.position.lng);
             }
            /*$scope.question._answer
            var pos = new google.maps.LatLng(.position.lat, markerObject.position.lng);
            //console.log("Add marker at ", markerObject.position);
            marker.setPosition(pos);
            marker.   */
         }

        //console.log(pos);

         if (!pos) {
             // Pas d'info, on supprime le marker
             if (marker) {
                 markerPositionListen($scope, false);
                 marker.setMap(null);
                 marker = null;
             }
             return;
         }

         if (!marker) {
            marker = new google.maps.Marker({ map: map });
            markerPositionListen($scope, true);
         }



         marker.setPosition(pos);



         /*if (!event && pos) {
            // Manual change -> recenter
             //console.log("Event", event)
             map.setCenter(pos);
         }*/

         if (!!event && !$scope.question._submitted) {
             $scope.question._answer.position = {
                 lat: marker.getPosition().lat(),
                 lng: marker.getPosition().lng()
             }
             $scope.$apply();
         }

         //console.log('init marker', $scope.question);
         marker.setDraggable(!$scope.question._submitted);

         var color = !$scope.question._submitted ? 'yellow' : 'grey';

         marker.setIcon({

            path: google.maps.SymbolPath.BACKWARD_CLOSED_ARROW,
            scale: 7,
            fillColor: color,
            fillOpacity: 1.0,
            strokeWeight: 2.0

         });
    }

    /*function addMarker($scope, markerObject) {
        // ADD MARKER
        var marker = new google.maps.Marker({ map: map });

        //markerObject._marker = marker;

        marker.setDraggable(!!markerObject.editable);

        if (!!markerObject.position) {
            var pos = new google.maps.LatLng(markerObject.position.lat, markerObject.position.lng);
            //console.log("Add marker at ", markerObject.position);
            marker.setPosition(pos);
        } else {
            marker.setPosition(map.getCenter());
        }

        if (!!markerObject.editable) {
            google.maps.event.addListener(marker, 'position_changed', function() {
                markerObject.position = {
                    lat: marker.getPosition().lat(),
                    lng: marker.getPosition().lng()
                }
                $scope.$apply();
            });
        }

        // On place le marker au clic



    } */

    return {
        restrict: 'E',
        link: function($scope, element, attrs) {



            //$scope.mapStatus = $scope.conf._map.status;
            $scope.checkMapStatus = function(s) { return $scope.conf._map.status == s; }



            initializeGeolocalisationMap($scope, element);

            $scope.$watch('conf._map.status', function() {
                //console.log("resize");
                if (map) {
                    setTimeout(function() {
                        console.log("Change size")
                        map.invalidateSize();
                        //google.maps.event.trigger(map, 'resize');
                        //recenterMap($scope);
                    }, 100);
                }
            });

            $scope.$watch('question._submitted', function() {
                initMarker($scope);
            });
            $scope.$watch('question._answer.position.lat', function() {
                initMarker($scope);
            });
            $scope.$watch('question._answer.position.lng', function() {
                initMarker($scope);
            });

            $scope.$watch('specimen', function() {
                resetInput($scope, element);
            });

            /*
            $scope.$watchCollection("map.markers", function() {
                _.each($scope.map.markers, function(m) {
                    //console.log("add marker", m);
                    addMarker($scope, m);
                });
            })
            */



        },
        scope: {
            question: '=',
            conf: '=',
            specimen: '='
        },
        templateUrl: function(elem, attr) {
            return 'templates/geolocalisation-map.html';
        }
    }
}]);



herbonautesApp.factory('UserProfileService', ['$resource', function($resource) {

    var urls = {
        specimenWithContributions: herbonautes.actions.User.specimenWithContributions({
            userId : ':userId'
        }),
    }

    return $resource('/missions/:id',
        { id: '@id' },
        {
            specimenWithContributions: { method: 'GET', url: urls.specimenWithContributions, isArray: false }
        }
    );
}]);



/* */
herbonautesApp.controller('UserProfileCtrl', ['$scope', '$rootScope', 'UserProfileService', 'MissionService', 'amMoment',
                                              function($scope, $rootScope, UserProfileService, MissionService, amMoment) {

     amMoment.changeLocale('fr');;

    $scope.questions = [];
    $scope.contributionsStats = [];
    $scope.contributionPage = false;
    $scope.contributionMissionId = null;

    $scope.contributionLines = [];


    $scope.init = function(userId) {
        //console.log("Init mission ", missionId);
        $scope.userId = userId;
    }

    $scope.questionAnswered = function(line, questionId) {
       return !!line.answers[questionId];
    }

    $scope.resetContributionsPage = function() {
        //console.log("Reset contribution page", $scope.contributionMissionId);
        $scope.contributionLines = [];
        $scope.noMoreContributions = false;
        $scope.contributionPage = false;
        $scope.questions = MissionService.questions({id : $scope.contributionMissionId });
        $scope.nextContributionPage();
    }

    $scope.specimenUrl = function(specimen) {
        if (!specimen) {
            return "#";
        }
        return herbonautes.ctxPath + '/specimens/' + specimen.institute + '/' + specimen.collection + '/' + specimen.code;
    }

    $scope.nextContributionPage = function() {

        $scope.contributionLoading = true;

        var nextPage = $scope.contributionPage ? $scope.contributionPage.pageIndex + 1 : 1;

        UserProfileService.specimenWithContributions({
            userId: $scope.userId,
            missionId:$scope.contributionMissionId,
            page: nextPage
        }, function(page) {

            $scope.contributionPage = page;

            //console.log(page);
            $scope.contributionLoading = false;

            if (page.list.length == 0) {
                $scope.noMoreContributions = true;
            }

            $scope.contributionPage.list.forEach(function(s) {
                var line = {};
                line.specimen = s.stat.specimen;

                line.lastModifiedAt = s.stat.lastModifiedAt;
                line.answers = {};

                s.answers.forEach(function(a) {
                    line.answers[a.questionId] = true;
                });

                $scope.contributionLines.push(line);
            })


        });

    }


}]);


herbonautesApp.factory('BotanistService', ['$resource', function($resource) {
    return $resource(herbonautes.ctxPath + '/botanists/:id',
        { id: '@id' },
        {
            specimens: {method: 'GET', url: herbonautes.ctxPath + '/botanists/:id/specimens', isArray: true}
        });
}]);

herbonautesApp.controller('BotanistController', ['$scope', 'BotanistService', function($scope, BotanistService) {

    $scope.page = 0;

    $scope.specimens = [];

    $scope.nextPage = function() {
        $scope.page = $scope.page + 1;
        $scope.loading = true;
        BotanistService.specimens({ id: $scope.botanistId, page: $scope.page }, function(specimens) {
            if (specimens.length == 0) {
                if ($scope.page == 1) {
                    $scope.noSpecimen = true;
                } else {
                    $scope.noMore = true;
                }
            }
            $scope.specimens = _.union($scope.specimens, specimens);
            $scope.loading = false;
        });
    }

    $scope.specimenPageUrl = function(specimen) {
        return herbonautes.ctxPath + '/specimens/' + specimen.institute + '/' +  specimen.collection + '/' + specimen.code;
    }

    $scope.init = function(botanistId) {

        $scope.botanistId = botanistId;

        $scope.nextPage();

    }

}]);

//
// Stats
//

herbonautesApp.factory('StatsService', ['$resource', function($resource) {
    return $resource(herbonautes.ctxPath + '/stats/:id',
        { id: '@id' },
        {
            topContributor: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/topContributors', isArray: false},
            lastRegistered: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/lastRegistered', isArray: false},
            specimenDetails: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/specimenDetails', isArray: true},
            topTagUsage: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/topTagUsage', isArray: true},
            lastTagUsage: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/lastTagUsage', isArray: true},
            topInsitution: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/topInstitution', isArray: true},
            topBotanist: {method: 'GET', url: herbonautes.ctxPath + '/stats/global/botanists', isArray: true},

    });
}]);



herbonautesApp.controller('StatsController', ['$scope', '$modal', 'StatsService', function($scope, $modal, StatsService) {

    // common

    $scope.userImageUrl = function(user) {
        return herbonautes.ctxPath + "/img/users/" + user.id + ".jpg";
    }

    $scope.userProfileUrl = function(user) {
        return herbonautes.ctxPath + '/users/' + user.login;
    }

    $scope.formatNumber = function(n) {
        return parseInt(n).toLocaleString()
    }

    $scope.tagUrl = function(tagLabel) {
        return herbonautes.ctxPath + '/tags/show/' + encodeURIComponent(tagLabel);
    }

    $scope.botanistUrl = function(id) {
        return herbonautes.ctxPath + '/botanists/' + id;
    }

    // more Countries

    $scope.moreCountries = function() {
        countryTop += 10;
        displayCountryChart();
    }
    $scope.topCountriesOnly = function() {
        countryTop = 10;
        displayCountryChart(function() { $scope.$digest() });
    }

    $scope.showMoreCountries = function() {
        return !!_.find(countryChart.data(), function(d) { return "Autres" == d.key });
    }
    $scope.showTopOnlyCountries = function() {
        return countryTop > 10;
    }
    $scope.showCancelCountriesSelection = function() {
        return countryChart.filters().length > 0;
    }

    // more regions

    $scope.moreRegions = function() {
        regionTop += 10;
        displayRegionChart();
    }
    $scope.topRegionsOnly = function() {
        regionTop = 10;
        displayRegionChart(function() { $scope.$digest() });
    }

    $scope.showMoreRegions = function() {
        return !!_.find(regionChart.data(), function(d) { return "Autres" == d.key });
    }
    $scope.showTopOnlyRegions = function() {
        return regionTop > 10;
    }
    $scope.showCancelCountriesSelection = function() {
        return regionChart.filters().length > 0;
    }

    // Bienvenue aux nouveaux

    $scope.lastRegisteredList = [];

    $scope.initWelcomeList = function() {
        StatsService.lastRegistered({ page: 1 }, function(page) {
            $scope.lastRegisteredList = $scope.lastRegisteredList.concat(page.list);
            //console.log($scope.lastRegisteredList);
        });
    }

    // Top Contributor

    $scope.topContributorPage = {};
    $scope.topContributorList = [];

    $scope.nextTopContributorPage = function() {

        var pageIndex = 1;
        if ($scope.topContributorPage) {
            pageIndex = $scope.topContributorPage.pageIndex + 1;
        }
        StatsService.topContributor({ page: pageIndex }, function(page) {
            $scope.topContributorPage = page;
            $scope.topContributorList = $scope.topContributorList.concat(page.list);
        });

    }

    $scope.openTopContributorModal = function() {
        var modalInstance = $modal.open({
            animation: false,
            templateUrl: 'modal-top-herbonautes.html',
            controller: 'ModalStatTopHerbonautesCtrl',
            size: 'sm',
            backdrop: true,
            resolve: {
                topContributorList: function() {
                    return $scope.topContributorList;
                }
            }

        });

        modalInstance.result.then(function(answer) {
            //console.log("unusable", answer);
        }, function () {
        });
    }

    //modal-top-herbonautes.html

    // Details specimen
    $scope.completeSpecimensCount = 0;
    $scope.unusableSpecimensCount = 0;
    $scope.conflictSpecimensCount=  0;
    StatsService.specimenDetails({}, function(stats) {
        $scope.completeSpecimensCount = stats[0][0];
        $scope.unusableSpecimensCount = stats[0][1];
        $scope.conflictSpecimensCount=  stats[0][2];
    });

    // Top tag

    $scope.topTagPageIndex = 0;
    $scope.topTagList = [];

    $scope.nextTopTagPage = function() {

        $scope.topTagPageIndex = $scope.topTagPageIndex + 1;
        StatsService.topTagUsage({ page: $scope.topTagPageIndex }, function(list) {
            $scope.topTagList = $scope.topTagList.concat(list);
        });

    }

    // Last tag

    $scope.lastTagPageIndex = 0;
    $scope.lastTagList = [];

    $scope.nextLastTagPage = function() {

        $scope.lastTagPageIndex = $scope.lastTagPageIndex + 1;
        StatsService.lastTagUsage({ page: $scope.lastTagPageIndex }, function(list) {
            $scope.lastTagList = $scope.lastTagList.concat(list);
        });

    }

    // Institutions

    $scope.topInstitutionPageIndex = 0;
    $scope.topInstitutionList = [];

    $scope.nextTopInstitution = function() {

        $scope.topInstitutionPageIndex = $scope.topInstitutionPageIndex + 1;
        StatsService.topInsitution({ page: $scope.topInstitutionPageIndex }, function(list) {
            $scope.topInstitutionList = $scope.topInstitutionList.concat(list);
        });

    }

    // Botanists

    $scope.topBotanistPageIndex = 0;
    $scope.topBotanistList = [];

    $scope.nextTopBotanist = function() {

        $scope.topBotanistPageIndex = $scope.topBotanistPageIndex + 1;
        StatsService.topBotanist({ page: $scope.topBotanistPageIndex }, function(list) {
            $scope.topBotanistList = $scope.topBotanistList.concat(list);
        });

    }




    // init

    $scope.nextTopContributorPage();
    $scope.initWelcomeList();
    $scope.nextTopTagPage();
    $scope.nextLastTagPage();
    $scope.nextTopInstitution();
    $scope.nextTopBotanist();

}]);


herbonautesApp.controller('StatsMissionCtrl', ['$scope', '$modal', 'StatsService', function($scope, $modal, StatsService) {



    // common

    $scope.userImageUrl = function(user) {
        return herbonautes.ctxPath + "/img/users/" + user.id + ".jpg";
    }

    $scope.userProfileUrl = function(user) {
        return herbonautes.ctxPath + '/users/' + user.login;
    }

    $scope.formatNumber = function(n) {
        return parseInt(n).toLocaleString()
    }

    $scope.tagUrl = function(tagLabel) {
        return herbonautes.ctxPath + '/tags/show/' + encodeURIComponent(tagLabel);
    }

    $scope.botanistUrl = function(id) {
        return herbonautes.ctxPath + '/botanists/' + id;
    }

    // more Countries

    $scope.moreCountries = function() {
        countryTop += 10;
        displayCountryChart();
    }
    $scope.topCountriesOnly = function() {
        countryTop = 10;
        displayCountryChart(function() { $scope.$digest() });
    }

    $scope.showMoreCountries = function() {
        return !!_.find(countryChart.data(), function(d) { return "Autres" == d.key });
    }
    $scope.showTopOnlyCountries = function() {
        return countryTop > 10;
    }
    $scope.showCancelCountriesSelection = function() {
        return countryChart.filters().length > 0;
    }


    // more regions

    $scope.moreRegions = function() {
        regionTop += 10;
        displayRegionChart();
    }
    $scope.topRegionsOnly = function() {
        regionTop = 10;
        displayRegionChart(function() { $scope.$digest() });
    }

    $scope.showMoreRegions = function() {
        return !!_.find(regionChart.data(), function(d) { return "Autres" == d.key });
    }
    $scope.showTopOnlyRegions = function() {
        return regionTop > 10;
    }
    $scope.showCancelCountriesSelection = function() {
        return regionChart.filters().length > 0;
    }




    // Botanists

    $scope.topBotanistPageIndex = 0;
    $scope.topBotanistList = [];

    $scope.nextTopBotanist = function() {

        $scope.topBotanistPageIndex = $scope.topBotanistPageIndex + 1;
        StatsService.topBotanist({ page: $scope.topBotanistPageIndex, missionId: $scope.missionId }, function(list) {
            $scope.topBotanistList = $scope.topBotanistList.concat(list);
        });

    }




    // init
    $scope.init = function(missionId) {
        $scope.missionId = missionId;
        $scope.nextTopBotanist();
    }


}]);

herbonautesApp.controller('ModalStatTopHerbonautesCtrl', ['$scope', '$modalInstance', 'topContributorList', 'StatsService',
    function($scope, $modalInstance, topContributorList, StatsService) {

    $scope.topContributorList = [];

    $scope.topContributorPage = {};

    $scope.loading = false;
    $scope.noMore = false;

    $scope.userImageUrl = function(user) {
        return herbonautes.ctxPath + "/img/users/" + user.id + ".jpg";
    }

    $scope.userProfileUrl = function(user) {
        return herbonautes.ctxPath + '/users/' + user.login;
    }

    $scope.formatNumber = function(n) {
        return parseInt(n).toLocaleString()
    }



    $scope.nextTopContributorPage = function() {

        var pageIndex = 1;
        if ($scope.topContributorPage) {
            pageIndex = $scope.topContributorPage.pageIndex + 1;
        }
        $scope.loading = true;
        StatsService.topContributor({ page: pageIndex, pageSize: 30 }, function(page) {
            $scope.topContributorPage = page;
            $scope.topContributorList = $scope.topContributorList.concat(page.list);
            if (page.list.length == 0) {
                $scope.noMore = true;
            }
            $scope.loading = false;
        });

    }

    $scope.nextTopContributorPage();

    $scope.cancel = function() {
        $modalInstance.close();
    };

}]);