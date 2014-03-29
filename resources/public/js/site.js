$(document).ready(function() {
	var code = $('div.content[role="main"]').find('pre');
	code.addClass('prettyprint');
	
	prettyPrint();
});
