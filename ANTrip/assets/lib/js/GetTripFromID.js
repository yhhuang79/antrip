	var g_tripPointArray = null;
	var g_tripMarkerArray = null;
	function GetTripPointfromID(userid, trip_id){
	//	$('#ub_download').css("display","none");
	//	$('#ub_download').css("z-index", "9999");
		$("#overlay").css("display","block");
		$("#overlay").html(g_str_loading);
	//	$('#ub_download').css("display","block");
		$('#map_canvas').gmap('destroy');
		//$('#map_canvas').gmap('clear', 'markers');
		//$('#map_canvas').gmap('clear', 'Polyline');
		$('#map_canvas').gmap({ 'zoom':g_zoom,'center':g_startLatLng, 'callback': function(map) {
			var self = this;
			g_tripPointArray = new Array(0);
			g_tripMarkerArray = new Array(0);
			self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetCheckInData.php',
			data:{userid: sid, trip_id: trip_id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				//alert(g_showtripmap);
				if(g_showtripmap==false){
					return;
				}
				else{
					g_showtripmap = false;
				}
				//alert("addMarker1");
				$.each(result.CheckInDataList, function(i, point) {
					var lat = point.lat.valueOf() / 1000000;
					var lng = point.lng.valueOf() / 1000000;
					var latlng = new google.maps.LatLng(lat, lng);
					g_tripPointArray.push(latlng);
					//throw "lat:"+lat+", lng:"+lng;
					if (typeof point.CheckIn != 'undefined'){
						var placemarker = self.addMarker({ 
							'position': latlng, 
							'bounds': true,
							'icon': im+"placemarker.png",
						}).click(function(){
							var CheckInInfo="";
							if(point.CheckIn.picture_uri!=null || typeof point.CheckIn.picture_uri!='undefined'){
								CheckInInfo +="<p><img src='"+point.CheckIn.picture_uri + "' height='300' /></p>";
							}
							if(point.CheckIn.emotion!=null || typeof point.CheckIn.emotion!='undefined'){
								CheckInInfo +="<p><img width='72px' src='"+im+emotionMapping[point.CheckIn.emotion]+".png'>"+g_Tooltip[emotionMapping[point.CheckIn.emotion]]+"</img></p>";
							}
							if(point.CheckIn.message!=null || typeof point.CheckIn.message!='undefined'){
								CheckInInfo += "<p>"+ point.CheckIn.message +"</p>";
							}
							CheckInInfo +="<p>"+lat+", "+lng+"</p>";
							self.openInfoWindow({'content': CheckInInfo}, this);
							/*if(g_infowindow==null){
								g_infowindow = new google.maps.InfoWindow({
									content: CheckInInfo
								});
								g_infowindow.open(self, this);
							}
							else{
								g_infowindow.close();
								g_infowindow=null;
							}*/
						});
						g_tripMarkerArray.push(placemarker);
					} else {
						self.addMarker({ 
							'position': latlng, 
							'bounds': true
						}).click(function(){
							self.openInfoWindow({'content': point.timestamp}, this);
							/*if(g_infowindow==null){
								g_infowindow = new google.maps.InfoWindow({
									content: point.timestamp
								});
								g_infowindow.open(self, this);
							}
							else{
								g_infowindow.close();
								g_infowindow=null;
							}*/
						});
					}
				});
				self.addShape('Polyline',{
					'strokeColor': "#FF0000", 
					'strokeOpacity': 0.8, 
					'strokeWeight': 4, 
					'path': g_tripPointArray
				});
				self.set('MarkerClusterer', new MarkerClusterer(map, self.get('markers')));
				$('#map_canvas').gmap('refresh');
				$("#overlay").css("display","none");
				$("#overlay").html("");
			}
			});				
		}});

		
	}
