$(document).ready(function() {
	var code = $('div.content[role="main"]').find('pre');
	code.each(function() {
		var pre = $(this);

		// markdown-clj sets up <pre> tags for use with another syntax highlighting lib that we're
		// not using. so we need to convert some css classes to work with highlight.js
		pre.removeClass('brush:');
		var languageHint = pre.attr('class'); // should be the only class left at this point

		var classAttr = '';
		if (languageHint && languageHint.length > 0) {
			if (languageHint == 'no-highlight')
				classAttr = 'no-highlight';
			else
				classAttr += ' lang-' + languageHint;
		}

		classAttr += ' hljs';

		pre.attr('class', classAttr);

		hljs.highlightBlock(this);
	});
});
