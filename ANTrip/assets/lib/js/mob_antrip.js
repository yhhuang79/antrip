<!--
		//load js and css file
		function loadjscssfile(filename, filetype){
			 if (filetype=="js"){ //if filename is a external JavaScript file
				 var fileref=document.createElement('script');
				  fileref.setAttribute("type","text/javascript");
				  fileref.setAttribute("src", filename);
			 }
			 else if (filetype=="css"){ //if filename is an external CSS file
				  var fileref=document.createElement("link");
				  fileref.setAttribute("rel", "stylesheet");
				  fileref.setAttribute("type", "text/css");
				  fileref.setAttribute("href", filename);
			 }
			 if (typeof fileref!='undefined'){
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
			var filename;
			if(g_lang=="ZH" ||g_lang=="CHINA" || g_lang=="CHINESE" || g_lang=="PRC" || g_lang=="SIMPLIFIED_CHINESE" || g_lang=="TAIWAN" || g_lang=="TRADITIONAL_CHINESE"){
				filename = "lib/lang/lang_Chinese.js";
				if(typeof setEnglishLang == 'function'){
					setChineseLang();
				}
			}
			else{
				filename = "lib/lang/lang_English.js";
				if(typeof setEnglishLang == 'function'){
					setEnglishLang();
				}
			}
		}
		// dropdown list
		var g_page="";

		$(document).ready(function(){
			var x=new Image(); 
			x.onerror=function(){
				alert('離線瀏覽');
				$("#map_canvas_1").hide();
				$("#map_canvas_2").hide();
			} 
			x.src= "http://plash2.iis.sinica.edu.tw/antrip/images/banner_logo.png"; 
		});

		function initLanguage(){
			setLanguage();
			initAPP();
		}
		
		function initAPP(){
			g_IsInited = true;
			var appendcontent = "<ul id='menu-left' data-role='menu'><li style='list-style-type:none;'><span data-role='button' data-icon='arrow-d' data-iconpos='right'><img id='dropdown_toggle' /></span><ul data-role='listview' data-inset='true' class='dropdown-menu'><li class='class_top_bt dropmenu_item' data-icon='false'><a class='class_top_bt_link'><img class='class_top_bt' id='tb_trip_recorder' /></a><span class='tip' id='tip_ub_trip_m'></span></li><li class='class_top_bt dropmenu_item' data-icon='false'><a class='class_top_bt_link' ><img class='class_top_bt' id='tb_trip_history' /></a><span class='tip' id='tip_ub_trip_h'></span></li><li class='class_top_bt dropmenu_item' data-icon='false'><a class='class_top_bt_link'><img class='class_top_bt' id='tb_friend' /></a><span class='tip' id='tip_ub_f'></span></li></ul></li></ul>"; 

			//init login page
			$('#blogin').unbind('click');
			$("#blogin").click(function(){
				Login();
			});
			$("input").keydown(function(evt){
				sid = $.cookie("sid");
				if(window.antrip){
					sid = window.antrip.getCookie("sid");
				}
				if(sid==null){
					if(evt.keyCode==13){
						Login();
					}
				}
			});

			//add footer to login page
			$('#login').append("<div data-role='footer' class='nav-glyphish-example ui-footer ui-bar-a  center_footer' data-position='fixed'><h6><img id='img_footer_ant' src='' />&nbsp;&nbsp;© 2012 ANTrip IIS Sinica | Design by ANTS</h6></div>");

			//init main page
			$('#mainpage').find('div:jqmData(role="header")').prepend(appendcontent);
			$('#mainpage').find('div:jqmData(role="header")').find('h4').replaceWith( "<"+g_titleSize+" style='text-align:center;margin-top:-70px;'>" + g_str_antrip + "</"+g_titleSize+">" );

			//clear mainpage content
			$('#mainpage').find('div:jqmData(role="content")').html("");
			//add locating button
			$('#map').find('div:jqmData(role="content")').prepend("<div class='locating_class'><a href='javascript:initRecorderMap()' data-icon='gear' id='LocatingButton' class='class_right_bt' ><img src='"+g_locatingPath+"' id='img_LocatingButton' /><span class='tip' id='locating_bt_tip'>"+g_str_locating+"</span></a></div>");
			
			//note current page
			g_page=$('#mainpage');

			//change layout
			$('#username').css('width','80%');
			$('#password').css('width','80%');
			$('#img_footer_ant').attr('src', g_antMarker);
			$('#CheckinButton').replaceWith("");

			//add tips
			$('li[class*=class_top_bt]').each(function() {
				var tip = $(this).find('.tip');
				var x = $(this).width() + 85,
					y = $(this).height() - 30;
				tip.css({
					left: x,
					top: y,
					'margin-bottom':-50,
					'width': '150px',
					'height': '50px',
					'float':'left',
					'position':'relative',
					'background-image': url+g_tip_img+")",
					visibility:"visible",
					"padding-left":"20px",
					display:"inline"
				});
			});
			$('a[class*=class_top_bt]').each(function() {
				var tip = $(this).find('.notip');
				tip.css({
					'margin-left':'-54px',
					'position':'relative'
				});
			});
			$('#selected-emotion-tip').html(g_str_pleased);
			$('#selected-emotion-tip').css({
				'padding-left':'32px',
				'position':'relative'
			});

			$("#camera_btn").click(function(e) {
			   if(window.antrip){
				   window.antrip.startCamera();
				}
				event.stopImmediatePropagation();
			});
			$("#check-in_yes").click(function(e) {
			   CheckIn();
			});
			$("#check-in_cancel").click(function(e) {
			   if(window.antrip){
				   window.antrip.cancelCheckin();
				}
			});
			$("#shareFriend_yes").click(function(e) {
				var trip_id = $.urlParam('trip_id');
			   shareTripToFriends(trip_id);
			});

			// set multiple language string
			var backlogo_img = im+g_str_bkpath;
			$('#login').find('div:jqmData(role="header")').find('h4').replaceWith( "<"+g_titleSize+" style='text-align:center'>" + g_str_antrip + "</"+g_titleSize+">" );
			$('#login').find('div:jqmData(role="content")').addClass('fixedBK');
			$('#login').find('div:jqmData(role="content")').addClass('center_content');
			$('#login_bk').css("background-image", url+backlogo_img+")");
			$('body').find('div:jqmData(role="page")').css("background-image", url+back_img+")");

			$('#emotion-sel').css("background", url+selemotion_img+") no-repeat");
			$('#emotion-sel_option').css("background", url+im+'typenotearea.png'+") no-repeat");
			$('#checkin').find('div:jqmData(role="header")').find('h1').html(g_str_checkin);
			$('#check-in_yes').html("<span class=\"ui-btn-text\"></span>");
			$("#check-in_yes .ui-btn-text").text(g_str_checkin);
			$('#check-in_cancel').html("<span class=\"ui-btn-text\"></span>");
			$("#check-in_cancel .ui-btn-text").text(g_str_cancel);
			$('#register_yes').html("<span class=\"ui-btn-text\"></span>");
			$("#register_yes .ui-btn-text").text(g_str_reg);
			$('#register_cancel').html("<span class=\"ui-btn-text\"></span>");
			$("#register_cancel .ui-btn-text").text(g_str_cancel);
			$('#shareFriend_yes').html("<span class=\"ui-btn-text\"></span>");
			$("#shareFriend_yes .ui-btn-text").text(g_str_share);
			$('#shareFriend_cancel').html("<span class=\"ui-btn-text\"></span>");
			$("#shareFriend_cancel .ui-btn-text").text(g_str_cancel);
			$('#camera_btn').html("<span class=\"ui-btn-text\"></span>");
			$('#camera_btn .ui-btn-text').html(g_str_takepicture);
			$('#checkin_feeling').html(g_str_checkin_feeling);
			$('#message').attr("placeholder", g_str_placemarkertext);
			$('a[id*=page_back]').each(function() {
				 $(this).html(g_str_back);
			});

			//init multi-lang for emotion type
			$('#pleased_tip').html(g_str_pleased);
			$('#happy_tip').html(g_str_happy);
			$('#excited_tip').html(g_str_excited);
			$('#angry_tip').html(g_str_angry);
			$('#nervous_tip').html(g_str_nervous);
			$('#calm_tip').html(g_str_calm);
			$('#bored_tip').html(g_str_bored);
			$('#sad_tip').html(g_str_sad);
			$('#sleepy_tip').html(g_str_sleepy);
			$('#peaceful_tip').html(g_str_peaceful);
			$('#relaxed_tip').html(g_str_relaxed);

			setLoginBKLocation();

			$('#username').attr('placeholder',g_username_holder);
			$('#username').addClass('fontclass');
			$('#password').attr('placeholder',g_password_holder);
			$('#username').addClass('fontclass');

			$('#blogin').html("<span class=\"ui-btn-inner ui-btn-corner-all\"><span class=\"ui-btn-text\"></span></span>");
			$('#blogin .ui-btn-text').text(g_str_login);
			$('#fblogin').html("<span class=\"ui-btn-inner ui-btn-corner-all\"><span class=\"ui-btn-text\"></span></span>");
			$('#fblogin .ui-btn-text').text(g_str_fblogin);
			$('#register_btn').html("<span class=\"ui-btn-inner ui-btn-corner-all\"><span class=\"ui-btn-text\"></span></span>");
			$('#register_btn .ui-btn-text').text(g_str_register);
			$('#share_trip').text(g_str_shareTrip);

			initRegisterDialog();

			$('#blogin').css('width','50%');
			$('#blogin').addClass('center_div');
			$('#fblogin').css('width','70%');
			$('#fblogin').addClass('center_div');

			$('#tip_ub_trip_m').html(g_str_recordtrip);
			$('#tip_ub_trip_h').html(g_str_triphistory);
			$('#tip_ub_f').html(g_str_friend);

			sid = $.cookie("sid");
			if(window.antrip){
				sid = window.antrip.getCookie("sid");
			}
			if(sid != null){
				if(g_currentPageID==g_PageIDTable['login']){
					$.mobile.changePage("#mainpage");
				}
			}
			else{
				$('#username').setfocus();
			}
		}

		$('#mainpage').live('pageshow',function(){
			$.mobile.changePage("#map");
		});

		$('#registerpage').live('pageshow',function(){
			initRegisterDialog();
		});

		// friend list 
		$('#friendCheckDialog').live('pagebeforeshow', function(){
			$(this).find('div:jqmData(role="header")').find('h4').replaceWith("<"+g_titleSize+" style='text-align:center'>" + g_str_shareTriptoWhom + "</"+g_titleSize+">");
			var trip_id = $.urlParam('trip_id');
			var div_data = [];
			$("#listview_5").html("");
			$.mobile.showPageLoadingMsg("b", "Loading Friend List ...", true);
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
				data:{sid : sid},
				type: 'GET', dataType: 'jsonp', cache: false,
				success:function(result){
					if(result.friend_list.length==0){
						$.mobile.hidePageLoadingMsg();
						return false;
					}
					$.each(result.friend_list, function(i,data){
						var checkbox_str = "<input type='checkbox' name='"+data.id+"' id='checkbox_"+sid+data.id+"' class='custom' />";
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php',
							data:{userid : sid, trip_id:trip_id, friend_id:data.id},
							type: 'GET', dataType: 'jsonp', cache: false, async: false,
							success:function(result){
								if(result.isShareTrip=="1"){
									checkbox_str = "<input type='checkbox' checked='checked' name='"+data.id+"' id='checkbox_"+sid+data.id+"' class='custom' />";
								}
								div_data[i] ="<li><a class='listview' id='list_"+sid+data.id+"' >"+checkbox_str+"<img src='" + data.image + "'/><h3>"+g_str_id+":" + data.name  + "</h3></a></li>";
								$("#listview_5").append(div_data[i]).listview('refresh');
								$("#list_"+sid+data.id).click(function(e) {
								   event.stopImmediatePropagation();
									$("checkbox_"+sid+data.id).trigger('click');
								});
							}
						});
					});
					$.mobile.hidePageLoadingMsg();
				}
			});
		});

		$('#checkin').live('pagebeforeshow', function(){
			if(window.antrip){
				window.antrip.startCheckin();
			}
		});

		$(document).delegate('div:jqmData(role="page")', 'pagebeforeshow', function(){ 
			if($(this).attr('id')=='map' || $(this).attr('id')=='triplist' || $(this).attr('id')=='friendlist' || $(this).attr('id')=='findfriend' || $(this).attr('id')=='friendrequest'){
				var titleTable = {	
					"mainpage": g_str_antrip,
					"map": g_str_recordtrip,
					"triplist": g_str_triphistory,
					"friendlist": g_str_friend,
					"findfriend": g_str_searchFriend,
					"friendrequest": g_str_friendRequest
				};

				//change title name
				if($(this).attr('id')!='findfriend' && $(this).attr('id')!='friendrequest' ){
					$(this).find('div:jqmData(role="header")').html(g_page.find('div:jqmData(role="header")').html());
					$(this).find('div:jqmData(role="header")').find(g_titleSize).html(titleTable[$(this).attr('id')]);
				}
				else{
					$(this).find('div:jqmData(role="header")').find('h4').replaceWith( "<"+g_titleSize+" style='text-align:center'>" + titleTable[$(this).attr('id')] + "</"+g_titleSize+">" );
					if($(this).attr('id')=='findfriend'){
						$("#friendrequest").html(g_str_friendRequest);
					}
					return;
				}

				//change logout button to check-in  
				if($(this).attr('id')=='map'){
					var replaceHtml = "<li data-iconpos='right' class='ui-btn-right'><a href='' data-icon='check' id='CheckinButton' data-rel='dialog' class='class_right_bt'><img src='"+g_checkin_invalid_imgPath+"' id='img_CheckInButton'  style='display:none'/><span class='tip' id='checkin_bt_tip'>"+g_str_checkin+"</span></a><a href='javascript:startRecordTrip()' data-icon='gear' id='RecordButton' class='class_right_bt' ><img src='"+g_tripRecorderPath+"' id='img_RecordButton' /><span class='tip' id='record_bt_tip'>"+g_str_startrecording+"</span></a>" ;
					
					//disable logout button
					if(window.antrip){
						;
					}
					else{
						replaceHtml += "<a href='javascript:Logout()' data-role='button' data-transition='fade' data-theme='a' data-icon='arrow-r' data-iconpos='right'>Logout</a>";
					}

					replaceHtml += "</li>";
					$(this).find('div:jqmData(role="header")').find('.ui-btn-right').replaceWith(replaceHtml );
					$(this).find('div:jqmData(role="navbar")').hide();
				}
				else if($(this).attr('id')=='triplist'){
					$(this).find('div:jqmData(role="header")').find('.ui-btn-right').replaceWith( "<a href='' class='ui-btn-right class_right_bt'></a>" );
				}
				else if($(this).attr('id')=='friendlist'){
					$(this).find('div:jqmData(role="header")').find('.ui-btn-right').replaceWith( "<a href='#findfriend' data-icon='search' id='findfriend' data-iconpos='right' data-prefetch class='ui-btn-right class_right_bt' data-transition='pop'>"+g_str_addFriend+"</a>" );
				}
				initRightBtTips();
				//add page click event
				$("ul:jqmData(role='menu') li > ul li").click(function(e) {
				   $('body').trigger('hideOpenMenus');
				   event.stopImmediatePropagation();
					$(this).find('a').trigger('click');
				});
				$('a[class*=class_top_bt_link]').each(function() {
					$(this).click(function(event){
						event.preventDefault();
						changeToUsedImg($(this).find('img'));
						triggerAnchor($(this).find('img'));
						return false;
					});
				});	
				$('body').trigger('hideOpenMenus');
				// clear previous page
				if(g_page.attr('id')!=$(this).attr('id')){
					g_page.find('div:jqmData(role="header")').html('');
				}
				g_page=$(this);
				g_currentPageID = g_PageIDTable[g_page.attr('id')];
				if(window.antrip){
					window.antrip.setMode(g_currentPageID);
				}
			}
			else{
				if($(this).attr('id')=='tripmap'){
					var trip_name = decodeURIComponent($.urlParam('trip_name'));
					$(this).find('div:jqmData(role="header")').find('h4').replaceWith("<"+g_titleSize+" style='text-align:center'>" + trip_name + "</"+g_titleSize+">");
					$(this).find('div:jqmData(role="header")').find(g_titleSize).html( trip_name);
				}
			}
		});

		 $(document).delegate('div:jqmData(role="page")', 'pageinit', function(){ 
			 if(g_IsInited==false){
				//note current page
				g_IsInited = true;
				g_page=$(this);
				g_currentPageID = g_PageIDTable[g_page.attr('id')];
				initLanguage();
			 }
			 var topbt_bk_img = im + "topbt_bk.png";
			$(this).find('div:jqmData(role="header")').attr('data-position','fixed');
			$(this).find('div:jqmData(role="header")').css('background-image',  url+topbt_bk_img+")");
			$(this).find('div:jqmData(role="header")').addClass('strokefont');
			$(this).find('div:jqmData(role="header")').addClass('fixedBK');
			$(this).find('div:jqmData(role="header")').css('height', g_titleHeight);
			$(this).find('div:jqmData(role="header")').css('font-size', '20px');
			$(this).find('div:jqmData(role="footer")').css('height', g_footerHeight);
			$(this).find('div:jqmData(role="header")').css('z-index', '999');
			$(this).find('div:jqmData(role="content")').css('z-index', '1');
		});

		$('#map').ready(function(){
			setMapHeight();
		});
		function setMapHeight(){
			var mapHeight = $(window).height() - g_titleHeight - fixedValue/2;
			$('#map_canvas_1').css('height', mapHeight);
		}

		$('#menu-left').ready(function(){
			 initDropdown();
		});

		// init drop down menu
		function initDropdown(){
			$('body').bind('hideOpenMenus', function(){
				$("ul:jqmData(role='menu')").find('li > ul').hide();
			}); 
			var menuHandler = function(e) {
				if($(this).find('li > ul').is(':hidden')){
					$(this).find('li > ul').show();
				}
				else{
					$('body').trigger('hideOpenMenus');
				}
				e.stopPropagation();
			};
			$("ul:jqmData(role='menu') li > ul li").click(function(e) {
			   $('body').trigger('hideOpenMenus');
			   event.stopImmediatePropagation();
			    $(this).find('a').trigger('click');
			});

			$("ul:jqmData(role='menu') li > ul a").click(function(e) {
			   $('body').trigger('hideOpenMenus');
			   event.stopImmediatePropagation();
			   $('#selected-emotion').attr('src', $(this).find('img').attr('src'));
			   $('#selected-emotion').attr('alt', $(this).find('img').attr('alt'));
			   $('#selected-emotion-tip').html($(this).find('span').html());
			});

			$('body').delegate("ul:jqmData(role='menu')",'click',menuHandler);
			$('body').click(function(e){
			   $('body').trigger('hideOpenMenus');
			});
			setTopBtnToDefaultImg();

			var changeImgID = g_dropdownImgTable[g_page.attr('id')];
			if(changeImgID!=null){
				changeToUsedImg($(changeImgID));
			}
			else{
				changeToUsedImg($(g_dropdownImgTable['map']));
			}
		}

		function triggerAnchor(obj){
			var anchor="";
			if(obj.attr('id')=="tb_trip_recorder"){
				anchor="#map";
			}
			else if(obj.attr('id')=="tb_friend"){
				anchor="#friendlist";
			}
			else if(obj.attr('id')=="tb_trip_history"){
				anchor="#triplist";
			}
			location.hash =  anchor;
			return false;
		}

		function setTopBtnToDefaultImg(){
			$('img[class*=class_top_bt]').each(function() {
				$(this).attr("src", im+$(this).attr('id')+".png");
			});
		}
		function changeToUsedImg(obj){
			setTopBtnToDefaultImg();
			obj.attr("src", im+obj.attr('id')+"_u.png");
			$('#dropdown_toggle').attr('src',obj.attr('src'));
		}
		
		// set content background position when window resizing
		$(window).resize(function() { 
			setMapHeight();
			setLoginBKLocation();
		});


		function setLoginBKLocation(){
			var loginHeight = $(window).height() - g_titleHeight - g_footerHeight - fixedValue;
			$('#login').find('div:jqmData(role="content")').css('height', loginHeight);
		}

		/* Tooltip function*/
		var g_tip;
		var g_tip_intval = null;
		var g_tip_img = im+"tipline.png";
		function slowHideTip(){
				g_tip.fadeOut('slow', function() {
					g_tip.hide();
				});
		}
		function initRightBtTips() {
			$('a[class*=class_right_bt]').each(function() {
				var tip = $(this).find('.tip');
				$(this).hover(
				function() {
					tip.appendTo('body');
				}, function() {
					tip.appendTo(this);
				}).mouseover(function(e) {
					var x = e.pageX - 60,
						y = e.pageY + 20,
						w = tip.width(),
						h = tip.height(),
						dx = $(window).width() - (x + w),
						dy = $(window).height() - (y + h);
					tip.css({
						left: x,
						top: y,
						'background-image': url+g_tip_img+")",
						visibility:"visible",
						"padding-left":"20px",
						display:"block"
					});
					g_tip = tip;
					g_tip_intval = setTimeout("slowHideTip()",3000);
				}).mouseleave(function(e) {
					if(g_tip_intval != null){
						window.clearInterval(g_tip_intval);
						g_tip_intval = null;
					}
					tip.fadeOut('slow', function() {
						tip.hide();
					});
				});
			});
		}

		function initScrollToEndEvent(){
				//Attach function to the scroll event of the div
				var obj = $(window);
				obj.scroll(function () {
					var scrolltop = obj.scrollTop();
					var scrollheight = obj[0].document.body.scrollHeight;
					var windowheight = obj[0].document.body.clientHeight;
					var scrolloffset = 10;
					if (scrolltop >= (scrollheight - (windowheight + scrolloffset))) {
						//var callframe = window.frames['triipListFrame'];
						g_tripPage++;
						showTripList(g_tripPage);
					}
				});
		}

		function initRegisterDialog(){
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
			$('#registerpage').find('div:jqmData(role="header")').find('h1').html(g_str_register);
			$('#reg_username').attr('placeholder',g_str_id);
			$('#reg_email').attr('placeholder',g_str_email);
			$('#reg_username').attr('value', "");
			$('#reg_email').attr('value', "");
			$('#email_forgotten').html(g_str_email_forgotten);
			$('#reg_password').attr('placeholder',g_str_password);
			$('#reg_re_password').attr('placeholder',g_str_re_password);
			$('#reg_password').attr('value', "");
			$('#reg_re_password').attr('value', "");
			$('#email_forgotten').html(g_str_email_forgotten);
		}
//-->