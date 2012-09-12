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
	var latlng_undefined_value=-999;
	var g_tripPointArray = null;
	var g_tripArray = null;
	var g_tripMarkerArray = null;
	var g_tripTimeStampArray = null;
	function GetTripPointfromID(userid, trip_id){
		$('#map_canvas').gmap('destroy');
		$('#map_canvas').gmap({ 'zoom':g_zoom,'center':g_startLatLng, 'callback': function(map) {
			var self = this;
			g_tripArray = new Array(0);
			g_tripPointArray = new Array(0);
			g_tripMarkerArray = new Array(0);
			g_tripTimeStampArray = new Array(0);
			tripPointMarkerArray = new Array(0);
			self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php',
			data:{userid: userid, trip_id: trip_id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				var firstlatlng=null;
				var lastlatlng=null;
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
						if (typeof point.CheckIn != 'undefined'){
							var CheckInInfo="<div style='display:block;overflow:hidden;'>";
							var title="";
							function mapPiccallback(sfile) { 
								CheckInInfo +="<p><img style='display:block;' src='" + sfile + "' height='200'";
								var exifOrientation =ImageInfo.getField(sfile, "exif").Orientation;
								if(exifOrientation==3){
									CheckInInfo += " style='";
									CheckInInfo += "-moz-transform:rotate(180deg);";
									CheckInInfo += "-webkit-transform:rotate(180deg);";
									CheckInInfo += "' ";
								}
								else if(exifOrientation==1){
									CheckInInfo += " style='";
									CheckInInfo += "-moz-transform:rotate(90deg);";
									CheckInInfo += "-webkit-transform:rotate(90deg);";
									CheckInInfo += "' ";
								}
								else if(exifOrientation==8){
									CheckInInfo += " style='";
									CheckInInfo += "-moz-transform:rotate(-90deg);";
									CheckInInfo += "-webkit-transform:rotate(-90deg);";
									CheckInInfo += "' ";
								}
								CheckInInfo +=" /></p>";
								if(point.CheckIn.emotion!=null && typeof point.CheckIn.emotion!='undefined'){
									CheckInInfo +="<p><img style='display:block;' width='128px' src='"+im+"s/"+emotionMapping[point.CheckIn.emotion]+".png'>"+g_Tooltip[emotionMapping[point.CheckIn.emotion]]+"</img></p>";
								}
								if(point.CheckIn.message!=null && typeof point.CheckIn.message!='undefined'){
									CheckInInfo += "<p>"+ point.CheckIn.message +"</p>";
								}
								CheckInInfo +="<p>"+lat+", "+lng+"</p></div>";
								var placemarker = self.addMarker({ 
									'title': title,
									'position': latlng, 
									'bounds': true,
									'icon': "images/placemarker.png"
								}).click(function(){
									self.openInfoWindow({'content': CheckInInfo}, this);
								});
								placemarker.openInfoWindow = function() {
									self.openInfoWindow({'content': CheckInInfo}, this);
								}
								placemarker.getTitle = function() {
									return title;
								}
								placemarker.getPosition = function() {
									return latlng;
								}
								g_tripMarkerArray.push(placemarker);
							}
							if(point.CheckIn.picture_uri!=null && typeof point.CheckIn.picture_uri!='undefined'){
								title = 'http://plash2.iis.sinica.edu.tw/picture/'+userid+"/"+trip_id+"/"+point.CheckIn.picture_uri;
								ImageInfo.loadInfo(title, mapPiccallback);
								//CheckInInfo +="<p><img style='display:block;' src=" + title + " height='200' /></p>";
							}
						} else {
							if(i==0)
							{
								self.addMarker({ 
									'position': latlng, 
									'bounds': true,
									'icon':"images/placemarker_cut_stop.png"
								}).click(function(){
									self.openInfoWindow({'content': point.timestamp+"<br/>"+latlng}, this);
								});
							}
							else if(i==(result.CheckInDataList.length-1))
							{
								self.addMarker({ 
									'position': latlng, 
									'bounds': true,
									'icon':"images/placemarker_cut_start.png"
								}).click(function(){
									self.openInfoWindow({'content': point.timestamp+"<br/>"+latlng}, this);
								});
							}
							else{
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
					}
				});
				g_mapPath = new google.maps.Polyline({
					'strokeColor': "#FF0000", 
					'strokeOpacity': 0.8, 
					'strokeWeight': 10, 
					'path': g_tripPointArray
				});
				if(firstlatlng!=null&&lastlatlng!=null){
					var bounds  = null;
					if(firstlatlng.lng()>lastlatlng.lng()){
						bounds = new google.maps.LatLngBounds(lastlatlng, firstlatlng);
					}
					else{
						bounds = new google.maps.LatLngBounds(firstlatlng, lastlatlng);
					}
					map.fitBounds(bounds);
				}
				g_mapPath.setMap(map);
				self.set('MarkerClusterer', new MarkerClusterer(map, tripPointMarkerArray));
				$('#map_canvas').gmap('refresh');
				if(typeof UpdateCalculation== 'function'){
					UpdateCalculation();
				}
				if(typeof UpdateAlbum== 'function'){
					UpdateAlbum();
				}
			}});				
		}});
	}
