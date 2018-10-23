
herbonautesApp.service('ElasticSearch', ['esFactory', function (esFactory) {
    return esFactory({ host: elasticHostExplore });
}]);

herbonautesApp.service('ImagerService', function() {
    this.getThumb = function (url, size) {
        var tmpurl = url.replace( 'http://','');
        var domaine = tmpurl.substring(0, tmpurl.indexOf("/"));
        var rest = tmpurl.replace(domaine,"");
        switch (domaine){
            case "dsiphoto.mnhn.fr" :
                return "http://imager.mnhn.fr/imager/v"+size + rest ;
            case "sonneratphoto.mnhn.fr" :
                return "http://imager.mnhn.fr/imager2/v"+size + rest ;
            case "mediaphoto.mnhn.fr" :
                return "http://imager.mnhn.fr/imager3/v"+size + rest ;
            default :
                return url ;
        }
    };
});


herbonautesApp.service('RecolnatSearch', ['ElasticSearch', function(ElasticSearch) {

    return {

        search: function(cartItem, page, pageSize) {


            var andQuery = [];
            for (var k in cartItem.terms){
                if (cartItem.terms.hasOwnProperty(k)) {
                    var val = cartItem.terms[k];
                    if (!!val) {
                        var term = { _cache: false };
                        term[k] = cartItem.terms[k];
                        andQuery.push({ term: term });
                    }
                }
            }

            // AVEC / IMAGE
            andQuery.push({ term : {
                _cache: false,
                hasmedia: 1
            }});

            // Sans info de collect
            if (!cartItem.noCollectInfo) {
                andQuery.push({
                    "or": [
                        {
                            "exists": {
                                "field": "country"
                            }
                        },
                        {
                            "exists": {
                                "field": "recordedby"
                            }
                        }
                    ]
                });
            } else {
                andQuery.push({
                    "and": [
                        {
                            "missing": {
                                "field": "country"
                            }
                        },
                        {
                            "missing": {
                                "field": "recordedby"
                            }
                        }
                    ]
                });
            }


            var query = {
                filtered: {
                    filter: {
                        and: andQuery
                    }
                }
            };

            return ElasticSearch.search({
                index: cartItem.indexName,
                from: ((page - 1) * pageSize),
                size: pageSize,
                body: {
                    "query": query,
                    "sort": '_score'
                }
            })

        },

        suggest: function(field, q, indexName) {

            if (!indexName) {
                indexName = 'botanique';
            }

            var query = {
                    filtered: {
                        query: {
                            wildcard: {}
                        },
                        filter: {
                            bool: {
                                must: []
                            }
                        }
                    }
                };
            query.filtered.query.wildcard[field] = {
                value: q + "*",
                boost: 2
            }
            var aggs = {
                    general: {
                        terms: {
                            field: field,
                            size: 20
                        }
                    }
               };

            return ElasticSearch.search({
                index: indexName,
                //from: (($scope.pagination.current_page - 1) * $scope.limit),
                size: 0,
                body: {
                    "query": query,
                    "aggs": aggs
                }
            });

        }

    }

}]);


