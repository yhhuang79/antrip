<!--
	var tripRecorder;
	var g_current_latitude;
	var g_current_longitude;
	var g_emotion_html=null;
	var g_tripPointArray_2 = new Array(0);
	var g_tripMarkerArray_2 = new Array(0);
	var g_tripPointObjArray_2 = new Array(0);

	var g_currentmarker = null;
	var latlng_undefined_value=-999;

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

	var g_intval=null;
	function changeIconToRecoding(){
			//g_intval=window.setInterval( function () { addPoint(); } ,  10000 );
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace.png");
			$('#RecordButton').attr('data-theme','e').removeClass('ui-btn-up-b').addClass('ui-btn-up-e').trigger('create');
			$('#b_add_note').find('.class_left_bt').attr('src', im+'PlaceRecording.png');
			$('#b_add_note').find('.tip').html(g_str_nowrecording);
			scaleInterval($('#b_add_note'));
	}

	function addPoint(){
		$('#map_canvas_2').gmap('watchPosition', function (position, status) {
			if ( status == 'OK' ) {
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
						if (infowindow.getMap()==null){
							infowindow.open(self,marker);
						}
						else{
							infowindow.close();
						}
					});
					marker.setMap(self);
			}
			$('#map_canvas_2').gmap('refresh');
		});
	}

	function cleanGlobalArray(){
		for(i=0;i<g_tripMarkerArray_2.length;i++){
			g_tripMarkerArray_2[i].setMap(null);
		}
		for(i=0;i<g_tripPointObjArray_2.length;i++){
			g_tripPointObjArray_2[i].setMap(null);
		}
		g_tripPointArray_2 = new Array(0);
		g_tripMarkerArray_2 = new Array(0);
		g_tripPointObjArray_2 = new Array(0);
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
		var self =  $('#map_canvas_2').gmap('get','map');
		$.each(result.CheckInDataList, function(i, point) {
			if(point.lat != latlng_undefined_value && point.lng !=latlng_undefined_value){	
				/*if(g_currentmarker!=null){
					g_currentmarker.setMap(null);
					g_currentmarker = null;
				}*/
				g_current_latitude = point.lat;
				g_current_longitude = point.lng;
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
					var CheckInInfo;
					if(point.CheckIn.picture_uri!=null || typeof point.CheckIn.picture_uri!='undefined'){
						CheckInInfo +="<p><img src='"+point.CheckIn.picture_uri + "' height='120' /></p>";
					}
					if(point.CheckIn.emotion!=null || typeof point.CheckIn.emotion!='undefined'){
						CheckInInfo +="<p><img width='72px' src='"+im+emotionMapping[point.CheckIn.emotion]+".png'>"+g_Tooltip[emotionMapping[point.CheckIn.emotion]]+"</img></p>";
					}
					if(point.CheckIn.message!=null || typeof point.CheckIn.message!='undefined'){
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
				if( $('#markplacewindow').is(":visible") == false) {
					$("#overlay").css("display","none");
				}
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
		if(latitude==null || longitude==null || latitude == latlng_undefined_value || longitude ==latlng_undefined_value){
			return;
		}
		g_current_latitude = latitude;
		g_current_longitude =longitude;
		var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);

		var self =  $('#map_canvas_2').gmap('get','map');
		self.setCenter(latlng);
		self.setZoom(g_zoom);

		if(g_currentmarker!=null){
			g_currentmarker.setMap(null);
			g_currentmarker = null;
		}
		g_currentmarker =  new google.maps.Marker({ 
			'position': latlng, 
			'bounds': true,
			'icon': im+"ant_24.png",
		});

		g_currentmarker.setMap(self);
		$("#overlay").css("display","none");
		$("#overlay").html("");

		$('#map_canvas_2').gmap('refresh');
	}

	function ShowRecorderMap(){
		$('#map_canvas').hide();
		$('#map_canvas_2').show();
		$('#map_canvas_2').css('margin-top','-690px');
		$("#overlay").css("display","block");
		$("#overlay").html(g_str_localizationing);
		var isRecording = null;
		isRecording = $.cookie("isRecording");
		if(window.antrip){
			isRecording = window.antrip.getCookie("isRecording");
		}

		if(isRecording == null){
			$('#b_seq_trip').find('.class_left_bt').attr("src", im+"MarkPlace_b.png");
			cleanGlobalArray();
		}
		else{
			changeIconToRecoding();
		}
		$('#map_canvas_2').gmap('watchPosition', function (position, status) {
			if ( status == 'OK' ) {
				g_current_latitude = position.coords.latitude;
				g_current_longitude = position.coords.longitude;
				var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
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
			$("#overlay").css("display","none");
		}else{
			$('#edit_display_area').attr('disabled', true);
			$('#sym_topbtnGroup').attr('disabled', true);
			$("#overlay").css("display","block");
			$('#takepicture').attr("src","");
			$('#emotion-sel').html("");
			$("#placemarktext").attr("value","");
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
		
		/*var latlng = new google.maps.LatLng(g_current_latitude, g_current_longitude);
		g_tripPointArray_2.push(latlng);

		var marker = new google.maps.Marker({
			'position': latlng, 
			'bounds': true,
			'icon': im+"placemarker.png",
		});

		var self = $('#map_canvas_2').gmap('get','map');
		var infowindow = new google.maps.InfoWindow(); 
		google.maps.event.addListener(marker, 'click', function() {
			var CheckInInfo;
			if($("#takepicture").attr("src")!=""){
				CheckInInfo +="<p><img height='120' src='"+ $("#takepicture").attr("src") +"' /></p>";
			}
			if(g_emotion_html!=null){
				CheckInInfo +="<p>"+ g_emotion_html  +"</p>";
			}
			if($("#placemarktext").val()!=null){
				CheckInInfo +="<p>"+ $("#placemarktext").val() +"</p>";
			}
			CheckInInfo +="<p>"+g_current_latitude+", "+g_current_longitude+"</p>";

			infowindow.setContent(CheckInInfo);
			if (infowindow.getMap()==null){
				infowindow.open(self,marker);
			}
			else{
				infowindow.close();
			}
		});
		marker.setMap(self);
		setPosition(g_current_latitude, g_current_longitude);

		g_tripMarkerArray_2.push(marker);

		DrawLine();
		$('#map_canvas_2').gmap('refresh');*/

		if(window.antrip){
			window.antrip.endCheckin();
		}

		openMarkplaceWindow();
	}

	function showEmtionCompass(){
		$('#markplacewindow :input').attr('disabled', true);
		$('#emotion_compass').show();
		$("#overlay").css("z-index","1012");
	}

	function initEmotionMap(){
		var image = $('#emotion-c');
		var emotionArray=new Array();
		emotionArray[0]="excited";
		emotionArray[1]="happy";
		emotionArray[2]="pleased";
		emotionArray[3]="relaxed";
		emotionArray[4]="peaceful";
		emotionArray[5]="sleepy";
		emotionArray[6]="sad";
		emotionArray[7]="bored";
		emotionArray[8]="nervous";
		emotionArray[9]="angry";
		emotionArray[10]="calm";

		var Tooltip = g_Tooltip;

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
				g_emotion_html = "<img width='72px' src='"+im+e.key+".png'>"+Tooltip[e.key]+"</img>";
				$('#emotion-sel').html("<img width='72px' src='"+im+e.key+".png' style='margin-left:-100px;font-size:24px;'><font size=10 color='#e9e5da'><b>"+Tooltip[e.key]+"</b></font></img>");
				$('#markplacewindow :input').removeAttr('disabled');
				$('#emotion_compass').hide();
				$("#overlay").css("z-index","1010");
				if(window.antrip){
					window.antrip.setEmotion(emotionArray.indexOf(e.key)+1);
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