<div class="mission-box">
	#{missionImg _arg /}
	<h4 class="mission-title">
		<a href="@{Missions.show(_arg.id)}">${_arg.title}</a>
	</h4>
	<p>${_arg.shortDescription}</p>
	<p>
	#{if _userLogin != null}
		#{if _arg.isMember(_userLogin)}
			<input type="checkbox" checked disabled /> <span class="light">&{'member'}</span>
		#{/if}
		#{else}
			#{form method: 'POST', action: @Missions.addMember(_arg.id) }
			<input type="hidden" name="login" value="${_userLogin}" />
			<input type="submit" class="btn btn-info" value="&{'mission.show.btn.join'}" />
			#{/form}
		#{/else}
	#{/if}
	</p>
	#{if _stats}
		<div class="mission-stats">
			<ul>
				<li>${_arg.membersCount} &{'members'}</li>
				<li>${_arg.specimensCount} &{'mission.box.specimens'}</li>
				<li>${_arg.contributionsCount} &{'mission.box.contributions'}</li>
				<li>&{'mission.box.date'} ${_arg.openingDate.format('dd MMMM yyyy')}</li>
			</ul>
			
		</div>
	#{/if}
</div>	