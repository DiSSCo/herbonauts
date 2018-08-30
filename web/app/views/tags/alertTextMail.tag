
#{if _arg.type == 'CONFLICT'}

	#{set 'userLink'}#{userLink _arg.contribution.user, absoluteURL: true/}#{/set}
	#{set 'specimenLink'}#{specimenLink _arg.contribution.specimen /}#{/set}
	
	&{'alert.conflict', userLink, specimenLink}
		
#{/if}

#{else}

	${_arg.type}

#{/else}

