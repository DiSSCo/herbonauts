#{if _absoluteURL}
	<a href="@@{Missions.show(_arg.id)}">${_arg.title}</a>
#{/if}
#{else}
	<a href="@{Missions.show(_arg.id)}">${_arg.title}</a>
#{/else}