herbonautesApp.controller('MissionSettingsCtrl', ['$scope', '$q',
                                                  'MissionService',
                                                  'QuestionTemplates',
                                                  'QuizService',
                                                  'QuestionUtils',
                                                  'References',
                                                  'ReferencesSearch',
                                                  function($scope, $q,
                                                           MissionService,
                                                           QuestionTemplates,
                                                           QuizService, QuestionUtils, References, ReferencesSearch) {

    $scope.specimensCount = 0;
    $scope.references = References.query();

    $scope.pendingModifications = function() {
        return false;
    };

    $scope.questions = [];
    $scope.templates = QuestionTemplates.query();

    // Check missing parent ref
    $scope.missingParentRefByQuestionName = {};
    $scope.confForMissingRef = {};

    $scope.$watch('questions', function() {

        $scope.questions.$promise.then(function(questions) {
            $scope.references.$promise.then(function(references) {
                $scope.missingParentRefByQuestionName = QuestionUtils.getMissingParentReferencesByQuestionName(references, questions);

                var referencesById = _.indexBy(references, "id");

                _.each($scope.missingParentRefByQuestionName, function(ref, name) {

                    var conf = _.find($scope.questionByName(name).configuration, function(c) {
                        if (c.type == 'reference') {
                           var cref = referencesById[c.options.reference];
                            if (cref.parent && cref.parent.id == ref.id) {
                               return true;
                            }
                        }
                        return false;
                    });


                    if (conf) {
                      $scope.confForMissingRef[name] = conf;
                    }

                   //console.log("Conf for missing", ref.label, conf);
                });

            });
        });
    }, true);

    $scope.suggestReferenceRecord = function(referenceId, val) {
        console.log("search", referenceId, val);
        return ReferencesSearch.search(referenceId, val, null).then(function(response) {
            return response.data;
        });
    };

    $scope.questionByName = function(name) {
        return _.find($scope.questions, function(q) {
            return q.name == name;
        });
    }

    $scope.getConfigurationsForRef = function(ref, question) {
       return QuestionUtils.getConfigurationsForRef(ref.id, question);
    }

    var savedQuestions = [];

    $scope.quizzes = QuizService.query();

    $scope.init = function(missionId) {
        $scope.missionId = missionId;
        $scope.questions = MissionService.questions({id: $scope.missionId});
        savedQuestions = _.map($scope.questions, _.clone);
    }

    $scope.setDefaultQuestions = function() {
        console.log("remove all")
        $scope.questions.splice(0, $scope.questions.length);

        //return;
        _.each($scope.templates, function(t) {
            console.log("add " + t.name + " ? " + t.defaultForMission);
            if (t.defaultForMission) {
                $scope.addQuestion(t);
            }
        });
    }

    $scope.isQuestionMandatory = function(question) {
        var template = _.find($scope.templates, function(t) {
            return t.name == question.name;
        });
        return template && template.mandatoryForMission;
    }


    $scope.addQuestion = function(question) {
        //console.log("add question", question);
        var newQuestion = _.omit(_.clone(question), 'id');
        newQuestion.templateId = question.id;
        $scope.questions.push(newQuestion);
    }

    $scope.removeQuestion = function(q) {
        var i = _.findIndex($scope.questions, function(e) { return e.name == q.name; })
        $scope.questions.splice(i, 1);
    }

    $scope.isTemplateUsed = function(q) {
        return _.chain($scope.questions).pluck("name").contains(q.name).value();
    }

    $scope.saveQuestions = function(callback) {
        $scope.questions = MissionService.saveQuestions({ id: $scope.missionId }, $scope.questions, function(data) {

            if (callback) {
                callback();
            }
        });
    }

    $scope.saveSettings = function() {
        $scope.saveQuestions(function() {
            //console.log("quesitons saved, submit")
            $('#mission-save').submit();
        });
    }

    $scope.reloadQuestions = function() {
        if (confirm("Le paramétrage des questions sera modifié")) {
            $scope.questions = MissionService.reloadQuestions({id: $scope.missionId}, function (questions) {
                $scope.questions = questions;
            });
        }
    }

    $scope.reloadQuestion = function(question) {
        MissionService.reloadQuestions({ id: $scope.missionId, questionId: question.id }, function(questions) {
            for (var i = 0; i < $scope.questions.length; i++) {
                if ($scope.questions[i].id == question.id) {
                    for (var j = 0; j < questions.length; j++) {
                        if (questions[j].id == question.id) {
                            $scope.questions[i] = questions[j];
                            break;
                        }
                    }
                    break;
                };
            }
            alert("Question '" + question.label + "' mise à jour");
        });
    }

    // SORTABLE QUESTIONS

    /*$scope.minified = false;

    $scope.minifyAll = function() {
        $scope.minified = true;
    };

    $scope.unminifyAll = function() {
        $scope.minified = false;
    };*/

    $scope.dragStart = function(e, ui) {
        ui.item.data('start', ui.item.index());

    };

    $scope.dragEnd = function(e, ui) {

        var start = ui.item.data('start'),
            end = ui.item.index();

        $scope.questions.splice(end, 0,
        $scope.questions.splice(start, 1)[0]);



        $scope.$apply();
    };

    $("#mission-questions").sortable({
        start: $scope.dragStart,
        update: $scope.dragEnd,
        handle: '.sort-handle'
    });

}]);

