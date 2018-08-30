<div class="message">

#{if _arg.type == 'CONFLICT'}

	#{set 'userLink'}#{userLink _arg.contribution.user, absoluteURL: _absoluteURL /}#{/set}
	#{set 'specimenLink'}#{specimenLink _arg.contribution.specimen, absoluteURL: _absoluteURL /}#{/set}
	#{set 'specimenURL'}#{specimenURL _arg.contribution.specimen, absoluteURL: _absoluteURL /}#{/set}
	#{set 'type'}&{'contribution.' + _arg.contribution.type.toLowerCase()}#{/set}
	#{set 'proposition'}#{contributionValue _arg.contribution /}#{/set}
	#{set 'initiale'}#{contributionValue _arg.contributionInitiale() /}#{/set}
	&{'alert.conflict', userLink, specimenLink, specimenURL, type, proposition, initiale}

#{/if}

#{elseif _arg.type == 'CONFLICT_V2'}

    #{set 'userLink'}#{userLink _arg.otherUser, absoluteURL: _absoluteURL /}#{/set}
    #{set 'specimenLink'}#{specimenBoardLink _arg.specimen, absoluteURL: _absoluteURL, question: _arg.question /}#{/set}
    #{set 'questionLabel'}&{_arg.question.label}#{/set}
	#{set 'specimenURL'}#{specimenURL _arg.specimen, absoluteURL: _absoluteURL /}#{/set}

	#{set 'otherAnswer'}
		#{list items: _arg.getOtherAnswer().toHumanValue(), as: 'val'}
			#{if val.isBoolean}
				#{if val.booleanValue}
                    <em>${val.label?.injectContext(_arg.specimen)}</em>
				#{/if}
			#{/if}
			#{else}
			${val.textValue}
			#{/else}
		#{/list}
	#{/set}

	#{set 'userAnswer'}
		#{list items: _arg.answer.toHumanValue(), as: 'val'}
			#{if val.isBoolean}
				#{if val.booleanValue}
                    <em>${val.label?.injectContext(_arg.specimen)}</em>
				#{/if}
			#{/if}
			#{else}
			${val.textValue}
			#{/else}
		#{/list}
	#{/set}

    &{'alert.conflict.v2', userLink, specimenLink, questionLabel, specimenURL, userAnswer, otherAnswer}

#{/elseif}

#{elseif _arg.type == 'ANNOUNCEMENT'}

	#{set 'missionLink'}#{missionLink _arg.mission, absoluteURL: _absoluteURL /}#{/set}
	
	&{'alert.announce', missionLink}
		
#{/elseif}


#{elseif _arg.type == 'MISSION_PUBLISHED'}

	#{set 'missionLink'}#{missionLink _arg.mission, absoluteURL: _absoluteURL /}#{/set}
	
	&{'alert.mission.published', missionLink}
		
#{/elseif}

#{elseif _arg.type == 'MISSION_COMMENT'}

	#{set 'userLink'}#{userLink _arg.missionComment.user, absoluteURL: _absoluteURL /}#{/set}
	#{set 'missionLink'}#{missionLink _arg.missionComment.mission, absoluteURL: _absoluteURL /}#{/set}
	
	&{'alert.mission.comment', userLink,  missionLink}
		
#{/elseif}

#{elseif _arg.type == 'SPECIMEN_COMMENT'}

	#{set 'userLink'}#{userLink _arg.specimenComment.user, absoluteURL: _absoluteURL /}#{/set}
	#{set 'specimenLink'}#{specimenLink _arg.specimenComment.specimen, absoluteURL: _absoluteURL /}#{/set}
	
	&{'alert.specimen.comment', userLink, specimenLink, _arg.specimenComment.getText()} 
		
#{/elseif}

#{else}

	${_arg.type}

#{/else}

#{if !_absoluteURL}
<p class="since">&{'message.since', _arg.date?.since()}</p>
#{/if}
</div>

#{if !_absoluteURL}
<a href="#" class="mark-alert-read" title="&{'mark.as.read'}" data-alert-id="${_arg.id}"><i class="icon-remove"></i></a>
#{/if}