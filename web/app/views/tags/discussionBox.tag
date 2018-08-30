*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Tag générique de boite de discussion
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}*



<div id="discussionsBox" ng-controller="discussionCtrl"  ng-init="init('${_tagType}', '${_tagLabel}', ${_discussionId == null ? 'null' : _discussionId}, ${_displayAll == null ? 'null' : _displayAll}, ${_forceDiscussion == null ? 'null' : _forceDiscussion})" class="row-fluid">

    #{if _isAdmin || _connectedLevel && _connectedLevel >= play.configuration.get("herbonautes.discussion.tags.minimum.level").toInteger()}

        <script type="text/ng-template" id="discussion-tag-template">
            <div class="discussion-tag-template">
                <div class="panel" ng-class="data.tagType">
                    <span><i ng-class="data.tagType == 'MISSION' ? 'icon-book' : data.tagType == 'SPECIMEN' ? 'icon-leaf' : 'icon-tag'"></i> {{$getDisplayText()}}</span>
                    <a ng-if="data.tagLabel != '${_tagLabel}'" class="remove-button" ng-click="$removeTag()">&#10006;</a>
                </div>
            </div>
        </script>

        <script type="text/ng-template" id="autocomplete-template">
            <div class="autocomplete-template">
                <div class="panel" ng-class="data.tagType">
                    <span><i ng-class="data.tagType == 'MISSION' ? 'icon-book' : data.tagType == 'SPECIMEN' ? 'icon-leaf' : 'icon-tag'"></i></span>
                    <span class="text" ng-bind-html="$highlight($getDisplayText())"></span>
                </div>

            </div>
        </script>
        <div class="modal hide" id="discussionTagsModal">
            <div class="modal-header">
                <h3>Tags</h3>
            </div>
            <div class="modal-body">
                <form id="saveDiscussionTagsForm" ng-submit="saveDiscussionTags()">
                    <tags-input  ng-model="modalTags"
                                 display-property="tagLabel"
                                 placeholder="&{'discussion.placeholder.tag.add'}"
                                 replace-spaces-with-dashes="false"
                                 template="discussion-tag-template" class="no-glow">
                        <auto-complete source="loadTags($query)"
                                       min-length="0"
                                       load-on-focus="true"
                                       load-on-empty="true"
                                       max-results-to-show="100"
                                       select-first-match="false"
                                       template="autocomplete-template"></auto-complete>
                    </tags-input>
                    <div class="buttons">
                        <button class="btn btn-success" type="submit">&{'btn.save'}</button>
                        <button class="btn btn-danger" type="button" ng-click="cancelDiscussionTagsModification()" >&{'button.cancel'}</button>
                    </div>
                </form>
            </div>
        </div>
    #{/if}
    #{if _connectedLogin}
        #{if _allowCreation}
        <div class="row-fluid">
            <div class="span12">
                <button class="btn" id="newDiscussionButton" type="button" ng-hide="newDiscussionFormDisplayed || nbDiscussions == 0" ng-click="showNewDiscussionForm()" >&{'discussion.newDiscussion'}</button>
                <div id="newDiscussionFormDiv" ng-show="newDiscussionFormDisplayed || nbDiscussions == 0" class="row-fluid">
                    <div class="span2">
                        #{userImg _connectedLogin, imageId: _imageId /}
                    </div>
                    <div class="span10">
                        <form id="newDiscussionForm" ng-submit="createDiscussion()">

                            <div>
                                <textarea ng-model="newDiscussionForm.text" msd-elastic="\n" name="newDiscussion.text" id="newDiscussion.text" maxlength="2000" placeholder="&{'discussion.placeholder.message'}" required ></textarea>
                            </div>

                            <div ng-show="showNewDiscussionFormTitle">
                                <input ng-model="newDiscussionForm.title" type="text" id="newDiscussion.title" name="newDiscussion.title" maxlength="100" placeholder="&{'discussion.placeholder.title'}" />
                            </div>

                            <div ng-show="showNewDiscussionFormCategories">
                                <div id="newDiscussion_categories" class="discussion_categories">
                                    <button ng-cloak ng-repeat="category in categories" ng-class="(isSelectedNewDiscussionCategory(category.id)) ? 'selectedCategory' : ''" class="btn category" type="button" ng-click="addOrDeleteNewDiscussionCategory(category.id)">{{category.label}}</button>
                                </div>
                            </div>

                            #{if _isAdmin || _connectedLevel && _connectedLevel >= play.configuration.get("herbonautes.discussion.tags.minimum.level").toInteger()}
                                <tags-input  ng-show="showNewDiscussionFormTags" ng-model="tags"
                                            display-property="tagLabel"
                                            placeholder="&{'discussion.placeholder.tag.add'}"
                                            replace-spaces-with-dashes="false"
                                            template="discussion-tag-template" class="no-glow">
                                    <auto-complete source="loadTags($query)"
                                                   min-length="0"
                                                   load-on-focus="true"
                                                   load-on-empty="true"
                                                   max-results-to-show="100"
                                                   select-first-match="false"
                                                   template="autocomplete-template"></auto-complete>
                                </tags-input>

                            #{/if}
                            <div id="newDiscussionFormButtons">
                                <span id="showNewDiscussionFormTitleButton" ng-hide="showNewDiscussionFormTitle" ng-click="displayNewDiscussionFormTitle()" ng-cloak><i class="icon-bookmark"></i>Ajouter un titre</span>
                                <span id="showNewDiscussionFormCategoriesButton" ng-hide="showNewDiscussionFormCategories" ng-click="displayNewDiscussionFormCategories()" ng-cloak><i class="icon-folder-open"></i>Ajouter des catégories</span>
                                #{if _isAdmin || _connectedLevel && _connectedLevel >= play.configuration.get("herbonautes.discussion.tags.minimum.level").toInteger()}
                                    <span id="showNewDiscussionFormTagsButton" ng-hide="showNewDiscussionFormTags" ng-click="displayNewDiscussionFormTags()" ng-cloak><i class="icon-tags"></i>Ajouter des tags</span>
                                #{/if}
                            </div>
                            <div class="buttons">
                                <button ng-hide="saveInProgress" class="btn btn-success" type="submit">&{'btn.send'}</button>
                                <button ng-show="saveInProgress" class="btn btn-success" disabled type="submit">&{'btn.save.in.progress'}</button>
                                <button class="btn btn-danger" type="button" ng-click="hideNewDiscussionForm()" >&{'button.cancel'}</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
        #{/if}
    #{/if}

    <div id="discussion_categories_filters" class="discussion_categories" ng-show="!!!discussionId && nbDiscussions > 0">
        <button class="btn btn-info" id="filter_button">&{'discussion.filter'}</button>
        <button ng-cloak ng-repeat="category in categories" ng-class="(isSelectedCategoryFilter(category.id)) ? 'selectedCategory' : ''" class="btn category" type="button" ng-click="selectOrUnselectCategoryFilter(category.id)">{{category.label}}</button>
    </div>

    #{if !_connectedLogin || !_allowCreation}
        <p ng-hide="nbDiscussions > 0" class="noResults">
            &{'no.result'}
        </p>
    #{/if}

    <div ng-repeat="discussion in discussions" id="discussion{{discussion.id}}" class="discussion row-fluid {{ forceDiscussionId == discussion.id ? 'discussion_force' : '' }}">
        <div class="discussion_tags" ng-cloak>

            <div ng-repeat="tag in discussion.tags" ng-class="tag.tagType">
                <a ng-href="{{getTagUrl(tag.tagLabel)}}" ><i ng-class="tag.tagType == 'MISSION' ? 'icon-book' : tag.tagType == 'SPECIMEN' ? 'icon-leaf' : 'icon-tag'"></i>{{tag.tagLabel}}</a>
            </div>

            #{if _isAdmin || _connectedLevel && _connectedLevel >= play.configuration.get("herbonautes.discussion.tags.minimum.level").toInteger()}
                <div class="modify" ng-click="loadTagsModal(discussion)"><i class="icon-cog"></i>&{'discussion.tags.modify'}</div>
            #{/if}
        </div>
        <div class="message row-fluid" ng-repeat="message in discussion.messages" ng-show="$first || message.resolution || $index >= discussion.nbMessagesInit - 1 || isOpenedDiscussion(discussion.id)" ng-class="{'background-dark': $index % 2 == 0, 'background-light': $index % 2 == 1, 'no_margin': $first}">
            <div class="pull-left resolved_block" title="&{'discussion.resolved'}">
                &nbsp;<i class="icon-ok" ng-show="message.resolution"></i>
            </div>
            <div class="pull-left img_block">
                <img ng-src="{{getMessageImageUrl(message)}}" alt="{{message.author}}" ng-class="{'first_image': $first}" ng-cloak />
            </div>
            <div class="pull-right message_content_block">
                #{if _connectedLogin || _isAdmin}
                    <div title="&{'message.delete'}" class="pull-right solution_button"
                        #{if _isAdmin}
                         ng-show="message.moderator == ''"
                        #{/if}
                        #{else}
                         ng-show="(message.author == '${_connectedLogin}' || discussion.author == '${_connectedLogin}') && message.moderator == ''"
                        #{/else}
                         ng-click="deleteMessage(message.id, '&{'message.delete.confirmation'}')" ng-cloak>
                        <i class="icon-remove"></i>
                    </div>
                #{/if}
                #{if _connectedLogin || _isAdmin || _isLeader}
                    <div title="&{'discussion.resolve'}" class="pull-right solution_button"
                         #{if _isAdmin || _isLeader}
                            ng-hide="discussion.resolved || $first || message.moderator != ''"
                         #{/if}
                        #{else}
                         ng-hide="discussion.resolved || $first || discussion.author != '${_connectedLogin}' || message.moderator != ''"
                        #{/else}
                         ng-click="solveDiscussion(message.id, '&{'message.resolution.confirmation'}')" ng-cloak>
                        <i class="icon-ok"></i>
                    </div>
                #{/if}
                <div class="discussion_title row-fluid" ng-show="$first && discussion.title != null" ng-cloak>
                    {{discussion.title}}
                </div>
                <div class="message_author row-fluid" ng-if="message.author != '_'">
                    <a ng-href="{{getUserPageUrl(message.author)}}" ng-cloak>{{message.author}}</a>
                </div>
                    <div class="message_author row-fluid" ng-if="message.author == '_'">
                        <i>&{'user.deleted.account'}</i>
                    </div>
                <div class="message_content row-fluid">
                    <span ng-show="message.moderator == ''" ng-bind-html="message.text | htmlLinky" ng-cloak></span>
                    <span ng-show="message.moderator != ''">
                        &{'message.moderator'} {{message.moderator}}
                    </span>
                </div>
                <div class="message_timeago row-fluid">
                    *{<span am-time-ago="message.date + timeDifference"></span>}*
                    <span class="since" h-time-ago="message.date" server-time="${new Date().getTime()}"></span>
                </div>
            </div>
            <div class="showAllMessagesBlock"
                 ng-show="$first && discussion.nbMessagesInit > 2 && !isOpenedDiscussion(discussion.id) && !resolutionShowed(discussion)"
                    >
                <span class="solution_button" ng-click="openDiscussion(discussion.id)">Montrer tous les messages</span>
            </div>
        </div>

        #{if _connectedLogin}
            <div class="postResponseDiv row-fluid" ng-hide="discussion.resolved"  ng-class="(discussion.messages.length % 2 == 0) ? 'background-dark' : 'background-light'">
                <div class="postResponseResolvedBlank pull-left">&nbsp;</div>
                <div class="postResponseDivImgBlock pull-left">
                    #{userImg _connectedLogin, imageId: _imageId /}
                </div>
                <div class="postResponseFormDiv pull-right">
                    <form class="postResponseForm" ng-submit="postMessage(discussion.id)" ng-cloak>
                        <textarea msd-elastic="\n" ng-model="newMessageFields[discussion.id]" name="newMessage[{{discussion.id}}].text" placeholder="&{'discussion.placeholder.reply'}" maxlength="2000"></textarea>
                        <button class="btn btn-success" type="submit">&{'discussion.post'}</button>
                    </form>
                </div>
            </div>
        #{/if}

        <div class="closed_discussion_div row-fluid" ng-show="discussion.resolved"  ng-class="(discussion.messages.length % 2 == 0) ? 'background-dark' : 'background-light'">
            &{'discussion.close'}
        </div>





    </div>
    <div class="moreButtonDiv" ng-if="buttonMoreDisplayed">
        <a href="#" class="btn btn-success" onclick="return false;" ng-click="getDiscussionsFilteredByCategories()">&{'discussion.loadMore'}</a>
    </div>

</div>