herbonautesApp.factory('ReferencesAdmin', ['$resource', function($resource) {
    return $resource(herbonautes.ctxPath + '/modules/admin/references/:id',
        { id: '@id' },
        {
            allReferences: { method: 'GET', url: herbonautes.ctxPath + '/modules/admin/references', isArray: true },
            saveReference: { method: 'POST', url: herbonautes.ctxPath + '/modules/admin/references', isArray: false },
            updateReference: { method: 'POST', url: herbonautes.ctxPath + '/modules/admin/references/update', isArray: false },
            allRecords: { method: 'GET', url: herbonautes.ctxPath + '/modules/admin/references/:id/records', isArray: true },
            saveRecord: { method: 'POST', url: herbonautes.ctxPath + '/modules/admin/references/:id/records', isArray: false },

            deleteRecord: { method: 'POST', url: herbonautes.ctxPath + '/modules/admin/references/:id/recordsTrash', isArray: false },

            recordsPage: { method: 'GET', url: herbonautes.ctxPath + '/modules/admin/references/:id/pagerecords', isArray: false },
            questionsUsingRef: { method: 'GET', url: herbonautes.ctxPath + '/modules/admin/references/:id/questions', isArray: true },

            deleteReference: { method: 'POST', url: herbonautes.ctxPath + '/modules/admin/references/:id/delete', isArray: false }

            // current: { method: 'GET', url: '/api/lots/current', isArray: true },
            // finished: { method: 'GET', url: '/api/lots/finished', isArray: false, params: {
            //     page: '@page'
            // } },
            // images: { method: 'GET', url: '/api/lots/:lotId/images', isArray: true },
            // stats: { method: 'GET', url: '/api/lots/:lotId/stats', isArray: false }
        }
    );
}]);

