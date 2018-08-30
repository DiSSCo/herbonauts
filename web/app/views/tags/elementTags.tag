<div id="elementTags" ng-controller="elementTagsCtrl"  ng-init="init('${_tagLinkType}', '${_targetId}', '${_principalTagLabel}')" class="row-fluid">
    #{if _connectedLogin && (_isAdmin || _connectedLevel >= play.configuration.get("herbonautes.elements.tags.save.minimum.level").toInteger())}
        <form id="saveTagsForm" ng-submit="saveTags()">
            <tags-input ng-model="tagsToAdd"
                        display-property="tagLabel"
                        placeholder="&{'discussion.placeholder.tag.add'}"
                        replace-spaces-with-dashes="false"
                        template="element-tag-template" class="no-glow">
                <auto-complete source="loadTagsAutoComplete($query)"
                               min-length="0"
                               load-on-focus="true"
                               load-on-empty="true"
                               max-results-to-show="100"
                               select-first-match="false"
                               template="autocomplete-template"></auto-complete>
            </tags-input>

            <script type="text/ng-template" id="element-tag-template">
                <div class="element-tag-template">
                    <div class="panel" ng-class="data.tagType">
                        <span><i ng-class="data.tagType == 'MISSION' ? 'icon-book' : data.tagType == 'SPECIMEN' ? 'icon-leaf' : 'icon-tag'"></i> {{$getDisplayText()}}</span>
                        <a ng-if="data.tagLabel != '${_principalTagLabel}'" class="remove-button" ng-click="$removeTag()">&#10006;</a>
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
            <div class="buttons">
                <button class="btn btn-success" type="submit">&{'btn.save'}</button>
                #{if !_hideCancelButton}
                    <button class="btn btn-danger" type="button" ng-click="cancel()" >&{'button.cancel'}</button>
                #{/if}
            </div>
        </form>

    #{/if}

    <div id="tags-list">
        <div ng-repeat="tag in elementTags" ng-class="tag.tagType">
            <i ng-class="tag.tagType == 'MISSION' ? 'icon-book' : tag.tagType == 'SPECIMEN' ? 'icon-leaf' : 'icon-tag'"></i><a href="#" ng-click="showTag(tag.tagLabel)" onclick="return false;">{{tag.tagLabel}}</a>
            #{if _connectedLogin && (_isAdmin || _connectedLevel >= play.configuration.get("herbonautes.elements.tags.save.minimum.level").toInteger())}
                <a ng-if="tag.tagLabel != principalTagLabel" class="remove-button" ng-click="deleteTagLink(tag.tagId)">&#10006;</a>
            #{/if}
        </div>
    </div>


</div>