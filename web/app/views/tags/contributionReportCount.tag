#{if _arg && _arg.count > 0}

	<span class="badge ${_arg.complete ? 'badge-success' : _arg.conflicts ? 'badge-warning' : '' }">${_arg.count}</span>

#{/if}
#{else}
	&middot;
#{/else}


