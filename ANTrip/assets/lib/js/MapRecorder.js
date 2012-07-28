<!--
	var tripRecorder;
	var g_current_latitude;
	var g_current_longitude;
	var g_emotion_html=null;
	var g_tripPointArray_2 = new Array(0);
	var g_tripMarkerArray_2 = new Array(0);
	var g_tripMarkerObjArray = new Array(0);

	var g_mapPath = null;

	var emotionMapping = {	
		1: "excited",
		2: "happy",
		3: "pleased",
		4: "relaxed",
		5: "peaceful",
		6: "sleepy",
		7: "sad",
		8: "bored",
		9: "nervous",
		10: "angry",
		11: "calm"
	};
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
	var g_intval=null;
	function changeIconToRecoding(){
			//g_intval=window.setInterval( function () { addPoint(); } ,  10000 );
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace.png");
			$('#RecordButton').attr('data-theme','e').removeClass('ui-btn-up-b').addClass('ui-btn-up-e').trigger('create');
			$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecording.png');
			//alert("Now Recording...");
			$('#b_add_note').find('.tip').html(g_str_nowrecording);
			scaleInterval($('#b_add_note'));
	}

	function addPoint(){
		alert('addPoint');
		$('#map_canvas_2').gmap('watchPosition', function (position, status) {
			if ( status == 'OK' ) {
					alert('OK');
					var self = $('#map_canvas_2').gmap('get','map');
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
						'content': marker.timestamp
					});
					google.maps.event.addListener(marker, 'click', function() {
						infowindow.open(self,marker);
					});
					marker.setMap(self);
			}
			$('#map_canvas_2').gmap('refresh');
		});
	}

	function cleanGlobalArray(){
		for(i=0;i<g_tripMarkerObjArray.length;i++){
			g_tripMarkerObjArray[i].setMap(null);
		}
		g_tripPointArray_2 = new Array(0);
		g_tripMarkerArray_2 = new Array(0);
		g_tripMarkerObjArray = new Array(0);
		g_emotion_html=null;
	}

	function startRecordTrip(){
		var isRecording = null;
		isRecording = $.cookie("isRecording");
		if(window.antrip){
			isRecording = window.antrip.getCookie("isRecording");
		}
		if(isRecording == null){
			var sid = null;
			sid = $.cookie("sid");
			$.cookie("isRecording", true);
			cleanGlobalArray();
			if(window.antrip){
				isRecording = window.antrip.setCookie("isRecording", "true");
				sid = window.antrip.getCookie("sid");
				window.antrip.setCookie("trip_id", window.antrip.startRecording().toString());
			}
			else{
				alert("ANTrip APP Exception!");
			}
			changeIconToRecoding();
		} else {
			//var tripname="";
			$("#dialog-tripname").dialog('open');
		}
	}

	function initTripNameDialog(){
		$("#dialog-tripname").dialog({
			//dialogClass: 'antripDialogStyle',
			autoOpen: false,
			bgiframe: false,
			draggable: false,
			resizable: false,
			height:260,
			width: 400,
			modal: true,
			open: function (event, ui) {
				$('.ui-dialog-buttonpane').css({
					'background-image':url+im+"typenotearea.png)",
					'background-position':"center center",
				});
				$("button").css({
					color: "#000000",
				});
				$(".ui-dialog-titlebar-close").hide();
			},
			buttons: {
				"OK": function() {
					$.cookie("isRecording", null);
					if(window.antrip){
						//alert($('#trip_name').val());
						window.antrip.stopRecording($('#trip_name').val());
						window.antrip.removeCookie("isRecording");
					}
					else{
						alert("ANTrip APP Exception!");
					}
					$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace_b.png");
					$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecorder.png');
					$('#b_add_note').find('.tip').html(g_str_startrecording);
					scaleRestore($('#b_add_note'));
					//window.clearInterval(g_intval);
					if(g_logoutbyIcon == true){
						logoutbyIcon();
					}
					$( this ).dialog( "close" );
				}
			},
			close: function() {
				//alert('close');
				$('#dialog').dialog('close');
			}
		});
	}

	function SetText(text){
		if(window.antrip){
			window.antrip.setText(text);
		}
	}

	function syncPosition(result){
		cleanGlobalArray();
		addPosition(result);
	}

	function addPosition(result){
		var Tooltip = {	
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

		var self =  $('#map_canvas_2').gmap('get','map');
		$.each(result.CheckInDataList, function(i, point) {
			g_current_latitude = point.lat.valueOf() / 1000000;
			g_current_longitude = point.lng.valueOf() / 1000000;
			var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
			self.setCenter(latlng);
			self.setZoom(g_zoom);
			g_tripPointArray_2.push(latlng);
			if (typeof point.CheckIn != 'undefined'){
				var checkinmarker =  new google.maps.Marker({ 
					'position': latlng, 
					'bounds': true,
					'icon': im+"placemarker.png",
					map: self
				});
				var infowindow = new google.maps.InfoWindow(
				{ 
					//'content': "<p><img src='"+im+e.key+".png' /></p><p>"+ point.CheckIn.message +"</p><img src='"+ point.CheckIn.picture_uri +"' height='120'/>"
					'content': "<p><img src='"+point.CheckIn.picture_uri + "' height='120' /></p><p><img width='72px' src='"+emotionMapping[im+point.CheckIn.emotion]+".png'>"+Tooltip[emotionMapping[im+point.CheckIn.emotion]]+"</img></p><p>"+ point.CheckIn.message +"</p><p>"+g_current_latitude+", "+g_current_longitude+"</p>"
					
				});
				google.maps.event.addListener(checkinmarker, 'click', function() {
					infowindow.open(self,checkinmarker);
				});		

				//placemarker.setMap(self);	
				g_tripMarkerArray_2.push(placemarker);
			} else {
				var marker =  new google.maps.Marker({ 
					'position': latlng, 
					'bounds': true
				});
				var infowindow = new google.maps.InfoWindow(
				{ 
					'content': marker.timestamp
				});
				google.maps.event.addListener(marker, 'click', function() {
					infowindow.open(self,marker);
				});

				marker.setMap(self);
			}
		});
		DrawLine();

		$('#map_canvas_2').gmap('refresh');
	}

	function DrawLine(){
		var self =  $('#map_canvas_2').gmap('get','map');
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

	function setPosition(latitude, longitude){
		//$('#map_canvas').css('margin-top','-690px');
		if(latitude==null || longitude==null ){
			return;
		}
		g_current_latitude = latitude;
		g_current_longitude =longitude;
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);

		var self =  $('#map_canvas_2').gmap('get','map');
		self.setCenter(latlng);
		self.setZoom(g_zoom);

		$('#map_canvas_2').gmap('refresh');
	}

	function ShowRecorderMap(){
		$('#map_canvas').hide();
		$('#map_canvas_2').show();
		$('#map_canvas_2').css('margin-top','-690px');
		var isRecording = null;
		isRecording = $.cookie("isRecording");
		if(window.antrip){
			isRecording = window.antrip.getCookie("isRecording");
			alert(isRecording);
		}
		//var isRecording = window.localStorage["isRecording"];
		if(isRecording == null){
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace_b.png");
			cleanGlobalArray();
			//$('#map_canvas_2').gmap('destroy');
			//alert("destory");
		}
		else{
			changeIconToRecoding();
			//setPosition(g_current_latitude, g_current_longitude);
			//clockwiseInterval($('#b_add_note').find('.class_left_bt'));
		}
		$('#map_canvas_2').gmap('watchPosition', function (position, status) {
			//var self =  $('#map_canvas_2').gmap('get','map');
			if ( status == 'OK' ) {
				//alert(position.coords.latitude);
				g_current_latitude = position.coords.latitude;
				g_current_longitude = position.coords.longitude;
				var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
				g_tripPointArray_2.push(latlng);
				setPosition(g_current_latitude, g_current_longitude);
			}
		});
		setPosition(g_current_latitude, g_current_longitude);

		$('#sym_edit_bt_list').show();
		$('#markplacewindow :input').removeAttr('disabled');
		$('#emotion_compass').hide();
		$('#map_canvas').hide();
		$('#map_canvas_2').gmap('refresh');
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
			$('#takepicture').attr("src","");
			$('#emotion-sel').html("");
			$("#placemarktext").attr("value","");
			//document.getElementById("edit_display_area").disabled = true;
			//document.getElementById("img_seq_trip").disabled = true;
			g_emotion_html=null;
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
			alert(g_str_inputsomething);
			return;
		}
		
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
		g_tripPointArray_2.push(latlng);
		g_tripMarkerArray_2.push(latlng);

		var marker = new google.maps.Marker({
			'position': latlng, 
			'bounds': true,
			'icon': im+"placemarker.png",
		});

		var self = $('#map_canvas_2').gmap('get','map');
		var infoWindow = new google.maps.InfoWindow(); 
		google.maps.event.addListener(marker, 'click', function() {
			var CheckInInfo;
			if($("#takepicture").attr("src")==""){
				CheckInInfo ="<p>"+ g_emotion_html  +"</p><p>"+ $("#placemarktext").val() +"</p><p>"+g_current_latitude+", "+g_current_longitude+"</p>";
				if(g_emotion_html==null){
					CheckInInfo ="<p>"+ $("#placemarktext").val() +"</p><p>"+g_current_latitude+", "+g_current_longitude+"</p>";
				}
			}
			else{
				CheckInInfo ="<p><img height='120' src='"+ $("#takepicture").attr("src") +"' /></p><p>"+ g_emotion_html  +"</p><p>"+ $("#placemarktext").val() +"</p><p>"+g_current_latitude+", "+g_current_longitude+"</p>";
			}
			infoWindow.setContent(CheckInInfo);
			if (infoWindow.getMap()==null){
				infoWindow.open(self,marker);
			}
			else{
				infoWindow.close();
			}
			//self.openInfoWindow({'content': CheckInInfo}, this);
		});
		marker.setMap(self);
		setPosition(g_current_latitude, g_current_longitude);

		g_tripMarkerObjArray.push(marker);

		DrawLine();
		$('#map_canvas_2').gmap('refresh');

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
				$('#emotion-sel').html("<img width='72px' src='"+im+e.key+".png' style='margin-left:-100px;font-size:24px;'><font size=10 color='#e9e5da'><b>"+Tooltip[e.key]+"</b></font></img>");
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
				   key: "excited",
				   toolTip: Tooltip["excited"],
					   id: 1,
				},
				{
					key: "happy",
					toolTip:Tooltip["happy"],
						id: 2,
				},
				{
					key: "pleased",
					toolTip:Tooltip["pleased"],
						id: 3,
				},
				{
					key: "relaxed",
					toolTip:Tooltip["relaxed"],
						id: 4,
				},
				{
					key: "peaceful",
					toolTip:Tooltip["peaceful"],
						id: 5,
				},
				{
					key: "sleepy",
					toolTip:Tooltip["sleepy"],
						id: 6,
				},
				{
					key: "sad",
					toolTip: Tooltip["sad"],
						id: 7,
					//fillColor: "000000"
				},
				{
					key: "bored",
					toolTip:Tooltip["bored"],
						id: 8,
					//fillColor: "ffffff"
				},
				{
					key: "nervous",
					toolTip: Tooltip["nervous"],
						id: 9,
					//fillColor: "000000"
				},
				{
					key: "angry",
					toolTip:Tooltip["angry"],
						id: 10,
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