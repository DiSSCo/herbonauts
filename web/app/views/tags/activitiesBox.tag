*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Liste d'activité
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}* 
%{ uuid = java.util.UUID.randomUUID() }%

<ul 
	id="${_id}-list-${uuid}" 
	class="activities paginable"
	data-paginate-url="@{Activities.list}?${_filter}"
	data-paginate-btn="#${_id}-btn-${uuid}"
	data-before-attr="activity-id">
</ul>
					
<div class="centered">
	<a class="btn btn-small" id="${_id}-btn-${uuid}">&{'activity.btn.more'}</a>
	<a class="btn btn-small hide" id="${_id}-loading-${uuid}">&{'activity.loading'}</a>
	<span class="hide" id="${_id}-no-data-${uuid}">&{'activity.no.data'}</span>
</div>
					
<script type="text/javascript">
	herbonautes.initActivityList(
		'#${_id}-list-${uuid}',
		'#${_id}-btn-${uuid}',
		'#${_id}-loading-${uuid}',
		'#${_id}-no-data-${uuid}'
	);
</script>




