#{if _absoluteURL}
	#{if _arg.specificEpithet}
    	<a href="@@{Specimens.list(genus: _arg.genus, specificEpithet: _arg.specificEpithet)}">${_arg.getGenusSpecies()}</a>
	#{/if}
	#{else}
    	<a href="@@{Specimens.list(genus: _arg.family, specificEpithet: _arg.genus)}">${_arg.getGenusSpecies()}</a>
	#{/else}
	(<a href="@@{Specimens.show(institute: _arg.institute, collection: _arg.collection, code: _arg.code)}">${_arg.institute}/${_arg.collection}/${_arg.code}</a>)
#{/if}
#{else}
	#{if _arg.specificEpithet}
		<a href="@{Specimens.list(genus: _arg.genus, specificEpithet: _arg.specificEpithet)}">${_arg.getGenusSpecies()}</a>
	#{/if}
	#{else}
    	<a href="@{Specimens.list(genus: _arg.family, specificEpithet: _arg.genus)}">${_arg.getGenusSpecies()}</a>
	#{/else}
	(<a href="@{Specimens.show(institute: _arg.institute, collection: _arg.collection, code: _arg.code)}">${_arg.institute}/${_arg.collection}/${_arg.code}</a>)	
#{/else}