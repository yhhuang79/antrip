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
	var g_tripIndex=0;
	var g_tripLength = 0;
	var g_scale=1.2;
	// google map var
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
					showTripList(g_page);
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

		$('#trip_tags').tagsInput({'width':'auto', 'height':'30px'});

		initScrollToEndEvent($("#sym_triplist_in_open_dialog"));

		$("#overlay").html(g_str_loading);
		$("#overlay").css("display","none");
	}

	function changeUploadTextName(){
		$("#upfileName").attr('value', $("#noteImage").attr('value').substr($("#noteImage").attr('value').lastIndexOf('\\')+1));
	}

	function inputNoteDialog(){
		var data="<p class=\"validateTips\">"+g_str_tripnotedes+"</p><textarea id='notetextarea'  class=\"text ui-widget-content ui-corner-all large_text\" style=\"font-size:20px;width:350px;height:300px;resize:none;\"></textarea> <input type='text' id='upfileName' name='upfile' size='50' readonly value='"+g_str_pls_upload_image+"' onClick='this.form.file.click();'><input type='file' accept='image/*' id='noteImage' name='file' onChange='javascript:changeUploadTextName();'>"; 
		$('#CheckInForm').append( data );
		$("#dialog").dialog({
			title: g_str_tripnote,
				autoOpen: false,
				bgiframe: false,
				width: 450,
				height: 550,
				modal: true,
				draggable: false,
				resizable: false,
				show: { effect: 'drop', direction: "up" } ,
				open: function (event, ui) {
					$('#notetextarea').attr('value','');
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
					$("#upfileName").attr('value', g_str_pls_upload_image);
					$("#noteImage").attr('value', "");
				},
				buttons: {
					'OK': function() {
						if($('#notetextarea').val()!="" ||  $("#noteImage").attr('value')){
							g_map = $('#map_canvas').gmap('get','map');
							//alert($('#notetextarea').val());
							$('#CheckInForm').ajaxForm({
								dataType:  'json',
								data: {sid : sid, lat:g_map.getCenter().lat(), lng:g_map.getCenter().lng(), content: encodeURI($('#notetextarea').val()), trip_id:g_trip},
								success:function(result){
									if($("#noteImage").attr('value')==""){
										setNoteMarker($('#notetextarea').val(), "", g_map.getCenter(), result);
									}
									else{
										setNoteMarker($('#notetextarea').val(), $("#upfileName").attr('value'), g_map.getCenter(), result);
									}
								},
								error: function(xhr, ajaxOptions, thrownError) {
									alert(xhr.status);
									alert(xhr.responseText);
								}
							});
							$('#CheckInForm').submit();
						}
						$('#dialog').dialog('close');
					}
				},
				close: function() {
					//$('#dialog').dialog('close');
				}
			});
	}

	function setNoteMarker(value, picturePath, position, id, editable){
		g_map = $('#map_canvas').gmap('get','map');
		//var CheckInInfo = "";
		var CheckInInfo="<div style='display:block;overflow:hidden;'><p>";
		if(picturePath){
			var title = 'http://plash2.iis.sinica.edu.tw/picture/'+sid+"/"+g_trip+"/"+picturePath;
			CheckInInfo +="<a href='"+title+"' target='_blank' style='float:left; margin:0 5px 0 0;'><img style='display:block;' src='" + title + "' height='200' /></a>";
		}
		//CheckInInfo += value;
		CheckInInfo += "<div style='width:100%;text-align:left'>"+value +"</div>";
		CheckInInfo +="</p>";
		CheckInInfo +="</div>";
		var draggable_flag = true;

		if(editable!=null && editable!='undefined'){
			draggable_flag = false;
		}
		var notemarker = new google.maps.Marker({
				'title': value,
				'id': id,
				'position': position, 
				'animation': google.maps.Animation.DROP,
				'icon': "images/placemarker_text.png",
				'draggable': draggable_flag
		});
		var infowindow = new google.maps.InfoWindow(
		{ 
				content: CheckInInfo
		});
		google.maps.event.addListener(notemarker, 'click', function() {
			if (infowindow.getMap()==null){
				infowindow.open(g_map,notemarker);
			}
			else{
				infowindow.close();
			}
		});
		if(draggable_flag == true){
			google.maps.event.addListener(notemarker,'dragend',function(event){
				$.ajax({url:'php/updateNote.php',
					data:{sid : sid, lat:event.latLng.lat(), lng:event.latLng.lng(), trip_id:g_trip, record_id:id},
					type: 'GET', dataType: 'json', cache: false,
					success:function(result){
						//alert(result);
					},
					error: function(xhr) {
						alert('Ajax request errors');
					}
				});
			})
			google.maps.event.addListener(notemarker,'rightclick',function(event){
				var r=confirm(g_str_deleteNote)
				if (r==true){
					$.ajax({url:'php/deleteNote.php',
						data:{sid : sid, trip_id:g_trip, record_id:id},
						type: 'GET', dataType: 'json', cache: false,
						success:function(result){
							notemarker.setMap(null);
							notemarker = null;
						},
						error: function(xhr) {
							alert('Ajax request errors');
						}
					});
				}else{
					;
				}
			});
		}
		notemarker.setMap(g_map);
	}

	function TripListDialog(){
		g_page=1;
		//showTripList(g_page);
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
					showTripList(1);
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
			var username = $.cookie("username");
			$("#sym_logintext").text(g_str_welcome+username);
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

	function initClipboard() {
		var sid = $.cookie("sid");
		$("#div_reloadtoDefault").click(function(){
			ShowTripMapfromID(sid.toString(), g_trip.toString());
		});

		$("#div_copyStaticMap").click(function(){
			initStaticMap();
			window.prompt ("Copy to clipboard: Ctrl+C, Enter", g_staticMapLink);
		});

		$("#div_copyDynamicMap").click(function(){
			var strShowDLink = "/showDTripFrame.html?userid="+sid+"&trip_id="+g_trip+"&g_lang="+g_lang+"&g_calUnit="+$("input:radio[name=radio]:checked").val()+"&picture_uri="+g_picture_uri;
			window.prompt ("Copy to clipboard: Ctrl+C, Enter", getRootPath()+strShowDLink);
		});
	}

	function initScrollToEndEvent(obj){
            //Attach function to the scroll event of the div
            obj.scroll(function () {
                var scrolltop = obj.scrollTop();
                var scrollheight = obj[0].scrollHeight;
                var windowheight = obj[0].clientHeight;
                var scrolloffset = 20;
                if (scrolltop >= (scrollheight - (windowheight + scrolloffset))) {
					g_page++;
					showTripList(g_page, false);
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
					if(g_tripnum!=0 && g_tripnum!=null){
						ShowTripMapfromID(sid, g_trip);
					}
					else{
						alert(g_str_notripdisplay);
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

	function nextTripAction(){
		var sid = $.cookie("sid");
		if(g_tripIndex>=1){
			g_tripIndex--;
			g_trip = g_tripList_result.tripInfoList[g_tripIndex].trip_id;
			g_tripname = g_tripList_result.tripInfoList[g_tripIndex].trip_name;
			ShowTripMapfromID(sid.toString(), g_trip.toString());
		}
		else{
			alert(g_str_firstrip);
		}
	}

	function preTripAction(){
		var sid = $.cookie("sid");
		if(g_tripIndex<g_tripLength-1){
			g_tripIndex++;
			g_trip = g_tripList_result.tripInfoList[g_tripIndex].trip_id;
			g_tripname = g_tripList_result.tripInfoList[g_tripIndex].trip_name;
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

	function deleteTrip(trip_id, isRefreshMap){
		$("#overlay").css("display","block");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/DelTripComponent.php',
			data:{userid : sid, trip_id: trip_id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				$("#overlay").css("display","none");
				if(top.frames['triipListFrame']){
					if(isRefreshMap==false){
						top.frames['triipListFrame'].location="frame-3.html?refreshMap=0";
					}
					else{
						top.frames['triipListFrame'].location="frame-3.html";
					}
				}
			},
			error: function(xhr) {
				$("#overlay").css("display","none");
				if(top.frames['triipListFrame']){
					if(isRefreshMap==false){
						top.frames['triipListFrame'].location="frame-3.html?refreshMap=0";
					}
					else{
						top.frames['triipListFrame'].location="frame-3.html";
					}
				}
			}
		});
	}

	function confirmDelete(trip_id, isRefreshMap){
		var r=confirm(g_str_delete)
		if (r==true){
			deleteTrip(trip_id, isRefreshMap);
		}else{
			;
		}
	}

	function getFrameWidth(){
		var width="100%";
		if( $(window.frames['triipListFrame']) ){
			var fixedValue = 100;
			width = top.window.frames["triipListFrame"].document.body.clientWidth - fixedValue;
		}

		return width;
	}

	// friend list 
	var g_friendnum = 0;
	function showFriendList(){
		$("div[id=products]").eq(0).html("<br/><br/><br/>");
		var div_data = [];

		var width=getFrameWidth();
		div_data[0]  ="<div id='div_friend_0' style='WHITE-SPACE:nowrap;margin-top:-5px;margin-right:0px;width:100%;display:table-row;'><button class='class_friend_bt' ><div class='product' ><div class='wrapper' style='width:"+width+"px' ><div class='listview_image' ><img src=''/></div><div class='listview_description listview' >"+ g_str_shareToAllUser  + "<br/><span id='tripnumOfFriend_0'></span></div></div></div></button></div><div id='div_friend_appendTrip_0' style='display:none;'></div>";
		$("div[id=products]").eq(0).append(div_data[0]);

		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetPublicTripInfo.php',
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				$('#tripnumOfFriend_0').html(g_str_sharenumberoftrip+result.tripInfoList.length);
				$("#div_friend_0").click(function(){
					if( $("#div_friend_appendTrip_0").is(":visible") ) {
						$("#div_friend_appendTrip_0").hide("slow");
					}
					else{
						if($("#div_friend_appendTrip_0").html()=="" || $("#div_friend_appendTrip_0").html()=='undefined'){
							showPublicTripListResult(result, $("#div_friend_appendTrip_0"));
						}
						$("#div_friend_appendTrip_0").show("slow");
					}
				});
			}
		});

		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				if(result.friend_list.length==0){
					//alert(g_str_nofriend);
					return;
				}
				g_friendnum = result.friend_list.length;
				$("div[id*=tripsum]").html(g_str_numberoffriend+g_friendnum);
				$.each(result.friend_list, function(i,data){
					if(i==0){
						;
					}
					else{
						div_data[i] ="<div id='div_friend_"+sid+data.id+"' style='WHITE-SPACE:nowrap;margin-top:-5px;margin-right:0px;width:100%;display:table-row;'><button class='class_friend_bt' ><div class='product' ><div class='wrapper' style='width:"+width+"px' ><div class='listview_image' ><img src='" + data.image + "'/></div><div class='listview_description listview' >"+g_str_id+":" + data.name  + "<br/><span id='tripnumOfFriend_"+sid+data.id+"'></span></div></div></div></button></div><div id='div_friend_appendTrip_"+sid+data.id+"' style='display:none;'></div>";
						$("div[id=products]").eq(0).append(div_data[i]);
						var _result= showShareTripByFriend(data, $("#div_friend_appendTrip_"+sid+data.id));
						if(_result.tripList!="" && _result.tripList!='undefined'){
							var trip_list=_result.tripList.toString().split(",");
						//alert(tripnum);
							$('#tripnumOfFriend_'+sid+data.id).html(g_str_sharenumberoftrip+trip_list.length);
							$("#div_friend_"+sid+data.id).click(function(){
								//alert($("#div_friend_appendTrip_"+sid+data.id).html());
								if( $("#div_friend_appendTrip_"+sid+data.id).is(":visible") ) {
									$("#div_friend_appendTrip_"+sid+data.id).hide("slow");
								}
								else{
									if($("#div_friend_appendTrip_"+sid+data.id).html()=="" || $("#div_friend_appendTrip_"+sid+data.id).html()=='undefined'){
										showTripListByFriend(data, _result, $("#div_friend_appendTrip_"+sid+data.id));
									}
									$("#div_friend_appendTrip_"+sid+data.id).show("slow");
								}
							});
							}
						else{
							$('#tripnumOfFriend_'+sid+data.id).html(g_str_sharenumberoftrip+"0");
						}
					}
				});

				$("button","div[id*=products]" ).button();
			}
		});
	}

	function addFriend(userid, friendname){
		$("#overlay").css("display","block");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/FriendRequest.php',
			data:{user_id : userid, friend_name : friendname},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				$("#overlay").css("display","none");
				alert(g_str_addfriend+" - " + friendname);
			}
		});			
	}
	function confirmFriend(fid, friendname, passcode){
		$("#overlay").css("display","block");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/ConfirmFriendRequest.php',
			data:{fid : fid, friendname : friendname, passcode : passcode},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				requestFriendList();
				$("#overlay").css("display","none");
			}
		});			
	}

	function requestFriendList(){
		var div_data = [];
		$("div[id=products]").eq(0).html("<br/><br/><br/>");
		$("#overlay").css("display","block");
		var width=getFrameWidth();
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendRequestList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				$.each(result.friendrequest_list, function(i,data){
					var requestUrl = "javascript:confirmFriend(" + data.fid + ",\"" + data.name + "\",\"" + data.passcode + "\")";
					div_data[i] ="<div id='div_"+sid+data.id+"' style='WHITE-SPACE:nowrap;margin-top:-5px;margin-right:0px;width:100%;display:table-row;'><button class='class_friend_bt' onClick='"+requestUrl+"' ><div class='product' ><div class='wrapper' style='width:"+width+"px'><div class='listview_image' ><img src='" + data.image + "'/></div><div class='listview_description listview' >"+g_str_id+":" + data.name  + "<br/><a href='" + requestUrl + "' data-icon='plus'  data-role='button' data-transition='fade' data-theme='a' >Add Friend</a></div></div></div></button></div>";
				});
				$("div[id=products]").eq(0).append(div_data.join(''));
				$("button","div[id*=products]" ).button();
				$("#overlay").css("display","none");
			}
		});
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
					if(tripArray==null || tripArray.length<2 ){
						tripArray = g_tripArray;
					}
						$("#overlay").css("display","block");
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
													$("#overlay").css("display","none");
													alert(g_str_newTrip_1+"\""+$('#trip_name').val()+"\""+g_str_newTrip_2);
													$("#dialog-tripname").dialog( "close" );
													if(top.frames['triipListFrame']){
														top.frames['triipListFrame'].location="frame-3.html?refreshMap=0";
													}
												},
												error: function () {
													$("#overlay").css("display","none");
													alert(g_str_newTrip_1+"\""+$('#trip_name').val()+"\""+g_str_newTrip_2);
													$("#dialog-tripname").dialog( "close" );
													if(top.frames['triipListFrame']){
														top.frames['triipListFrame'].location="frame-3.html?refreshMap=0";
													}
												}
											});
										}
										else{
											$("#overlay").css("display","none");
											alert("error");
										}
									},
									error: function () {
										$("#overlay").css("display","none");
										throw new Error("Could not send trip point");
									}
								});
							},
							error: function () {
									$("#overlay").css("display","none");
									throw new Error("Could not get trip id");
							}
						});
					/*}
					else{
						alert(g_str_cuttripfirst);
					}*/
				}
			},
			close: function() {
			}
		});
	}
//-->