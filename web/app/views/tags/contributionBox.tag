*{~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  
	Tag générique de boite de contribution
    
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~}* 

<div 
	data-level-required="${_mission.requiredLevel(_type)}"
	data-type="${_type}"  
	data-cascade="${_cascade}"
	class="contribution-box closed empty">
	
	<h4 class="title">
		&{'contribution.' + _type.toLowerCase()}
		<a href="#" class="box-link close-box-link hide"><i class="icon-chevron-up"></i></a>
		<a href="#" class="box-link open-box-link hide"><i class="icon-chevron-down"></i></a>
	</h4>

	<span class="lock-cause"></span>

	#{doBody /}
	
	<div class="hidden-links">
			#{removeLink type: _type /}
	</div>
		
	<div class="status done"><i class="icon-ok-circle"></i></div>
	<div class="status unusable"><i class="icon-ban-circle"></i></div>
	<div class="status lock"><i class="icon-lock"></i></div>
	<div class="status pending"><i class="icon-lock"></i></div>
	<div class="status conflicts"><i class="icon-warning-sign"></i></div>
	<div class="status complete"><i class="icon-ok-sign"></i></div>
	
	<div class="help help-${_type.toLowerCase()}"><a href="#">&{'contribution.help'} <i class="icon-question-sign"></i></a></div>
	
</div>

<script>
	$(function() {
		$('.help-${_type.toLowerCase()}').popover({
			title: '&{'contribution.help.title.' + _type.toLowerCase()}',
			content: '&{'contribution.help.content.' + _type.toLowerCase()}',
			trigger: 'hover'
		})
	})
</script>


