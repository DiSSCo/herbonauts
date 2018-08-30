%{ connectedLogin = session.get('_ulo') }%
#{if connectedLogin && connectedLogin == _arg}
	#{doBody /}
#{/if}