<!--
	//**<-- detecting user platform -->**
	var agent = navigator.userAgent.toLowerCase();
	var scrWidth = screen.width;
	var scrHeight = screen.height;
	// The document.documentElement dimensions seem to be identical to
	// the screen dimensions on all the mobile browsers I've tested so far
	var elemWidth = document.documentElement.clientWidth;
	var elemHeight = document.documentElement.clientHeight;
	// We need to eliminate Symbian, Series 60, Windows Mobile and Blackberry
	// browsers for this quick and dirty check. This can be done with the user agent.
	var otherBrowser = (agent.indexOf("series60") != -1) || (agent.indexOf("symbian") != -1) || (agent.indexOf("windows ce") != -1) || (agent.indexOf("blackberry") != -1);
	// If the screen orientation is defined we are in a modern mobile OS
	var mobileOS = typeof orientation != 'undefined' ? true : false;
	// If touch events are defined we are in a modern touch screen OS
	var touchOS = ('ontouchstart' in document.documentElement) ? true : false;
	// iPhone and iPad can be reliably identified with the navigator.platform
	// string, which is currently only available on these devices.
	var iOS = (navigator.platform.indexOf("iPhone") != -1) ||
			(navigator.platform.indexOf("iPad") != -1) ? true : false;
	// If the user agent string contains "android" then it's Android. If it
	// doesn't but it's not another browser, not an iOS device and we're in
	// a mobile and touch OS then we can be 99% certain that it's Android.
	var android = (agent.indexOf("android") != -1) || (!iOS && !otherBrowser && touchOS && mobileOS) ? true : false;

	//**<-- global images path-->**
	var im='images/';
	//**<-- global parameter -->**
	var back_img = im+"background-img.png";
	var scroll_img = im+"scroll2.png";
	var topbk_img = im+"topbt_bk.png";
	var backlogo_img = im+"background.png";
	var banner_logo_img = im+"banner_logo.png"
	var images_scroll_img = im+"scroll.png"
	var tip_img = im+"tipline.png";
	var trip_m_img = im+"ub_trip_management_r.png";

	var url = "url(";
	var g_page=1;
	var g_mode="login";
	var g_numsofpage=5;
	var g_tripnum=0;
	var g_triplength=0;
	var g_scale=1.2;
	// google map var
	var g_startLatLng = new google.maps.LatLng(25.04752,121.614189);
	var g_zoom=20;
	var g_myOptions = {zoom:g_zoom,center:g_startLatLng,mapTypeId:google.maps.MapTypeId.ROADMAP};
	var g_map = null;
	var g_infowindow=null;
	var g_isForMobile=false;
	var g_friend_width="150px";
	var g_trip=-1;
	var g_triplist_width="1200px";
	var g_triplist_height="800";

	var g_enablewithoutLogin=false;
	var g_lang="English";
	var g_ready=false;
	var g_str_numberoftrip = "the number of trips:";
	var g_str_loginfirst = "Please login first!";
	var g_str_lostinsertdata = "Please insert user name and password!";
	var g_str_firstpage = "This page is the first page!"
	var g_str_lastpage = "This page is the last page!";
	var g_str_firstrip = "This trip is the first trip!"
	var g_str_lastrip = "This trip is the last trip!"
	var g_str_tripnote = "Trip Note";
	var g_str_tripnotedes = "Please take your note to this trip:";
	var g_str_triplist = "Trip List";

	var g_default_user_checkin_path = "user/checkin/";

	var g_tripPointArray;

	/*! global usage function*/
	function loadjscssfile(filename, filetype){
		 if (filetype=="js"){ //if filename is a external JavaScript file
			  require(filename);
		 }
		 else if (filetype=="css"){ //if filename is an external CSS file
			  var fileref=document.createElement("link");
			  fileref.setAttribute("rel", "stylesheet");
			  fileref.setAttribute("type", "text/css");
			  fileref.setAttribute("href", filename);
		 }
		 if (typeof fileref!="undefined"){
			  document.getElementsByTagName("head")[0].appendChild(fileref);
		 }
	}
	function require(script) {
		$.ajax({
			url: script,
			dataType: "script",
			async: false,           // <-- this is the key
			success: function () {
				// all good...
			},
			error: function () {
				throw new Error("Could not load script " + script);
			}
		});
	}

	//get parameters from URL
	$.urlParam = function(name){
		var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
		return results[1] || 0;
	}
	function getUrlVars()
	{
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	}

	$.extend({
		  getUrlVars: function(){
			var vars = [], hash;
			var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
			for(var i = 0; i < hashes.length; i++)
			{
			  hash = hashes[i].split('=');
			  vars.push(hash[0]);
			  vars[hash[0]] = hash[1];
			}
			return vars;
		  },
		  getUrlVar: function(name){
			return $.getUrlVars()[name];
		  }
	});

	/*! html file ready*/
	$(document).ready(function(){
		$("button","#sym_takepicture" ).button();
		$("button","#sym_selectemotion" ).button();
		$('#markplacewindow').hide();
		$('#sym_topbtnGroup').hide();
		PreloadandSet();
		$('#sym_topbtnGroup').show();
	});

