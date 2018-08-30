	#{if _arg.notPresent}
		&{'not.present'}
	#{/if}
	#{else}
	
	
		#{if _arg.type == 'COUNTRY'}
			${_arg.country?.name}
			
		#{/if}
		
		#{elseif _arg.type == 'REGION_1'}
		
			${_arg.regionLevel1?.name}
			
		#{/elseif}
		
		#{elseif _arg.type == 'REGION_2'}
		
			${_arg.regionLevel2?.name}
			
		#{/elseif}
		
		#{elseif _arg.type == 'DATE'}
		
			#{if !_arg.period}
				Le ${_arg.collectDate?.format('dd MMMM yyyy')}
			#{/if}
			#{else}
				Entre ${_arg.collectStartDate?.format('dd MMMM yyyy')}
				et ${_arg.collectEndDate?.format('dd MMMM yyyy')}
			#{/else}
			
		#{/elseif}
		
		#{elseif _arg.type == 'LOCALITY'}
		
			${_arg.locality}
			
		#{/elseif}
		
		#{elseif _arg.type == 'GEOLOCALISATION'}
		
			${_arg.latitude} - ${_arg.longitude}
			
		#{/elseif}
		
		#{elseif _arg.type == 'COLLECTOR'}
		
			#{if _arg.collectorNotPresent}
				&{'not.present'}
			#{/if}
			#{else}
				${_arg.collector.name}
				#{if _arg.otherCollectors}
					#{list items:_arg.otherCollectors.name, as:'name'}
					 	, ${name}
					#{/list}
				#{/if}
			#{/else}
			
		#{/elseif}
		#{elseif _arg.type == 'IDENTIFIEDBY'}
		
			#{if _arg.determinerNotPresent }
				&{'not.present'}
			#{/if}
			#{else}
				${_arg.determiner.name}
			#{/else}
		#{/elseif}

	#{/else}

