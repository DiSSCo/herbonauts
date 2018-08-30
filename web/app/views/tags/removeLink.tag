#{if _arg}
	<a class="remove-link" data-type="${_type}" href="@{Contributions.delete(id: _arg.id)}"><i class="icon-trash"></i></a>
#{/if}
#{else}
	<a class="remove-link" data-type="${_type}" href="#"><i class="icon-trash"></i></a>
#{/else}