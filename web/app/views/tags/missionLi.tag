<div class="mission-box content-box clearfix">

	<table class="mission-li">
		<tr>
			<td class="avatar">#{missionImg _mission /}</td>
			<td class="text">
			
				<h4 class="mission-title">
					<a href="@{Missions.show(_mission.id)}">${_mission.title}</a>
					
					#{if _isLeader}
						<br/>
						#{if _mission.isLeader(_userLogin)}
							<span class="label label-info">${_mission.leader.login}</span>
						#{/if}
						#{else}
							<span class="label">${_mission.leader.login}</span>
						#{/else}
						#{if _mission.published}
							<span class="label label-success">&{'published'}</span>
						#{/if}
						#{else}
							<span class="label label-warning">&{'not.published'}</span>
						#{/else}
					#{/if}
					
				</h4>
				<p>${_mission.shortDescription}</p>
				<p>
				#{if _userLogin != null}
					#{if _mission.isMember(_userLogin)}
						<input type="checkbox" checked disabled /> <span class="light">&{'member'}</span>
					#{/if}
					#{else}
						<a href="@{Missions.show(id: _mission.id)}" class="btn btn-small btn-info">&{'btn.visit'}</a>
					#{/else}
				#{/if}
				</p>
			</td>
		</tr>
	</table>
	

</div>