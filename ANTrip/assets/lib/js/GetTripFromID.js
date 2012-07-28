	var g_tripPointArray = null;
	var g_tripMarkerArray = null;
	function GetTripPointfromID(userid, trip_id){
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
							'icon': "images/placemarker.png"
						}).click(function(){
							var CheckInInfo = "<p>"+ point.CheckIn.message +"</p><img src='"+ point.CheckIn.picture_uri +"' height='120'/>";
							self.openInfoWindow({'content': CheckInInfo}, this);
						});
						g_tripMarkerArray.push(placemarker);
					} else {
						self.addMarker({ 
							'position': latlng, 
							'bounds': true
						}).click(function(){
							self.openInfoWindow({'content': point.timestamp}, this);
						});
					}
				});
				self.addShape('Polyline',{
					'strokeColor': "#FF0000", 
					'strokeOpacity': 0.8, 
					'strokeWeight': 4, 
					'path': g_tripPointArray
				});
				$('#map_canvas').gmap('refresh');
			}});				
		}});

		
	}
