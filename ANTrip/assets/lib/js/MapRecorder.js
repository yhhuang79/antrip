<!--
	var tripRecorder;
	function getLocation(){
		if (navigator.geolocation){
			navigator.geolocation.getCurrentPosition(uploadPosition);
		} else {
			alert("Geolocation is not supported by this browser.");
		}
	}
	function uploadPosition(position){
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
	}
	function startRecordTrip(){
		var isRecording = $.cookie("isRecording");
		//$('#RecordButton').attr('data-theme','b').removeClass('ui-btn-up-e').removeClass('ui-btn-hover-e').addClass('ui-btn-up-b').trigger('create');
		if(isRecording == null){
			$.cookie("isRecording", "true");
			var sid = $.cookie("sid"); 
			$.ajax({url:'http://plash2.iis.sinica.edu.tw/antrip/lib/php/GetNewTripId.php',
				data:{userid: sid},							 
				type: 'GET', dataType: 'jsonp', cache: false,
				success:function(result){
					$.cookie("trip_id",result.newTripId);
					tripRecorder = setInterval(function(){getLocation()},5000);
					$('#RecordButton').attr('data-theme','e').removeClass('ui-btn-up-b').addClass('ui-btn-up-e').trigger('create');
					$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecording.png');
					$('#b_add_note').find('.tip').html("Now Recording...");
					alert("Start Recording Trip");
					clockwiseInterval($('#b_add_note').find('.class_left_bt'));
				}
			});

		} else {
			$.cookie("isRecording", null);
			g_tripPointArray = [];
			$('#map_canvas_2').gmap('clear', 'Polyline');
			$('#map_canvas_2').gmap('refresh');
			clearInterval(tripRecorder);
			$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecorder.png');
			$('#b_add_note').find('.tip').html("start recording");
			alert("Stop Recording Trip");
			window.clearInterval(g_clockintval);
		}
	}


	function ShowRecorderMap(){
		$('#map_canvas').css('margin-top','-690px');
		$('#sym_edit_bt_list').show();
		$('#markplacewindow :input').removeAttr('disabled');
		$('#emotion_compass').hide();
		$('#map_canvas').gmap('destroy');
		$('#map_canvas').gmap({'center': g_startLatLng, 'zoom': g_zoom, 'callback': function(map) {
				var self = this;
				self.addControl('control', google.maps.ControlPosition.LEFT_TOP);
				g_tripPointArray = new Array(0);
				self.watchPosition(function(position, status) {
					if ( status === 'OK' ) {
						var latlng = new google.maps.LatLng(position.coords.latitude, position.coords.longitude);
						if($.cookie("isRecording") != null){
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
					}
				});
		}});
		$('#map_canvas_1').gmap('refresh');
	}

	function openMarkplaceWindow(){
		if( $('#markplacewindow').is(":visible") ) {
			$('#edit_display_area :input').removeAttr('disabled');
			$('#sym_topbtnGroup :input').removeAttr('disabled');
			$('#markplacewindow :input').removeAttr('disabled');
			$('#markplacewindow').hide();
			$('#emotion_compass').hide();
		}else{
			$('#edit_display_area :input').attr('disabled', true);
			$('#sym_topbtnGroup :input').attr('disabled', true);
			$('#markplacewindow').show();
			$('#emotion_compass').hide();
		}
	}

	function initEmotionMap(){
		require("lib/jq_includes/jquery.imagemapster.js");
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
				$('#emotion-sel').html("<img width='72px' src='"+im+e.key+".png' style='margin-left:-100px;'>"+Tooltip[e.key]+"</img>");
				$('#markplacewindow :input').removeAttr('disabled');
				$('#emotion_compass').hide();
			},
			showToolTip: true,
			toolTipClose: ["tooltip-click", "area-click"],
			areas: [
				{
					key: "bored",
					toolTip:Tooltip["bored"],
					//fillColor: "ffffff"
				},
				{
					key: "sad",
					toolTip: Tooltip["sad"],
					//fillColor: "000000"
				},
				{
					key: "sleepy",
					toolTip:Tooltip["sleepy"],
				},
				{
					key: "peaceful",
					toolTip:Tooltip["peaceful"],
				},
				{
					key: "relaxed",
					toolTip:Tooltip["relaxed"],
				},
				{
					key: "pleased",
					toolTip:Tooltip["pleased"],
				},
				{
					key: "happy",
					toolTip:Tooltip["happy"],
				},
				{
				   key: "excited",
				   toolTip: Tooltip["excited"],
				},
				{
					key: "angry",
					toolTip:Tooltip["angry"],
				},
				{
					key: "nervous",
					toolTip: Tooltip["nervous"],
					//fillColor: "000000"
				},
				{
				   key: "calm",
				   toolTip:Tooltip["calm"],
				   strokeColor: "FFFFFF"
				}
				]
		});
	}
//-->