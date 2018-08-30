#{if _absoluteURL}
<a href="@@{ContributionBoard.showBoard(_arg.mission.id, _arg.id)}?conflicts=${_question.name}">${_arg.code} (${_arg.getGenusSpecies()})</a>
#{/if}
#{else}
<a href="@{ContributionBoard.showBoard(_arg.mission.id, _arg.id)}?conflicts=${_question.name}">${_arg.code} (${_arg.getGenusSpecies()})</a>
#{/else}

