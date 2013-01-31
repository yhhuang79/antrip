<!--
	function showTripMap(userid, trip_id, hashCode){
		$("body").css("cursor","default");
		g_trip=trip_id;
		g_openMarker = true;
		GetTripPointfromID(hashCode);
		if(userid!=$.cookie("sid")){
			$("#tripname").html(g_str_tripname+":<input type='text' id='input_tripName' name='tripName' disabled=true value='"+g_tripname+"' />");
		}
		else{
			$("#tripname").html(g_str_tripname+":<input type='text' id='input_tripName' name='tripName' onblur='changeTripName("+trip_id+", this.value);' onkeyup='changeTripNameByEnter(event, "+trip_id+", this.value);' value='"+g_tripname+"' />");
		}

		$("#trip_st").html(g_str_start+": " +$.cookie('g_trip_st'));
		$("#trip_et").html(g_str_end+": " +$.cookie('g_trip_et'));
		$("#trip_length").html(g_str_length+": "+$.cookie('g_trip_length') + " M");
		//alert($.cookie("g_shareuser"));
		if($.cookie("g_shareuser")!=null && $.cookie("g_shareuser")!='undefined'){
			$("#shareuser").html(g_str_sharefromWho+$.cookie('g_shareuser'));
		}	
	}

	function changeTripNameByEnter(e, trip_id, value){
			var unicode=e.keyCode? e.keyCode : e.charCode;
			if(unicode==13){
				changeTripName(trip_id, value);
			}
	}

	function captureStaticTripMap(){
			$("#overlay").css("display","block");
			html2canvas( [ document.getElementById("map_canvas") ], {
				onrendered: function( canvas ) {
					var img = canvas.toDataURL();
					var meta = document.createElement('meta');
					meta.setAttribute("property", "og:image");
					meta.setAttribute("content", img);
					(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(meta);    
					$("#overlay").css("display","none");
					window.open(img);
					//alert(img);
					var data = img.substr(22); 
					//alert(data);
					$.post('php/upload.php',{ 'data' : data, 'userid' : sid, 'trip_id':g_trip} ); 
				}
			});
	}

	function initRegisterDialog(){
		$("#dialog-register").dialog({
			autoOpen: false,
			bgiframe: false,
			draggable: false,
			resizable: false,
			height:600,
			width: 800,
			modal: true,
			title:g_str_register,
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
				$(this).attr('title',g_str_register);
				$('#reg_username').attr('placeholder',g_str_id);
				$('#reg_email').attr('placeholder',g_str_email);
				$('#email_forgotten').html(g_str_email_forgotten);
				$('#reg_password').attr('placeholder',g_str_password);
				$('#reg_re_password').attr('placeholder',g_str_re_password);
				$('#email_forgotten').html(g_str_email_forgotten);
				$('#sym_video').hide();
			},
			close: function() {
				 $('#sym_video').show();
			},
			buttons: [{
						text: g_str_create_account,
						click: function() {
								  verifyRegister();
							   }
					},
					{
						text: g_str_cancel,
						click: function() {
								  $( this ).dialog( "close" );
								  $('#sym_video').show();
								  return;
							   }
					}
			]
		});
	}

	// trip list
	var g_page=1;
	var g_numsofpage=20;// get trip amount each time
	var g_tripnum=0;
	var g_tripIndex=0;
	var g_tripLength = 0;
	var g_firstload = true;
	function showTripList(page){
		if(page==null){
			page=1;
		}
		var sid = $.cookie("sid");
		var order = $("input:radio[name=radio_triplist]:checked").val();
		if($.cookie("g_triporder")!=null){
			order = $.cookie("g_triporder");
		}
		if(order==null){
			order = "trip_id";
		}
		$("#overlay").css("display","block");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php',
			data:{userid: sid, sort: order, FirstResult: (page-1)*g_numsofpage, MaxResult: g_numsofpage},
			type: 'GET', dataType: 'jsonp', cache: false, async: !g_firstload,
			success:function(result){
				g_tripnum = result.tripInfoNum;
				g_tripList_result = result;
				g_tripIndex = (page-1)*g_numsofpage;
				g_firstload = false;

				if((result == null || result.tripInfoList == null || result.tripInfoList.length==0) ){
					if(g_tripnum==0){
						//alert(g_str_notrip);
						if( $(window.frames['triipListFrame'])){
							$(window.frames['triipListFrame']).unbind("scroll");
						}
						return;
					}
				}
				else{
					showTripListResult(result);
				}
				$("#overlay").css("display","none");
			}
		});
	}

	function trimTimeFormat(time){
				var stringTime = new String(time);
				if(typeof stringTime.lastIndexOf=='function' && stringTime.lastIndexOf(".")>=0){
					stringTime = stringTime.slice(0, stringTime.lastIndexOf("."));
				}
				return stringTime;
	}

	function showTripListResult(result){
			g_trip = result.tripInfoList[0].trip_id;
			g_tripname = result.tripInfoList[0].trip_name.toString();
			g_tripLength = result.tripInfoList.length;
			$("div[id*=tripsum]").html(g_str_numberoftrip+g_tripnum);
			
			var width="100%";
			if( $(window.frames['triipListFrame']) ){
				var fixedValue = 120;
				width = top.window.frames["triipListFrame"].document.body.clientWidth - fixedValue;
			}
			$.each(result.tripInfoList, function(i,data){
				if(data.st_addr_prt2==null || data.st_addr_prt2==""){
					data.st_addr_prt2 = data.st_addr_prt1;
				}

				data.trip_st = trimTimeFormat(data.trip_st);
				data.trip_et = trimTimeFormat(data.trip_et);
				var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:E|size:mid|"+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"";
				var tripname = data.trip_name.toString();
				var appendcontent="<div id='div_"+sid+data.trip_id+"' style='margin-top:-5px;margin-right:0px;width:100%;display:table-row;'><button  id='button"+sid+data.trip_id+"' class='tripItem' onClick=\"g_trip="+data.trip_id+";g_tripname='"+ data.trip_name+"';g_tripIndex="+i+";	$.cookie('g_trip', g_trip);$.cookie('g_tripname', g_tripname);$.cookie('hash', '"+data.hash+"');$.cookie('g_trip_st', '"+data.trip_st+"');$.cookie('g_trip_et', '"+data.trip_et+"');$.cookie('g_trip_length', '"+data.trip_length+"');$.cookie('g_trip_st_address', '" + data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 + "');$.cookie('g_trip_et_address', '" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "');$.cookie('g_shareuser', null);top.frames['tripMapFrame'].location='frame-1.html';\"><div class='product' ><div class='wrapper' style='width:"+width+"px'><div class='listview_image' ><a class='listview' href=''><img src='" + mapurl + "'/></a></div><div class='listview_description' ><a class='listview' href=''>" + data.trip_name  + "<br/>"+g_str_start+": " + data.trip_st + "<br/>"+g_str_end+": " + data.trip_et + "<br/>" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "<br/>"+g_str_length+": " + data.trip_length  + " M</a></div></div></div></button>";
				
				appendcontent += "<div id='div_deletemarker'  onClick=\"confirmDelete('"+data.trip_id+"', false);\"><img src='"+im+"deletemarker.png' align=right></img></div>";

				appendcontent += "</div>";

				$("div[id=products]").eq(0).append(appendcontent);
			});
			$("button","div[id*=products]" ).button();
			$("#products").css("position","static");
	}

	function showPublicTripListResult(result, appendToObj, topOrder){
			g_trip = result.tripInfoList[0].trip_id;
			g_tripname = result.tripInfoList[0].trip_name.toString();
			g_tripLength = result.tripInfoList.length;
			$("div[id*=tripsum]").html(g_str_numberoftrip+g_tripnum);
			
			var width="100%";
			if( $(window.frames['triipListFrame']) ){
				var fixedValue = 100;
				width = top.window.frames["triipListFrame"].document.body.clientWidth - fixedValue;
			}
			$.each(result.tripInfoList, function(i,data){
				if(data.st_addr_prt2==null || data.st_addr_prt2==""){
					data.st_addr_prt2 = data.st_addr_prt1;
				}

				var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:E|size:mid|"+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"";
				var tripname = data.trip_name.toString();
				data.trip_st = trimTimeFormat(data.trip_st);
				data.trip_et = trimTimeFormat(data.trip_et);
				var appendcontent="<div id='div_"+sid+data.trip_id+"' style='margin-top:-5px;margin-right:0px;width:100%;display:table-row;'><button  id='button"+sid+data.trip_id+"' class='tripItem' onClick=\"g_trip="+data.trip_id+";g_tripname='"+ data.trip_name+"';g_tripIndex="+i+";	$.cookie('g_trip', g_trip);$.cookie('g_tripname', g_tripname);$.cookie('hash', '"+data.hash+"');$.cookie('g_trip_st', '"+data.trip_st+"');$.cookie('g_trip_et', '"+data.trip_et+"');$.cookie('g_trip_length', '"+data.trip_length+"');$.cookie('g_trip_st_address', '" + data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 + "');$.cookie('g_trip_et_address', '" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "');$.cookie('g_shareuser', null);top.frames['tripMapFrame'].location='frame-1.html?userid="+data.username+"';\"><div class='product' ><div class='wrapper' style='width:"+width+"px'><div class='listview_image' ><a class='listview' href=''><img src='" + mapurl + "'/></a></div><div class='listview_description' ><a class='listview' href=''>" + data.trip_name  + "<br/>"+g_str_start+": " + data.trip_st + "<br/>"+g_str_end+": " + data.trip_et  + "<br/>" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "<br/>"+g_str_length+": " + data.trip_length  + " M<br/>"+g_str_sharefromWho+data.username+"</a></div></div></div></button>";

				appendcontent += "</div>";

				if(appendToObj!=null && appendToObj!="undefined"){
					appendToObj.append(appendcontent);
				}
				else{
					$("div[id=products]").eq(0).append(appendcontent);
				}
			});
			$("button","div[id*=products]" ).button();
			$("#products").css("position","static");
	}


	function showPublicTripListResultInFirstPage(result, appendToObj, topOrder){		
			$.each(result.tripInfoList, function(i,data){
				if(topOrder!='undefined' && i >= topOrder){
					return;
				}
				if(data.st_addr_prt2==null || data.st_addr_prt2==""){
					data.st_addr_prt2 = data.st_addr_prt1;
				}

				var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:E|size:mid|"+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"";
				var tripname = data.trip_name.toString();

				var strShowDLink = "/showDTripFrame.php?hash="+data.hash+"&g_lang="+g_lang+"&g_calUnit="+$("input:radio[name=radio]:checked").val();
				var s_url = getRootPath()+strShowDLink;
				$.ajax({url:'http://plash.tw/yourls-api.php',
					data:{signature : "da36cdffd2", url:encodeURI(s_url), format:"json", action:"shorturl"},
					type: 'GET', dataType: 'json', cache: false, async: false,
					success:function(result){
						if(result.shorturl!=null && result.shorturl!="undefined" ){
							s_url = result.shorturl;
						}
					},
					error: function(xhr) {
						//alert('Ajax request errors');
					}
				});
				var appendcontent="<div class='listview_image'  onClick=\"g_trip="+data.trip_id+";g_tripname='"+ data.trip_name+"';g_tripIndex="+i+";	$.cookie('g_trip', g_trip);$.cookie('g_tripname', g_tripname);$.cookie('hash', '"+data.hash+"');$.cookie('g_trip_length', '"+data.trip_length+"');$.cookie('g_shareuser', null);window.open('"+s_url+"', '_blank');\"><a class='listview' href=''><img src='" + mapurl + "'/><br/>" + data.trip_name  + "<br/>"+g_str_sharefromWho+data.username+"</a></div>";

				if(appendToObj!=null && appendToObj!="undefined"){
					appendToObj.append(appendcontent);
				}
			});
	}

	function shareTrip(){
		showCheckFriendList(g_trip);
		$("#dialog-friendlist").dialog('open');
	}

	function initFriendListDialog(){
		var sid = $.cookie("sid");
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php',
			data:{userid : sid, trip_id:g_trip},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				//alert(result.TripShareUser.length);
				if(result.TripShareUser.length>0){
					$("#ub_share_map").attr('src',im+'sharemarker_enable.png');
				}
				else{
					$("#ub_share_map").attr('src',im+'sharemarker.png');
				}
			}
		});
		$("#dialog-friendlist").dialog({
			autoOpen: false,
			bgiframe: false,
			draggable: false,
			resizable: false,
			height:500,
			width: 450,
			show: { effect: 'drop', direction: "up" } ,
			modal: true,
			title: g_str_shareTriptoWhom,
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
					$("#dialog-friendlist").dialog('close');
					var friend_list="";
					$("input[id*='checkbox_']").each(function() {
						if($(this).attr("checked")=="checked"){
							if(friend_list!=""){
								friend_list +=",";
							}
							friend_list += $(this).attr("name");
						}
						else{
							/*$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/DelAuthFriend.php',
									data:{userid : sid, trip_id:g_trip, friend_id:$(this).attr("name")},
									type: 'GET', dataType: 'jsonp', cache: false,
									success:function(result){
										//alert(result.delAuthFriend);
									},
									error: function(xhr) {
										//alert('Ajax request errors');
									}
							});*/
						}
					});
					if(friend_list!=""){
						$("#ub_share_map").attr('src',im+'sharemarker_enable.png');
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/SetAuthFriendComponent.php',
							data:{userid : sid, trip_id:g_trip, friend_id:friend_list},
							type: 'GET', dataType: 'jsonp', cache: false,
							success:function(result){
								//alert('success');
							},
							error: function(xhr) {
								//alert('Ajax request errors');
							}
						});
					}
					else{
						$("#ub_share_map").attr('src',im+'sharemarker.png');
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/SetAuthFriendComponent.php',
							data:{userid : sid, trip_id:g_trip, friend_id:""},
							type: 'GET', dataType: 'jsonp', cache: false,
							success:function(result){
								//alert('success');
							},
							error: function(xhr) {
								//alert('Ajax request errors');
							}
						});
					}
				}
			},
			close: function() {

			}
		});
	}
	function showCheckFriendList(trip_id){
		$("div[id=products]").eq(0).html("<br/>");
		var div_data = [];
		var shared_friends = [];
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false, async: false,
			success:function(result){
				if(result.friend_list.length==0){
					//alert(g_str_nofriend);
					return false;
				}
				$.each(result.friend_list, function(i,data){
					if(i==0){
						var checkbox_str = "<input type='checkbox' name='0' id='checkbox_0' class='custom' />";
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php',
							data:{userid : sid, trip_id:trip_id, friend_id:'0'},
							type: 'GET', dataType: 'jsonp', cache: false, async: false,
							success:function(result){
								if(result.isShareTrip=="1"){
									checkbox_str = "<input type='checkbox' name='0' id='checkbox_0' class='custom'  checked='checked' />";
									shared_friends.push('0');
								}
								div_data[0] ="<div id='div_alluser' data-role='fieldcontain' style='WHITE-SPACE:nowrap;margin-top:-5px;margin-right:0px;'><button class='class_friend_bt' style='margin:0 auto;width:80%;'><fieldset data-role='controlgroup'>"+checkbox_str+"<div class='listview_description listview' >"+g_str_shareToAllUser+"</div></fieldset></button></div>";
							}
						});			
					}
					else{
						var checkbox_str = "<input type='checkbox' name='"+data.id+"' id='checkbox_"+sid+data.id+"' class='custom' />";
						$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php',
							data:{userid : sid, trip_id:trip_id, friend_id:data.id},
							type: 'GET', dataType: 'jsonp', cache: false, async: false,
							success:function(result){
								if(result.isShareTrip=="1"){
									checkbox_str = "<input type='checkbox' checked='checked' name='"+data.id+"' id='checkbox_"+sid+data.id+"' class='custom' />";
									shared_friends.push(data.id);
								}
								div_data[i] ="<div id='div_"+sid+data.id+"' data-role='fieldcontain' style='WHITE-SPACE:nowrap;margin-top:-5px;margin-right:0px;'><button class='class_friend_bt' style='margin:0 auto;width:80%;'><fieldset data-role='controlgroup'>"+checkbox_str+"<div class='listview_image' ><img src='" + data.image + "'/></div><div class='listview_description listview' >"+g_str_id+":" + data.name  + "</div></fieldset></button></div>";
							}
						});
					}
				});

				$("div[id=products]").eq(0).append(div_data.join(''));
				$("button","div[id*=products]" ).button();
			}
		});

		return shared_friends;
	}

	function getSharedFriendListByTripID(trip_id){
		var shared_friends = [];
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false, async: false,
			success:function(result){
				if(result.friend_list.length==0){
					return false;
				}
				$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php',
						data:{userid : sid, trip_id:trip_id, friend_id:'0'},
						type: 'GET', dataType: 'jsonp', cache: false, async: false,
						success:function(result){
							if(result.isShareTrip=="1"){
								shared_friends.push(g_str_shareToAllUser);
							}
						}
				});
				$.each(result.friend_list, function(i,data){
					$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripShareUser.php',
						data:{userid : sid, trip_id:trip_id, friend_id:data.id},
						type: 'GET', dataType: 'jsonp', cache: false, async: false,
						success:function(result){
							if(result.isShareTrip=="1"){
								shared_friends.push(data.name);
							}
						}
					});
				});
			}
		});

		return shared_friends;
	}

	function showPublicTripList(topOrder){
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetPublicTripInfo.php',
			type: 'GET', dataType: 'jsonp', cache: false, async: false,
			success:function(result){
				if(result.tripInfoList.length>0){
					if(topOrder!='undefined' && topOrder!=null){
						g_tripnum += topOrder;
						showPublicTripListResultInFirstPage(result, $("div[id=publicTrip]").eq(0), topOrder);
					}
					else{
						g_tripnum += result.tripInfoList.length;
						showPublicTripListResult(result);
					}
				}
				//$("button","div[id*=products]" ).button();
			}
		});
	}

	function showShareTrip(){
		$("div[id=products]").eq(0).html("<br/><br/><br/>");
		$("#overlay").css("display","block");
		g_tripnum = 0;
		showPublicTripList();
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false, async: false,
			success:function(result){
				if(result.friend_list.length==0){
					return false;
				}
				$.each(result.friend_list, function(i,data){
					var _result = showShareTripByFriend(data);
					if(_result.tripList!="" && _result.tripList!='undefined'){
						var trip_list=_result.tripList.toString().split(",");
						g_tripnum += trip_list.length;
						$("div[id*=tripsum]").html(g_str_numberoftrip+g_tripnum);
						showTripListByFriend(data, _result);
					}
				});
				$("button","div[id*=products]" ).button();
			}
		});
	}
	function showShareTripByFriend(user_data, appendToObj){
		var _result = null;
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripAuthData.php',
			data:{userid :user_data.id, friend_id:sid},
			type: 'GET', dataType: 'jsonp', cache: false, async: false,
			success:function(result){
				//alert(result.getAuthTrip);
				//trip_list=result.getAuthTrip.toString().split(",");
				_result = result
				$("#overlay").css("display","none");
				//alert(trip_list.length);
				//tripnum = trip_list;
			},
			error: function(xhr) {
				alert('Ajax request errors');
				$("#overlay").css("display","none");
			}
		});
		//alert(tripnum);
		return _result;
	}
	function showTripListByFriend(user_data, result, appendToObj){
		var trip_list=result.tripList.toString().split(",");
		var share_userid = user_data.id.toString();
		var share_username = user_data.name.toString();
		for(var index=0;index<trip_list.length;index++)
		{
			var trip_data = trip_list[index].toString();
			//alert(share_userid);
			//alert(trip_data);
			if(result.tripList!=""&&result.tripList!=null){
				$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php',
					data:{userid: share_userid, trip_id:trip_data},
					type: 'GET', dataType: 'jsonp', cache: false, async: false,
					success:function(result){
						//alert(result);
						//alert(trip_data);
						if((result == null || result.length==0) ){
							;
						}
						else{
							g_trip = trip_data;
							g_tripname = result.trip_name;
							g_tripLength = result.trip_length;
							//tripnum ++;
							
							var width="100%";
							if( $(window.frames['triipListFrame']) ){
								var fixedValue = 100;
								width = top.window.frames["triipListFrame"].document.body.clientWidth - fixedValue;
							}
							var data = result;
							//var tripurl = "#sym_editpage?userid="+ share_userid +"&trip_id="+ trip_data;
							//alert(tripurl);
							if(data.st_addr_prt2==null || data.st_addr_prt2==""){
								data.st_addr_prt2 = data.st_addr_prt1;
							}
							var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:E|size:mid|"+ data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 +"";
							var tripname = data.trip_name;
							data.trip_st = trimTimeFormat(data.trip_st);
							data.trip_et = trimTimeFormat(data.trip_et);
							var appendcontent="<div id='div_"+share_userid+trip_data+"' style='margin-top:-5px;margin-right:0px;width:100%;display:table-row;'><button  id='button"+share_userid+trip_data+"' class='tripItem' onClick=\"g_trip="+trip_data+";g_tripname='"+ data.trip_name+"';$.cookie('g_trip', g_trip);$.cookie('g_tripname', g_tripname);$.cookie('g_trip_st', '"+data.trip_st+"');$.cookie('hash', '"+data.hash+"');$.cookie('g_trip_et', '"+data.trip_et+"');$.cookie('g_trip_length', '"+data.trip_length+"');$.cookie('g_trip_st_address', '" + data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 + "');$.cookie('g_trip_et_address', '" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "');$.cookie('g_shareuser', '"+share_username+"');top.frames['tripMapFrame'].location='frame-1.html?userid="+share_userid+"';\"><div class='product' ><div class='wrapper' style='width:"+width+"px'><div class='listview_image' ><a class='listview' href=''><img src='" + mapurl + "'/></a></div><div class='listview_description' ><a class='listview' href=''>" + data.trip_name  + "<br/>"+g_str_start+": " + data.trip_st + "<br/>"+g_str_end+": " + data.trip_et  + "<br/>" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "<br/>"+g_str_length+": " + data.trip_length  + " M<br/>"+g_str_sharefromWho+share_username+"</a></div></div></div></button></div>"
							
							if(appendToObj!=null && appendToObj!="undefined"){
								appendToObj.append(appendcontent);
							}
							else{
								$("div[id=products]").eq(0).append(appendcontent);
							}
						}
					}
				});
			}
		}
		$("button","div[id*=products]" ).button();
	}

	var g_des_value = "";
	// for map frame show buttons list
	function showTripEditBtList(userid){
		var username = $.cookie("username");
		$("#username").text(g_str_welcome+username);

		//update buttons icon
		$("#ub_home").attr('src',im+'ub_home.png');
		$("#ub_shared_trip").attr('src',im+'ub_shared_trip.png');
		$("#ub_friend").attr('src',im+'ub_friend.png');
		$("#img_open_trip").attr('src',im+'b_open_trip.png');
		$("#img_seq_trip").attr('src',im+'b_seq_trip.png');
		$("#img_add_note").attr('src',im+'b_add_note.png');
		$("#img_exp_trip").attr('src',im+'b_export_trip.png');
		$("#ub_checkin").attr('src',im+'ub_checkin.png');
		$("#ub_static_map").attr('src',im+'ub_static_map.png');
		if(isPCMacOS()==true){
			$("#ub_play_map").attr('src',im+'ub_play.png');
		}
		else{
			$("#div_playMap").hide();
		}
		$("#ub_dynamic_map").attr('src',im+'ub_dynamic_map.png');
		$("#ub_share_map").attr('src',im+'sharemarker.png');
		$("#ub_delete_map").attr('src',im+'deletemarker.png');
		$("#up-arrow").attr('src',im+'up-arrow.png');

		$('img[class*=class_menu_bt]').each(function() {
			$(this).mouseenter(function() {
				$(this).attr('src',im+$(this).attr('name')+'_r.png');
			});
			$(this).mouseleave(function() {
				$(this).attr('src',im+$(this).attr('name')+'.png');
			});
		});
		$("#div_openCheckIn").click(function(){
			OpenAllMarkerInfo(g_openMarker);
			if(g_openMarker==true){
				g_openMarker = false;
			}
			else{
				g_openMarker = true;
			}
		});
		var sid = userid;
		var hash =  $.cookie("hash");
		$("#div_dynamicMap").click(function(){
			var strShowDLink = "/showDTripFrame.php?hash="+hash+"&g_lang="+g_lang+"&g_calUnit="+$("input:radio[name=radio]:checked").val();
			var s_url = getRootPath()+strShowDLink;
			$.ajax({url:'http://plash.tw/yourls-api.php',
				data:{signature : "da36cdffd2", url:encodeURI(s_url), format:"json", action:"shorturl"},
				type: 'GET', dataType: 'json', cache: false, async: false,
				success:function(result){
					if(result.shorturl!=null && result.shorturl!="undefined" ){
						s_url = result.shorturl;
					}
					window.prompt ("Copy to clipboard: Ctrl+C, Enter", s_url);
					window.prompt ("Embeded code:", "<iframe width='550' height='600' src='"+s_url+"&width=550' frameborder='0' allowfullscreen='false'></iframe>");
				},
				error: function(xhr) {
					alert('Ajax request errors');
				}
			});
		});
		$("#copyStaticMap").click(function(){
			captureStaticTripMap();
		});
		initTips();
		//showNote(sid, g_trip);
	}

	function showTags(userid, tripid){
		if($('#trip_tags').length>0){
			$('#trip_tags').tagsInput({
				'width':"auto", 
				'height':'60px', 
				'defaultText':g_str_input_tags,
				'autocomplete':{selectFirst:true,width:'200px',autoFill:true},
				onAddTag: function(value){
					if(userid == $.cookie("sid")){
						$.ajax({url:'php/addtag.php',
							data:{sid : userid, content: value, trip_id:tripid},
							type: 'GET', dataType: 'json', cache: false,
							success:function(result){
							},
							error: function(xhr) {
								alert('Ajax request errors');
							}
						});
					}
				},
				onRemoveTag: function(value){
					if(userid == $.cookie("sid")){
						$.ajax({url:'php/deleteTag.php',
							data:{sid : userid, content: value, trip_id:tripid},
							type: 'GET', dataType: 'json', cache: false,
							success:function(result){
							},
							error: function(xhr) {
								alert('Ajax request errors');
							}
						});
					}
				}
			});
			$.ajax({url:'php/getTag.php',
				data:{sid : userid, trip_id:tripid},
				type: 'GET', dataType: 'json', cache: false,
				success:function(result){
					$.each(result, function(i,data){
						$('#trip_tags').addTag(data.text);
					});
				},
				error: function(xhr) {
					//alert('Ajax request errors');
				}
			});
		}

		if(userid != $.cookie("sid")){
			 $('.tagsinput input').attr('disabled', 'disabled');
		}
	}

	function showDescription(userid, tripid){
		//init description
		if($( "#des_textarea").length>0){
			$( "#des_textarea").html(g_str_tripdescription);
			
			$.ajax({url:'php/getDescription.php',
				data:{sid : userid, trip_id:tripid},
				type: 'GET', dataType: 'json', cache: false,
				success:function(result){
					if(result==0){
						;
					}
					else{
						$.each(result, function(i,data){
							if(data.text=="" || data.text==null || data.text=="undefined"){
							}
							else{
								$( "#des_textarea").html(data.text);
								//alert($( "#des_textarea").html());
								 g_des_value = data.text;
							}
						});
					}
				},
				error: function(xhr) {
					//alert('Ajax request errors');
				}
			});
			if(userid == $.cookie("sid")){
				$( "#des_textarea").editable(function(value, settings) { 
							if(value=="" || value==null || value=="undefined"){
								$( "#des_textarea").html(g_str_tripdescription);
							}
							else{
								 $( "#des_textarea").html(value);
								 g_des_value = value;
								$.ajax({url:'php/addDescription.php',
									data:{sid : userid, content: value, trip_id:tripid},
									type: 'GET', dataType: 'json', cache: false,
									success:function(result){
									},
									error: function(xhr) {
										alert('Ajax request errors');
									}
								});
							}
						  },{
						'tooltip':g_str_tripdescription, 
						'type':'textarea',
						'select' : true,
						'indicator' : 'Saving...', 
						'onblur':'submit',
						'data': function(value, settings) {
							if(value==g_str_tripdescription){
							  value="";
							}
							else{
								value = g_des_value;
							}
							return value;
						}
					}
				);
				$("#des_display_unit").click(function () {
					event.stopImmediatePropagation();
					$("#des_textarea").trigger('click');
				});
				$("#des_textarea").click(function () {
					return false;
				});
			}
		}
	}

	function showNote(userid, trip){
		//init Note
		sid = userid;
		g_trip = trip;
		$.ajax({url:'php/getNote.php',
			data:{sid : userid, trip_id:trip},
			type: 'GET', dataType: 'json', cache: false,
			success:function(result){
				$.each(result, function(i,data){
					var position = new google.maps.LatLng(data.latitude, data.longitude);
					var editable=null;
					if(userid != $.cookie("sid")){
						editable = false;
					}
					setNoteMarker(data.text, data.uri, position, data.record_id, editable);
				});
			},
			error: function(xhr) {
				alert('Ajax request errors');
			}
		});
	}

	function showTripListInFrame(){
		if(top.frames['triipListFrame']){
			top.frames['triipListFrame'].location="frame-3.html?refreshMap=0";
		}
	}

	function showShareTripInFrame(){
		if(top.frames['triipListFrame']){
			top.frames['triipListFrame'].location="frame-3.html?showsharedTrip=true&refreshMap=0";
		}
	}

	function showFriendListInFrame(){
		if(top.frames['triipListFrame']){
			top.frames['triipListFrame'].location="frame-3.html?showfriend=true";
		}
	}
//-->