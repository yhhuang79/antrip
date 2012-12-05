<!--
		var im='images/';
		var url = "url(";
		var g_lang="Chinese";
		var g_zoom=20;
		var latlng_undefined_value=-999;

		var newOptions = {
			'Chinese' : 'Chinese',
			'English' : 'English'
		};

		var g_str_loading = "Loading..."
		var g_str_welcome = "Welcome! ";
		var g_str_welcome_anonmous = "Welcom to Antrip! Please login...";
		var g_str_register = " Registration";
		var g_str_or = "Or";
		var g_str_id = "ID";
		var g_str_password = "PASSWORD";
		var g_str_email = "EMAIL";
		var g_str_re_password = "Re-Enter PASSWORD";
		var g_str_create_account = "Create an account";
		var g_str_cancel ="Cancel";
		var g_str_email_forgotten = "(Your password will be sent to this email if forgotten)";
		var g_str_hasregistered = "You have login! Don't register again!";
		var g_str_pswnotsync ="The password is not confirmed to sync!";

		var g_str_delete = "Do you really want to delete this trip?";
		var g_str_deleteNote = "Do you really want to delete this note?";
		var  g_str_shareTriptoWhom = "Please choose who you want to share this trip with?";
		var g_str_shareToAllUser = "Share to all user";

		var g_str_radio_trip_id = "Created Time";
		var g_str_radio_trip_st = "Recorded Time";
		var g_str_radio_length_st = "Recorded Length";
		var g_str_start = "Start";
		var g_str_end = "End";
		var g_str_length = "Length";

		 var g_str_search = "search";

		var g_str_orderby = "Order By:"
		var g_str_s = "s";
		var g_str_m = "m";
		var g_str_h = "h";

		var g_str_second = "second";
		var g_str_minute = "minute";
		var g_str_hour = "hour";

		var g_str_speed_table = "Speed(unit: km/h)";
		var g_str_checkin_table = "Check-in";
		var g_str_altitude_table = "Altitude(unit: m)";

		var g_str_selectLang = "Select Language";
		var g_str_sologn = "Take an antrip! go insight!";
		var g_str_bkImage = "background_English.png";

		var g_str_login = "Login";
		var g_str_logout = "Logout";
		var g_str_startAntrip = "Start Antrip";
		var g_str_loginFB = "Login with Facebook";

		var g_str_home = "Home";
		var g_str_friends = "Friends";
		var g_str_addfriend = "Add friend";
		var g_str_shared_triplist ="Shared Trips from friends";
		var g_str_openTrip = "Open a Trip";
		var g_str_segTrip = "Segement a Trip";
		var g_str_addNote = "Add a Note";
		var g_str_expTrip = "Export a Trip";
		var g_str_sharemap = "Share this trip";
		var g_str_deletemap = "Delete this trip";

		var g_str_checkin = "Check in";

		var g_str_download_tip = "Download APP";
		var g_str_caution ="About ANTrip";
		var g_str_numberoftrip = "the total number of trips:";
		var g_str_sharenumberoftrip = "the total number of shared trips:";
		var g_str_numberoffriend = "the total number of friends:";
		var g_str_sharefromWho = "this trip is shared from ";
		var g_str_sharetoWho = "this trip is shared to ";

		var g_str_loginfirst = "Please login first!";
		var g_str_loginfail = "Login Fail";
		var g_str_lostinsertdata = "Please insert user name and password!";
		var g_str_firstpage = "This page is the first page!";
		var g_str_lastpage = "This page is the last page!";
		var g_str_firstrip = "This trip is the first trip!";
		var g_str_lastrip = "This trip is the last trip!";
		var g_str_notripdisplay = "no trip can be displayed";

		var g_str_tripnote = "Trip Note";
		var g_str_tripnotedes = "Please take your note to this trip:";
		var g_str_tripdescription = "Please click here to type your description";

		var g_str_triplist = "Trip List";

		var g_str_savetrip = "Save Trip";
		var g_str_tripname = "Trip Name";
		var g_str_type_tripname = "Please Type your Trip Name:";

		var g_str_notrip = "You have no Trip!"
		var g_str_nofriend = "You have no friend!";

		var g_str_bored= 'Bored';
		var g_str_sad= 'Sad';
		var g_str_sleepy= 'Sleepy';
		var g_str_peaceful= 'Peaceful';
		var g_str_relaxed= 'Relaxed';
		var g_str_pleased= 'Pleased';
		var g_str_happy= 'Happy';
		var g_str_excited= 'Excited';
		var g_str_angry= 'Angry';
		var g_str_nervous='Nervous';
		var g_str_calm= "Calm";

		var g_str_playmap = "Play trip";
		var g_str_copythelink = "Copy the link of static map";
		var g_str_copythedlink = "Copy the link of dynamic map";
		var g_str_reloadtoDefault = "Reload to Default";
		var g_str_opencloseMarker = "Open/Close all Check-in";
		var g_str_nocheckin = "There is no check-in data in this trip!";
		var g_str_input_tags="Please input tags for this trip:";
		var g_str_pls_upload_image="Please select a image in your local disk:";

		var g_str_request_friend="who request friend to you";

		var g_str_downloadlink = "https://play.google.com/store/apps/details?id=tw.plash.antrip&hl=en";

		var g_str_newTrip_1 = "A new Trip: ";
		var g_str_newTrip_2 = " is created!";

		var g_str_cutwithoutpoint = "can't cut segment without points!";
		var g_str_cuttripfirst = "Please segment your trip!";

		var g_str_description_title ="Following words are the description for this trip:<br/>";

		var g_str_caution_string = "<h1>About ANTrip</h1><h3><p>This is the web browser version of ANTrip ¡V a travel log and LBS software that also features Android app version on Google play.</p><p>ANTrip is a free trip recorder! It lets you to conveniently record the paths you have travelled and share your travel logs with your friends and relatives. For travel lovers, enthusiastic sharers, don¡¦t miss this particular app.</p><p><a href='http://goo.gl/odpfO' target='blank'>For detailed description, please refer to our page on Google Play.</a></p><p>The following descriptions explains how to use this website.</p></h3><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_home'));\">Home</a></h2><h3>The homepage will display your most recent trip upon successful login.</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_trip_history'));\">Trip History</a></h2><h3>View all your previous trips.</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_trip_management'));\">Trip Management</a></h2><h3>Manage all your previous trips.</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_friend'));\">Friend</a></h2><h3>View the trips shared by your friends and add new friends</h3></li><li><h2><a href='http://goo.gl/odpfO' target='blank'>Download</a></h2><h3>Go to our page on Google Play.</h3></li><h1>About this software</h1><h3><p><a href='Manual.docx' >English documentation</a></p><p><a href='Manual_cht.docx' >Chinese documentation</a></p></h3>";

		var g_str_sologan_string = "\"If someone marked the journey I was passing in a map<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;and connected them with a line, it is perhaps as a Minotaur.\"<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;by Picasso";
//-->