herbonautesApp.controller('ReferencesListCtrl', ['$scope', '$modal', '$window', 'ReferencesAdmin',
                                                 function($scope, $modal, $window, ReferencesAdmin) {

    $scope.deletableMap = {}

    $scope.references = ReferencesAdmin.allReferences(function(references) {

        references.forEach(function(ref) {
            ReferencesAdmin.questionsUsingRef({id: ref.id}, function(questions) {
                if (questions.length > 0) {
                    $scope.deletableMap[ref.id] = false;
                } else {
                    $scope.deletableMap[ref.id] = true;
                }
            });
        })

    });

    $scope.usedAsParent = function(reference) {
        return !!(_.find($scope.references, function(r) {
            return r.parent && r.parent.id == reference.id;
        }));
    }

    $scope.delete = function(ref) {
        if (confirm("Confirmez-vous la suppression du référentiel '" + ref.label + "' ?")) {
            ReferencesAdmin.deleteReference({id: ref.id}, function () {
                var i = _.findIndex($scope.references, function (r) {
                    return ref.id == r.id;
                })
                $scope.references.splice(i, 1);
            });
        }


    }

    $scope.toAdminEdit = function(ref) {
        console.log("toAdminEdit", ref);
        $window.location = toReferenceAdminEdit({ id: ref.id });
    }

    $scope.open = function (size) {

        var modalInstance = $modal.open({
            animation: $scope.animationsEnabled,
            templateUrl: 'new-reference-modal.html',
            controller: 'ModalNewReferenceCtrl',
            size: size,
            resolve: {
                references: function() {
                    return $scope.references;
                }
            }
        });

        modalInstance.result.then(function (ref) {
            console.log("save ref", ref);
            ReferencesAdmin.saveReference(null, ref, function(ref) {
                $scope.references.push(ref);
                $scope.deletableMap[ref.id] = true;
            });
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

    $scope.edit = function (ref) {

        var modalInstance = $modal.open({
            animation: $scope.animationsEnabled,
            templateUrl: 'edit-reference-modal.html',
            controller: 'ModalEditReferenceCtrl',
            size: 'lg',
            resolve: {
                reference: function() {
                    return ref;
                },
                references: function() {
                    return $scope.references;
                }
            }
        });

        modalInstance.result.then(function (newRef) {
            console.log("update ref", newRef);
           //ReferencesAdmin.saveReference(null, ref, function(ref) {
           //    $scope.references.push(ref);
           //    $scope.deletableMap[ref.id] = true;
           //});
            ReferencesAdmin.updateReference(null, newRef, function(r) {
                ref.parent = r.parent;
                //$scope.deletableMap[ref.id] = true;
            });
        }, function () {
            console.log('Modal dismissed at: ' + new Date());
        });
    };

}]);

herbonautesApp.controller('ModalEditReferenceCtrl', ['$scope', '$modalInstance', 'reference','references', function ($scope, $modalInstance, reference, references) {

    $scope.references = references;
    $scope.ref = reference;
    $scope.newRef = {

    }
    if (reference.parent) {
        $scope.newRef.parentId = reference.parent.id;
        $scope.newRef.previousParentId = reference.parent.id;
    }

    $scope.alert = false;

    $scope.toggleAlert = function(parentId) {
        console.log($scope.newRef.previousParentId,  $scope.newRef.parentId, parentId);
        if ($scope.newRef.previousParentId && ($scope.newRef.previousParentId != parentId)) {
            $scope.alert = true;
        } else {
            $scope.alert = false;
        }
        return true;
    }

    $scope.confirmChangeParentId = function() {
        $scope.alert = false;
    }

    $scope.cancelChangeParentId = function() {
        //console.log("change parent id ", $scope.parentId, $scope.previousParentId);
        $scope.newRef.parentId = $scope.newRef.previousParentId;
        $scope.alert = false;
    }

    $scope.save = function(parentId) {
        //console.log("$scope", $scope);
        var ref = {
            id: $scope.ref.id,
            label: $scope.ref.label
            //    parent: $scope.ref.parent
        }
        if ($scope.newRef.parentId) { ref.parent = { id: $scope.newRef.parentId } };
        //console.log("Save", ref, parentId);
        $modalInstance.close(ref);
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

}]);

herbonautesApp.controller('ModalNewReferenceCtrl', ['$scope', '$modalInstance','references', function ($scope, $modalInstance, references) {

    $scope.references = references;
    $scope.ref = {};

    $scope.create = function(parentId) {
        //console.log("$scope", $scope);
        var ref = {
            label: $scope.ref.label
        //    parent: $scope.ref.parent
        }
        if (parentId) { ref.parent = { id: parentId } };
        console.log("Save", ref, parentId);
        $modalInstance.close(ref);
    };

    $scope.cancel = function() {
        $modalInstance.dismiss('cancel');
    };

}]);

herbonautesApp.controller('ReferencesCtrl', ['$scope', '$modal', 'ReferencesAdmin', function($scope, $modal, ReferencesAdmin) {

    $scope.page = null;
    $scope.sortBy = "label";
    $scope.order = "asc";

    $scope.pageCount = function() {
        if ($scope.page) {

            return Math.floor(($scope.page.totalRowCount - 1) / $scope.page.pageSize) + 1;
        }
        return 0;
    }

    $scope.changeFilter = function() {
        $scope.toPage(1);
    }


    $scope.toggleSort = function(sortBy) {
        if (sortBy == $scope.sortBy) {
            if ($scope.order == "asc") {
                $scope.order = "desc";
            } else {
                $scope.order = "asc";
            }
        } else {
            $scope.sortBy = sortBy;
            $scope.order = "asc";
        }
        $scope.toPage($scope.page.pageIndex);
    }

    $scope.toPage = function(pageIndex) {
        ReferencesAdmin.recordsPage({
            id: $scope.referenceId,
            page: pageIndex,
            sortBy: $scope.sortBy,
            order: $scope.order,
            filter: $scope.filter
        }, function(page) {
            $scope.page = page;
            $scope.records = page.list;
        });
    }

    $scope.init = function(referenceId) {
        $scope.referenceId = referenceId;
        $scope.reference = ReferencesAdmin.get({ id: referenceId }, function(reference) {
            if (!!reference.parent) {
                $scope.parentRecords = ReferencesAdmin.allRecords({ id: reference.parent.id });
            }
        });

        // Init if records are deletable
        ReferencesAdmin.questionsUsingRef({id: referenceId}, function(questions) {
            if (questions.length > 0) {
                $scope.deletableRecords = false;
            } else {
                $scope.deletableRecords = true;
            }
        });

        //$scope.records = ReferencesAdmin.allRecords({ id: referenceId });

        $scope.toPage(1);


    }

    $scope.deleteRecord = function(record) {
        if (confirm("Supprimer l'enregistrement '" + record.label + "'")) {
            ReferencesAdmin.deleteRecord({ id: $scope.reference.id }, record, function(newRecord) {
                _.remove($scope.records, function(d) {
                    return d.id == record.id;
                })
            });
        }
    }

    $scope.addInfo = function(record) {
        console.log("add info", record);
        if (!record.info) {
            record.info = [];
        }
        record.info.push({ name: "", value: "" });
    }
    $scope.removeInfo = function(record, index) {
        record.info.splice(index, 1);
    }


    $scope.toggleEdit = function(record) {
        record._editing = !record._editing;
    }

    $scope.newRecord = function() {
        $scope.records.unshift({ _new: true, _editing: true });
    }

    $scope.saveRecord = function(record) {

        ReferencesAdmin.saveRecord({ id: $scope.reference.id }, record, function(newRecord) {
            record.id = newRecord.id;
            record.value = newRecord.value;
            record.label = newRecord.label;
            record.parent = newRecord.parent;
            record.lastUpdateDate = newRecord.lastUpdateDate;
            record._new = false;
            $scope.toggleEdit(record);
        });
    }




    //



}]);


herbonautesApp.service('CartService', ['$http', function($http) {

    this.saveItem = function(missionId, cartItem) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/settings/cart/items', cartItem);
    },

    this.getItems = function(missionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/settings/cart/items');
    },


    this.isLoading = function(missionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/settings/cart/loading');
    },

    this.syncCart = function(missionId) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/settings/cart/sync');
    },

    this.existingSpecimens = function(missionId, catalogNumbers) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/settings/existing', catalogNumbers);

    },

    this.cancelModifications = function(missionId) {
       return $http.post(herbonautes.ctxPath + '/missions/' + missionId + '/settings/cart/cancel');
    },

    this.specimensCount = function(missionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId + '/specimens/count');
    }

    this.commonMissionsCount = function(missionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId +  '/settings/cart/incommon')
    }

    this.getImportExceptions = function(missionId) {
        return $http.get(herbonautes.ctxPath + '/missions/' + missionId +  '/settings/cart/exceptions')
    }

    this.saveImportException = function(missionId, ignoreMissionId, mode) {
        return $http.post(herbonautes.ctxPath + '/missions/' + missionId +  '/settings/cart/exceptions/' + ignoreMissionId + '/' + mode);
    }


    ///missions/{id}/settings/cart/cancel
    /*this.save = function(missionId, cart) {
        return $http.post('/missions/' + missionId + '/settings/cart', cart);
    }

    this.get = function(missionId) {
        return $http.get('/missions/' + missionId + '/settings/cart');
    }

    this.validate = function(missionId, cart) {
        return $http.post('/missions/' + missionId + '/settings/cart/load', cart);
    }

    this.remove = function(missionId, cart) {
        return $http.post('/missions/' + missionId + '/settings/cart/remove', cart)
    }

    this.isLoading = function(missionId) {
        return $http.get('/missions/' + missionId + '/settings/cart/loading');
    }*/

}]);

