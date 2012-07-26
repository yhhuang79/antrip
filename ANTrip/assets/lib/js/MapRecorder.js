<!--
	var tripRecorder;
	var g_current_latitude;
	var g_current_longitude;
	var g_emotion_html=null;
	var g_tripPointArray = new Array(0);

	/*function getLocation(){
		/*if (navigator.geolocation){
			navigator.geolocation.getCurrentPosition(uploadPosition);
		} else {
			alert("Geolocation is not supported by this browser.");
		}*/
	//}
	/*function setLocation(position){
		var self = $('#map_canvas').gmap('get','map');
		var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
		g_tripPointArray.push(latlng);

		var marker = new google.maps.Marker({
			'position': latlng, 
			'bounds': true		
		});

		google.maps.event.addListener(marker, 'click', function() {
			var CheckInInfo =position.coords.latitude+", "+position.coords.longitude;
			var infowindow = new google.maps.InfoWindow({content: CheckInInfo});
			infowindow.open(self,marker);
		});
		marker.setMap(self);
		if(g_cuttedPath!=null)
		{
			g_cuttedPath.setMap(null);
		}
		g_cutPath = new google.maps.Polyline({
			'strokeColor': "#FF0000", 
			'strokeOpacity': 0.8, 
			'strokeWeight': 4, 
			'path': g_tripPointArray
		});
		g_cutPath.setMap(self);

		$('#map_canvas').gmap('refresh');
	}*/
	/*function uploadPosition(position){
		Date.prototype.Timestamp = function() {
			var yyyy = this.getFullYear().toString();
			var mm = (this.getMonth()+1).toString(); // getMonth() is zero-based
			var dd  = this.getDate().toString();
			var h = this.getHours().toString();
			var m = this.getMinutes().toString();
			var s = this.getSeconds().toString();
			var ms = this.getUTCMilliseconds().toString();
			return yyyy+"-"+(mm[1]?mm:"0"+mm[0])+"-"+(dd[1]?dd:"0"+dd[0])+"%20"+(h[1]?h:"0"+h[0])+":"+(m[1]?m:"0"+m[0])+":"+(s[1]?s:"0"+s[0])+"."+ms; // padding
		};
		var sid = $.cookie("sid"); 
		var trip_id = $.cookie("trip_id");
		var d = new Date();				
		var timestamp = d.Timestamp();
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/Input.php',
			data:{userid: sid, trip_id: trip_id, lat: position.coords.latitude, lng: position.coords.longitude, timestamp: timestamp},							 
			type: 'GET', dataType: 'json', cache: false,
			success:function(result){
			}
		});
	}*/
	function changeIconToRecoding(){
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace.png");
			$('#RecordButton').attr('data-theme','e').removeClass('ui-btn-up-b').addClass('ui-btn-up-e').trigger('create');
			$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecording.png');
			$('#b_add_note').find('.tip').html("Now Recording...");
	}

	function startRecordTrip(){
		var isRecording = null;
		if(window.antrip){
			isRecording = window.antrip.getCookie("isRecording");
		}
		if(isRecording == null){
			var sid = null;
			if(window.antrip){
				isRecording = window.antrip.setCookie("isRecording", "true");
				sid = window.antrip.getCookie("sid");
			}
			g_tripPointArray = [];
			$('#map_canvas').gmap('clear', 'Polyline');
			$('#map_canvas').gmap('refresh');

			if(window.antrip){
				var newTripId = window.antrip.startRecording();
				window.antrip.setCookie("trip_id", newTripId.toString());
			}
			else{
				alert("ANTrip APP Exception!");
			}
			changeIconToRecoding();
			scaleInterval($('#b_add_note'));
			//clockwiseInterval($('#b_add_note').find('.class_left_bt'));
		} else {
			if(window.antrip){
				window.antrip.stopRecording();
			}
			else{
				alert("ANTrip APP Exception!");
			}
			if(window.antrip){
				isRecording = window.antrip.removeCookie("isRecording");
			}
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace_b.png");
			$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecorder.png');
			$('#b_add_note').find('.tip').html("start recording");
			scaleRestore($('#b_add_note'));
			//window.clearInterval(g_clockintval);
		}
	}

	function SetText(text){
		if(window.antrip){
			window.antrip.setText(text);
		}
	}

	function setPosition(latitude, longitude){
		//$('#map_canvas').css('margin-top','-690px');
		$('#map_canvas').gmap({'center': g_startLatLng, 'zoom': g_zoom, 'callback': function(map) {
				var self = this;
				self.addControl('control', google.maps.ControlPosition.LEFT_TOP);

				g_current_latitude = latitude;
				g_current_longitude =longitude;
				var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
				if(isRecording != null){
					g_tripPointArray.push(latlng);
					self.addShape('Polyline',{
						'strokeColor': "#FF0000", 
						'strokeOpacity': 0.8, 
						'strokeWeight': 2, 
						'path': g_tripPointArray
					});
				}
				if ( !self.get('markers').client ) {
					self.addMarker({ 'id': 'client', 'position': latlng, 'bounds': true });
				} else {
					self.get('markers').client.setPosition(latlng);
					map.panTo(latlng);
				}
		}});
	}

	function ShowRecorderMap(){
		$('#map_canvas').css('margin-top','-690px');
		var isRecording = null;
		if(window.antrip){
			isRecording = window.antrip.getCookie("isRecording");
		}
		//var isRecording = window.localStorage["isRecording"];
		if(isRecording == null){
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace_b.png");
		}
		else{
			changeIconToRecoding();
			//clockwiseInterval($('#b_add_note').find('.class_left_bt'));
		}
		$('#sym_edit_bt_list').show();
		$('#markplacewindow :input').removeAttr('disabled');
		$('#emotion_compass').hide();
		if(isRecording == null){
			$('#map_canvas').gmap('destroy');
			g_tripPointArray = new Array(0);
		}
		//$('#map_canvas').gmap('refresh');
	}

	function openMarkplaceWindow(){
		if($('#b_seq_trip').find('.class_left_bt').attr("src")==im+"MarkPlace_b.png"){
			return;
		}
		if( $('#markplacewindow').is(":visible") ) {
			$('#edit_display_area :input').removeAttr('disabled');
			$('#sym_topbtnGroup :input').removeAttr('disabled');
			$('#markplacewindow :input').removeAttr('disabled');
			$('#markplacewindow').hide();
			$('#emotion_compass').hide();
		}else{
			$('#edit_display_area').attr('disabled', true);
			$('#sym_topbtnGroup').attr('disabled', true);

			//document.getElementById("edit_display_area").disabled = true;
			//document.getElementById("img_seq_trip").disabled = true;
			$('#markplacewindow').show();
			$('#emotion_compass').hide();
			if(window.antrip){
				window.antrip.startCheckin();
			}
		}
	}

	function showPicture(path){
		if(path!=-1)
		{
			$("#takepicture").attr("src", "file://"+path);
		}
	}

	function CheckIn(){
		if( $("#takepicture").attr("src")=="" && g_emotion_html==null && $("#placemarktext").val() ==""){
			alert("please input something!");
			return;
		}

		var self = $('#map_canvas').gmap('get','map');
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
		g_tripPointArray.push(latlng);

		var marker = new google.maps.Marker({
			'position': latlng, 
			'bounds': true,
			'icon': im+"placemarker.png",
			
		});

		google.maps.event.addListener(marker, 'click', function() {
			var CheckInInfo ="<p><img src='"+ $("#takepicture").attr("src") +"' /></p><p>"+ g_emotion_html  +"</p><p>"+ $("#placemarktext").val() +"</p><p>"+g_current_latitude+", "+g_current_longitude+"</p>";
			var infowindow = new google.maps.InfoWindow({content: CheckInInfo});
			infowindow.open(self,marker);
			//self.openInfoWindow({'content': CheckInInfo}, this);
		});
		marker.setMap(self);

		$('#map_canvas').gmap('refresh');

		if(window.antrip){
			window.antrip.endCheckin();
		}

		openMarkplaceWindow();
	}

	function initEmotionMap(){
		//require("lib/jq_includes/jquery.imagemapster.js");
		var image = $('#emotion-c');
		//var excitedTooltip = 'Excited!';
		var Tooltip = {
			bored: 'Bored',
			sad: 'Sad',
			sleepy: 'Sleepy',
			peaceful: 'Peaceful',
			relaxed: 'Relaxed',
			pleased: 'Pleased',
			happy: 'Happy',
			excited: 'Excited',
			angry: 'Angry',
			nervous: 'Nervous',
			calm: "Calm"
		};
		image.mapster(
		{
			fillOpacity: 0.4,
			fillColor: "d42e16",
			strokeColor: "4fb6d1",
			strokeOpacity: 0.8,
			strokeWidth: 4,
			stroke: true,
			isSelectable: true,
			singleSelect: true,
			mapKey: 'name',
			listKey: 'name',
			onClick: function (e) {
				//var newToolTip = defaultDipTooltip;
				g_emotion_html = "<img width='72px' src='"+im+e.key+".png'>"+Tooltip[e.key]+"</img>";
				$('#emotion-sel').html("<img width='72px' src='"+im+e.key+".png' style='margin-left:-100px;'>"+Tooltip[e.key]+"</img>");
				$('#markplacewindow :input').removeAttr('disabled');
				$('#emotion_compass').hide();
				if(window.antrip){
					window.antrip.endCheckin(id[e.key]);
				}
			},
			showToolTip: true,
			toolTipClose: ["tooltip-click", "area-click"],
			areas: [
				{
					key: "bored",
					toolTip:Tooltip["bored"],
						id: 1,
					//fillColor: "ffffff"
				},
				{
					key: "sad",
					toolTip: Tooltip["sad"],
						id: 2,
					//fillColor: "000000"
				},
				{
					key: "sleepy",
					toolTip:Tooltip["sleepy"],
						id: 3,
				},
				{
					key: "peaceful",
					toolTip:Tooltip["peaceful"],
						id: 4,
				},
				{
					key: "relaxed",
					toolTip:Tooltip["relaxed"],
						id: 5,
				},
				{
					key: "pleased",
					toolTip:Tooltip["pleased"],
						id: 6,
				},
				{
					key: "happy",
					toolTip:Tooltip["happy"],
						id: 7,
				},
				{
				   key: "excited",
				   toolTip: Tooltip["excited"],
					   id: 8,
				},
				{
					key: "angry",
					toolTip:Tooltip["angry"],
						id: 9,
				},
				{
					key: "nervous",
					toolTip: Tooltip["nervous"],
						id: 10,
					//fillColor: "000000"
				},
				{
				   key: "calm",
				   toolTip:Tooltip["calm"],
				   strokeColor: "FFFFFF",
				   id: 11,
				}
				]
		});
	}
//-->