<!--
	//set multi-langauge string to object
	var g_Tooltip = {	
		excited: g_str_excited,
		happy: g_str_happy,
		pleased: g_str_pleased,
		relaxed: g_str_relaxed,
		peaceful: g_str_peaceful,
		sleepy: g_str_sleepy,
		sad: g_str_sad,
		bored: g_str_bored,
		nervous: g_str_nervous,
		angry: g_str_angry,
		calm: g_str_calm
	};

	$('.login_bt_link').html("<p style='font-szie:24px;'><font size=24px><b>" + g_str_login+"</b></font></p>");
	$('.fblogin_bt_link').html("<p>" + g_str_fblogin+"</p>");
	$("#username").attr("placeholder", g_username_holder);
	$("#password").attr("placeholder", g_password_holder);
	$("#takemarker_checkin").html("<p>" +g_str_checkin+"</p>");
	$("#takemarker_cancel").html("<p>" +g_str_cancel+"</p>");

	$('#b_open_trip').find('.tip').html(g_str_localization);
	$('#b_seq_trip').find('.tip').html(g_str_markplace);
	$('#b_add_note').find('.tip').html(g_str_startrecording);

	$('#tip_ub_trip_m').html(g_str_recordtrip);
	$('#tip_ub_trip_h').html(g_str_triphistory);
	$('#tip_ub_f').html(g_str_frined);
	$('#tip_ub_d').html(g_str_prepage);
	$('#tip_ub_home').html(g_str_logout);

	$('#b_add_note').find('.tip').html(g_str_startrecording);
	$('#sym_logingroup').fadeIn('slow', function() {
			$('#sym_logingroup').css("background-image", url+backlogo_img+")");
	});


	$('#logoutwhenrecord').html(g_str_logoutwhenrecord);
	$('#typetripname').html(g_str_typetripname);
	$('#tripinput1').html(g_str_tripname);

	$('#takepicture_id').css("background", url+takepic_img+") no-repeat");
	$('#emotion-sel').css("background", url+selemotion_img+") no-repeat");
	$('#emotion-c').attr("src", emocompass_img);
	$('#placemarktext').attr("placeholder", g_str_placemarkertext);

	var elements = $('#sym_takepicture');
	elements.each(function() {
		var element = $(this);
		element.removeClass('ui-state-default');
	});

	elements = $('#sym_selectemotion');
	elements.each(function() {
		var element = $(this);
		element.removeClass('ui-state-default');
	});

	elements = $('#sym_textarea');
	elements.each(function() {
		var element = $(this);
		element.removeClass('ui-state-default');
	});

	initEmotionMap();

	ChangeToUsedIcon($('#ub_trip_management'),false);
//-->