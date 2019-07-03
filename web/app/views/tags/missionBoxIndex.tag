<div class="mission-box-index" data-mission-url="@{Missions.show(_arg.id)}">

	<table class="mission-table">
		<tr>
			<td class="avatar">
				<a href="@{Missions.show(_arg.id)}">#{missionImg _arg /}</a>
			</td>
			<td class="description">
				<h4 class="mission-title">
				<a href="@{Missions.show(_arg.id)}">${_arg.title}</a>
				</h4>
				<p>${_arg.shortDescription}</p>
				
				#{if _isLeader}
						<br/>
						#{list items: _mission.getLeaderList(), as: 'leader'}

							#{if leader.visible}
								#{if leader.userLogin == _userLogin}
									<span class="label label-info">${leader.userLogin}</span>
								#{/if}
								#{else}
									<span class="label">${leader.userLogin}</span>
								#{/else}
							#{/if}

						#{/list}


						#{if _mission.published}
							<span class="label label-success">&{'published'}</span>
						#{/if}
						#{else}
							<span class="label label-warning">&{'not.published'}</span>
						#{/else}
				#{/if}
				
			</td>
			<td class="stats">
				*{ %{
				 	if (_arg.goal > 0) {
				 		goalRatio = _arg.getContributionsCount() * 100 / _arg.goal;
				 	} else {
				 		goalRatio = 100;
				 	}
				 	if (goalRatio > 100) goalRatio = 100;
				 	goalRatio = java.lang.Math.round(goalRatio)
				 }% }*


				#{set contributionsCompletedCount: _arg.getCompletedSpecimensCount() /}
				#{set specimenCount: _arg.getSpecimensCount() /}

				%{
                    if (specimenCount > 0) {
                    	goalRatio = contributionsCompletedCount * 100 / specimenCount;
                    } else {
                    	goalRatio = 0;
                    }
                    if (goalRatio > 100) {
                    	goalRatio = 100;
                    }
					goalRatio = Math.round(goalRatio)

				}%


				
				<div class="percent">
					${goalRatio}%
				</div>
				<div class="goal">

					&{'mission.goal.specimens', specimenCount}

				</div>
				<div class="button">
					<a href="@{Missions.show(_arg.id)}" class="btn btn-primary">&{_arg.isOpened() ? 'mission.btn.participate' : 'mission.show.btn.join.soon'}</a>
				</div>
			</td>
		</tr>
	</table>
			
</div>	
