*{ -------
	Tag de traduction des activités selon leur type
   ------- }*
<span class="text">
#{if _arg.type == 'CONTRIBUTION_ADD'}

	#{set 'missionURL'}
		@{Missions.show(_arg.contribution.mission.id)}
	#{/set}
	#{set 'userURL'}
		@{Users.show(_arg.user.login)}
	#{/set}
	#{set 'specimenURL'}
		@{Specimens.show(_arg.contribution.specimen.institute, _arg.contribution.specimen.collection, _arg.contribution.specimen.code)}
	#{/set}
	&{_arg.getI18nKey(), 
		_arg.user.login, 
		userURL,
		_arg.contribution.getI18nKey() + '.art',
		_arg.contribution.specimen.code,
		specimenURL,
		_arg.contribution.mission.title,
		missionURL}
#{/if}

#{elseif _arg.type == 'BADGE_WIN'}

	#{set 'userURL'}
		@{Users.show(_arg.user.login)}
	#{/set}
	
	&{_arg.getI18nKey(), 
		_arg.user.login, 
		userURL,
		_arg.badge.getI18nKey()}
#{/elseif}

#{elseif _arg.type == 'MISSION_JOIN'}

	#{set 'missionURL'}
		@{Missions.show(_arg.mission.id)}
	#{/set}
	#{set 'userURL'}
		@{Users.show(_arg.user.login)}
	#{/set}
	
	&{_arg.getI18nKey(), 
		_arg.user.login, 
		userURL,
		_arg.mission.title,
		missionURL}
#{/elseif}
#{else}

	ACTIVITY ${_arg.type}
#{/else}
</span>
<span class="since">
&{'message.since', _arg.date.since()}
</span>