/*	window.addEventListener('DOMContentLoaded', function() {
		PreloadandSet();
	});*/

	function ShowRecorderMap(){
		
	}
	function openMarkplaceWindow(){

	}
	function PreloadandSet(){
		
		g_isForMobile=true;
		// Platform checking...

		if(android || iOS || touchOS || g_isForMobile==true)
		{
			//alert("U are using with Android, iPad or iPhone...");
			//alert(document.body.clientHeight);
			//alert(document.body.clientWidth);
			g_isForMobile=true;
			//alert("AntripJS.min.js");
			//require("lib/jq_includes/jquery-1.7.2.min.js");
			require("lib/js/AntripJS.min.js");
			require("lib/js/MapRecorder.js");			
		}
		else{
			//loadjscssfile("lib/css/jquery.ui.all.css", "css");

			require("lib/js/AlbumSlider.js");
			require("lib/js/MapEditing.js");
			loadjscssfile("lib/css/pcframe.css", "css");
		}
			require("lib/jq_includes/jquery.bgiframe-2.1.2.js");
			require("lib/jq_includes/jquery.ui.core.js");
			require("lib/jq_includes/jquery.ui.widget.js");
			require("lib/jq_includes/jquery.ui.mouse.js");
			require("lib/jq_includes/jquery.ui.button.js");
			require("lib/jq_includes/jquery.ui.draggable.js");
			require("lib/jq_includes/jquery.ui.position.js");
			require("lib/jq_includes/jquery.ui.resizable.js");
			require("lib/jq_includes/jquery.ui.dialog.js");
			require("lib/jq_includes/jquery.effects.core.js");
		//reset image path
		back_img = im+"background-img.png";
		scroll_img = im+"scroll2.png";
		backlogo_img = im+"background.png";
		banner_logo_img = im+"banner_logo.png"
		images_scroll_img = im+"scroll.png"
		tip_img = im+"tipline.png";
		trip_m_img = im+"ub_trip_management_r.png";
	
		MM_preloadImages(back_img);
		MM_preloadImages(scroll_img);
		
		MM_preloadImages(backlogo_img);
		if(!g_isForMobile){
			MM_preloadImages(banner_logo_img);
			MM_preloadImages(images_scroll_img);
			$( "#images_ants" ).draggable({ revert: "invalid" });
			$( "#banner_logo" ).draggable({ revert: true });

			$( "#calculate_area" ).droppable({
				drop: function( event, ui ) {
				}
			});

			$('#sym_loginarea').corner(); 
		}
		else{
			MM_preloadImages(topbk_img);
		}
		MM_preloadImages(tip_img);
		MM_preloadImages(trip_m_img);

		$("#sym_triplist").hide();

		//ChangeToUsedIcon($('#ub_trip_history'),false);

		//ChangeToUsedIcon($('#ub_home'));
		$('#body').css("background-image", url+back_img+")");
		$('#Stage').css("background-image", url+scroll_img+")");
		$('#sym_logingroup').css("background-image", url+backlogo_img+")");

		if(!g_isForMobile){
			$('#images_scroll').css("background-image", url+images_scroll_img+")");
		}

		$('#langOption').change(function() 
		{
		   g_lang=$(this).attr('value');
		   setLanguage();
		});
		initNoteDialog();
		
		//alert($('#sym_topbtnGroup').css("background-image"));
		//$("body").queryLoader2();
	}

	function inputNoteDialog(){
		var data="<p class=\"validateTips\">"+g_str_tripnotedes+"</p><textarea id='notetextarea'  class=\"text ui-widget-content ui-corner-all\" style=\"width:350px;height:300px;resize:none;\"></textarea>"; 
		$('#dialog').html( data );
		$("#dialog").dialog({
			title: g_str_tripnote,
				bgiframe: false,
				width: 450,
				height: 550,
				modal: true,
				draggable: false,
				resizable: false,
				//overlay:{opacity: 0.5, background: "black" },
				buttons: {
					'OK': function() {
						$('#dialog').dialog('close');
					}
				},
				close: function() {
					//$('#dialog').dialog('close');
				}
			});
	}

	function TripListDialog(){
		g_page=1;
		ShowTripList(g_page);
		$("#sym_triplist_in_open_dialog").dialog({
			title: g_str_triplist,
				bgiframe: false,
				width: g_triplist_width,
				height: g_triplist_height,
				modal: true,
				draggable: false,
				resizable: false,
				color: "black",
				//overlay:{opacity: 0.5, background: "black" },
				close: function() {
					//$("#sym_triplist_in_open_dialog").dialog('close');
				}
			});
	}

	function initNoteDialog(){

	}

	function MM_preloadImages() { //v3.0
	  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
		var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
		if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
	}

	function MM_swapImgRestore() { //v3.0
	  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
	}

	function MM_findObj(n, d) { //v4.01
	  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
		d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
	  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
	  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
	  if(!x && d.getElementById) x=d.getElementById(n); return x;
	}

	function MM_swapImage() { //v3.0
	  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
	   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
	}

	function MM_callJS(jsStr) { //v2.0
	  return eval(jsStr)
	}


	//**<-- Top buttons scale animation -->**
	function show_login_div(){
		sid = $.cookie("sid");
		$('#sym_login').show();
		if(sid!=null){
			//usrname = $.cookie("usrname");
			//$("#sym_logintext").text("Welcome! "+usrname);
			$("#sym_loginarea").hide();
			$(".class_login_bt").hide();
			$(".class_logout_bt").show();
		}
		else{
			//$("#sym_logintext").text("Welcome to Antrip! Please login...");
			$('#sym_loginarea').show();
			$('.class_login_bt').show();
			$('.class_logout_bt').hide();
		}
		if(g_isForMobile==false)
		{
			$('#sym_video').show();
		}
	}
	function show_edit_div(){
		$('#sym_editpage').show();
		//$('img[class*=class_left_bt]').hide();
		//showGMap();
	}

	function showGMap(){
		g_map = new google.maps.Map(document.getElementById("map_canvas"),g_myOptions);
		var $marker = new google.maps.Marker({
			  position: g_startLatLng, 
			  map: g_map,
			  title:"Trip Name",
			  icon: "images/placemarker.png",
			  draggable: true
		  });

		google.maps.event.addListener($marker, 'click', function() {
			if(g_infowindow==null){
				g_infowindow = new google.maps.InfoWindow({
					content: 'Information'
				});

				g_infowindow.open(g_map, $marker);
			}
			else{
				g_infowindow.close();
				g_infowindow=null;
			}
		});
	}

	function OnlyShowADiv(object){
		if(object.attr('name')==$("#ub_download").attr('name')){
			return window.location ="http://www.facebook.com/antrip.plash";
		}

		$("body").css("cursor","default");
		$("#img_seq_trip").css("cursor","pointer");
		
		if(g_isForMobile==false){
			changeCursorToDefaultMode();
		}

		sid = $.cookie("sid");
		if(object.attr('name')==$("#ub_about").attr('name') && g_mode!="about"){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			$('#about_page').show();
			g_mode = "about";
		}
		else if( g_enablewithoutLogin==false && g_mode!= "login" &&((sid==null) || (object.attr('name')==$("#ub_home").attr('name')))){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			show_login_div();
			if(g_isForMobile==true){
				$("#end_Text").show();
			}
			g_mode = "login";
		}
		else if((object.attr('name')==$("#ub_trip_history").attr('name')) && g_mode!="triplist"){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			g_page=1;
			ShowTripList(g_page);
			$("#sym_triplist").show();
			$("#sym_editpage").hide();
			$('.class_prepage_bt').show();
			$('.class_nextpage_bt').show();
			if(g_isForMobile==true){
				$('#markplacewindow').hide();
				$("#end_Text").hide();
			}
			g_mode ="triplist";
		}
		else if((object.attr('name')==$("#ub_trip_management").attr('name'))&&g_mode !="tripedit"){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			show_edit_div();
			if(g_isForMobile==true){
				ShowRecorderMap();
			}
			else if($.getUrlVar('trip_id')==null){
				sid = $.cookie("sid");
				GetTripMapfromURL(sid, g_tripnum);
			}
			if(g_isForMobile==true){
				$("#end_Text").hide();
			}
			g_mode ="tripedit";
		}
		else if((object.attr('name')==$("#ub_friend").attr('name'))&&g_mode !="friendlist"){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			showfriendList();
			if(g_isForMobile==true){
				$("#end_Text").hide();
			}
			g_mode ="friendlist";
		}
	}

	function ChangeToUsedIcon(object, msg){
		//if(object.attr('src')!='images/'+object.attr('name')+'_r.png'){
			sid = $.cookie("sid");
			if( g_enablewithoutLogin==true || sid!=null || object.attr('name')==$("#ub_home").attr('name')){
				//alert("change");
				TopBtChangeToDefaultImg(object.attr('name'));
				MM_swapImage(object.attr('name'),'',im+object.attr('name')+'_r.png',1);
			}
			else if(sid==null){
				if(msg==null&&(object.attr('name')!=$("#ub_download").attr('name'))&&(object.attr('name')!=$("#ub_about").attr('name'))){
					alert(g_str_loginfirst);
				}
				TopBtChangeToDefaultImg($("#ub_home").attr('name'));
				MM_swapImage($('#ub_home').attr('name'),'',im+$('#ub_home').attr('name')+'_r.png',1);
			}
			OnlyShowADiv(object);
		//}
	}

	function scaleAnimation(object, scale){
		if(object.attr('src')!='images/'+object.attr('name')+'_r.png'){
			object.css('-webkit-transition-duration', '1s');
			if(scale==null){
				scale = 1.2;
			}
			object.css('-webkit-transform','scale('+scale+')');
		}
	}

	function  scaleInterval(object) {
		var  param  =   object ;
		scaleAnimation(param, g_scale);
		intval=window.setInterval( function () { scaleAnimation(param, g_scale); } ,  1000 )
	}

	function scaleRestore(object){
		if(object.attr('src')!='images/'+object.attr('name')+'_r.png'){
			object.css('-webkit-transition-duration', '1s');
			object.css('-webkit-transform','scale(1)');
		}
	}

	//**<-- left buttons rotate animation -->**
	var g_intval="";
	var g_clockintval="";
	var g_angle=45;
	var g_rotatetime='2s';
	function rotateImg(object){
		g_angle = -(g_angle);
		object.css('-webkit-transition-duration', g_rotatetime);
		object.css('-webkit-transform','rotate('+g_angle+'deg)');
	}

	function stopRotateInterval(object){
		if(g_clockintval!=-1){
			window.clearInterval(g_intval);
			object.css('-webkit-transition-duration', g_rotatetime);
			object.css('-webkit-transform','rotate(0deg)');
			g_angle=45;
		}
	}

	function  rotateInterval(object, ex_angle) {
		var  param  =   object ;
		if(ex_angle!=null){
			g_angle = ex_angle;
		}
		rotateImg(param);
		g_intval=window.setInterval( function () { rotateImg(param); } ,  2000 )
	} 

	function  clockwiseInterval(object) {
		var  param  =   object ;
		g_angle = 0;
		clockwiseImg(param, g_angle);
		window.clearInterval(g_intval);
		g_intval=-1;
		g_clockintval=window.setInterval( function () {g_angle+=180;clockwiseImg(param, g_angle); } ,  1000 )
	}

	function clockwiseImg(object, angle){
		object.css('-webkit-transition-duration', '1s');
		object.css('-webkit-transform','rotate('+angle+'deg)');
	}

	function TopBtChangeToDefaultImg(object){
		$('img[class*=class_top_bt]').each(function() {
			if(object!=$(this).attr('name')){
				if(g_isForMobile==true&&($(this).attr('name')=="ub_download"||$(this).attr('name')=="ub_about"))
				{
					;
				}
				else{
					$(this).attr("src", im+$(this).attr('name')+".png");
					$(this).css('-webkit-transform','scale(1)');
				}
			}			
		});
	}


	/* Tooltip sample*/
	$(function() {
		$('div[class*=class_left_bt]').each(function() {
			var tip = $(this).find('.tip');
			$(this).hover(

			function() {
				tip.appendTo('body');
			}, function() {
				tip.appendTo(this);
			}).mousemove(function(e) {
				var x = e.pageX + 20,
					y = e.pageY + 20,
					w = tip.width(),
					h = tip.height(),
					dx = $(window).width() - (x + w),
					dy = $(window).height() - (y + h);
				tip.css({
					left: x,
					top: y,
					'background-image': url+tip_img+")",
					visibility:"visible",
					display:"block"
				});
			}).mouseout(function(e) {
				tip.css({
					visibility:"hidden",
					display:"none"
				});
			});
		});

	});


	// login & logout page
	$('#sym_login').ready(function(){
		var login = $(this).find('.login_bt_link');
		login.click(function(){
			Login();
		});

		var logout = $(this).find('.logout_bt_link');
		logout.click(function(){
			Logout();
		});

		sid = $.cookie("sid");
		if(sid==null){
			$('.class_logout_bt').hide();
		}
	});

	function Login(){
		var username = $("#username").val();
		var org_password = $("#password").val();
		var password = hex_md5(org_password);
		if(username==""&&org_password==""){
			alert(g_str_lostinsertdata);
		}
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/login.php',
				data:{username: username, password: password},							 
				type: 'GET', dataType: 'jsonp', cache: false,
		success:function(result){
			if(result.sid != "0"){ 
				$.cookie("sid", result.sid);
				$.cookie("usrname", username);
				if(window.android){
					alert("login is good");
					window.android.saveSid(result.sid);
				} else{
					alert("false");
				}
				
				ChangeToUsedIcon($("#ub_trip_history"));
				
			} else { 
				alert("Login Fail");
			}
		}
		});
	}

	function Logout(){
		if($.cookie("facebookid")){
			FB.logout(function(response) {
				$.cookie("facebookid", null);
			}); 
		}
		$.cookie("sid", null);
		$.cookie("trip_id", null);
		ChangeToUsedIcon($("#ub_home"));
		if(window.android){
			window.android.logout();
		}
	}
	
	// trip list
	function ShowTripList(page){
		//$.mobile.showPageLoadingMsg("b", "Loading Trip List ...");
		var div_data = [];
		var sid = $.cookie("sid");
		if(window.android){
			result = window.android.getTripList();
		/*}
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetTripInfoComponent.php',
			data:{userid: sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){*/
				var index=1;
				$("div[id*=products]").html("");
				$("div[id*=tripsum]").html("");
				if(page==null){
					page=1;
				}
				g_tripnum = result.tripInfoList[0].trip_id;
				g_triplength = result.tripInfoList.length;
				$("div[id*=tripsum]").append(g_str_numberoftrip+g_triplength);
				if(result.tripInfoList.length==0){
					var str_noTrip = "You have no Trip!";
					alert(str_noTrip);
					var appendcontent="<div style='margin:auto;width:100%;text-align:center;display:table-cell;vertical-align:middle;'>"+str_noTrip+"</div>";
					$("div[id=products]").eq(0).append(appendcontent);
				}
				else{
					$.each(result.tripInfoList, function(i,data){
						
						var tripurl = "#sym_editpage?userid="+ sid +"&trip_id="+ data.trip_id;
						var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.st_addr_prt2 +"&zoom=12&size=100x100&sensor=false";
						var appendcontent;
						if(data.upload==true)
						{
							appendcontent="<button class='tripItem' onClick=\"if(g_isForMobile==false){ChangeToUsedIcon($('#ub_trip_management'));}else{OnlyShowADiv($('#ub_trip_management'));$('#sym_edit_bt_list').hide();$('#map_canvas').css('margin-top','0');}GetTripMapfromURL("+sid+","+data.trip_id+");\" href='" + tripurl  + "'><div class='product'><div class='wrapper'><div class='listview_image'><a class='listview' href='" + tripurl  + "' rel='external'><img src='" + mapurl + "'/></a></div><div class='listview_description'><a class='listview' href='" + tripurl  + "' rel='external'><h3>" + data.trip_name  + "</h3><p>Start: " + data.trip_st + "</p><p>End: " + data.trip_et  + "</p><p>Length: " + data.trip_length  + " M</p></a></div></div></div></button>";
						}
						else{
							appendcontent="<button class='tripItem' onClick=\"if(g_isForMobile==false){ChangeToUsedIcon($('#ub_trip_management'));}else{OnlyShowADiv($('#ub_trip_management'));$('#sym_edit_bt_list').hide();$('#map_canvas').css('margin-top','0');}GetTripMapfromURL("+sid+","+data.trip_id+");\" href='" + tripurl  + "'><div class='product'><div class='wrapper'><div class='listview_image'><a class='listview' href='" + tripurl  + "' rel='external'><img src='" + mapurl + "'/></a></div><div class='listview_description'><a class='listview' href='" + tripurl  + "' rel='external'><h3>" + data.trip_name  + "</h3><p>Start: " + data.trip_st + "</p><p>End: " + data.trip_et  + "</p><p>Length: " + data.trip_length  + " M</p></a></div></div></div></button>";
						}

						if(page==0|| g_isForMobile==true ||((index>(page-1)*g_numsofpage)&&(index<=page*g_numsofpage))){
							$("div[id=products]").eq(0).append(appendcontent);
						}
						// this is for trip list of popup dialog.
						$("div[id=products_in_dialog]").append(appendcontent);
						index++;
					});
					$("button","div[id*=products]" ).button();
				}
				//alert($("button[class*=tripItem]"));
				//$("button","div[id*=products]" ).draggable({ revert: true });
			}
	//	});*/
	}

	function prepageAction(){
		if(g_page>1){
			g_page--;
			ShowTripList(g_page);
		}
		else{
			alert(g_str_firstpage);
		}
	}

	function nextpageAction(){
		if(g_page*g_numsofpage<g_tripnum){
			g_page++;
			ShowTripList(g_page);
		}
		else{
			alert(g_str_lastpage);
		}
	}

	function preTripAction(){
		var sid = $.cookie("sid");
		if(g_trip>1){
			g_trip--;
			GetTripMapfromURL(sid, g_trip);
		}
		else{
			alert(g_str_firstrip);
		}
	}

	function nextTripAction(){
		var sid = $.cookie("sid");
		if(g_trip<g_tripnum){
			g_trip++;
			GetTripMapfromURL(sid, g_trip);
		}
		else{
			alert(g_str_lastrip);
		}
	}

	// prepage & nextpage
	$('#sym_triplist').ready(function(){
		var prepagebt = $(this).find('.class_prepage_bt');
		prepagebt.click(function(){
			prepageAction();
		});

		var nextpagebt = $(this).find('.class_nextpage_bt');
		nextpagebt.click(function(){
			nextpageAction();
		});
	});


	$('#sym_editpage').ready(function(){
		var pretripbt = $(this).find('.class_pretrip_bt');
		pretripbt.click(function(){
			preTripAction();
		});

		var nextripbt = $(this).find('.class_nextrip_bt');
		nextripbt.click(function(){
			nextTripAction();
		});
	});

	// friend list 
	function showfriendList(){
		//var sid = $.cookie("sid");
		$("#friend_list").html("");
		var div_data = [];
		//$.mobile.showPageLoadingMsg("b", "Loading Friend List ...", true);
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				if(result.friend_list.length==0){
					var str_noFriend = "You have no friend!";
					alert(str_noFriend);
					var appendcontent="<div style='margin:auto;width:100%;text-align:center;display:table-cell;vertical-align:middle;'>"+str_noFriend+"</div>";
					$("#friend_list").append(appendcontent);
				}
				$.each(result.friend_list, function(i,data){
					div_data[i] ="<button class='class_friend_bt' style='background:rgba(255,255,255,0) url("+im+"friend_bk.png) 100% 100% no-repeat;'><li><a href='#'><img src='" + data.image + "'/><h3>" + data.name  + "</h3><p>" + data.id + "</p></a></li></button>";
				});
				$("#friend_list").append(div_data.join('')).listview('refresh');
				$("button","#friend_list" ).button();
				//$.mobile.hidePageLoadingMsg();
			}
		});

		$("#sym_friends").show();
	}

	function GetTripMapfromURL(userid, trip_id){
				//ChangeToUsedIcon($("#ub_trip_management"));
				$('#map_canvas').gmap('destroy');
				$('#map_canvas').gmap({ 'zoom':g_zoom,'center':g_startLatLng, 'callback': function(map) {
					var self = this;
					g_tripPointArray = new Array(0);
					g_tripMarkerArray = new Array(0);
					g_trip = trip_id;
					var url = "http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetTripDataComponent.php?userid="+userid+"&trip_id="+trip_id+"&field_mask=110010000000000";
					self.addControl('control', google.maps.ControlPosition.LEFT_TOP);				
					$.getJSON(url, function(data) { 

						$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetCheckInData.php',
						data:{userid: sid, trip_id: trip_id},
						type: 'GET', dataType: 'jsonp', cache: false,
						success:function(result){
							$.each(data.tripDataList, function(i, marker) {
								var lat = marker.lat.valueOf() / 1000000;
								var lng = marker.lng.valueOf() / 1000000;
								var latlng = new google.maps.LatLng(lat, lng);
								g_tripPointArray.push(latlng);
								if (typeof marker.CheckIn != 'undefined'){
									var placemarker = self.addMarker({ 
										'position': latlng, 
										'bounds': true,
										'icon': im+"placemarker.png"
									}).click(function(){
										var CheckInInfo = "<p>"+ marker.CheckIn.message +"</p><img src='"+ marker.CheckIn.picture_uri +"' height='120'/>";
										self.openInfoWindow({'content': CheckInInfo}, this);
									});
									g_tripMarkerArray.push(placemarker);
								} else {
									self.addMarker({ 
										'position': latlng, 
										'bounds': true,
										'icon': im+"placemarker.png"
									}).click(function(){
										self.openInfoWindow({'content': marker.timestamp}, this);
									});
								}
							});
					
							//g_tripPointArray = trip_1;
							self.addShape('Polyline',{
								'strokeColor': "#FF0000", 
								'strokeOpacity': 0.8, 
								'strokeWeight': 4, 
								'path': g_tripPointArray
							});
							var clusterStyles = [
							  {
								opt_textColor: 'black',
								url: im+"placemarker1.png",
								height: 64,
								width: 48
							  },
							 {
								opt_textColor: 'black',
								url: im+"placemarker2.png",
								height:64,
								width: 48
							  },
							 {
								opt_textColor: 'black',
								url: im+"placemarker3.png",
								height: 64,
								width: 63
							  }
							];
							self.set('MarkerClusterer', new MarkerClusterer(map, self.get('markers'), {styles: clusterStyles}));
							$('#map_canvas').gmap('refresh');
						}});
					});				
				}});

				//$("#sym_triplist_in_open_dialog").dialog("close");

				//UpdateAlbum();
	}
//-->