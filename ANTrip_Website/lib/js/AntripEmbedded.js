<!--
//	$("#map_canvas").ready(function(){
	var userid;
	var trip_id;
	var g_lang;
	if(g_ready==false){
		var overlay = jQuery('<div valign=center id="overlay"></div>');
		overlay.appendTo(document.body);
		$("#overlay").css("display","block");
		google.load('visualization', '1', {packages: ['corechart']});
		userid = $.getUrlVar('userid');
		trip_id = $.getUrlVar('trip_id');
		g_lang = $.getUrlVar('g_lang');
		g_calUnit = $.getUrlVar('g_calUnit');
		$('#images_scroll').css("background-image", url+images_scroll_img+")");
		$("#calculate_area").css("background-image", url+im+"paper.png"+")");
		$("#calculate_area").css('margin-top',"200px");
		$("#end_Text").css('float',"left");
		$("#end_Text").css('margin-top',"800px");
		$("#end_Text").css('margin-left',"250px");
		$("#end_Text").css('width',"auto");
		setLanguage(true);
		GetTripPointfromID(userid, trip_id, g_calUnit);
		g_ready =true;
		$("#img_des_ant").attr('src',im+'ant_24.png');
		$("#overlay").html(g_str_loading);
		$("#overlay").css("display","none");
	}
	else{
		//alert(g_ready);
//		g_lang = $.getUrlVar('g_lang');
		$("#copyStaticMap").click(function(){
			captureStaticTripMap();
		});
		//setTimeout("captureStaticTripMap()", 3000);
		g_ready =true;
	}

	function captureStaticTripMap(){
			$("#overlay").css("display","block");
			html2canvas( [ document.getElementById("map_canvas") ], {
				onrendered: function( canvas ) {
					var img = canvas.toDataURL();
					var meta = document.createElement('meta');
					meta.setAttribute("property", "og:image");
					meta.setAttribute("content", img);
					(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(meta);    
					$("#overlay").css("display","none");
					window.open(img);
				}
			});
	}

	function setMarkerOpen(){
		//OpenAllMarkerInfo(true);
	}
//	});
//-->