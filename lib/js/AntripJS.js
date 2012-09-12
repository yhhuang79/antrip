<!--
	//**<-- global parameter -->**
	var back_img = im+"background-img.png";
	var scroll_img = im+"scroll2.png";
	var topbk_img = im+"topbt_bk.png";
	var backlogo_img = im+"background.png";
	var banner_logo_img = im+"banner_logo.png";
	var tip_img = im+"tipline.png";
	var trip_m_img = im+"ub_trip_management_r.png";

	var g_page=1;
	var g_numsofpage=5;
	var g_tripnum=0;
	var g_triplength=0;
	var g_scale=1.2;
	// google map var
	var g_myOptions = {zoom:g_zoom,center:g_startLatLng,mapTypeId:google.maps.MapTypeId.ROADMAP};
	var g_map = null;
	var g_infowindow=null;
	var g_isForMobile=false;
	var g_trip=-1;
	var g_triplist_width="1200px";
	var g_triplist_height="800";

	var g_enablewithoutLogin=false;

	var g_mapPath = null;
	var g_tripname = null;
	var g_noteid = 0;
	var g_tripList_result = null;

	/*! html file ready*/
	$(document).ready(function(){
		$("button","#sym_takepicture" ).button();
		$("button","#sym_selectemotion" ).button();
		$('#markplacewindow').hide();
		$('#sym_topbtnGroup').hide();
		PreloadandSet();
		$('#sym_topbtnGroup').fadeIn('slow', function() {
			$('#sym_topbtnGroup').show();
		});
	});

	function openMarkplaceWindow(){

	}

	function PreloadandSet(){
		var overlay = jQuery('<div valign=center id="overlay"></div>');
		overlay.appendTo(document.body);
		$("#overlay").css("display","block");

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
		require("lib/js/getURLVar.js");
		require("lib/js/AntripLogin.js");
		require("lib/js/GetTripFromID.js");
		require("lib/js/AlbumSlider.js");
		require("lib/js/MapEditing.js");
		loadjscssfile("lib/css/visualize.css", "css");
		loadjscssfile("lib/css/visualize-light.css", "css");
		require("lib/jq_includes/visualize.jQuery.js");
		loadjscssfile("lib/css/pcframe.css", "css");

		//forbidden select all
		$(document).bind("selectstart",function(){return false;});

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

			$( "#map_canvas" ).droppable({
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

		ChangeToUsedIcon($('#ub_trip_history'),false);

		$('#body').css("background-image", url+back_img+")");
		$('#Stage').css("background-image", url+scroll_img+")");

		if(!g_isForMobile){
			$('#images_scroll').css("background-image", url+images_scroll_img+")");
		}
		
		// multi-lang detection from user selection
		$('#langOption').change(function() 
		{
		   g_lang=$(this).attr('value');
		   setLanguage();
		});
		initNoteDialog();
		initRegisterDialog();
		initTripNameDialog();

		$('#calculation_table').visualize({type: 'area', width: '800px', height:'180px'});

		$(".aradio label").css("background","rgba(140,199,214,0.5)");
		$(".aradio label").css("color","#000000");
		$(".aradio label").css("background-image","none");
		$(".aradio label").css("font-size","10px");

		$(".aradio label").css("filter", "alpha(opacity=50)");
		$(".aradio label").css("-moz-opacity", "0.5");
		$(".aradio label").css("-khtml-opacity", "0.5");
		$(".aradio label").css("width","150px");


		$(".aradio_trip label").css("background","rgba(238,77,77,0.5)");
		$(".aradio_trip label").css("color","#000000");
		$(".aradio_trip label").css("background-image","none");
		$(".aradio_trip label").css("font-size","10px");

		$(".aradio_trip label").css("filter", "alpha(opacity=50)");
		$(".aradio_trip label").css("-moz-opacity", "0.5");
		$(".aradio_trip label").css("-khtml-opacity", "0.5");
		$(".aradio_trip label").css("width","230px");

		$('input:radio[name=radio]').click(
				function(){
					UpdateCalculation($("input:radio[name=radio]:checked").val());
				}
		);

		$('input:radio[name=radio_triplist]').click(
				function(){
					showTripList(g_page, $("input:radio[name=radio_triplist]:checked").val());
				}
		);

		initClipboard();
		$("#OpenCloseMarker").click(function(){
			OpenAllMarkerInfo(g_openMarker);
			//alert(g_openMarker);
			if(g_openMarker==true){
				g_openMarker = false;
			}
			else{
				g_openMarker = true;
			}
		});

		$("#overlay").html(g_str_loading);
		$("#overlay").css("display","none");
	}

	function inputNoteDialog(){
		var data="<p class=\"validateTips\">"+g_str_tripnotedes+"</p><textarea id='notetextarea'  class=\"text ui-widget-content ui-corner-all large_text\" style=\"font-size:20px;width:350px;height:300px;resize:none;\"></textarea>"; 
		$('#dialog').html( data );
		$("#dialog").dialog({
			title: g_str_tripnote,
				bgiframe: false,
				width: 450,
				height: 550,
				modal: true,
				draggable: false,
				resizable: false,
				show: { effect: 'drop', direction: "up" } ,
				open: function (event, ui) {
					$('.ui-dialog-buttonpane').css({
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
						'background-repeat': 'no-repeat',
					});
					$("button").css({
						color: "#000000",
					});
					$('.ui-dialog .ui-dialog-content').css({
						'background': '#ffffff',
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
						'background-repeat': 'no-repeat',
					});
				},
				buttons: {
					'OK': function() {
						if($('#notetextarea').val()!=""){
							g_noteid++;
							var noteid = g_noteid.toString();
							$("#map_canvas").append("<div id=\"des_display_unit"+noteid+"\" class=\"ui-widget ui-state-default ui-corner-all des_display_unit_class\"><div id=\"des_close"+noteid+"\" class=\"des_close_class\">X</div><div id='des_textarea'"+noteid+">"+$('#notetextarea').val()+"</div></div>");
							$( "#des_display_unit" +g_noteid.toString() ).resizable();
							$("#des_textarea"+noteid).attr('contentEditable',true);
							$("#des_display_unit"+noteid).fadeIn('slow', function(){ $("#des_display_unit"+noteid).show(); });
							$("#des_close"+noteid).click(function () {
								  $("#des_display_unit"+noteid).fadeOut('slow', function(){ $("#des_display_unit"+noteid).remove(); });
							});
							$( "#des_display_unit" +g_noteid.toString()).draggable({ revert: "invalid" });
						}
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
		showTripList(g_page);
		$("#sym_triplist_in_open_dialog").dialog({
			title: g_str_triplist,
				bgiframe: false,
				width: g_triplist_width,
				height: g_triplist_height,
				modal: true,
				draggable: false,
				resizable: false,
				show: { effect: 'drop', direction: "up" } ,
				color: "black",
				open: function (event, ui) {
					$('.ui-dialog-buttonpane').css({
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
						'background-repeat': 'no-repeat',
					});
					$("button").css({
						color: "#000000",
					});
					$('.ui-dialog .ui-dialog-content').css({
						'background': '#ffffff',
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
						'background-repeat': 'no-repeat',
					});
				},
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
		sid = $.cookie("sid");
		if(sid!=null){
			$("#sym_loginarea").hide();
			$(".class_login_bt").hide();
			$('.class_fblogin_bt').hide();
			$(".class_logout_bt").show("slow");
			var usrname = $.cookie("usrname");
			$("#sym_logintext").text(g_str_welcome+usrname);
		}
		else{
			$('#sym_loginarea').show("slow");
			$('.class_login_bt').show();
			$('.class_fblogin_bt').show();
			$('.class_logout_bt').hide();
			$("#sym_logintext").html(g_str_welcome_anonmous+"<br/>"+g_str_or+"<a class='listview_reg' style='font-size:20px;' href='javascript:showRegisterDialog();' >"+g_str_register+"</a>");
		}
		if(g_isForMobile==false)
		{
			$('#sym_video').fadeIn('slow', function() {
				$('#sym_video').show();
			});
		}
	}
	function show_edit_div(){
		$('#sym_editpage').fadeIn('slow', function() {
			$('#sym_editpage').show();
		});
	}
	var g_DouglasArray = null;
	function UpdateCuttedTripArray(cuttedTripArray){
			g_DouglasArray = cuttedTripArray;
			$('#div_reloadtoDefault').fadeIn('slow', function() {
				$("#div_reloadtoDefault").show();
			});
	}

	var clip = null;
	var g_staticMapLink=null;

	function initStaticMap(){
			var DouglasArray;
			if(g_DouglasArray!=null){
				//alert(g_DouglasArray.length.toString());
				if(g_DouglasArray.length <= 50 && g_DouglasArray.length > 25){
					DouglasArray = GDouglasPeucker(g_DouglasArray, 5);
				}
				else if(g_DouglasArray.length <= 100 && g_DouglasArray.length > 50){
					DouglasArray = GDouglasPeucker(g_DouglasArray, 5);
				}
				else if(g_DouglasArray.length <= 250 && g_DouglasArray.length > 100){
					DouglasArray = GDouglasPeucker(g_DouglasArray, 125);
				}
				else if(g_DouglasArray.length <= 500 && g_DouglasArray.length > 250){
					DouglasArray = GDouglasPeucker(g_DouglasArray, 500);
				}
				else if(g_DouglasArray.length <= 1000 && g_DouglasArray.length > 500){
					DouglasArray = GDouglasPeucker(g_DouglasArray, 1000);
				}
				else if(g_DouglasArray.length > 1000){
					DouglasArray = GDouglasPeucker(g_DouglasArray, 2500);
				}
				else{
					DouglasArray = g_DouglasArray;
				}
			}
			else{
				if(g_tripPointArray.length <= 50 && g_tripPointArray.length > 25){
					DouglasArray = GDouglasPeucker(g_tripPointArray, 5);
				}
				else if(g_tripPointArray.length <= 100 && g_tripPointArray.length > 50){
					DouglasArray = GDouglasPeucker(g_tripPointArray, 5);
				}
				else if(g_tripPointArray.length <= 250 && g_tripPointArray.length > 100){
					DouglasArray = GDouglasPeucker(g_tripPointArray, 125);
				}
				else if(g_tripPointArray.length <= 500 && g_tripPointArray.length > 250){
					DouglasArray = GDouglasPeucker(g_tripPointArray, 500);
				}
				else if(g_tripPointArray.length <= 1000 && g_tripPointArray.length > 500){
					DouglasArray = GDouglasPeucker(g_tripPointArray, 1000);
				}
				else if(g_tripPointArray.length > 1000){
					DouglasArray = GDouglasPeucker(g_tripPointArray, 2500);
				}
				else{
					
					DouglasArray = g_tripPointArray;
				}
			}
			// update the text on mouse over
			var staticMapLink = "http://maps.google.com/maps/api/staticmap?size=600x600&path=color:0xff0000ff|weight:4";
			for(var i=0;i<DouglasArray.length;i++){
				
				staticMapLink = staticMapLink + "|";
				staticMapLink = staticMapLink + DouglasArray[i].lat()+",";
				staticMapLink = staticMapLink + DouglasArray[i].lng();
				//alert(staticMapLink);
			}
			//staticMapLink = staticMapLink + "&markers=color:red|Clabel:P|size:mid";
			for(var i=0;i<DouglasArray.length;i++){
				if(i==0){
					staticMapLink = staticMapLink + "&markers=color:green|label:S|" + DouglasArray[i].lat()+","+DouglasArray[i].lng();
				}
				else if(i==DouglasArray.length-1){
					staticMapLink = staticMapLink + "&markers=color:green|label:E|" + DouglasArray[i].lat()+","+DouglasArray[i].lng();
				}
				else{
					staticMapLink = staticMapLink + "&markers=color:red|label:P|size:mid|" + DouglasArray[i].lat()+","+DouglasArray[i].lng();
				}
			}

			staticMapLink = staticMapLink + "&sensor=false";
			g_staticMapLink = staticMapLink;
	}

	function initClipboard() {
		/*clip = new ZeroClipboard.Client();
		clip.setHandCursor( true );
		
		clip.addEventListener('load', function (client) {
			;
		});
		
		clip.addEventListener('mouseOver', function (client) {

		});*/
		
		//clip.addEventListener('complete', function (client, text) {
			//var sid = $.cookie("sid");
			//var strShowDLink = "showDTripPage.html?userid="+sid+"&trip_id="+g_trip+"&g_lang="+g_lang;
		//	window.prompt ("Copy to clipboard: Ctrl+C, Enter", g_staticMapLink);
		//	window.open(g_staticMapLink,"_blank ");
		//	window.open(strShowDLink,"dynamicTripPage");
		//});

	//	var dynamicMap = $(this).find('#copyDynamicMap');
		var sid = $.cookie("sid");
		$("#div_reloadtoDefault").click(function(){
			ShowTripMapfromID(sid.toString(), g_trip.toString());
		});

		$("#div_copyStaticMap").click(function(){
			initStaticMap();
			window.prompt ("Copy to clipboard: Ctrl+C, Enter", g_staticMapLink);
		});

		$("#div_copyDynamicMap").click(function(){
			var strShowDLink = "/showDTripFrame.html?userid="+sid+"&trip_id="+g_trip+"&g_lang="+g_lang+"&g_calUnit="+$("input:radio[name=radio]:checked").val();
			window.prompt ("Copy to clipboard: Ctrl+C, Enter", getRootPath()+strShowDLink);
		});

		//clip.glue( 'copyStaticMap', 'div_copyStaticMap' );
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
		window.scrollTo(0,0);
		if(object.attr('name')==$("#ub_download").attr('name')){
			window.open(g_str_downloadlink,"_blank ");
			return;
		}
		$('div[class*=class_fun_div]').each(function() {
			$(this).hide();
		});
		$("body").css("cursor","default");
		$("#img_seq_trip").css("cursor","pointer");
		MM_swapImage($('#img_seq_trip').attr('name'),'','images/b_seq_trip.png',1);

		if(g_isForMobile==false){
			changeCursorToDefaultMode();
		}

		sid = $.cookie("sid");
		if(object.attr('name')==$("#ub_about").attr('name')){
			$('#about_page').show("slow");
		}
		else if( g_enablewithoutLogin==false && ((sid==null) || (object.attr('name')==$("#ub_home").attr('name')))){
			var viewportHeight = $(window).height();
			window.scrollTo(0,viewportHeight/3);
			show_login_div();
			if(g_isForMobile==true){
				$("#end_Text").show();
			}
		}
		else if(object.attr('name')==$("#ub_trip_history").attr('name')){
			g_page=1;
			g_tripList_result = null;
			showTripList(g_page);
			$("#sym_triplist").show("slow");
			$("#sym_editpage").hide();
			$('.class_prepage_bt').show();
			$('.class_nextpage_bt').show();
			if(g_isForMobile==true){
				$('#markplacewindow').hide();
				$("#end_Text").hide();
			}
		}
		else if(object.attr('name')==$("#ub_trip_management").attr('name')){
			var viewportHeight = $(window).height();
			//alert(viewportHeight);
			window.scrollTo(0,viewportHeight/3);
			if($.getUrlVar('trip_id')==null){
					sid = $.cookie("sid");
					if(g_trip==-1)
					{
						if(g_tripnum!=0 && g_tripnum!=null){
							ShowTripMapfromID(sid, g_tripnum, g_tripname);
						}
						else{
							alert(g_str_notripdisplay);
						}
					}
			}
			if(g_isForMobile==true){
				$("#end_Text").hide();
			}
			show_edit_div();
		}
		else if(object.attr('name')==$("#ub_friend").attr('name')){
			showFriendList();
			if(g_isForMobile==true){
				$("#end_Text").hide();
			}
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
					"padding-left":"60px",
					"padding-top":"10px",
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

		$("input").keydown(function(evt){
			//alert(evt.keyCode);
			if(evt.keyCode==13){
				Login();
			}
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
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/login.php',
				data:{username: username, password: password},							 
				type: 'GET', dataType: 'jsonp', cache: false,
		success:function(result){
			if(result.sid != "0"){ 
				$.cookie("sid", result.sid, { expires: 1});
				$.cookie("usrname", username);
				ChangeToUsedIcon($("#ub_trip_history"));
			} else { 
				alert(g_str_loginfail);
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
		g_tripnum = 0;
		g_tripname ="";
		g_triplength=0;
		$("#username").attr("value","");
		$("#password").attr("value","");
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
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/FacebookLogin.php',
							data:{email: email, facebookid: facebookid},							 
							type: 'GET', dataType: 'jsonp', cache: false,
							success:function(result){
								if(result.sid != "0"){ 
									$.cookie("sid", result.sid);
									ChangeToUsedIcon($('#ub_trip_history'));
								} else { 
									$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/FacebookRegister.php',
										data:{username: username, email: email, facebookid: facebookid},							 
										type: 'GET', dataType: 'json', cache: false,
										success:function(result){
											if(result.sid != "0"){ 
												var body = 'Reading Connect JS documentation';
												FB.api('/me/feed', 'post', {body: body, link: 'http://antrip.plash.tw', message: 'ANTrip ¶³¤ô³~»x ¦n¦nª±!!!', picture: 'http://plash2.iis.sinica.edu.tw/antrip_icon.jpg'}, function(response) {});
												$.cookie("sid", result.sid);
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
	function showTripList(page, order){
		var sid = $.cookie("sid");
		if(order!=null || g_tripList_result==null){
			$("#overlay").css("display","block");
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php',
				data:{userid: sid, sort: order},
				type: 'GET', dataType: 'jsonp', cache: false,
				success:function(result){
					g_tripList_result = result;
					showEachTripList(page, result);
					$("#overlay").css("display","none");
				}
			});
		}
		else{
			if(order==null){
				order = "trip_st";
			}
			showEachTripList(page, g_tripList_result);
		}
	}

	function showEachTripList(page, result){
		var index=1;
		$("div[id*=products]").html("");
		$("div[id*=tripsum]").html("");
		if(page==null){
			page=1;
		}
		var prepagebt = $('#sym_triplist').find('.class_prepage_bt');
		var nextpagebt = $('#sym_triplist').find('.class_nextpage_bt');
		if((result == null || result.tripInfoList == null || result.tripInfoList.length==0) ){
			prepagebt.hide();
			nextpagebt.hide();
			$("#unit_radio_trip").hide();
			var str_noTrip = g_str_notrip;
			alert(str_noTrip);
		}
		else{
			prepagebt.show();
			nextpagebt.show();
			$("#unit_radio_trip").show();
			g_tripnum = result.tripInfoList[0].trip_id;
			g_tripname = result.tripInfoList[0].trip_name.toString();
			g_triplength = result.tripInfoList.length;
			$("div[id*=tripsum]").append(g_str_numberoftrip+g_triplength);
			$.each(result.tripInfoList, function(i,data){
				var tripurl = "#sym_editpage?userid="+ sid +"&trip_id="+ data.trip_id;
				if(data.st_addr_prt2==null || data.st_addr_prt2==""){
					data.st_addr_prt2 = data.st_addr_prt1;
				}
				var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.st_addr_prt2 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:S|size:mid|"+data.st_addr_prt2;
				var tripname = data.trip_name.toString();
				var appendcontent="<div id='div_"+sid+data.trip_id+"' style='WHITE-SPACE:nowrap;margin-right:30px;width:1100px;'><button  id='button"+sid+data.trip_id+"' class='tripItem' onClick=\"if(g_isForMobile==false){g_trip="+data.trip_id+";g_tripname='"+ tripname+"';ChangeToUsedIcon($('#ub_trip_management'));}else{OnlyShowADiv($('#ub_trip_management'));$('#sym_edit_bt_list').hide();$('#map_canvas').css('margin-top','0');}ShowTripMapfromID("+sid+","+data.trip_id+");\"><div class='product' ><div class='wrapper'><div class='listview_image' ><a class='listview' href=''><img src='" + mapurl + "'/></a></div><div class='listview_description' ><a class='listview' href=''><h3>" + data.trip_name  + "</h3><p>"+g_str_start+": " + data.trip_st + "</p><p>"+g_str_end+": " + data.trip_et  + "</p><p>"+g_str_length+": " + data.trip_length  + " M</p></a></div></div></div></button><div id='div_deletemarker'  onClick=\"confirmDelete('"+data.trip_id+"');\"><img src='"+im+"deletemarker.png' align=right></img></div></div>"
				if(page==0|| g_isForMobile==true ||((index>(page-1)*g_numsofpage)&&(index<=page*g_numsofpage))){
					$("div[id=products]").eq(0).append(appendcontent);
				}
				// this is for trip list of popup dialog.
				$("div[id=products_in_dialog]").append(appendcontent);
				
				/*$("#button"+sid+data.trip_id).click(function(){
					alert("fuck1");
				});
				$("#div_deletemarker"+sid+data.trip_id).click(function(){
					alert("fuck2");
				});*/

				index++;
			});
			$("button","div[id*=products]" ).button();
		}
	}

	function prepageAction(){
		if(g_page>1){
			g_page--;
			showTripList(g_page);
		}
		else{
			alert(g_str_firstpage);
		}
	}

	function nextpageAction(){
		if(g_page*g_numsofpage<g_tripnum){
			g_page++;
			showTripList(g_page);
		}
		else{
			alert(g_str_lastpage);
		}
	}

	function preTripAction(){
		var sid = $.cookie("sid");
		if(g_trip>1){
			g_trip--;
			ShowTripMapfromID(sid.toString(), g_trip.toString());
		}
		else{
			alert(g_str_firstrip);
		}
	}

	function nextTripAction(){
		var sid = $.cookie("sid");
		if(g_trip<g_tripnum){
			g_trip++;
			ShowTripMapfromID(sid.toString(), g_trip.toString());
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

	function deleteTrip(trip_id){
		$("#overlay").css("display","block");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/DelTripComponent.php',
			data:{userid : sid, trip_id: trip_id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				$("#overlay").css("display","none");
				$('#div_'+sid+trip_id).fadeOut('slow', function() {
					$('#div_'+sid+trip_id).hide();
				});
			},
			error: function(xhr) {
				$("#overlay").css("display","none");
				$('#div_'+sid+trip_id).fadeOut('slow', function() {
					$('#div_'+sid+trip_id).hide();
				});
			}
		});
	}

	function confirmDelete(trip_id){
		$("#dialog-confirm").dialog({
			resizable: false,
			draggable: false,
			height:200,
			width:400,
			modal: true,
			open: function (event, ui) {
					//$("#sym_topbtnGroup").removeClass("topbtnTripClass");
					$('.ui-dialog-buttonpane').css({
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
					});
					$('.ui-dialog .ui-dialog-content').css({
						'background': '#ffffff',
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
						'background-repeat': 'no-repeat',
					});
					$('#logoutwhenrecord').html(g_str_delete);
					$("button").css({
						color: "#000000",
					});
				},
			buttons: {
				"Yes": function() {
					$( this ).dialog( "close");
					 deleteTrip(trip_id);
					return;
				},
				"No": function() {
					$( this ).dialog( "close" );
					return;
				}
			}
		});
	}

	// friend list 
	function showFriendList(){
		$("#friend_list").html("");
		var div_data = [];
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				if(result.friend_list.length==0){
					alert(g_str_nofriend);
				}
				$.each(result.friend_list, function(i,data){
					div_data[i] ="<button class='class_friend_bt' style='background:rgba(255,255,255,0) url("+im+"friend_bk.png) 100% 100% no-repeat;'><li><a href='#'><img src='" + data.image + "'/><h3>" + data.name  + "</h3><p>" + data.id + "</p></a></li></button>";
				});
				$("#friend_list").append(div_data.join('')).listview('refresh');
				$("button","#friend_list" ).button();
			}
		});

		$("#sym_friends").show("slow");
	}
	function ShowTripMapfromID(userid, trip_id){
		$("body").css("cursor","default");
		$("#img_seq_trip").css("cursor","pointer");
		MM_swapImage($('#img_seq_trip').attr('name'),'','images/b_seq_trip.png',1);
		GetTripPointfromID(userid, trip_id);
		g_trip=trip_id;
		$("#tripname").html(g_str_tripname+":<input type='text' id='input_tripName' name='tripName' onChange='changeTripName("+trip_id+", this.value);' value='"+g_tripname+"' />");
		$("#sym_triplist_in_open_dialog").dialog("close");
		setToDefaultMap();
		$('#div_reloadtoDefault').hide();
		g_noteid = 0;
		g_openMarker = true;
	}

	function changeTripName(trip_id, trip_name){
		var sid = $.cookie("sid");
		/*$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/InputTripInfoComponent.php',
			data:{"userid":sid,"trip_id":trip_id, "trip_name": encodeURI(trip_name)},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				//alert("change");
			},
			error: function () {
				alert("failed");
			}
		});*/
	}

	function initTripNameDialog(){
		var sid = $.cookie("sid");
		$("#dialog-tripname").dialog({
			autoOpen: false,
			bgiframe: false,
			draggable: false,
			resizable: false,
			height:300,
			width: 400,
			show: { effect: 'drop', direction: "up" } ,
			modal: true,
			title: g_str_savetrip,
			open: function (event, ui) {
				$('.ui-dialog-buttonpane').css({
						'background-image':url+im+"typenotearea.png)",
						'background-position':"center center",
						'background-repeat': 'no-repeat',
					});
				$("button").css({
					color: "#000000",
				});
				$('.ui-dialog .ui-dialog-content').css({
					'background': '#ffffff',
					'background-image':url+im+"typenotearea.png)",
					'background-position':"center center",
					'background-repeat': 'no-repeat',
				});
				$('#typetripname').html(g_str_type_tripname);
				$('#tripinput1').html(g_str_tripname);
			},
			buttons: {
				"OK": function() {
				var tripArray = getCuttedTrip();
					if(tripArray!=null && tripArray.length>=2 ){
						var distance = 0;
						for(var i=0;i<tripArray.length-1;i++){
							$.ajax({
								url: 'php/get2PointDis.php',
								async: false,
								data: {lat1: tripArray[i].lat.valueOf(),lat2: tripArray[i+1].lat.valueOf(), lng1: tripArray[i].lng.valueOf(), lng2: tripArray[i+1].lng.valueOf()},
								dataType:'json',
								type: 'GET',
								error: function(xhr) {
									alert('Ajax request errors');
								},
								success: function(response) {
									
									distance += response;
								}
							});
						}
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetNewTripId.php',
							data:{userid: sid},	
							//async: false,
							type: 'GET', dataType: 'jsonp', cache: false,
							success:function(result){
								var tripid = result.newTripId;
								var myJSONObject ="{\"userid\":" + sid + ",\"trip_id\":" + tripid + ",\"CheckInDataList\":" + JSON.stringify(tripArray) + "}";
								$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/UploadTrip.php',
									data:{"trip":myJSONObject},
									type: 'POST', dataType: 'jsonp', cache: false,
									success:function(result){	
										if(result.message=="Ok")
										{	
											$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/InputTripInfoComponent.php',
												data:{"userid":sid,"trip_id":tripid, "trip_name": encodeURI($('#trip_name').val()), "trip_length": Math.round(distance).toString(), "num_of_pts": tripArray.length, "trip_st": encodeURI(tripArray[0].timestamp), "trip_et": encodeURI(tripArray[tripArray.length-1].timestamp), "st_addr_prt2":tripArray[0].lat+","+tripArray[0].lng },
												type: 'GET', dataType: 'jsonp', cache: false,
												success:function(result){
													alert(g_str_newTrip_1+"\""+$('#trip_name').val()+"\""+g_str_newTrip_2);
													$("#dialog-tripname").dialog( "close" );	
												},
												error: function () {
													alert(g_str_newTrip_1+"\""+$('#trip_name').val()+"\""+g_str_newTrip_2);
													$("#dialog-tripname").dialog( "close" );	
												}
											});
										}
										else{
											alert("error");
										}
									},
									error: function () {
										throw new Error("Could not send trip point");
									}
								});
							},
							error: function () {
									throw new Error("Could not get trip id");
							}
						});
					}
					else{
						alert(g_str_cuttripfirst);
					}
				}
			},
			close: function() {
			}
		});
	}
//-->