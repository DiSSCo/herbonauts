#{if _absoluteURL}
	<a href="@@{Users.show(_arg.login)}">${_arg.login}</a>
#{/if}
#{else}
	<a href="@{Users.show(_arg.login)}">${_arg.login}</a>
#{/else}