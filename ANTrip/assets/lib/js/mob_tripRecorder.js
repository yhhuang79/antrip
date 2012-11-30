<!--
	var tripRecorder;
	var g_current_latitude;
	var g_current_longitude;
	var g_emotion_html=null;
	var g_tripPointArray_2 = new Array(0);
	var g_tripMarkerArray_2 = new Array(0);
	var g_tripPointObjArray_2 = new Array(0);

	var g_currentmarker = null;
	var g_xmarker = null;
	var latlng_undefined_value=-999;
	var g_bounds = new google.maps.LatLngBounds();

	var g_markerCluster=null;
	
	var g_intval=-1;
	var g_clockintval=-1;
	var g_angle=45;
	var g_scale=1.2;
	var g_rotatetime='2s';
	var g_mapPath = null;

	function stopRotateInterval(object){
		if(g_intval!=-1){
			window.clearInterval(g_intval);
			g_intval = -1;
			object.css('-webkit-transition-duration', g_rotatetime);
			object.css('-webkit-transform','rotate(0deg)');
			g_angle=45;
		}
		if(g_clockintval!=-1){
			window.clearInterval(g_clockintval);
			g_angle = 0;
			clockwiseImg(object, g_angle);
			g_clockintval =-1;
		}
	}
	function scaleRestore(object){
		object.css('-webkit-transition-duration', '1s');
		object.css('-webkit-transform','scale(1)');
		window.clearInterval(g_intval);
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
		g_intval=window.setInterval( function () { scaleAnimation(param, g_scale); } ,  1000 )
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
	function changeIconToRecoding(){
			$('#img_CheckInButton').attr("src", im+"check-in.png");
			$('#img_RecordButton').attr('src', im+'tripRecorder_starting.png');
			$('#RecordButton').find('.tip').html(g_str_nowrecording);
			$('#CheckinButton').attr('href', '#checkin');

			scaleInterval($('#img_RecordButton'));
			stopRotateInterval($('#img_RecordButton'));
			clockwiseInterval($('#img_RecordButton'));
	}

	function changeIconToStopRecord(){
			$('#img_CheckInButton').attr("src", g_checkin_invalid_imgPath);
			$('#img_RecordButton').attr('src', g_tripRecorderPath);
			$('#CheckinButton').attr('href', '');
			$('#RecordButton').find('.tip').html(g_str_startrecording);
	}

	function addPoint(){
		$('#map_canvas_1').gmap('watchPosition', function (position, status) {
			if ( status == 'OK' ) {
					var self = $('#map_canvas_1').gmap('get','map');
					g_current_latitude = position.coords.latitude;
					g_current_longitude = position.coords.longitude;
					var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
					g_tripPointArray_2.push(latlng);
					var marker =  new google.maps.Marker({ 
						'position': latlng, 
						'bounds': true
					});
					var infowindow = new google.maps.InfoWindow(
					{ 
						'content': g_current_latitude+", "+g_current_longitude
					});
					google.maps.event.addListener(marker, 'click', function() {
						if (infowindow.getMap()==null){
							infowindow.open(self,marker);
						}
						else{
							infowindow.close();
						}
					});
					alert("marker");
					marker.setMap(self);
					g_tripPointObjArray_2.push(marker);
					$('#map_canvas_1').gmap('clearWatch');
					$('#map_canvas_1').gmap('refresh');
			}
		});
	}

	function cleanGlobalArray(){
		if(g_markerCluster!=null){
			g_markerCluster.clearMarkers();
			g_markerCluster = null;
		}
		if(g_tripMarkerArray_2!=null){
			for(i=0;i<g_tripMarkerArray_2.length;i++){
				//if(g_tripMarkerArray_2[i]!=null){
					g_tripMarkerArray_2[i].setMap(null);
					g_tripMarkerArray_2[i] = null;
			//	}
			}
		}
		if(g_tripPointObjArray_2!=null){
			for(i=0;i<g_tripPointObjArray_2.length;i++){
			//	if(g_tripPointObjArray_2[i]!=null){
					g_tripPointObjArray_2[i].setMap(null);
					g_tripPointObjArray_2[i] = null;
			//	}
			}
		}
		g_tripPointArray_2 = new Array(0);
		g_tripMarkerArray_2 = new Array(0);
		g_tripPointObjArray_2 = new Array(0);
		if(g_mapPath!=null)
		{
			g_mapPath.setMap(null);
			g_mapPath = null;
		}

		g_emotion_html=null;
		g_bounds = new google.maps.LatLngBounds();
	}

	// map page for pc web version
	function getLocation(){
		if (navigator.geolocation){
			navigator.geolocation.getCurrentPosition(uploadPosition);
		} else {
			alert("Geolocation is not supported by this browser.");
		}
	}
	function uploadPosition(position){
		var self = $('#map_canvas_1').gmap('get','map');
		g_current_latitude = position.coords.latitude;
		g_current_longitude = position.coords.longitude;
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
		g_tripPointArray_2.push(latlng);
		var marker =  new google.maps.Marker({ 
			'position': latlng, 
			'bounds': true
		});
		var infowindow = new google.maps.InfoWindow(
		{ 
			'content': g_current_latitude+", "+g_current_longitude
		});
		google.maps.event.addListener(marker, 'click', function() {
			if (infowindow.getMap()==null){
				infowindow.open(self,marker);
			}
			else{
				infowindow.close();
			}
		});
		marker.setMap(self);
		DrawLine();
		g_tripPointObjArray_2.push(marker);
		g_markerCluster = new MarkerClusterer(self, g_tripPointObjArray_2);
		$('#map_canvas_1').gmap('refresh');
	}

	function startRecordTrip(){
		var isRecording = null;
		isRecording = $.cookie("isRecording");
		if(window.antrip){
			isRecording = window.antrip.isRecording();
		}
		if(isRecording == null){
			var sid = null;
			sid = $.cookie("sid");
			$.cookie("isRecording", true);
			initRecorderMap(true);
			if(window.antrip){
				//isRecording = window.antrip.setCookie("isRecording", "true");
				sid = window.antrip.getCookie("sid");
				window.antrip.setCookie("trip_id", window.antrip.startRecording().toString());
			}
			else{
				$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/GetNewTripId.php',
						data:{userid: sid},							 
						type: 'GET', dataType: 'jsonp', cache: false,
						success:function(result){
							$.cookie("trip_id",result.newTripId);
							tripRecorder = setInterval(function(){getLocation()},5000);
						}
				});
			}
			changeIconToRecoding();
			g_currentmarker.changeContent(g_str_checkinmessage);
		} else {
			if(window.antrip){
					window.antrip.prepareStopRecording();
			}

			/*var y = window.prompt(g_str_typetripname, g_str_untitledtrip);
			if(y==null || y=='undefined' || y==''){
				y = g_str_untitledtrip;
			}*/

			$.cookie("isRecording", null);
			if(window.antrip){
				window.antrip.stopRecording();
//				window.antrip.removeCookie("isRecording");
				window.antrip.removeCookie("trip_id");
			}
			else{
				alert("ANTrip APP Exception! You are not running on APP");
				window.clearInterval(tripRecorder);
				tripRecorder = -1;
			}

			changeIconToStopRecord();
			g_currentmarker.changeContent(g_str_iamhere);
			scaleRestore($('#img_RecordButton'));
			stopRotateInterval($('#img_RecordButton'));
		}
	}

	function SetText(text){
		if(window.antrip){
			window.antrip.setText(text);
		}
	}
	
	var g_worker=null;
	function syncPosition(result){		
			cleanGlobalArray();
			initRecorderMap(true);
			addPosition(result);
			/*Concurrent.Thread.create(function(){
				addPosition(result);
			});*/
	}

	function addPosition(result){
		var self =  $('#map_canvas_1').gmap('get','map');
		$.each(result.CheckInDataList, function(i, point) {
			if(point.lat != latlng_undefined_value && point.lng !=latlng_undefined_value){	
				g_current_latitude = point.lat;
				g_current_longitude = point.lng;
				var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
				g_tripPointArray_2.push(latlng);
				g_bounds.extend(latlng);
				if (typeof point.CheckIn != 'undefined'){
					var checkinmarker =  new google.maps.Marker({ 
						'position': latlng, 
						'bounds': true,
						'animation': google.maps.Animation.DROP,
						'icon': im+"placemarker.png",
						map: self
					});
					var CheckInInfo="";
					if(point.CheckIn.picture_uri!=null && typeof point.CheckIn.picture_uri!='undefined'){
						CheckInInfo +="<p><img src='"+point.CheckIn.picture_uri + "' height='150' /></p>";
					}
					if(point.CheckIn.emotion!=null && typeof point.CheckIn.emotion!='undefined'){
						CheckInInfo +="<p><img width='54px' src='"+im+emotionMapping[point.CheckIn.emotion]+".png'>"+g_Tooltip[emotionMapping[point.CheckIn.emotion]]+"</img></p>";
					}
					if(point.CheckIn.message!=null && typeof point.CheckIn.message!='undefined'){
						CheckInInfo += "<p>"+ point.CheckIn.message +"</p>";
					}
					CheckInInfo +="<p>"+g_current_latitude+", "+g_current_longitude+"</p>";
					var infowindow = new google.maps.InfoWindow(
					{ 
						'content': CheckInInfo	
					});
					google.maps.event.addListener(checkinmarker, 'click', function() {
						if (infowindow.getMap()==null){
							infowindow.open(self,checkinmarker);
						}
						else{
							infowindow.close();
						}
					});	
					g_tripMarkerArray_2.push(checkinmarker);
				} else {
					var marker =  new google.maps.Marker({ 
						'animation': google.maps.Animation.DROP,
						'position': latlng, 
						'bounds': true
					});
					var infowindow = new google.maps.InfoWindow(
					{ 
						'content': g_current_latitude+", "+g_current_longitude
					});
					google.maps.event.addListener(marker, 'click', function() {
						if (infowindow.getMap()==null){
							infowindow.open(self,marker);
						}
						else{
							infowindow.close();
						}
					});

					marker.setMap(self);
					g_tripPointObjArray_2.push(marker);
				}
			}
		});
		DrawLine();
		if(g_markerCluster!=null){
			g_markerCluster.clearMarkers();
			g_markerCluster = null;
		}
		g_markerCluster = new MarkerClusterer(self, g_tripPointObjArray_2);

		var curlatlng = null;
		if(g_currentmarker!=null){
			curlatlng = g_currentmarker.getPosition();
		}
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
		if(curlatlng == null || curlatlng.equals(latlng) != true){
			drawCurrentPosition(latlng);
		}

		$('#map_canvas_1').gmap('refresh');
	}

	function DrawLine(){
		var self =  $('#map_canvas_1').gmap('get','map');
		if(g_mapPath!=null)
		{
			g_mapPath.setMap(null);
			g_mapPath = null;
		}
		g_mapPath = new google.maps.Polyline({
			'strokeColor': "#FF0000", 
			'strokeOpacity': 0.8, 
			'strokeWeight': 4, 
			'path': g_tripPointArray_2
		});
		g_mapPath.setMap(self);
	}
	
	var g_circle = null;
	function drawCircle(center, rad){
		var self =  $('#map_canvas_1').gmap('get','map');
		if(g_circle!=null){
			g_circle.setMap(null);
			g_circle = null;
		}
		g_circle = new google.maps.Circle({
					center: center,
					radius: rad,
					strokeColor: "#FF0000",
					strokeOpacity: 0.8,
					strokeWeight: 2,
					fillColor: "#FF0000",
					fillOpacity: 0.35
		});
		g_circle.setMap(self);
	}


	function setPosition(latitude, longitude, accuracy){
		if(latitude==null || longitude==null || latitude == latlng_undefined_value || longitude ==latlng_undefined_value){
			return;
		}
		g_current_latitude = latitude;
		g_current_longitude =longitude;
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
		//g_bounds.extend(latlng);

		var self =  $('#map_canvas_1').gmap('get','map');
		var curlatlng = null;
		if(g_currentmarker!=null){
			curlatlng = g_currentmarker.getPosition();
		}

		if(curlatlng == null || curlatlng.equals(latlng) != true){
			drawCurrentPosition(latlng);
			g_currentmarker.openInfoWindow(true);
			self.setCenter(latlng);
			self.setZoom(g_zoom);
		//	self.fitBounds(g_bounds);
			$("#RecordButton").css('visibility','visible');
			$('#map_canvas_1').gmap('refresh');
		}

		$.mobile.hidePageLoadingMsg();
	}

	function drawCurrentPosition(latlng){
		var self =  $('#map_canvas_1').gmap('get','map');
		var iconpath = g_antMarker;
		var xpath = g_xpath;
		var accuracy=null;
		if(accuracy!='undefined' && accuracy!=null){
			drawCircle(latlng, accuracy);
			iconpath = im+"ant_24_unlocat.png";
		}
		if(g_currentmarker!=null){
			g_currentmarker.setMap(null);
			g_currentmarker = null;
		}
		if(g_xmarker!=null){
			g_xmarker.setMap(null);
			g_xmarker = null;
		}
		g_currentmarker =  new google.maps.Marker({ 
				'animation': google.maps.Animation.BOUNCE,
				'position': latlng, 
				//'bounds': true,
				'icon': iconpath,
		});
		g_xmarker=  new google.maps.Marker({ 
				'animation': google.maps.Animation.DROP,
				'position': latlng, 
				//'bounds': true,
				'icon': xpath,
		});
		g_currentmarker.setMap(self);
		g_xmarker.setMap(self);
		var infowindow = new google.maps.InfoWindow(
		{ 
			'content': g_str_iamhere,
			'maxWidth': '50px',
			size: new google.maps.Size(50,50),
			'position':latlng,
			zIndex:1009
		});
		google.maps.event.addListener(g_currentmarker, 'click', function() {
			var isRecording = $.cookie("isRecording");
			if(window.antrip){
				isRecording = window.antrip.isRecording();
			}
			if(isRecording == null){
				g_currentmarker.openInfoWindow();
			}else{
				$('#CheckinButton').trigger('click');
			}
		});	
		g_currentmarker.changeContent = function(text) {
			infowindow.setContent(text);
		}
		g_currentmarker.openInfoWindow = function(IsOpen) {
			if(IsOpen!=null){
				if(IsOpen==false){
					infowindow.close();
				}
				else{
					infowindow.open(self,g_currentmarker);
				}
			}
			else{
				if (infowindow.getMap()==null){
					infowindow.open(self,g_currentmarker);
				}
				else{
					infowindow.close();
				}
			}
		}
	}

	function initRecorderMap(isSetPosition){

		$.mobile.showPageLoadingMsg("b", g_str_localizationing, true);
		var isRecording = null;
		isRecording = $.cookie("isRecording");
		if(window.antrip){
			isRecording = window.antrip.isRecording();
		}

		$('#LocateButton').find('.tip').html(g_str_locating);
		$('#CheckinButton').find('.tip').html(g_str_checkin);

		if(isRecording == null){
			changeIconToStopRecord();
			cleanGlobalArray();
		}
		else{
			if(window.antrip){
				;
			}
			else{
				tripRecorder = setInterval(function(){getLocation()},5000);
			}
			changeIconToRecoding();
		}
		if(g_currentmarker!=null){
			g_currentmarker.setMap(null);
			g_currentmarker = null;
		}
		if(g_xmarker!=null){
			g_xmarker.setMap(null);
			g_xmarker = null;
		}

		if(isSetPosition==true){
			$('#map_canvas_1').gmap('watchPosition', function (position, status) {
				if ( status == 'OK' ) {
					g_current_latitude = position.coords.latitude;
					g_current_longitude = position.coords.longitude;
					var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
					setPosition(g_current_latitude, g_current_longitude);
					$('#map_canvas_1').gmap('clearWatch');
				}
			});
			setPosition(g_current_latitude, g_current_longitude);
		}
	}

	function CheckIn(){
		if( $("#takepicture").attr("src")=="" &&  $("#selected-emotion").attr("src")==""  && $("#message").val() ==""){
			alert(g_str_inputsomething);
			return;
		}

		g_emotion_html = "<img src='"+$("#selected-emotion").attr("src")+"' />"+$('#selected-emotion-tip').html();
		//alert($("#selected-emotion").attr("alt"));
		if(window.antrip){
			window.antrip.setEmotion(eval($("#selected-emotion").attr("alt")));
		}
		else{
			var self =  $('#map_canvas_1').gmap('get','map');
			var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
			if(g_current_latitude == latlng_undefined_value || g_current_longitude ==latlng_undefined_value){
				alert(g_str_positionerror);
				return;
			}
			g_tripPointArray_2.push(latlng);
			g_bounds.extend(latlng);
			var checkinmarker =  new google.maps.Marker({ 
				'position': latlng, 
				'bounds': true,
				'animation': google.maps.Animation.DROP,
				'icon': im+"placemarker.png",
				map: self
			});
			var CheckInInfo="";
			if($("#takepicture").attr("src")!="" && $("#takepicture").attr("src")!=" " && $("#takepicture").attr("src")!='undefined'){
				CheckInInfo +="<p><img src='"+$("#takepicture").attr("src")+ "' height='150' /></p>";
			}
			if(g_emotion_html!=null){
				CheckInInfo +=g_emotion_html;
			}
			if($("#message").val()!=null && $("#message").val()!='undefined'){
				CheckInInfo += "<p>"+ $("#message").val() +"</p>";
			}
			CheckInInfo +="<p>"+g_current_latitude+", "+g_current_longitude+"</p>";
			var infowindow = new google.maps.InfoWindow(
			{ 
				'content': CheckInInfo	
			});
			google.maps.event.addListener(checkinmarker, 'click', function() {
				if (infowindow.getMap()==null){
					infowindow.open(self,checkinmarker);
				}
				else{
					infowindow.close();
				}
			});	

			g_tripMarkerArray_2.push(checkinmarker);

			DrawLine();
			$('#map_canvas_1').gmap('refresh');
		}

		if(window.antrip){
			window.antrip.endCheckin();
		}

		//openMarkplaceWindow();
	}
//-->