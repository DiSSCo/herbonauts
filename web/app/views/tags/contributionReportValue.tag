#{if _arg && _arg.complete && _arg.validatedContribution}

	#{if _arg.validatedContribution.notPresent}
		<span class="label label-important">&{'not.present'}</span>
	#{/if}
	#{else}
	
	
		#{if _arg.validatedContribution.type == 'COUNTRY'}
		
			${_arg.validatedContribution.country?.name}
			
		#{/if}
		
		#{elseif _arg.validatedContribution.type == 'REGION_1'}
		
			${_arg.validatedContribution.regionLevel1?.name}
			
		#{/elseif}
		
		#{elseif _arg.validatedContribution.type == 'REGION_2'}
		
			${_arg.validatedContribution.regionLevel2?.name}
			
		#{/elseif}
		
		#{elseif _arg.validatedContribution.type == 'DATE'}
			#{if !_arg.validatedContribution.period}
				Le ${_arg.validatedContribution.collectDate?.format('dd MMMM yyyy')}
			#{/if}
			#{else}
				Entre ${_arg.validatedContribution.collectStartDate?.format('dd MMMM yyyy')}
				et ${_arg.validatedContribution.collectEndDate?.format('dd MMMM yyyy')}
			#{/else}
			
		#{/elseif}
		#{elseif _arg.validatedContribution.type == 'LOCALITY'}
		
			${_arg.validatedContribution.locality}
			
		#{/elseif}
		
		#{elseif _arg.validatedContribution.type == 'GEOLOCALISATION'}
		
			${_arg.validatedContribution.latitude} - ${_arg.validatedContribution.longitude}
			
		#{/elseif}
		
		#{elseif _arg.validatedContribution.type == 'COLLECTOR'}
		
			#{if _arg.validatedContribution.collectorNotPresent }
				<span class="label label-important">&{'not.present'}</span>
			#{/if}
			#{else}
				${_arg.validatedContribution.collector.name}
				#{if _arg.validatedContribution.otherCollectors}
					#{list items:_arg.validatedContribution.otherCollectors.name, as:'name'}
					 	, ${name}
					#{/list}
				#{/if}
			#{/else}
			
		#{/elseif}
		#{elseif _arg.validatedContribution.type == 'IDENTIFIEDBY'}
		
			#{if _arg.validatedContribution.determinerNotPresent }
				<span class="label label-important">&{'not.present'}</span>
			#{/if}
			#{else}
				${_arg.validatedContribution.determiner.name} 	
			#{/else}
		#{/elseif}
		
	#{/else}
#{/if}

#{else}
	<em>&{'to.be.determined'}</em>
#{/else