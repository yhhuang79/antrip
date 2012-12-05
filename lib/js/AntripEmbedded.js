<!--
//	$("#map_canvas").ready(function(){
	var userid;
	var trip_id;
	var g_lang;
	var g_hashCode;
	if(g_ready==false){
		var overlay = jQuery('<div valign=center id="overlay"></div>');
		overlay.appendTo(document.body);
		$("#overlay").css("display","block");
		google.load('visualization', '1', {packages: ['corechart']});
		userid = $.getUrlVar('userid');
		trip_id = $.getUrlVar('trip_id');
		g_trip = trip_id;
		g_lang = $.getUrlVar('g_lang');
		g_calUnit = $.getUrlVar('g_calUnit');
		g_hashCode = $.getUrlVar('hash');
		$('#images_scroll').css("background-image", url+images_scroll_img+")");
		$("#calculate_area").css("background-image", url+im+"paper.png"+")");
		$("#calculate_area").css('margin-top',"220px");
		$("#end_Text").css('float',"center");
		$("#end_Text").css('margin-top',"810px");
		//$("#end_Text").css('margin-left',"250px");
		//$("#end_Text").css('width',"auto");
		setLanguage(true);
		//alert(g_hashCode);
		GetTripPointfromID(g_hashCode, g_calUnit);
		showTripEditBtList(userid);
		g_ready =true;
		$("#img_des_ant").attr('src',im+'ant_24.png');
		$("#overlay").html(g_str_loading);
		$("#overlay").css("display","none");
	}
	else{
		$("#copyStaticMap").click(function(){
			captureStaticTripMap();
		});
		g_ready =true;
	}

	function setMarkerOpen(){

	}
//	});
//-->