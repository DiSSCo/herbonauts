*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Tag générique d'affichage des 3 derniers messages
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}*

<div id="lastMessagesBox" ng-controller="lastMessagesBoxCtrl"  ng-init="init('${_tagType}', '${_tagLabel}')" class="row-fluid">

    #{if !_connectedLogin}
        <p ng-hide="messages.length > 0" class="noResults">
            &{'no.result'}
        </p>
    #{/if}

    <div class="message row-fluid" ng-repeat="message in messages" >
        <div class="pull-left img_block">
            <img ng-src="{{getMessageImageUrl(message)}}" alt="{{message.author}}" ng-cloak />
        </div>

        <div class="pull-right message_content_block">
            <div class="message_author row-fluid">
                <a ng-if="message.author != '_'" ng-href="{{getUserPageUrl(message.author)}}" ng-cloak>{{message.author}}</a>
                <em ng-if="message.author == '_'" ng-cloak>&{'user.deleted.account'}</em>
            </div>
            <div class="message_content row-fluid">
                <span ng-show="message.moderator == ''" ng-bind-html="message.text | htmlLinky" ng-cloak></span>
            <span ng-show="message.moderator != ''">
                &{'message.moderator'} {{message.moderator}}
            </span>
            </div>
            <div class="message_timeago row-fluid">
                *{<span am-time-ago="message.date  + timeDifference"></span>}*
                <span class="since" h-time-ago="message.date" server-time="${new Date().getTime()}"></span>
            </div>
        </div>

    </div>
</div>