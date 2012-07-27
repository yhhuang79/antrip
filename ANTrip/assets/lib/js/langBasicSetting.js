<!--
	//set multi-langauge string to object
	$('.login_bt_link').html("<p>" + g_str_login+"</p>");
	$("#username").attr("placeholder", g_username_holder);
	$("#password").attr("placeholder", g_password_holder);
	$("#takemarker_checkin").html("<p>" +g_str_checkin+"</p>");
	$("#takemarker_cancel").html("<p>" +g_str_cancel+"</p>");

	$('#b_open_trip').find('.tip').html(g_str_localization);
	$('#b_seq_trip').find('.tip').html(g_str_markplace);
	$('#b_add_note').find('.tip').html(g_str_startrecording);

	initEmotionMap();
//-->