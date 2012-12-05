	var emotionMapping = {	
		"1": "excited",
		"2": "happy",
		"3": "pleased",
		"4": "relaxed",
		"5": "peaceful",
		"6": "sleepy",
		"7": "sad",
		"8": "bored",
		"9": "nervous",
		"10": "angry",
		"11": "calm"
	};
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
	var g_zoom=20;
	var g_startLatLng = new google.maps.LatLng(25.04752,121.614189);
	var latlng_undefined_value=-999;
	var g_tripPointArray = null;
	var g_tripArray = null;
	var g_tripMarkerArray = null;
	var g_tripPointMarkerArray = null;
	var g_tripTimeStampArray = null;
	var g_openMarker = true;
	var g_picture_uri = null;
	function GetTripPointfromID(hashCode, _unit){
		$('#map_canvas').css('visibility', 'hidden');
		$('#map_canvas').gmap({ 'zoom':g_zoom, 'callback': function(map) {
			var self = this;
			var bounds=new google.maps.LatLngBounds();
			g_tripArray = new Array(0);
			g_tripPointArray = new Array(0);
			g_tripMarkerArray = new Array(0);
			g_tripTimeStampArray = new Array(0);
			g_tripPointMarkerArray = new Array(0);
			self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php',
			data:{hash: hashCode},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				var firstlatlng=null;
				var firstpoint=null;
				var lastlatlng=null;
				var lastpoint=null;
				if(result.CheckInDataList){
					$.each(result.CheckInDataList, function(i, point) {
						var lat = point.lat.valueOf() / 1000000;
						point.lat = lat;
						var lng = point.lng.valueOf() / 1000000;
						point.lng = lng;
						var latlng = new google.maps.LatLng(lat, lng);
						if(lat != latlng_undefined_value && lng !=latlng_undefined_value){
							if(firstlatlng==null){
								firstlatlng = latlng; 
							}
							lastlatlng = latlng;
							g_tripArray.push(point);
							g_tripPointArray.push(latlng);
							g_tripTimeStampArray.push(point.timestamp);
							bounds.extend(latlng);
							if (typeof point.CheckIn != 'undefined'){
								var CheckInInfo="<div style='display:block;overflow:hidden;'>";
								var title="";
								var markicon = null;
								CheckInInfo +="<p>";
								if(point.CheckIn.picture_uri!=null && typeof point.CheckIn.picture_uri!='undefined'){
									title = 'http://plash2.iis.sinica.edu.tw/picture/'+result.userid+"/"+result.trip_id+"/thumb/"+point.CheckIn.picture_uri;
									var meta = document.createElement('meta');
									meta.setAttribute("property", "og:image");
									meta.setAttribute("content", title);
									(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(meta);
									g_picture_uri = point.CheckIn.picture_uri;
									CheckInInfo +="<a href='"+title+"' target='_blank' style='float:left; margin:0 5px 0 0;' ><img style='display:block;' src=" + title + " height='200' /></a>";
									markicon = "images/placemarker.png";
								}
								if(point.CheckIn.emotion!=null && typeof point.CheckIn.emotion!='undefined'){
									CheckInInfo +="<img style='display:block;' width='72px' src='"+im+"s/"+emotionMapping[point.CheckIn.emotion]+".png' />"+g_Tooltip[emotionMapping[point.CheckIn.emotion]]+"<br/>";

									if(markicon==null){
										markicon = "images/placemarker_emotion.png";
									}
								}
								if(point.CheckIn.message!=null && typeof point.CheckIn.message!='undefined'){
									CheckInInfo += ""+ point.CheckIn.message +"";
									if(markicon==null){
										markicon = "images/placemarker_text.png";
									}
								}
								CheckInInfo +="</p>";
								CheckInInfo +="</div>";
								if(markicon==null){
									markicon = "images/placemarker.png";
								}
								var placemarker = new google.maps.Marker({
									'title': title,
									'position': latlng, 
									'bounds': true,
									'icon': markicon
								});

								var infowindow = new google.maps.InfoWindow(
								{ 
										content: CheckInInfo
								});
								placemarker.openInfoWindow = function(IsOpen) {
									if(IsOpen!=null){
										if(IsOpen==false){
											infowindow.close();
										}
										else{
											infowindow.open(map,placemarker);
										}
									}
									else{
										if (infowindow.getMap()==null){
											infowindow.open(map,placemarker);
										}
										else{
											infowindow.close();
										}
									}
								}
								google.maps.event.addListener(placemarker, 'click', function() {
									//OpenAllMarkerInfo(false);
									placemarker.openInfoWindow();
								});
								google.maps.event.addListener(placemarker, 'mouseout', function() {
									;
								});
								/*google.maps.event.addListener(placemarker, 'click', function() {
									window.open(title,"_blank ");
								});*/
								placemarker.getTitle = function() {
									return title;
								}
								placemarker.getPosition = function() {
									return latlng;
								}
								placemarker.setMap(map);
								g_tripMarkerArray.push(placemarker);
							} else {
								if(i==0||firstpoint==null)
								{
									self.addMarker({ 
										'position': latlng, 
										'bounds': true,
										'icon':"images/placemarker_cut_start.png"
									}).click(function(){
										self.openInfoWindow({'content': point.timestamp+"<br/>"+latlng+"<br/>"+$.cookie('g_trip_st_address')}, this);
									});
									firstpoint = latlng;
								}
								else if(i==(result.CheckInDataList.length-1))
								{
									self.addMarker({ 
										'position': latlng, 
										'bounds': true,
										'icon':"images/placemarker_cut_stop.png"
									}).click(function(){
										self.openInfoWindow({'content': point.timestamp+"<br/>"+latlng+"<br/>"+$.cookie('g_trip_et_address')}, this);
									});
								}
								else{
									var pointmarker = new google.maps.Marker({
										'id':'client',
										'position': latlng, 
										'bounds': true
									});
									var infowindow = new google.maps.InfoWindow(
										  { 
											content: point.timestamp+"<br/>"+latlng
									});
									pointmarker.openInfoWindow = function(IsOpen) {
										if(IsOpen!=null){
											if(IsOpen==false){
												infowindow.close();
											}
											else{
												infowindow.open(map,pointmarker);
											}
										}
										else{
											if (infowindow.getMap()==null){
												infowindow.open(map,pointmarker);
											}
											else{
												infowindow.close();
											}
										}
									}
									google.maps.event.addListener(pointmarker, 'click', function() {
											OpenAllPointInfo(false);
											pointmarker.openInfoWindow(true);
									});
									pointmarker.setMap(map);
									g_tripPointMarkerArray.push(pointmarker);
								}
							}
						}
					});
				}
				g_mapPath = new google.maps.Polyline({
					'strokeColor': "#FF0000", 
					'strokeOpacity': 0.8, 
					'strokeWeight': 10, 
					'path': g_tripPointArray
				});

				if(bounds!=null){
					map.fitBounds(bounds);
				}
				g_mapPath.setMap(map);
				self.set('MarkerClusterer', new MarkerClusterer(map, g_tripPointMarkerArray));
				$('#map_canvas').gmap('refresh');
				$('#map_canvas').css('visibility', 'visible');

				showNote(result.userid, result.trip_id);

				if(top.frames['triipStatisticsFrame']){
					top.frames['triipStatisticsFrame'].location="frame-2.html";
				}
				else{
					if(typeof UpdateCalculation== 'function'){
						if(_unit != null &&  _unit != 'undefined'){
							UpdateCalculation(_unit);
						}
						else{
							UpdateCalculation();
						}
					}
				}

				if(top.frames['triipListFrame']){
					top.frames['triipListFrame'].g_tripMarkerArray = g_tripMarkerArray;
					top.frames['triipListFrame'].setUpdateAlbum();
				}
				else{				
					if(typeof UpdateAlbum== 'function'){
						UpdateAlbum();
					}
				}
			}});				
		}});
	}

	function OpenAllMarkerInfo(IsOpen){
		if(g_tripMarkerArray.length<=0){
			alert(g_str_nocheckin);
			return;
		}
		for(var i=0; i<g_tripMarkerArray.length;i++){
			if(g_tripMarkerArray[i]){
				g_tripMarkerArray[i].openInfoWindow(IsOpen);
			}
		}
	}

	function OpenAllPointInfo(IsOpen){
		if(g_tripPointMarkerArray.length<=0){
			//alert(g_str_nocheckin);
			return;
		}
		for(var i=0; i<g_tripPointMarkerArray.length;i++){
			if(g_tripPointMarkerArray[i]){
				g_tripPointMarkerArray[i].openInfoWindow(IsOpen);
			}
		}
	}