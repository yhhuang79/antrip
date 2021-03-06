//Language selection option re-generation
//$("#div_langOption").html("<select id='langOption'></select>");
//alert("English");
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

var g_str_delete = "Do you really want to delete this trip?";

var g_str_radio_trip_id = "Created Time";
var g_str_radio_trip_st = "Recorded Time";
var g_str_radio_length_st = "Recorded Length";
var g_str_start = "Start";
var g_str_end = "End";
var g_str_length = "Length";

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

$("#span_langtitle").html("Language");

$("#banner_text").html("Take an antrip! go insight!");

$('#sym_logingroup').css("background-image", url+im+"background_English.png)");

$("#ub_home").attr('src',im+'ub_home_English.png');
$('#ub_home').attr('name','ub_home_English');
$("#ub_trip_history").attr('src',im+'ub_trip_history_English.png');
$('#ub_trip_history').attr('name','ub_trip_history_English');
$("#ub_trip_management").attr('src',im+'ub_trip_management_English.png');
$('#ub_trip_management').attr('name','ub_trip_management_English');
$("#ub_friend").attr('src',im+'ub_friend_English.png');
$('#ub_friend').attr('name','ub_friend_English');
$("#ub_download").attr('src',im+'ub_download_English.png');
$('#ub_download').attr('name','ub_download_English');
$("#ub_about").attr('src',im+'ub_about_English.png');
$('#ub_about').attr('name','ub_about_English');


$(".login_bt_link").html("<p>" +"Login"+"</p>");
$(".logout_bt_link").html("<p>" +"Logout"+"</p>");
$(".fblogin_bt_link").html("<p>" + "Login with Facebook"+"</p>");
$(".pre_page_link").html("<p>" +"<<"+"</p>");
$(".next_page_link").html("<p>" +">>"+"</p>");
$(".pre_trip_link").html("<p>" +"<<"+"</p>");
$(".next_trip_link").html("<p>" +">>"+"</p>");
$("#open_trip_tip").html("Open a Trip");
$("#seg_trip_tip").html("Segement a Trip");
$("#add_note_tip").html("Add a Note");
$("#exp_trip_tip").html("Export a Trip");

$('#username').attr('placeholder',g_str_id);
$('#password').attr('placeholder',g_str_password);

$(".takemarker_bt_link").html("Check in");

g_str_numberoftrip = "the number of trips:";

g_str_loginfirst = "Please login first!";
g_str_loginfail = "Login Fail";
g_str_lostinsertdata = "Please insert user name and password!";
g_str_firstpage = "This page is the first page!";
g_str_lastpage = "This page is the last page!";
g_str_firstrip = "This trip is the first trip!";
g_str_lastrip = "This trip is the last trip!";
g_str_notripdisplay = "no trip can be displayed";

g_str_tripnote = "Trip Note";
g_str_tripnotedes = "Please take your note to this trip:";
g_str_tripdescription = "Please click here to type your description";

g_str_triplist = "Trip List";

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

var g_str_copythelink = "Copy the link of static map";
var g_str_copythedlink = "Copy the link of dynamic map";
var g_str_reloadtoDefault = "Reload to Default";
var g_str_opencloseMarker = "Open/Close all Check-in";

var g_str_downloadlink = "https://play.google.com/store/apps/details?id=tw.plash.antrip&hl=en";

var g_str_newTrip_1 = "A new Trip: ";
var g_str_newTrip_2 = " is created!";

var g_str_cutwithoutpoint = "can't cut segment without points!";
var g_str_cuttripfirst = "Please segment your trip!";

$("#about_page").html("<h1>About ANTrip</h1><h3><p>This is the web browser version of ANTrip – a travel log and LBS software that also features Android app version on Google play.</p><p>ANTrip is a free trip recorder! It lets you to conveniently record the paths you have travelled and share your travel logs with your friends and relatives. For travel lovers, enthusiastic sharers, don’t miss this particular app.</p><p><a href='http://goo.gl/odpfO' target='blank'>For detailed description, please refer to our page on Google Play.</a></p><p>The following descriptions explains how to use this website.</p></h3><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_home'));\">Home</a></h2><h3>The homepage will display your most recent trip upon successful login.</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_trip_history'));\">Trip History</a></h2><h3>View all your previous trips.</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_trip_management'));\">Trip Management</a></h2><h3>Manage all your previous trips.</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_friend'));\">Friend</a></h2><h3>View the trips shared by your friends and add new friends</h3></li><li><h2><a href='http://goo.gl/odpfO' target='blank'>Download</a></h2><h3>Go to our page on Google Play.</h3></li><h1>About this software</h1><h3><p><a href='Manual.docx' >English documentation</a></p><p><a href='Manual_cht.docx' >Chinese documentation</a></p></h3>");

$(".class_sologan").html("\"If someone marked the journey I was passing in a map<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;and connected them with a line, it is perhaps as a Minotaur.\"<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;by Picasso");

if(typeof loadjscssfile== 'function'){
	if(g_forceAsync!=null && g_forceAsync==true){
		loadjscssfile("lib/js/langBasicSetting.js", "js", g_forceAsync);
	}
	else{
		loadjscssfile("lib/js/langBasicSetting.js", "js");
	}
}