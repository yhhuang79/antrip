<!--
	// trip list
	var g_tripPage=1;
	var g_tripname = null;
	var g_numsofpage=5;// get trip amount each time
	var g_tripnum=0;
	var g_tripIndex=0;
	var g_tripLength = 0;
	var g_firstload = true;

	$('#triplist').live('pageinit', function(){
		reloadTripList();
	});

	function showLocalTripList(){
		var localresult=-1;
		var div_data = [];
		if(window.antrip){
			localresult = eval("("+window.antrip.getLocalTripList()+")");
			if(localresult!=-1 && localresult.tripInfoList.length>0)
			{
				g_tripLength += localresult.tripInfoList.length;
				$.each(localresult.tripInfoList, function(i,data){
					var tripurl = "#tripmap?userid="+ sid +"&trip_id="+ data.id+"&trip_name="+ encodeURIComponent(data.trip_name) +"&local=true";
					var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:S|size:mid|"+ data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 +"";
					div_data[i] ="<li><div class='des_close_class_"+sid+"_"+data.id+"' ></div><a class='listview' href='" + tripurl  + "'  rel='external'><div style='width:100px;margin:10px 10px 10px 0;text-align:center; float:left;border:2px solid #555;'><img onClick=\"if(window.antrip){window.antrip.uploadTrip("+data.id+"); $('#des_close_class_"+sid+"_"+data.id+"').hide('slow');return false;}\" src='" + uploadicon_img + "'  /></div><div class='listview_description' ><div id='div_deletemarker' onClick=\"confirmDelete("+sid+","+data.id+", true);return false;\"><img src='"+im+"deletemarker.png' style='text-align:right'></img></div><h3>" + data.trip_name  + "</h3><p>"+g_str_start+": " + data.trip_st + "</p><p>"+g_str_end+": " + data.trip_et    + "</p><p>" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "</p><p>"+g_str_Length+": " + data.trip_length  + " M</p></div></a><li>"		

				});
				$("#listview_3").append(div_data.join('')).listview('refresh');
			}
		}
	}
	function showTripList(page){
		$.mobile.showPageLoadingMsg("b", "Loading Trip List ...");
		var div_data = [];

		if(page==null || page=='undefined'){
			page=1;
		}

		var order = "trip_id";
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetTripInfoComponent.php',
			data:{userid: sid, sort: order, FirstResult: (page-1)*g_numsofpage, MaxResult: g_numsofpage},
			type: 'GET', dataType: 'jsonp', cache: false, async: !g_firstload,
			success:function(result){
				g_tripnum = result.tripInfoNum;
				g_tripList_result = result;
				g_tripIndex = (page-1)*g_numsofpage;
				g_firstload = false;

				if((result == null || result.tripInfoList == null || result.tripInfoList.length==0) && (localresult==-1 || localresult.tripInfoList == null) ){
					var str_noTrip = g_str_notrip;
					div_data[i] ="<div class='class_friend_bt' style='text-align:center;vertical-align:middle;'><b>"+str_noTrip+"</b></div>";
					$("#listview_3").append(div_data.join('')).listview('refresh');
					return;
				}
				else{
					if(result != null && result.tripInfoList != null && result.tripInfoList.length>0){
						g_trip = result.tripInfoList[0].trip_id;
						g_tripname = result.tripInfoList[0].trip_name.toString();
						g_tripLength = result.tripInfoList.length;
						$("div[id*=tripsum]").html(g_str_numberoftrip+g_tripnum);
						
						var width="100%";
						$.each(result.tripInfoList, function(i,data){
							var tripurl = "#tripmap?userid="+ sid +"&trip_id="+ data.trip_id+"&trip_name="+ encodeURIComponent(data.trip_name) +"&local=false";;
							var mapurl = "http://maps.google.com/maps/api/staticmap?center="+ data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 +"&zoom=12&size=100x100&sensor=false&markers=color:red|label:S|size:mid|"+ data.st_addr_prt2 + " " + data.st_addr_prt3 + " " + data.st_addr_prt4 +"";
							div_data[i] ="<li><div class='des_close_class_"+sid+"_"+data.trip_id+"' ></div><a class='listview'  href='" + tripurl  + "' rel='external'><div style='width:100px;margin:10px 10px 10px 0;text-align:center; float:left;border:2px solid #555;'><img src='" + mapurl + "' /></div><div class='listview_description' ><div id='div_deletemarker' onClick=\"confirmDelete("+sid+","+data.id+", false);event.stopImmediatePropagation();return false;\"><img src='"+im+"deletemarker.png' style='text-align:right'></img></div><h3>" + data.trip_name  + "</h3><p>"+g_str_start+": " + data.trip_st + "</p><p>"+g_str_end+": " + data.trip_et    + "</p><p>" + data.et_addr_prt2 + " " + data.et_addr_prt3 + " " + data.et_addr_prt4 + "</p><p>"+g_str_Length+": " + data.trip_length  + " M</p></div></a><li>"
						});
						$("#listview_3").append(div_data.join('')).listview('refresh');
					}
				}
				$.mobile.hidePageLoadingMsg();
			}
		});
	}

	function showLocalTripData(trip_id){
		if(g_showtripmap==false){
			return;
		}
		var localtripdata;

		if(window.antrip){
			var localresult = window.antrip.getLocalTripData(trip_id);
			if(localresult==-1){
				return alert("error");
			}
			else{
				if(g_showtripmap==false){
					return;
				}
				if(g_mapPath!=null)
				{
					g_mapPath.setMap(null);
					g_mapPath = null;
				}
				localtripdata = eval("("+localresult+")");
				$('#map_canvas_2').gmap({ 'zoom':g_zoom, 'callback': function(map) {
					var self = this;
					g_tripPointArray = new Array(0);
					g_tripMarkerArray = new Array(0);
					var tripPointObjArray = new Array(0);
					var index=0;
					self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
					$.each(localtripdata.CheckInDataList, function(i, point) {
						if(index>localtripdata.CheckInDataList.length){
							var _map =  $('#map_canvas_2').gmap('get','map');
							if(g_mapPath!=null)
							{
								g_mapPath.setMap(null);
								g_mapPath = null;
							}
							g_mapPath = new google.maps.Polyline({
								'strokeColor': "#FF0000", 
								'strokeOpacity': 0.8, 
								'strokeWeight': 4, 
								'path': g_tripPointArray
							});
							return;
						}
						else if(point.lat != latlng_undefined_value && point.lng !=latlng_undefined_value){
							var lat = point.lat;
							var lng = point.lng;
							var latlng = new google.maps.LatLng(lat, lng);
							g_tripPointArray.push(latlng);
							if (typeof point.CheckIn != 'undefined'){
								var placemarker = self.addMarker({ 
									'position': latlng, 
									'bounds': true,
									'icon': im+"placemarker.png",
								}).click(function(){
									var CheckInInfo="";
									if(point.CheckIn.picture_uri!=null && typeof point.CheckIn.picture_uri!='undefined'){
										var title = 'http://plash2.iis.sinica.edu.tw/picture/'+sid+"/"+trip_id+"/"+point.CheckIn.picture_uri;
										CheckInInfo +="<p><img src='"+title+ "' height='300' /></p>";
									}
									if(point.CheckIn.emotion!=null && typeof point.CheckIn.emotion!='undefined'){
										CheckInInfo +="<p><img width='72px' src='"+im+emotionMapping[point.CheckIn.emotion.toString()]+".png'>"+g_Tooltip[emotionMapping[point.CheckIn.emotion.toString()]]+"</img></p>";
									}
									if(point.CheckIn.message!=null && typeof point.CheckIn.message!='undefined'){
										CheckInInfo += "<p>"+ point.CheckIn.message +"</p>";
									}

									CheckInInfo +="<p>"+lat+", "+lng+"</p>";
									self.openInfoWindow({'content': CheckInInfo}, this);
								});
								g_tripMarkerArray.push(placemarker);
							} else {
								var marker = self.addMarker({ 
									'position': latlng, 
									'bounds': true
								}).click(function(){
									self.openInfoWindow({'content': point.timestamp}, this);
								});
								tripPointObjArray.push(marker);
							}
						}
						index++;
					});
					if(g_showtripmap==false){
						return;
					}
					else{
						g_showtripmap = false;
					}
					var _map =  $('#map_canvas_2').gmap('get','map');
					if(g_mapPath!=null)
					{
						g_mapPath.setMap(null);
						g_mapPath = null;
					}
					g_mapPath = new google.maps.Polyline({
						'strokeColor': "#FF0000", 
						'strokeOpacity': 0.8, 
						'strokeWeight': 4, 
						'path': g_tripPointArray
					});
					g_mapPath.setMap(_map);
					self.set('MarkerClusterer', new MarkerClusterer(_map, self.get('markers')));
				}});
			}
		}
	}

	// friend list 
	function showfriendList(){
		var sid = $.cookie("sid");
		if(window.antrip){
			sid = window.antrip.getCookie("sid");
		}
		$("#friend_list").html("");
		var div_data = [];
		$("#overlay").css("display","block");
		$("#overlay").html(g_str_loading);	
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetFriendList.php',
			data:{sid : sid},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				if(result.friend_list.length==0){
					var str_noFriend = g_str_nofriend;
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
				$("#overlay").css("display","none");
				$("#overlay").html("");
			}
		});
		$("#sym_friends").show("slow");
	}

	var g_showtripmap = false;

	function showTripMapfromID(userid, trip_id){
		g_showtripmap = true;
		$('#map_canvas').show("slow");
		$('#map_canvas_2').hide();
		$("#tripname").html(g_tripname.toString());
		GetTripPointfromID(userid, trip_id);
		g_trip=trip_id;
	}


	function confirmDelete(sid, id, bIsLocal){
		var r=confirm(g_str_delete);
		if (r==true){
			$("#des_close_class_"+sid+"_"+id).fadeOut('slow', function() {
				$("#des_close_class_"+sid+"_"+id).hide();
			});
			if(bIsLocal==true && window.antrip){
				window.antrip.deleteTrip(id);
			}
			else{
				deleteTrip(id);
			}
		}
		else{
		}
	}
	function deleteTrip(id){
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/DelTripComponent.php',
			data:{userid : sid, trip_id: id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				
			},
			error: function(xhr) {
				alert("Error!");
			}
		});
	}

	var g_tripPointArray = null;
	var g_tripMarkerArray = null;
	function GetTripPointfromID(userid, trip_id){
		$('#map_canvas_2').gmap({ 'zoom':g_zoom, 'callback': function(map) {
			var self = this;
			var bounds=new google.maps.LatLngBounds();
			g_tripPointArray = new Array(0);
			g_tripMarkerArray = new Array(0);
			tripPointMarkerArray = new Array(0);
			self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php',
			data:{userid: sid, trip_id: trip_id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				var firstlatlng=null;
				var lastlatlng=null;
				$.each(result.CheckInDataList, function(i, point) {
					var lat = point.lat.valueOf() / 1000000;
					var lng = point.lng.valueOf() / 1000000;
					var latlng = new google.maps.LatLng(lat, lng);
					if(lat != latlng_undefined_value && lng !=latlng_undefined_value){
						if(firstlatlng==null){
							firstlatlng = latlng; 
						}
						g_tripPointArray.push(latlng);
						lastlatlng = latlng;
						bounds.extend(latlng);
						if (typeof point.CheckIn != 'undefined'){
							var placemarker = self.addMarker({ 
								'position': latlng, 
								'bounds': true,
								'icon': im+"placemarker.png",
							}).click(function(){
								var CheckInInfo="";
								var title="";
								if(point.CheckIn.picture_uri!=null && typeof point.CheckIn.picture_uri!='undefined'){
									title = 'http://plash2.iis.sinica.edu.tw/picture/'+sid+"/"+trip_id+"/"+point.CheckIn.picture_uri;
									CheckInInfo +="<p><img src=" + title + " height='300' /></p>";
								}
								if(point.CheckIn.emotion!=null && typeof point.CheckIn.emotion!='undefined'){
									CheckInInfo +="<p><img width='72px' src='"+im+emotionMapping[point.CheckIn.emotion]+".png'>"+g_Tooltip[emotionMapping[point.CheckIn.emotion]]+"</img></p>";
								}
								if(point.CheckIn.message!=null && typeof point.CheckIn.message!='undefined'){
									CheckInInfo += "<p>"+ point.CheckIn.message +"</p>";
								}
								CheckInInfo +="<p>"+lat+", "+lng+"</p>";
								self.openInfoWindow({'content': CheckInInfo}, this);
							});
							g_tripMarkerArray.push(placemarker);
						} else{
							var pointmarker = new google.maps.Marker({
								'id':'client',
								'position': latlng, 
								'bounds': true,
							});
							var infowindow = new google.maps.InfoWindow(
								  { 
									content: point.timestamp+"<br/>"+latlng
							});
							google.maps.event.addListener(pointmarker, 'click', function() {
									if (infowindow.getMap()==null){
										infowindow.open(map,pointmarker);
									}
									else{
										infowindow.close();
									}
							});
							pointmarker.setMap(map);
							tripPointMarkerArray.push(pointmarker);
						}
					}
				});
				self.addShape('Polyline',{
					'strokeColor': "#FF0000", 
					'strokeOpacity': 0.8, 
					'strokeWeight': 4, 
					'path': g_tripPointArray
				});
				if(bounds!=null){
					map.fitBounds(bounds);
				}
				self.set('MarkerClusterer', new MarkerClusterer(map, tripPointMarkerArray));
				$('#map_canvas_2').gmap('refresh');
			}
			});				
		}});
	}

//-->