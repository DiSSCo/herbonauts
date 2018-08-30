#{if _arg.hasImage}
	<img src="@{Missions.image(_arg.id, _arg.imageId)}" alt="Image ${_arg.title}" />
#{/if}
#{else}
	<img src="@{'/public/img/leaf.png'}" alt="Image ${_arg.title}" />
#{/else}