<!--
	//Load Language files.
	var g_lang="Chinese";
	var fixedValue = 20;

	function setLanguage(){
		var lang =  $.cookie("g_lang");
		if(lang!=null && lang!='undefined'){
			g_lang = lang;
		}
		if(g_lang=="English"){
			if(typeof setEnglishLang == 'function'){
				setEnglishLang();
			}
		}
		else if(g_lang=="Chinese"){
			if(typeof setChineseLang == 'function'){
				setChineseLang();
			}
		}
		refreshMultipleLang();
	}

	function getRootPath(){ 
		var strFullPath=window.document.location.href; 
		var strPath=window.document.location.pathname; 
		var pos=strFullPath.indexOf(strPath); 
		var prePath=strFullPath.substring(0,pos); 
		var postPath=strPath.substring(0,strPath.substr(1).indexOf('/')+1); 
		return(prePath+postPath); 
	} 

	function isPCMacOS(){
	//	if(navigator.platform == "Win16" ||  navigator.platform == "Mac" || navigator.platform == "Win32" || navigator.platform == "Win64"){
			return true;
	//	}
	//	else{
	//		return false;
	//	}
	}

	function setLoginBKLocation(){
		var loginHeight = $(window).height();
		$('#ui-carousel-next').css('top', loginHeight/2-loginHeight*0.1);
		$('#ui-carousel-prev').css('top', loginHeight/2-loginHeight*0.1);
		$('.stage_class').css('height', loginHeight);
		$('.bklogo_class').css('height', loginHeight);
		$('.loginFun_class').css('height', loginHeight);
	}

	function setMapCanvasSize(IsExtend){
		var fixedValue = 20;
		var calheight = document.body.clientHeight;
		var calWidth = document.body.clientWidth;
		if(isPCMacOS()==false){
			calheight = window.innerHeight*80/100;
		}
		if(IsExtend == null || IsExtend=="undefined"){
			$("#map_canvas").css('height', calheight-fixedValue-$("#topmenu_div").height());
			$("#player_canvas").css('height', calheight-fixedValue-$("#topmenu_div").height());
		}
		else{
			$("#map_canvas").css('height', calheight-fixedValue);
			$("#player_canvas").css('height', calheight-fixedValue);
		}
		$(".rightBtnList").css('margin-left', calWidth-80);
		$("#map_canvas").css('width', document.body.clientWidth-fixedValue);
		$("#player_canvas").css('width', document.body.clientWidth-fixedValue);
	}

	function initCautionDialog(){
		$("#caution_page").dialog({
			autoOpen: false,
			bgiframe: false,
			draggable: false,
			resizable: false,
			height:600,
			width: 800,
			modal: true,
			title:g_str_caution,
			show: { effect: 'drop', direction: "up"},
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
				$(this).attr('title',g_str_caution);
				$('#sym_video').hide();
			},
			close: function() {
				 $('#sym_video').show();
			},
			buttons: [
					{
						text: "OK",
						click: function() {
								  $( this ).dialog( "close" );
								  $('#sym_video').show();
								  return;
							   }
					}
			]
		});
	}
	function initScrollFrameToEndEvent(){
			//Attach function to the scroll event of the div
			var obj = $(window.frames['triipListFrame']);
			obj.scroll(function () {
				var scrolltop = obj.scrollTop();
				var scrollheight = obj[0].document.body.scrollHeight;
				var windowheight = obj[0].document.body.clientHeight;
				if(isPCMacOS()==false){
					windowheight = window.innerHeight;
				}
				var scrolloffset = 10;
				if (scrolltop >= (scrollheight - (windowheight + scrolloffset))) {
					var callframe = window.frames['triipListFrame'];
					callframe.g_page++;
					callframe.showTripList(callframe.g_page);
				}
			});
	}
	function refreshMultipleLang(){
		//set language
		var select = $('#langOption');
		if(select.prop) {
		  var options = select.prop('options');
		}
		else {
		  var options = select.attr('options');
		}

		if(options!=null){
			$("#langOption option").remove();
			$.each(newOptions, function(val, text) {
				options[options.length] = new Option(text, val);
			});
			// select default language index
			if(g_lang==null){
				$("#langOption option[0]").attr('selected', 'selected');
			}
			else{
				$("#langOption option[value='"+g_lang+"']").attr('selected', 'selected');
			}
		}

		$.cookie("lang", g_lang);

		sid = $.cookie("sid");
		if(sid==null || sid=='undefined'){
			$("#loginDes").html(g_str_welcome_anonmous+"<br/>"+g_str_or+"<a class='listview' href='javascript:showRegisterDialog();' ><h2 style='font-size:large'>"+g_str_register+"</h2></a>");
		}
		else{
			username = $.cookie("username");
			$("#loginDes").text(g_str_welcome+username);
		}

		$('#username').attr('placeholder',g_str_id);
		$('#password').attr('placeholder',g_str_password);
		$("#download_tip").html(g_str_download_tip);
		$("#caution_tip").html(g_str_caution);

		$("#banner_text").html(g_str_sologn);
		$("#span_langtitle").html(g_str_selectLang);
		
		//update background
		$('.bklogo_class').css("background-image", url+im+g_str_bkImage+")");

		// update buttons icon
		$("#img_des_ant").attr('src',im+'ant_24.png');
		$("#ub_download").attr('src',im+'download.png');
		$("#ub_caution").attr('src',im+'caution.png');

		$(".takemarker_bt_link").html(g_str_checkin);
		$(".login_bt_link").html("<p>" +g_str_login+"</p>");
		$(".logout_bt_link").html("<p>" +g_str_logout+"</p>");
		$(".antrip_bt_link").html("<p>" +g_str_startAntrip+"</p>");
		$(".fblogin_bt_link").html("<p>" + g_str_loginFB+"</p>");
		$(".pre_page_link").html("<p>" +"<<"+"</p>");
		$(".next_page_link").html("<p>" +">>"+"</p>");
		$(".pre_trip_link").html("<p>" +"<<"+"</p>");
		$(".next_trip_link").html("<p>" +">>"+"</p>");

		//update tips
		$("#home_tip").html(g_str_home);
		$("#friend_tip").html(g_str_friends);
		$("#shared_triplist_tip").html(g_str_shared_triplist);
		$("#open_trip_tip").html(g_str_openTrip);
		$("#seg_trip_tip").html(g_str_segTrip);
		$("#add_note_tip").html(g_str_addNote);
		$("#exp_trip_tip").html(g_str_expTrip);
		$("#showStaticMap_tip").html(g_str_copythelink);
		$("#playMap_tip").html(g_str_playmap);
		$("#showDynamicMap_tip").html(g_str_copythedlink);
		$("#openCheckIn_tip").html(g_str_opencloseMarker);
		$("#shareMap_tip").html(g_str_sharemap);
		$("#deleteMap_tip").html(g_str_deletemap);

		//update page content
		$("#caution_page").html(g_str_caution_string);
		$(".sologan_class").html(g_str_sologan_string);

		//update request button
		$('#findRequest .ui-btn-text').text(g_str_request_friend);
		$('#searchFriend').attr('placeholder', g_str_search);

		$("#radio_trip_id_label").text(g_str_radio_trip_id);
		$("#radio_trip_st_label").text(g_str_radio_trip_st);
		$("#radio_trip_length_label").text(g_str_radio_length_st);
	}

	function Logout(){
		if($.cookie("facebookid")){
			FB.logout(function(response) {
				$.cookie("facebookid", null);
			}); 
		}
		$.cookie("sid", null);
		$.cookie("username", null);
		$.cookie("g_trip", null);
		$.cookie("hash", null);
		g_tripnum = 0;
		g_tripname ="";
		g_tripIndex=0;
		g_tripLength = 0;
		$("#username").attr("value","");
		$("#password").attr("value","");
		top.window.location = "index.html";
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
					showTripList(g_page);
                }
            });
	}

	function changeTripName(trip_id, trip_name){
		var sid = $.cookie("sid");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/TripInfoManagerComponent.php',
			data:{"task_id":1, "userid":sid,"trip_id":trip_id, "trip_name":encodeURI(trip_name)},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				showTripListInFrame();
			},
			error: function () {
				alert("failed");
			}
		});
	}

	function downloadPage(){
		window.open(g_str_downloadlink,"_blank ");
		return;
	}

	/* Tooltip function*/
	var g_tip;
	var g_tip_intval = null;
	var tip_img = im+"tipline.png";
	function slowHideTip(){
			g_tip.fadeOut('slow', function() {
				g_tip.hide();
			});
	}
	function initTips(){
			$('div[class*=class_top_bt]').each(function() {
			var tip = $(this).find('.tip');
			$(this).hover(

			function() {
				tip.appendTo('body');
			}, function() {
				tip.appendTo(this);
			}).mouseenter(function(e) {
				var x = e.pageX - 80,
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
				g_tip = tip;
				g_tip_intval = setTimeout("slowHideTip()",3000);
			}).mouseleave(function(e) {
				if(g_tip_intval != null){
					window.clearInterval(g_tip_intval);
					g_tip_intval = null;
				}
				tip.hide();
			});
		});
	}
	(function($)  
	{  
		jQuery.fn.setfocus = function()  
		{  
			return this.each(function()  
			{  
				var dom = this;  
				setTimeout(function()  
				{  
					try { dom.focus(); } catch (e) { }   
				}, 0);  
			});  
		};  
	})(jQuery);
//-->