$(document).ready(function() {
	var code = $('div.content[role="main"]').find('pre');
	code.each(function() {
		var pre = $(this);

		// markdown-clj sets up <pre> tags for use with another syntax highlighting lib that we're
		// not using. so we need to convert some css classes to work with Prettify
		pre.removeClass('brush:');
		var languageHint = pre.attr('class'); // should be the only class left at this point

		var classAttr = 'prettyprint';
		if (languageHint && languageHint.length > 0)
			classAttr += ' lang-' + languageHint;

		pre.attr('class', classAttr);
	});

	prettyPrint();
});