herbonautesApp.controller('MissionSettingsCartCtrl', ['$scope', '$timeout', 'CartService',
    'RecolnatSearch', 'ImagerService', 'upload',
    function($scope, $timeout, CartService, RecolnatSearch, ImagerService, upload) {

    $scope.pageSize = 10;

    $scope.searchFields = [
        { name: "Collection" , key: "collectionname"    } ,
        { name: "Famille"    , key: "family"            } ,
        { name: "Genre"      , key: "genus"             } ,
        { name: "Espèce"     , key: "specificepithet"   }
    ];


    $scope.newCartItem = {
        indexName: 'botanique',
        noCollectInfo: true,
        terms: {
             /*COLLECTIONNAME: "herbarium specimens of the muséum d'histoire naturelle of aix-en-provence (aix)"*/
        }
    }

    $scope.textFileIndexName = 'botanique';

    $scope.cart = {
        cartItems: []
    }

    $scope.currentCartItem = null;

    $scope.mission;

    var loadingUpdateInterval = 1000,
        countUpdateInterval = 1000;

    function updateCartItems() {
        CartService.getItems($scope.missionId).then(function(response) {
            $scope.cart.cartItems = response.data;
            $scope.newSearch();
        });
    }
    function updateCartLoading() {
        CartService.isLoading($scope.missionId).then(function(response) {
            if (!!$scope.cart.loading !=  !!response.data.loading) {
               // fin de chargement, update du panier
                updateCartItems();
            }
            $scope.cart.loading = response.data.loading;
            console.log("Cart loading ? " + $scope.cart.loading);
            $timeout(updateCartLoading, loadingUpdateInterval);
        })
    }

    function updateSpecimenCount() {
        CartService.specimensCount($scope.missionId).then(function(response) {
            console.log(response);
            $scope.mission = response.data;
            if ($scope.mission.loading) {
                $timeout(updateSpecimenCount, countUpdateInterval);
            }
        });
    }

    function updateCommonMissionsCount($scope) {
        CartService.commonMissionsCount($scope.missionId).then(function(response) {
            $scope.commonMissionsCounts = response.data;
        });
    }

    function updateImportExceptions($scope) {
        CartService.getImportExceptions($scope.missionId).then(function(response) {
            $scope.importExceptions = response.data;
        });
    }

    $scope.isMissionIdIgnored = function(missionId) {
        return !!_.find($scope.importExceptions, { ignoreMissionId: missionId });
    }

    $scope.importMissionId = function(missionId) {
        CartService.saveImportException($scope.missionId, missionId, "import").then(function() {
            updateCartItems();
            updateImportExceptions($scope);
        });
    }

    $scope.ignoreMissionId = function(missionId) {
        CartService.saveImportException($scope.missionId, missionId, "ignore").then(function() {
            updateCartItems();
            updateImportExceptions($scope);
        });
    }

    $scope.specimenPerMissionLimit = 0;

    $scope.init = function(missionId, specimenPerMissionLimit) {
        $scope.missionId = missionId;
        $scope.specimenPerMissionLimit = specimenPerMissionLimit;
        updateCartItems();
        updateCartLoading();
        updateSpecimenCount();
    }

    function updateCurrentCartItem() {

        _.each($scope.cart.cartItems, function(item) {
            item._current = false
        });

        var cleanTerms = {}; // sans espace ou chaine vide
        _.each($scope.newCartItem.terms, function(v, k) {
            console.log("add clean " + k, v, !!v);
            if(!!v) {
                cleanTerms[k] = v;
            }
        });

        var existingItem = _.find($scope.cart.cartItems, function(item) {
            return !item.textFile && (item.noCollectInfo == $scope.newCartItem.noCollectInfo) &&
                (item.indexName == $scope.newCartItem.indexName) &&
                _.isEqual(item.terms, cleanTerms);
        });

        if (existingItem) {
            $scope.currentCartItem = existingItem;
        } else {
            $scope.currentCartItem = {
                indexName: $scope.newCartItem.indexName,
                noCollectInfo: $scope.newCartItem.noCollectInfo,
                terms: _.extend({}, cleanTerms),
                allSelected: false,
                selection: [],
                allSelectedDraft: false,
                selectionDraft: [],
                sync: true // aucun specimen donc sync ok
            }
            $scope.cart.cartItems.push($scope.currentCartItem);
        }
        $scope.currentCartItem._current = true;

        updateCommonMissionsCount($scope);
        updateImportExceptions($scope);
        //

    }

    $scope.result = { }

    $scope.suggest = function(field, val) {
        $scope.suggestField = field;
        return RecolnatSearch.suggest(field, /*$scope.q[field] */ val.toLowerCase(), $scope.newCartItem.indexName).then(function(response) {
            $scope.suggestions = response.aggregations.general.buckets;
            return $scope.suggestions;
        });
    }

    $scope.newSearch = function() {
        updateCurrentCartItem();
        $scope.fileContent = null;
        $scope.search(1);

    }

    $scope.uploadFile = function() {
        var url = herbonautes.ctxPath + '/missions/' + $scope.missionId + '/settings/cart/uploadFile';
        $scope.fileLoading = true;
        upload({
            url: url,
            method: 'POST',
            data: {
                index: $scope.textFileIndexName,
                codeFile: $("#codeFile"), // a jqLite type="file" element, upload() will extract all the files from the input and put them into the FormData object before sending.
            }
        }).then(
            function (response) {
                $scope.result = {};
                $scope.fileContent = response.data.textFile.data;
                $scope.fileName = response.data.textFile.name;
                $scope.fileLoading = false;
                $scope.cart.cartItems.push(response.data);

                $scope.setCurrentCartItem(response.data);
                //console.log(response.data); // will output whatever you choose to return from the server on a successful upload
            },
            function (response) {
                $scope.fileLoading = false;
                //console.error(response); //  Will return if status code is above 200 and lower than 300, same as $http
                alert("Erreur de chargement, vérifiez la taille limite autorisée et le format du fichier");
            }
        );
    }

    $scope.setCurrentCartItem = function(item) {
        if (item.textFile) {
            _.each($scope.cart.cartItems, function(item) {
                item._current = false
            });
            item._current = true;
            $scope.fileContent = item.textFile.data;
            $scope.fileName = item.textFile.name;
            $scope.result = {};
        } else {
            $scope.newCartItem.indexName = item.indexName;
            $scope.newCartItem.noCollectInfo = item.noCollectInfo;
            $scope.newCartItem.terms = _.extend({}, item.terms);
            $scope.newSearch();
            $scope.fileContent = null;
        }
    }

    $scope.removeCartItem = function(item) {
        item.allSelectedDraft = false;
        item.selectionDraft = [];
        if (item.textFile) {
            item.hits = 0;
        }
        CartService.saveItem($scope.missionId, item).then(function(response) {
            var cartItem = response.data;
            item.id = cartItem.id;
            item.sync = cartItem.sync;
        });

    }

    function hasSpecimenImage(s) {
        return s._source.m_ && s._source.m_.length > 0;
    }

    $scope.hasSpecimenImage = hasSpecimenImage;


    $scope.search = function(page) {

        console.log("Search", $scope.currentCartItem, page, $scope.pageSize);



        RecolnatSearch.search($scope.currentCartItem, page, $scope.pageSize).then(function(response) {
            $scope.result.specimens = response.hits.hits;
            $scope.result.hits = response.hits.total;
            $scope.result.pageCount = Math.floor(($scope.result.hits - 1) / $scope.pageSize) + 1;
            $scope.result.pageIndex = page;

            $scope.result.specimens.forEach(function(s) {
                if (hasSpecimenImage(s)) {
                    var url = s._source.m_[0].identifier;
                    s.imageUrl = ImagerService.getThumb(url, 15);
                } else {
                    s.imageUrl = null;
                }
            });

            console.log("s", response.hits.hits);

            var catalogNumbers = _.map($scope.result.specimens, function(h) {
               return h._source.catalognumber;
            });
            console.log("catalog numbers", catalogNumbers);

            CartService.existingSpecimens($scope.missionId, catalogNumbers).then(function(response) {
                // existing specimens
                _.each(response.data, function(existing) {
                    var result = _.find($scope.result.specimens, function(h) {
                        return h._source.catalognumber == existing.code;
                    });

                    if (!result._existing) {
                        result._existing = []
                    }

                    result._existing.push(existing);
                });
                //console.log("existing", response.data);
            })

            $scope.currentCartItem.hits = response.hits.total;

            // console.log($scope.result);
        });
    }

    $scope.totalSelectionCount = function() {
        var total = 0;
        _.each($scope.cart.cartItems, function(item) {
            total += $scope.selectedSpecimensCount(item);
        });
        return total;
    }

    $scope.isCartSync = function() {
        var sync = true;
        _.each($scope.cart.cartItems, function(item) {
            if (!item.sync) {
                sync = false;
            }
        });
        return sync;
    }

    $scope.isAllSelected = function() {
        return $scope.currentCartItem && $scope.currentCartItem.allSelectedDraft;
    }


    $scope.toggleAllSelected = function() {
        $scope.currentCartItem.allSelectedDraft = !$scope.currentCartItem.allSelectedDraft;
        if ($scope.currentCartItem.allSelectedDraft) {
            $scope.currentCartItem.selectionDraft = [];
        }


        CartService.saveItem($scope.missionId, $scope.currentCartItem).then(function(response) {
            var cartItem = response.data;
            $scope.currentCartItem.id = cartItem.id;
            $scope.currentCartItem.sync = cartItem.sync;
        });
    }

    $scope.isSelected = function(id) {
        return $scope.isAllSelected() || _.contains($scope.currentCartItem.selectionDraft, id);
    }

    $scope.toggleSelected = function(id) {
        if (_.contains($scope.currentCartItem.selectionDraft, id)) {
            $scope.currentCartItem.selectionDraft.splice($scope.currentCartItem.selectionDraft.indexOf(id), 1);
        } else {
            $scope.currentCartItem.selectionDraft.push(id);
        }
        CartService.saveItem($scope.missionId, $scope.currentCartItem).then(function(response) {
            var cartItem = response.data;
            $scope.currentCartItem.id = cartItem.id;
            $scope.currentCartItem.sync = cartItem.sync;
        });
    }

    $scope.isFileItem = function(item) {
      return !!item.textFile;
    }

    $scope.selectedSpecimensCount = function(item) {

        if (!!item.textFile) {
            return item.hits;
        }

        if (item.allSelectedDraft) {
            return item.hits;
        } else {
            if (!item.selectionDraft) {
                return 0;
            }
            return item.selectionDraft.length;
        }
    }

    $scope.updateSpecimens = function() {
        $scope.cart.loading = true;
        CartService.syncCart($scope.missionId).then(function(response) {
            console.log("Sync ok, update count");
            updateSpecimenCount();
        });
    }

    $scope.cancelModifications = function() {
        var currentItemId =
        CartService.cancelModifications($scope.missionId).then(function(response) {
            $scope.cart.cartItems = response.data;
            $scope.newSearch();
        });
    }
}]);


