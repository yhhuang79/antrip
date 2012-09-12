//borrowed from jQuery easing plugin
//http://gsgd.co.uk/sandbox/jquery.easing.php
require("lib/jq_includes/jquery.scrollTo-min.js");
require("lib/jq_includes/jquery.scrollShow-min.js");
require("lib/jq_includes/jquery.easing.1.3.js");

function setAlbumScroll(){
	$.easing.backout = function(x, t, b, c, d){
		var s=1.70158;
		return c*((t=t/d-1)*t*((s+1)*t + s) + 1) + b;
	};
	$('#images_scroll').scrollShow({
		view:'#view_scroll',
		content:'#images_scroll_content',
		easing:'backout',
		wrappers:'link,crop',
		navigators:'a[id]',
		navigationMode:'s',
		circular:true,
		start:0
	});
}