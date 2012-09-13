<!--
	var g_mapclickListener = null;
	var g_mapdblclickListener = null;
	var g_mousedownListener = null;
	var g_mousemoveListener = null;
	var g_mouseupListener = null;
	var g_dragstartListener = null;
	var g_dragListener = null;
	var g_dragendListener = null;
	var g_startmarker = null;
	var g_endmarker = null;
	var g_cutPath = null;
	var g_cuttedPath = null;
	var g_cuttedPath_left = null;
	var g_cuttedPath_right = null;
	var g_startpoint = null;
	var g_endpoint = null;

	var g_tripstartx;
	var g_tripstarty;
	var g_tripendx;
	var g_tripendy;

	var g_firstcut = null;
	var g_secondcut = null;
	var g_firstcutpoint = null;
	var g_secondcutpoint = null;

	var g_tripMarkerArray;
	var g_firstcutIndex = null;
	var g_secondcutIndex = null;

	var g_cuttedtripArray = null;

	var g_default_user_checkin_path = "user/checkin/";

	function changeCursorToCut(object){
		var cursorOption="url('images/cut-icon.cur'), auto";
		if($('#map_canvas').gmap('get','map').draggableCursor!=cursorOption)
		{
			$("body").css("cursor",cursorOption);
			object.css("cursor",cursorOption);
			
			g_map = $('#map_canvas').gmap('get','map');
			g_map.setOptions({'draggableCursor':cursorOption});
			g_map.setOptions({draggable:false});
			//g_map.disableDragging();
			g_map.setOptions({ disableDoubleClickZoom: true});
			g_mapclickListener = google.maps.event.addListener(g_map, 'click', function (clickEvent) {			
				//alert("Please draw a line!");
				//clearAllDrawOnMap();
			});
			g_mapdblclickListener = google.maps.event.addListener(g_map, 'dblclick', function (clickEvent) {			

			});	

			g_mousedownListener = google.maps.event.addListener(g_map, 'mousedown', function(clickEvent) {
				//alert("mousedown");
				cutOnMousedown(clickEvent);
			 });
			g_dragstartListener = google.maps.event.addListener(g_map, 'dragstart', function(clickEvent) {
				//alert("dragstart");
				cutOnMousedown(clickEvent);
			 });
			g_mousemoveListener = google.maps.event.addListener(g_map, 'mousemove', function(clickEvent) {
				if(g_startpoint!=null){
					g_endpoint = clickEvent.latLng;
					cutOndrag(clickEvent);
				}
			  });
			g_dragListener = google.maps.event.addListener(g_map, 'drag', function(clickEvent) {
				if(g_startpoint!=null){
					g_endpoint = clickEvent.latLng;
					cutOndrag(clickEvent);
				}
			 });
			g_mouseupListener = google.maps.event.addListener(g_map, 'mouseup', function(clickEvent) {
				//alert("mouseup");
				cutOnMouseup(clickEvent);
			  });
			g_dragendListener = google.maps.event.addListener(g_map, 'dragend', function(clickEvent) {
				//alert("dragend");
				cutOnMouseup(clickEvent);
			 });
	
			 $('#map_canvas').mouseup(function() {
				cutOnMouseup2();
			});
			if(g_isForMobile==false)
			{
				MM_swapImage($('#img_seq_trip').attr('name'),'','images/b_hand.png',1);
			}
		}
		else{ // change cut cursor to default cursor
			$("body").css("cursor","default");
			object.css("cursor","pointer");

			changeCursorToDefaultMode();
		}
		
		//showGMap();
	}

	function changeCursorToDefaultMode(){
		if(g_map != null)
		{
//			alert("default");
			g_map = $('#map_canvas').gmap('get','map');
			g_map.setOptions({'draggableCursor':null});
			g_map.setOptions({ disableDoubleClickZoom: false});
			g_map.setOptions({draggable:true});

			google.maps.event.removeListener(g_mapclickListener);
			google.maps.event.removeListener(g_mapdblclickListener);
			google.maps.event.removeListener(g_mousedownListener);
			google.maps.event.removeListener(g_mousemoveListener);
			google.maps.event.removeListener(g_mouseupListener);
			google.maps.event.removeListener(g_dragstartListener);
			google.maps.event.removeListener(g_dragListener);
			google.maps.event.removeListener(g_dragendListener);
			g_startmarker = null;
			g_endmarker = null;
			g_cutPath = null;
			g_startpoint = null;
			g_endpoint = null;
			$('#map_canvas').mouseup(function() {
				;
			});
			if(g_isForMobile==false)
			{
				MM_swapImgRestore();
			}
		}
	}

	function placeNewMarker(mouse_location) {
	  var marker = new google.maps.Marker({
		  position: mouse_location,
		  icon: "images/placemarker_cut.png",
		  map: g_map
	  });

	/*	var circle = new google.maps.Circle({
		  center: mouse_location,
		  clickable: false,
		  radius: 200,
		  map: g_map,
		  strokeColor: "#ee4f4f",
		  strokeWidth: 2,
		  strokeOpacity: 1.0,
		  fillColor: "#428876",
		  fillOpacity: 0.5
		});*/


	 // g_map.setCenter(mouse_location);

	  return marker;
	}

	function placeNewMarker2(x, y, markfileName) {
	  if(markfileName==null){
		  markfileName = "placemarker_cut.png";
	  }
	  var marker = new google.maps.Marker({
		  position: new google.maps.LatLng(x,y),
		  icon: "images/" + markfileName,
		  map: g_map
	  });
	  return marker;
	}

	function setToDefaultMap(){
			if(g_firstcut){
				g_firstcut.setMap(null);
			}
			if(g_secondcut){
				g_secondcut.setMap(null);
			}		
			
			g_firstcutpoint = null;
			g_secondcutpoint = null;
			g_firstcutIndex = null;
			g_secondcutIndex = null;

			g_cuttedtripArray = null;

			clearAllDrawOnMap();
	}

	function clearAllDrawOnMap(){
			//g_startmarker.setMap(null);
			//g_endmarker.setMap(null);
			if(g_cutPath){
				g_cutPath.setMap(null);
			}

			g_startmarker=null;
			g_endmarker=null;
			g_endpoint = null;
			g_startpoint = null;
	}

	function cutOnMousedown(clickEvent){
		if(g_startmarker!=null)
		{
			//clearmarker();
			clearAllDrawOnMap();
		}
		//g_map.setOptions({draggable:false});
		g_startpoint = clickEvent.latLng;
		//g_startmarker = placeNewMarker(g_startpoint);
	}

	function getIntersectionOfTwoLine(x1, x2, y1, y2, x3, x4, y3, y4)
	{
		var A1 = y2 - y1;
		var B1 = x1 - x2;
		var C1 = A1*x1 + B1*y1;

		var A2 = y4 - y3;
		var B2 = x3 - x4;
		var C2 = A2*x3 + B2*y3;

		var cx = (B2*C1 - B1*C2) / (A1*B2 - A2*B1);
		var cy = (A1*C2 - A2*C1) / (A1*B2 - A2*B1);
		
		
		if (((cx >= Math.min(x1,x2)) && (cx <= Math.max(x1,x2)) && (cy >= Math.min(y1,y2)) && (cy <= Math.max(y1,y2))) && ((cx >= Math.min(x3,x4)) && (cx <= Math.max(x3,x4)) && (cy >= Math.min(y3,y4)) && (cy <= Math.max(y3,y4)))) {
			var matchP = new Array(2);
			matchP["x"] = cx;
			matchP["y"] = cy;
			return matchP;
		}
		else
		{
			return null;
		}
	}

	function cutOndrag(clickEvent){
	//	g_map.setOptions({draggable:true});
		if(g_cutPath!=null){
			g_cutPath.setMap(null);
			g_cutPath=null;
		}
		if(g_startpoint != null){
			//g_map.setCenter(g_startpoint);
			var CutCoordinates = [
				g_startpoint,
				clickEvent.latLng
			];

			g_cutPath = new google.maps.Polyline({
				path: CutCoordinates,
				strokeColor: "#00FFFF",
				strokeOpacity: 1.0,
				strokeWeight: 5
			});
			g_cutPath.setMap(g_map);
		}
	}

	function cutOnMouseup(clickEvent){
		//alert("up");
		//g_map.setOptions({draggable:true});
		g_endpoint = clickEvent.latLng;
		//g_endmarker = placeNewMarker(g_endpoint);
		cutOnMouseup2();
	}
	function cutOnMouseup2(){
		//g_endmarker = placeNewMarker(g_endpoint);
		//alert(g_tripstartx);
		var matchP = null;
		if(g_startpoint != null && g_endpoint != null)
		{
			for(var i=0;i<(g_tripPointArray.length-1);i++)
			{
				matchP = getIntersectionOfTwoLine(g_tripPointArray[i].lat(), g_tripPointArray[i+1].lat(), g_tripPointArray[i].lng(), g_tripPointArray[i+1].lng(), g_startpoint.lat(), g_endpoint.lat(), g_startpoint.lng(), g_endpoint.lng());

				if(matchP!=null)
				{
					if(g_firstcutpoint == null)
					{
						g_firstcutpoint = matchP;
						g_firstcutIndex = i;
						if(drawCuttedSection()!=-1){
							if(g_firstcut!=null)
							{
								g_firstcut.setMap(null);
							}
							var cut = placeNewMarker2(matchP.x, matchP.y, "placemarker_cut");
							g_firstcut = cut;
							g_secondcutpoint = null;
						}
						else{
							g_firstcutpoint = null;
						}
					}
					else if(g_secondcutpoint == null)
					{
						g_secondcutIndex = i;
						g_secondcutpoint = matchP;
						if(drawCuttedSection()!=-1){
							if(g_secondcut!=null)
							{
								g_secondcut.setMap(null);
							}
							var cut = placeNewMarker2(matchP.x, matchP.y, "placemarker_cut");
							g_secondcut = cut;
							g_firstcutpoint = null;
						}
						else{
							g_secondcutpoint = null;
						}
					}

					break;
				}
			}
		}
		if(matchP==null)
		{
			//alert("Please draw a line cross the trip");
		}
		else{

		}
		g_startpoint = null;
		clearAllDrawOnMap();
	}

	function getCuttedTrip(){
		return g_cuttedtripArray;
	}
	
	function drawCuttedSection(){
			if(g_firstcutIndex!=null && g_secondcutIndex!=null)
			{
				var tripPointArray = new Array(0);
				g_cuttedtripArray = new Array(0);
				var timeStampArray = new Array(0);
				var original_tripArray_left = new Array(0);
				var original_tripArray_right = new Array(0);
				var maxi, mini;
				if(g_secondcutIndex>g_firstcutIndex)
				{
					var index=1;
					maxi = g_secondcutIndex;
					mini = g_firstcutIndex;
					tripPointArray.push(new google.maps.LatLng(g_firstcutpoint.x, g_firstcutpoint.y));
					original_tripArray_left.push(new google.maps.LatLng(g_secondcutpoint.x, g_secondcutpoint.y));
					
					for(var i=0;i<(g_tripPointArray.length);i++)
					{
						if(i<=maxi && i>mini)
						{
							g_cuttedtripArray.push(g_tripArray[i]);
							tripPointArray.push(g_tripPointArray[i]);
							timeStampArray.push(g_tripTimeStampArray[i]);
						}
						else if(i>maxi){
							original_tripArray_left.push(g_tripPointArray[i]);
						}
						else if( i<=mini){
							original_tripArray_right.push(g_tripPointArray[i]);
						}
					}
					tripPointArray.push(new google.maps.LatLng(g_secondcutpoint.x, g_secondcutpoint.y));
					original_tripArray_right.push(new google.maps.LatLng(g_firstcutpoint.x, g_firstcutpoint.y));
				}
				else if(g_secondcutIndex<g_firstcutIndex)
				{
					var index=1;
					mini = g_secondcutIndex;
					maxi = g_firstcutIndex;
					tripPointArray.push(new google.maps.LatLng(g_secondcutpoint.x, g_secondcutpoint.y));
					original_tripArray_left.push(new google.maps.LatLng(g_firstcutpoint.x, g_firstcutpoint.y));
					for(var i=0;i<(g_tripPointArray.length);i++)
					{
						if(i<=maxi && i>mini)
						{
							g_cuttedtripArray.push(g_tripArray[i]);
							tripPointArray.push(g_tripPointArray[i]);
							timeStampArray.push(g_tripTimeStampArray[i]);
						}
						else if(i>maxi){
							original_tripArray_left.push(g_tripPointArray[i]);
						}
						else if( i<=mini){
							original_tripArray_right.push(g_tripPointArray[i]);
						}
					}
					tripPointArray.push(new google.maps.LatLng(g_firstcutpoint.x, g_firstcutpoint.y));
					original_tripArray_right.push(new google.maps.LatLng(g_secondcutpoint.x, g_secondcutpoint.y));
				}
				else
				{
					alert(g_str_cutwithoutpoint);
					return -1;
					var index=1;
					mini = g_secondcutIndex;
					maxi = g_firstcutIndex;
					original_tripArray_left.push(new google.maps.LatLng(g_secondcutpoint.x, g_secondcutpoint.y));
					for(var i=0;i<(g_tripPointArray.length);i++)
					{
						if(i>maxi){
							original_tripArray_left.push(g_tripPointArray[i]);
						}
						else if( i<mini){
							original_tripArray_right.push(g_tripPointArray[i]);
						}
					}
					original_tripArray_right.push(new google.maps.LatLng(g_firstcutpoint.x, g_firstcutpoint.y));
					tripPointArray.push(new google.maps.LatLng(g_firstcutpoint.x, g_firstcutpoint.y));
					tripPointArray.push(new google.maps.LatLng(g_secondcutpoint.x, g_secondcutpoint.y));
				}
				if(g_mapPath!=null)
				{
					g_mapPath.setMap(null);
				}
				if(g_cuttedPath!=null)
				{
					g_cuttedPath.setMap(null);
				}
				if(g_cuttedPath_left!=null)
				{
					g_cuttedPath_left.setMap(null);
				}
				if(g_cuttedPath_right!=null)
				{
					g_cuttedPath_right.setMap(null);
				}
				g_cuttedPath = new google.maps.Polyline({
					'strokeColor': "#FF0000",
					'strokeWeight': 10, 
					'path': tripPointArray
				});
				g_cuttedPath.setMap(g_map);
				var lineSymbol = {
				  path: 'M 0,-1 0,1',
				  strokeOpacity: 1,
				  scale: 4
				};
				g_cuttedPath_left = new google.maps.Polyline({
					'strokeColor': "#00FFFF", 
					'strokeWeight': 4, 
					'strokeOpacity': 0,
					  'icons': [{
						icon: lineSymbol,
						offset: '0',
						repeat: '20px'
					  }],
					'path': original_tripArray_left
				});
				g_cuttedPath_left.setMap(g_map);

				g_cuttedPath_right = new google.maps.Polyline({
					'strokeColor': "#00FFFF", 
					'strokeWeight': 4, 
					'strokeOpacity': 0,
					  'icons': [{
						icon: lineSymbol,
						offset: '0',
						repeat: '20px'
					  }],
					'path': original_tripArray_right
				});
				g_cuttedPath_right.setMap(g_map);

				UpdateCalculation();
				UpdateCuttedTripArray(tripPointArray);
			}
	}

	/* displayed picture orientation  as following
		  1           2                3      4         5               6                          7          8

	888888		888888	      88	88       8888888888   88                           88   8888888888
	88				      88		  88	88       88  88			 88  88                 88  88         88   88
	8888			   8888	   8888	8888    88               8888888888  8888888888               88
	88				      88		  88	88
	88				      88	888888  888888
	*/
	function albumcallback(sfile) { 
		//alert("EXIF: " + ImageInfo.getField(sfile, "exif").Orientation);
		var exifOrientation =ImageInfo.getField(sfile, "exif").Orientation;
		if(exifOrientation==3){
			$("img[src='"+sfile+"']").css("-moz-transform","rotate(180deg)");
			$("img[src='"+sfile+"']").css("-webkit-transform", "rotate(180deg)");
		}
		else if(exifOrientation==6){
			$("img[src='"+sfile+"']").css("-moz-transform","rotate(90deg)");
			$("img[src='"+sfile+"']").css("-webkit-transform", "rotate(90deg)");
		}
		else if(exifOrientation==8){
			$("img[src='"+sfile+"']").css("-moz-transform","rotate(-90deg)");
			$("img[src='"+sfile+"']").css("-webkit-transform", "rotate(-90deg)");
		}
	}

	function UpdateAlbum(){
		var content_index=1;
		var index=1;
		$("div[id*=images_scroll_content_]").html("");
		for(var i=0;i<g_tripMarkerArray.length;i++){
			var file = g_tripMarkerArray[i].getTitle();
			if(file==""){
				;//$("div[id=images_scroll_content_"+content_index+"]").append("<div><img id=scrollimg_"+i+" exif='true' src='images/takepicture.png' align='left bottom' class='class_imagesOfAlbum' onMouseOver=\"pointToMarkerOnMap("+i+")\" ></img></div><br/><br/>");
			}
			else{
				$("div[id=images_scroll_content_"+content_index+"]").append("<div><img id=scrollimg_"+i+" exif='true' src='"+file+"' align='left bottom' class='class_imagesOfAlbum' onMouseOver=\"pointToMarkerOnMap("+i+")\" ></img></div><br/><br/>");
				ImageInfo.loadInfo(file, albumcallback);
				index++;
				if(index>3){
					content_index++;
					index=1;
				}
			}
		}

		if(g_tripMarkerArray.length>0){
			setAlbumScroll();
		}

	}

	function pointToMarkerOnMap(i){
		var self =  $('#map_canvas').gmap('get','map');
		self.setCenter(g_tripMarkerArray[i].getPosition());
		OpenAllMarkerInfo(false);
		g_tripMarkerArray[i].openInfoWindow();
	}
//-->