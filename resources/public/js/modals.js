$(document).ready(function() {
	
	////////////////////////////////////////////////////////////////////////////
	// Publish Post Modal

	$('a[data-publishpostid]').click(function() {
		var postId = $(this).data('publishpostid');

		$('#publishModalOkNoReset').data('forpostid', postId);
		$('#publishModalOk').data('forpostid', postId);
		$('#publishModal').modal({backdrop: 'static', keyboard: false});
	});

	$('#publishModalOkNoReset').click(function() {
		var postId = $(this).data('forpostid');
		$('#publishModal').find('button').attr('disabled', 'disabled');
		window.location = context + "/publish/" + postId;
	});

	$('#publishModalOk').click(function() {
		var postId = $(this).data('forpostid');
		$('#publishModal').find('button').attr('disabled', 'disabled');
		window.location = context + "/publish/" + postId + "?reset-date=true"; 
	});
	
	////////////////////////////////////////////////////////////////////////////
	// Delete Post Modal
	
	$('a[data-deletepostid]').click(function() {
		var postId = $(this).data('deletepostid');
		
		$('#deleteModalOk').data('forpostid', postId);
		$('#deleteModal').modal({backdrop: 'static', keyboard: false});
	});
	
	$('#deleteModalOk').click(function() {
		var postId = $(this).data('forpostid');
		$('#deleteModal').find('button').attr('disabled', 'disabled');
		window.location = context + "/deletepost/" + postId; 
	});
	
	////////////////////////////////////////////////////////////////////////////
})
