<!--
		var im='images/';
		var url = "url(";
		var g_lang="Chinese";
		var g_IsInited = false;
		var g_titleHeight = 50;
		var g_footerHeight = 60;
		var fixedValue = 40;
		var g_titleSize = "h3";
		var g_currentPageID = 1;
		var g_PageIDTable = {
			"login": 1,
			"mainpage": 0,
			"map": 3,
			"triplist": 2,
			"friendlist": 4
		};
		var g_dropdownImgTable = {
			"map": "#tb_trip_recorder",
			"triplist":"#tb_trip_history",
			"friendlist": "#tb_friend"
		};

		var g_zoom=17;
		var g_startLatLng = new google.maps.LatLng(25.04752,121.614189);
		var g_tripRecorderPath =  im+'tripRecorder.png';
		var g_locatingPath =  im+'locating.png';
		var g_checkin_invalid_imgPath = im + "check-in_invalid.png";
		var g_checkin_imgPath = im + "check-in.png";
		var g_antMarker = im+'ant_48.png';

		// multi-language parameter
		var g_str_numberoftrip = "Total number of trips:";
		var g_str_loginfirst = "Please login first!";
		var g_str_lostinsertdata = "Please insert user name and password!";
		var g_str_firstpage = "This page is the first page!"
		var g_str_lastpage = "This page is the last page!";
		var g_str_firstrip = "This trip is the first trip!"
		var g_str_lastrip = "This trip is the last trip!"
		var g_str_tripnote = "Trip Note";
		var g_str_tripnotedes = "Please take your note to this trip:";
		var g_str_triplist = "Trip List";
		var g_str_logout = "Logout!";
		var g_str_login = "Login";
		var g_str_fblogin = "Facebook Login";
		var g_username_holder="ID";
		var g_password_holder="PASSWORD";
		var g_str_start ="Start";
		var g_str_end ="End";
		var g_str_Length = "Length";

		var g_str_loading = "Now Loading...";
		var g_str_localizationing = "Now Locating...";
		var g_str_iamhere = "You are here!";
		var g_str_checkinmessage = "You can click me to check in!";
		var g_str_takepicture = "Take Picture";
		var g_str_checkin_feeling = "How are you feeling?";

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

		var g_str_delete = "Are you sure you want to delete this trip?";

		var g_str_locating="Locating";
		var g_str_checkin = "Check in";
		var g_str_startrecording = "Start recording";
		var g_str_nowrecording= "Now recording...";
		var g_str_checkin = "check in";
		var g_str_cancel = "cancel";

		var g_str_inputsomething ="please input something!";
		var g_str_notrip = "You have no trip!"
		var g_str_nofriend = "You have no friend!";

		var g_str_recordtrip = "Trip Recorder";
		var g_str_triphistory = "Trip History";
		var g_str_friend = "Friends List";
		var g_str_prepage = "Previous Page";

		var g_str_typetripname="Please type your trip name:";
		var g_str_untitledtrip ="untitled trip";
		var g_str_logoutwhenrecord="Recording now, stop and logout?";
		var g_str_tripname = "Trip Name";
		var g_str_positionerror = "Locating error! Can't check in now, please wait a minute...";

		var g_str_bkpath = "background_eng.png";

		var takepic_img = im+ "takepicture_eng.png";
		var selemotion_img = im+ "selectemotion_eng.png";
		var emocompass_img = im+ "emotion-compass_eng.png";
		var uploadicon_img = im+ "uploadicon_eng.png";
		var g_str_placemarkertext = "Please Type your Message";
		backlogo_img = im+ "background_eng.png";

		var g_str_antrip = "ANTrip";
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
//-->