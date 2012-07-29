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
	var g_mode= null;
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

	var g_enableDebugMode = false;
	var g_enablewithoutLogin=false;
	//set default language to Chinese
	var g_lang="English";
	var g_ready=false;

	var g_eMode={'eHome':1,'eTripList':2,'eTripDisplay':3,'eFriendList':4};

	/*! global usage function*/
	function loadjscssfile(filename, filetype){
		 if (filetype=="js"){ //if filename is a external JavaScript file
			 var fileref=document.createElement('script')
			  fileref.setAttribute("type","text/javascript")
			  fileref.setAttribute("src", filename)
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

	//Load Language files.
	function setLanguage(){
		//Except following languages, others are English.
		//CHINA
		//CHINESE
		//PRC
		//SIMPLIFIED_CHINESE
		//TAIWAN
		//TRADITIONAL_CHINESE
		g_lang = g_lang.toUpperCase();
		if(g_lang=="ZH" ||g_lang=="CHINA" || g_lang=="CHINESE" || g_lang=="PRC" || g_lang=="SIMPLIFIED_CHINESE" || g_lang=="TAIWAN" || g_lang=="TRADITIONAL_CHINESE"){
			//require("lib/lang/lang_Chinese.js");
			loadjscssfile("lib/lang/lang_Chinese.js", "js");
		//	throw new Error("loaded lang_Chinese.js");
		}
		else{
			//require("lib/lang/lang_English.js");
			loadjscssfile("lib/lang/lang_English.js", "js");
		//	throw new Error("loaded lang_English.js");
		}
	}

	/*! html file ready*/
	$(document).ready(function(){
		PreloadandSet();
	});

	function ShowRecorderMap(){
		
	}
	function openMarkplaceWindow(){

	}
	function PreloadandSet(){
		if(window.antrip)
		{
			g_lang = window.antrip.getLocale();
		}

		setLanguage();

		g_isForMobile=true;

		$("button","#sym_takepicture" ).button();
		$("button","#sym_selectemotion" ).button();
		$('#markplacewindow').hide();
		$('#sym_topbtnGroup').hide();
		
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

		//ChangeToUsedIcon($('#ub_home'));
		$('#body').css("background-image", url+back_img+")");
		$('#Stage').css("background-image", url+scroll_img+")");
		$('#sym_logingroup').css("background-image", url+backlogo_img+")");

		//initNoteDialog();
		initTripNameDialog();
		//initScrollTopBtn();

		var overlay = jQuery('<div id="overlay"> </div>');
		overlay.appendTo(document.body)
		$("#overlay").css("display","none");

		//$('#sym_topbtnGroup').show();
	}

	/*function inputNoteDialog(){
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
	}*/

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
		var sid = $.cookie("sid");
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}
		$('#sym_login').show();
		$('#sym_topbtnGroup').hide();
		if(sid!=null){
			$("#sym_loginarea").hide();
			$(".class_login_bt").hide();
			$(".class_fblogin_bt").hide();
			$(".class_logout_bt").hide();
			//alert(g_str_logout);
			Logout();
		}
		else{
			$('#sym_loginarea').show();
			$('.class_login_bt').show();
			$(".class_fblogin_bt").show();
			$('.class_logout_bt').hide();
		}
		$("#end_Text").show();
	}

	function show_edit_div(){
		$('#sym_editpage').show();
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

	function initScrollTopBtn(){
		var topBtns = [{}];
	//	for (var i = 1; i < 2; i++) {
			var wheel = {};
			for (var j = 0; j < 101; j++) {
				if(j%4==0){
					wheel[j] = "<div id='Symbol_ub_home' class='class_top_bt' ><img src='"+im+"ub_home.png' class='class_top_bt' name='ub_home' id='ub_home' onClick='ChangeToUsedIcon($(this))' ></img></div>";
				}
				else if(j%4==1){
					wheel[j] = "<div id='Symbol_trip_history' class='class_top_bt' ><img src='"+im+"ub_trip_history.png' class='class_top_bt' name='ub_trip_history' id='ub_trip_history' onClick='ChangeToUsedIcon($(this))' ></img></div>";
				}
				else if (j%4==2){
					wheel[j] = "<div id='Symbol_ub_trip_m' class='class_top_bt'><img src='"+im+"ub_trip_management.png' class='class_top_bt' name='ub_trip_management'  id='ub_trip_management' onClick='ChangeToUsedIcon($(this))' ></img></div>";
				}
				else if (j%4==3){
					wheel[j] = "<div id='Symbol_friend' class='class_top_bt' ><img src='"+im+"ub_friend.png' class='class_top_bt' name='ub_friend' id='ub_friend' onClick='ChangeToUsedIcon($(this))' ></img></div>";
				}
			}
			topBtns[0]['topBtns'] = wheel;
		//}

		$('#scrollerInput').scroller({
			//preset: 'select',
			theme: 'default',
			showLabel:false,
			display: 'inline',
			mode: 'scroller',
			wheels: topBtns,
			//rows: 4,
			height: 40,
			inputClass: 'scrollTopBtn'
		});
	}

	function changeIconToBackBtn(){
		$("#Symbol_ub_home").hide();
		$("#Symbol_trip_history").hide();
		$("#Symbol_ub_trip_m").hide();
		$("#Symbol_friend").hide();
		$("#Symbol_download").show();
	}

	function OnlyShowADiv(object){
		var sid = null;
		sid = $.cookie("sid");
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}
		if(g_enableDebugMode==true)
		{
			alert(sid);
		}
		$("#end_Text").hide();

		if(object.attr('name')==$("#ub_about").attr('name')){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			$('#about_page').show();
			g_mode = "eAbout";
		}
		else if(object.attr('name')==$("#ub_download").attr('name')){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			$('#sym_topbtnGroup').show();
			$("#sym_triplist").show();
			$("#sym_editpage").hide();
			$('#markplacewindow').hide();

			$("#Symbol_ub_home").show();
			$("#Symbol_trip_history").show();
			$("#Symbol_ub_trip_m").show();
			$("#Symbol_friend").show();
			$("#Symbol_download").hide();

			TopBtChangeToDefaultImg($("#ub_trip_history").attr('name'));
			MM_swapImage($("#ub_trip_history").attr('name'),'',im+$("#ub_trip_history").attr('name')+'_r.png',1);
		}
		else if( g_enablewithoutLogin==false &&((sid==null) || (object.attr('name')==$("#ub_home").attr('name')))){
			if(g_enableDebugMode==true)
			{	
				alert("start");
			}
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			show_login_div();
			
			g_mode = "eHome";
			if(window.antrip){
				window.antrip.setMode(g_eMode[g_mode]);
			}
		}
		else if((object.attr('name')==$("#ub_trip_history").attr('name'))){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			$('#sym_topbtnGroup').show();
			g_page=1;
			ShowTripList(g_page);
			if(g_enableDebugMode==true)
			{
				alert("ShowTripList");
			}
			$("#sym_triplist").show();
			$("#sym_editpage").hide();
			$('.class_prepage_bt').show();
			$('.class_nextpage_bt').show();
			$('#markplacewindow').hide();

			g_mode ="eTripList";
			if(window.antrip){
				window.antrip.setMode(g_eMode[g_mode]);
			}
		}
		else if((object.attr('name')==$("#ub_trip_management").attr('name'))){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			$('#sym_topbtnGroup').show();
			show_edit_div();
			g_mode ="eTripDisplay";
			if(g_trip==-1)
			{
				ShowRecorderMap();
				if(window.antrip){
					window.antrip.setMode(g_eMode[g_mode]);
				}
			}
			$("#end_Text").hide();
		}
		else if((object.attr('name')==$("#ub_friend").attr('name'))){
			$('div[class*=class_fun_div]').each(function() {
				$(this).hide();
			});
			$('#sym_topbtnGroup').show();
			showfriendList();

			g_mode ="eFriendList";
			if(window.antrip){
				window.antrip.setMode(g_eMode[g_mode]);
			}
		}
	}

	var g_logoutbyIcon = false;

	function ChangeToUsedIcon(object, msg){
			var sid = $.cookie("sid");
			if(window.antrip){
				sid = window.antrip.getCookie("sid");
			}
			//alert(sid);
			if(g_enableDebugMode==true)
			{	
				alert(object.attr('src'));
				alert((im+object.attr('name')+"_r.png"));
			}
			// do not click button again except home button
			if(object.attr('src')==(im+object.attr('name')+"_r.png") &&  object.attr('name')!=$("#ub_home").attr('name'))
			{
				return;
			}
			
			if( g_enablewithoutLogin==true || sid!=null || object.attr('name')==$("#ub_home").attr('name')){
				var isRecording = null;
				isRecording = $.cookie("isRecording");
				if(window.antrip){
					isRecording = window.antrip.getCookie("isRecording");
				}
				if(object.attr('name')==$("#ub_trip_management").attr('name')){
					g_trip=-1;
				}
				if(isRecording != null && sid!=null && object.attr('name')==$("#ub_home").attr('name')){
					$("#dialog-confirm").dialog({
						resizable: false,
						draggable: false,
						height:200,
						modal: true,
						open: function (event, ui) {
								$('.ui-dialog-buttonpane').css({
									'background-image':url+im+"typenotearea.png)",
									'background-position':"center center",
								});
								$("button").css({
									color: "#000000",
								});
							},
						buttons: {
							"Yes": function() {
								$( this ).dialog( "close" );
								startRecordTrip();
								g_logoutbyIcon = true;
								return;
							},
							"No": function() {
								$( this ).dialog( "close" );
								return;
							}
						}
					});
				}
				else{
					TopBtChangeToDefaultImg(object.attr('name'));
					MM_swapImage(object.attr('name'),'',im+object.attr('name')+'_r.png',1);
					OnlyShowADiv(object);
				}
			}
			else if(sid==null){
				if(msg==null&&(object.attr('name')!=$("#ub_download").attr('name'))&&(object.attr('name')!=$("#ub_about").attr('name'))){
					alert(g_str_loginfirst);
				}
				TopBtChangeToDefaultImg($("#ub_home").attr('name'));
				MM_swapImage($('#ub_home').attr('name'),'',im+$('#ub_home').attr('name')+'_r.png',1);
				OnlyShowADiv(object);
			}
	}

	function logoutbyIcon(){
		TopBtChangeToDefaultImg($("#ub_home").attr('name'));
		MM_swapImage($("#ub_home").attr('name'),'',im+$("#ub_home").attr('name')+'_r.png',1);
		OnlyShowADiv($("#ub_home"));

		g_logoutbyIcon = false;
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
		g_intval=window.setInterval( function () { scaleAnimation(param, g_scale); } ,  1000 )
	}

	function scaleRestore(object){
	//	if(object.attr('src')!='images/'+object.attr('name')+'_r.png'){
			object.css('-webkit-transition-duration', '1s');
			object.css('-webkit-transform','scale(1)');
	//	}
		window.clearInterval(g_intval);
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
				if(g_isForMobile==true&&($(this).attr('name')=="ub_about"))
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
		if(window.antrip){
			sid = window.antrip.getCookie("sid");;
		}
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
			//	$.cookie("usrname", username);
				if(window.antrip){
					window.antrip.setCookie("usrname", username.toString());
					window.antrip.setCookie("sid", result.sid.toString());
				}
				ChangeToUsedIcon($("#ub_trip_history"));
			} else { 
				alert("Login Fail");
			}
		}
		});
	}

	function Logout(){
		if(g_enableDebugMode==true)
		{
			alert("Logout");
		}
		var facebookid = $.cookie("facebookid");
		if(window.antrip){
			facebookid = window.antrip.getCookie("facebookid");
		}
		if(facebookid){
			FB.logout(function(response) {
				$.cookie("facebookid", null);
				if(window.antrip){
					isRecording = window.antrip.removeCookie("facebookid");
				}
			}); 
		}
		$.cookie("sid", null);
		//$.cookie("trip_id", null);
		if(window.antrip){
			window.antrip.removeCookie("trip_id");
			window.antrip.removeCookie("sid");
			window.antrip.logout();
		}
		ChangeToUsedIcon($("#ub_home"));
	}
	window.fbAsyncInit = function() {
		FB.init({
		  appId      : '314048998686760', // App ID
		  status     : true, // check login status
		  cookie     : true, // enable cookies to allow the server to access the session
		  xfbml      : true  // parse XFBML
		});

		// Additional initialization code here
	};
	  // Load the SDK Asynchronously
	  (function(d){
		 var js, id = 'facebook-jssdk', ref = d.getElementsByTagName('script')[0];
		 if (d.getElementById(id)) {return;}
		 js = d.createElement('script'); js.id = id; js.async = true;
		 js.src = "//connect.facebook.net/en_US/all.js";
		 ref.parentNode.insertBefore(js, ref);
	   }(document));
	   
	   function LoginFacebook(){
			FB.login(function(response) {
				if (response.authResponse) {
					FB.api('/me', function(response) {
						var username = response.username.toString();
						var email = response.email.toString();
						var facebookid = response.id.toString();
						$.cookie("facebookid", facebookid);
						if(window.antrip){
							window.antrip.setCookie("facebookid", facebookid.toString());
						}
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/FacebookLogin.php',
							data:{email: email, facebookid: facebookid},							 
							type: 'GET', dataType: 'jsonp', cache: false,
							success:function(result){
								if(result.sid != "0"){ 
									$.cookie("sid", result.sid);
									if(window.antrip){
										window.antrip.setCookie("sid", result.sid.toString());
									}
									ChangeToUsedIcon($('#ub_trip_history'));
								} else { 
									$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/FacebookRegister.php',
										data:{username: username, email: email, facebookid: facebookid},							 
										type: 'GET', dataType: 'json', cache: false,
										success:function(result){
											if(result.sid != "0"){ 
												var body = 'Reading Connect JS documentation';
												FB.api('/me/feed', 'post', {body: body, link: 'http://antrip.plash.tw', message: 'ANTrip ¶³¤ô³~»x ¦n¦nª±!!!', picture: 'http://plash2.iis.sinica.edu.tw/antrip_icon.jpg'}, function(response) {});
												$.cookie("sid", result.sid);
												if(window.antrip){
													window.antrip.setCookie("sid", result.sid.toString());
												}
												ChangeToUsedIcon($('#ub_trip_history'));
											}
										}
									});
								}
							}
						});
					});
				}
			},{scope: 'email, publish_stream'});
	   }
	
	// trip list
	function ShowTripList(page){
		//$.mobile.showPageLoadingMsg("b", "Loading Trip List ...");
		var div_data = [];
		var sid = $.cookie("sid");
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}

		var localresult=-1;
		if(window.antrip){
			localresult = window.antrip.getLocalTripList();
		}

		$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetTripInfoComponent.php',
			data:{userid: sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				var index=1;
				$("div[id*=products]").html("");
				$("div[id*=tripsum]").html("");
				if(page==null){
					page=1;
				}
				if((result == null || result.tripInfoList == null || result.tripInfoList.length==0) && (localresult==-1 || localresult.tripInfoList == null) ){
					var str_noTrip = g_str_notrip;
					//alert(str_noTrip);
					var appendcontent="<div class='class_friend_bt' style='margin-top:200px;position:static; height:480px;vertical-align:middle;'><b>"+str_noTrip+"</b></div>";
					$("div[id=products]").eq(0).append(appendcontent);
				}
				else{
					g_tripnum = result.tripInfoList[0].trip_id;
					g_triplength = result.tripInfoList.length;
					$("div[id*=tripsum]").append(g_str_numberoftrip+g_triplength);
					if(window.antrip && localresult!=-1)
					{
//						$.each(localresult.tripInfoList, function(i,data){
							var data = localresult.tripInfoList[0];
							alert(localresult.tripInfoList[0]);
							var tripurl = "#sym_editpage?userid="+ sid +"&trip_id="+ data.trip_id;
							var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.st_addr_prt2 +"&zoom=12&size=100x100&sensor=false";
							var appendcontent;
				
							appendcontent="<button class='tripItem' onClick=\"if(g_isForMobile==false){ChangeToUsedIcon($('#ub_trip_management'));}else{g_trip="+data.trip_id+";OnlyShowADiv($('#ub_trip_management'));changeIconToBackBtn();$('#sym_edit_bt_list').hide();$('#map_canvas').css('margin-top','0');}ShowTripMapfromID("+sid+","+data.trip_id+");\" href='" + tripurl  + "'><div class='product'><div class='wrapper'><div class='listview_image'><a class='listview' href='" + tripurl  + "' rel='external'><img src='" + mapurl + "' style='border:2px solid #555;'/></a></div><div class='listview_description'><a class='listview' href='" + tripurl  + "' rel='external'><h3>" + data.trip_name  + "</h3><p>"+g_str_start+ ":" + data.trip_st + "</p><p>"+g_str_end+": " + data.trip_et  + "</p><p>"+g_str_Length+": " + data.trip_length  + " M</p></a></div></div></div></button>";
							
							if(page==0|| g_isForMobile==true ||((index>(page-1)*g_numsofpage)&&(index<=page*g_numsofpage))){
								$("div[id=products]").eq(0).append(appendcontent);
							}

	//						index++;
//						});
					}

					$.each(result.tripInfoList, function(i,data){
						
						var tripurl = "#sym_editpage?userid="+ sid +"&trip_id="+ data.trip_id;
						var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.st_addr_prt2 +"&zoom=12&size=100x100&sensor=false";
						var appendcontent;

						appendcontent="<button class='tripItem' onClick=\"if(g_isForMobile==false){ChangeToUsedIcon($('#ub_trip_management'));}else{g_trip="+data.trip_id+";OnlyShowADiv($('#ub_trip_management'));changeIconToBackBtn();$('#sym_edit_bt_list').hide();$('#map_canvas').css('margin-top','0');}ShowTripMapfromID("+sid+","+data.trip_id+");\" href='" + tripurl  + "'><div class='product'><div class='wrapper'><div class='listview_image'><a class='listview' href='" + tripurl  + "' rel='external'><img src='" + mapurl + "' style='border:2px solid #555;'/></a></div><div class='listview_description'><a class='listview' href='" + tripurl  + "' rel='external'><img src='"+im+"uploadedmarker.png' align=right></img><h3>" + data.trip_name  + "</h3><p>"+g_str_start+ ":"+ data.trip_st + "</p><p>"+g_str_end+": " + data.trip_et  + "</p><p>"+g_str_Length+": " + data.trip_length  + " M</p></a></div></div></div></button>";

						if(page==0|| g_isForMobile==true ||((index>(page-1)*g_numsofpage)&&(index<=page*g_numsofpage))){
							$("div[id=products]").eq(0).append(appendcontent);
						}

						index++;
					});

					$("button","div[id*=products]" ).button();
				}
			}
		});
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
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}
		if(g_trip>1){
			g_trip--;
			ShowTripMapfromID(sid, g_trip);
		}
		else{
			alert(g_str_firstrip);
		}
	}

	function nextTripAction(){
		var sid = $.cookie("sid");
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}
		if(g_trip<g_tripnum){
			g_trip++;
			ShowTripMapfromID(sid, g_trip);
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
		var sid = $.cookie("sid");
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}
		$("#friend_list").html("");
		var div_data = [];
		//$.mobile.showPageLoadingMsg("b", "Loading Friend List ...", true);
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				if(result.friend_list.length==0){
					var str_noFriend = g_str_nofriend;
					//alert(str_noFriend);
					var appendcontent="<div class='class_friend_bt' style='margin-top:200px;position:static; height:480px;vertical-align:middle;'><b>"+str_noFriend+"</b></div>";
					$("#friend_list").append(appendcontent);
				}
				else{
					$.each(result.friend_list, function(i,data){
						div_data[i] ="<button class='class_friend_bt' style='background:rgba(255,255,255,0) url("+im+"friend_bk.png) 100% 100% no-repeat;'><li><a href='#'><img src='" + data.image + "'/><h3 style='color:#000000;'>" + data.name  + "</h3><p></p></a></li></button>";
					});
					$("#friend_list").append(div_data.join('')).listview('refresh');
					$("button","#friend_list" ).button();
				}
				//$.mobile.hidePageLoadingMsg();
			}
		});
		$("#sym_friends").show();
	}

	var g_showtripmap = false;

	function ShowTripMapfromID(userid, trip_id){
		g_showtripmap = true;
		$('#map_canvas').show();
		$('#map_canvas_2').hide();
		GetTripPointfromID(userid, trip_id);
		g_trip=trip_id;
	}
//-->