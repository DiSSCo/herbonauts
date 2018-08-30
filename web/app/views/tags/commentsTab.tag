
<div class="centered">
	<a href="#" id="older-comments-btn">&{'show.all.comments'}</a>
</div>

<ul 
	id="comments-list" 
	class="comments paginable"
	data-paginate-url="${_commentsURL}"
	data-before-attr="comment-id"
	data-paginate-btn="#older-comments-btn" 
	data-insert-type="prepend">
	     
	<li id="no-comment" class="hide">&{'no.comment'}</li>    
	     
</ul>
	     
#{doBody /}	     
	     
<script>

	var initializedComments = false;

	$('#comments-list').bind('page-received', function(event, comments) {
			$.each(comments, function(i, comment) {
				herbonautes.appendComment('#comments-list', comment, { top: true });
			});
			//console.log(comments.length);
			if ($('#comments-list').data('current-page') == 1) {
				$('#older-comments-btn').hide();
			}
			
			if ($('#older-comments-btn').data('disabled') == 'true') {
				$('#older-comments-btn').show();
			}
			
			return false;
		}).bind('page-no-data', function(event) {
			$('#older-comments-btn').hide();
			$('#no-comment').show();
		});
     		
	$('#${_formId}').submit(function() {
		var $form = $(this);
		
		// Disable
		var $btn = $('[type=submit]', $form);
		var _oldText = $btn.val();
		$btn.attr('disabled', 'disabled');
		$btn.val('&{'btn.sending'}');
		
		function restoreButton() {
			$btn.attr('disabled', null);
			$btn.val(_oldText);
		}
		
		var url = $form.attr('action');
		var data = $form.serialize();
		
		$.ajax({
			url: url,
			type: 'POST',
			data: data,
			dataType: 'json',
			success: function(data) {
				restoreButton();
				$('#no-comment').hide();
				$('#comment-field').val('');
				herbonautes.appendComment('#comments-list', data);
				oldCommentCount = parseInt($('#comments-tab span').html());
    			$('#comments-tab span').html(oldCommentCount+1);
				$(window).scrollTop(
		  				$('#comments-list').height() + 
		  				$('#comments-list').position().top - 
		  				$(window).height() + 
		  				100); // 100 px de marge
			},
			error: function(jqXHR) {
				restoreButton();	
			}
		});
		
		return false;
	});
	
	
	$('#${_tabId}').on('shown', function() {
		if(!initializedComments) {
			$('#older-comments-btn').hide().data('disabled', 'true');
			initializedComments = true;
			$('#comments-list').paginate();
		}
	})

</script>