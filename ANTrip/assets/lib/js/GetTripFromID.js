	var g_tripPointArray = null;
	var g_tripMarkerArray = null;
	function GetTripPointfromID(userid, trip_id){
		$("#overlay").css("display","block");
		$("#overlay").html(g_str_loading);
		$('#map_canvas').gmap('destroy');
		$('#map_canvas').gmap({ 'zoom':g_zoom,'center':g_startLatLng, 'callback': function(map) {
			var self = this;
			g_tripPointArray = new Array(0);
			g_tripMarkerArray = new Array(0);
			self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php',
			data:{userid: sid, trip_id: trip_id},
			type: 'GET', dataType: 'jsonp', cache: false,
			success:function(result){
				if(g_showtripmap==false){
					return;
				}
				else{
					g_showtripmap = false;
				}
				$.each(result.CheckInDataList, function(i, point) {
					var lat = point.lat.valueOf() / 1000000;
					var lng = point.lng.valueOf() / 1000000;
					var latlng = new google.maps.LatLng(lat, lng);
					if(lat != latlng_undefined_value && lng !=latlng_undefined_value){
						g_tripPointArray.push(latlng);
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
							self.addMarker({ 
								'position': latlng, 
								'bounds': true
							}).click(function(){
								self.openInfoWindow({'content': point.timestamp}, this);
							});
						}
					}
				});
				self.addShape('Polyline',{
					'strokeColor': "#FF0000", 
					'strokeOpacity': 0.8, 
					'strokeWeight': 4, 
					'path': g_tripPointArray
				});
			//	self.set('MarkerClusterer', new MarkerClusterer(map, self.get('markers')));
				$('#map_canvas').gmap('refresh');
				$("#overlay").css("display","none");
				$("#overlay").html("");
			}
			});				
		}});

		
	}
