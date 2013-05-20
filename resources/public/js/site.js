$(document).ready(function() {
	var code = $('div.content[role="main"]').find('pre');
	code.addClass('prettyprint');
	
	// TODO: properly fix this in markdown-clj instead of doing this here...
	//       the problem is markdown-clj inserts newlines immediately after
	//       the opening <pre><code> bit which looks a bit silly
	code.each(function() {
		var text = $(this).text();
		if (text.length >= 2 && text.substring(0, 2) === '\r\n')
			$(this).text(text.substring(2));
		else if (text.length >= 1 && text.substring(0, 1) === '\n')
			$(this).text(text.substring(1));
	});
	
	prettyPrint();
});