herbonautesApp.controller('RecolnatTransferCtrl', ['$scope', 'MissionService', function($scope, MissionService) {

    $scope.missionId;

    $scope.init = function(missionId) {
        $scope.missionId = missionId;
    }

    $scope.startTransfer = function() {
        console.log("start transfer");
        MissionService.startRecolnatTransfer({ id: $scope.missionId }, null);
        $scope.inProgress = true;
    }


    MissionService.recolnatTransferReport({ id: $scope.missionId }, function(report) {
        $scope.report = report;

        var transferBySpecimen = {};
        _.forEach(report.transferList, function(t) {
            var s = transferBySpecimen[t.specimenId] || { transfers: 0, questions: 0 };
            transferBySpecimen[t.specimenId] = s;
            s.questions += 1;
        });

        $scope.transferBySpecimen = transferBySpecimen;

        $scope.completeTransfers = 0;
        $scope.emptyTransfers = 0;
        $scope.partialTransfers = 0;
        _.forEach(transferBySpecimen, function(k, v) {
            if (v.transfers == v.questions) {
                $scope.completeTransfers++;
            } else if (v.transfers > 0) {
                $scope.partialTransfers++;
            } else {
                $scope.emptyTransfers++;
            }
        });

        $scope.questionsTransfered = 0;
        $scope.questionsNotTransfered = 0;
        $scope.noTransferCauseCount = {};

        _.forEach($scope.report.transferList, function(t) {

            if (t.transferDone) {
                $scope.questionsTransfered++
            } else {
                $scope.questionsNotTransfered++;

                var causeCount = $scope.noTransferCauseCount[t.noTransferCause] || 0
                $scope.noTransferCauseCount[t.noTransferCause] = ++causeCount;
            }

        });


        $scope._i18n = function(code) {
            return i18n(code);
        }


    });

}]);

