google.load("earth", "1.x");

var ge;




//player related variables
var player1PM;
var player1Icon;
var player1Style;
var player1GPS;
var player1UserID;

//player ground overlay
var playerGO; 
var playerGOOpacity;
var playerGOOpacityInc; // opacity increment

var player1Lat, player1NextLat;
var player1Lon, player1Lon; 

//3d model
var player3DPM;
var playerModel;

var modelID;
var car1URL = 'http://earth-api-samples.googlecode.com/svn/trunk/demos/drive-simulator/smart.kmz';
var car0URL =
	'http://sketchup.google.com/3dwarehouse/download?mid=3c9a1cac8c73c61b6284d71745f1efa9&rtyp=zip&fn=milktruck&ctyp=milktruck';	  

var car2URL= 'http://ants.iis.sinica.edu.tw/PLASHTripPlayer/SF.zip';
var car3URL= 'http://ants.iis.sinica.edu.tw/PLASHTripPlayer/LP640.zip';
	                    

//global trip data list
var tripDataList;
var currentPtIndex; //the current point index of the trip data list 
var TDLLat;
var TDLLon;
var tripDataListLength; //size of trip data

//trip path line
var pathLine;
var pathLineMG; //path line multi-geometry
var pathLinePM; //path line place mark

//general control
var timeObj;
var currentState; //0 = stopped, not loaded, 1 = stopped, trip loaded, 2 = paused, 3 = playing

//variables for display loop

var step;
var projStep; //projected step, used in speed change
var counter;
var latInc, lonInc;
var playerHeading;
var projHeading; //heading that is projected to be reached 
var headingInc;



//initialization function
//Precondition: none
//Postcondition: create an google earth instance
var g_hashCode;
function ge_init(divCanvas, hashCode) {
  google.earth.createInstance(divCanvas, ge_initCallback, ge_failureCallback);
  g_hashCode = hashCode;
}//end function init

//initialization call back
//Precondition: none
//Postcondition: The following objects are initialized: Camera, tour route
function ge_initCallback(object) {

	ge = object;
	ge.getWindow().setVisibility(true);

	
	//current: 0 = stopped and trip not loaded
	currentState = 0;
	
	
	//set up view
	var default_cam = ge.createLookAt('');
	default_cam.set(25.041253, 
		121.614339,
		500, // altitude
		ge.ALTITUDE_CLAMP_TO_GROUND ,
		180, // heading
		60, // straight-down tilt
		200 // range (inverse of zoom)
	);
	//ge.getOptions().setFlyToSpeed(0.101);  
	ge.getOptions().setFlyToSpeed(ge.SPEED_TELEPORT);  
	ge.getView().setAbstractView(default_cam); //*/
	ge.getSun().setVisibility(true);
	
	//set up display loop
	
	step = 32;
	projStep = 32; 
	counter = 65536;
	playerHeading = 0;
	projHeading = 0;
	
	currentState = 0;
			
	//Layers
	ge.getLayerRoot().enableLayerById(ge.LAYER_TREES, true);
	ge.getLayerRoot().enableLayerById(ge.LAYER_BUILDINGS, true);	
	ge.getLayerRoot().enableLayerById(ge.LAYER_ROADS, true);	
	ge.getNavigationControl().setVisibility(ge.VISIBILITY_HIDE);
	

	

	//set up timer
	timeObj = (new Date()).getTime();
	
	//set up current trip data point index
	currentPtIndex = 0;
	TDLLat = new Array();
	TDLLon = new Array();
	
	//initialize player placemarks
	player1PM = ge.createPlacemark('');
	//iniitalize player icons and icon placeholder
	player1Icon = ge.createIcon('');
	player1Icon.setHref('http://plash2.iis.sinica.edu.tw/~ivanchiou/images/ant.png');
	player1Style = ge.createStyle('');
	//set them
	player1Style.getIconStyle().setIcon(player1Icon); //apply the icon to the style
	player1PM.setStyleSelector(player1Style); //apply the style to the placemark	
	// Set the placemark's location. 
	player1GPS = ge.createPoint('');		
	var randomLat = 25.04110 + Math.random()*0.0005;
	player1Lat = randomLat;
	player1GPS.setLatitude(randomLat);
	var randomLng = 121.614672 + Math.random()*0.0005;
	player1Lon = randomLng;
	player1GPS.setLongitude(randomLng);
	player1PM.setGeometry(player1GPS);
	//Set default player user id = -1
	player1UserID = -1;
	
	// Create the player GroundOverlay
	playerGO = ge.createGroundOverlay('');
	playerGO.setIcon(player1Icon);
	ge.getFeatures().appendChild(playerGO);	
	playerGOOpacity = 1;
	
	//set up line

	pathLine = ge.createLineString('');
	pathLine.setTessellate(true);
	pathLine.setAltitudeMode(ge.ALTITUDE_CLAMP_TO_GROUND);

	pathLineMG = ge.createMultiGeometry('');
	pathLineMG.getGeometries().appendChild(pathLine);

	
	pathLinePlacemark = ge.createPlacemark('');
	pathLinePlacemark.setGeometry(pathLineMG);
	
	pathLinePlacemark.setStyleSelector(ge.createStyle(''));
	var lineStyle = pathLinePlacemark.getStyleSelector().getLineStyle();
	lineStyle.setWidth(10);
	lineStyle.getColor().set('ff0000ff');
	
	ge.getFeatures().appendChild(pathLinePlacemark);		


					

	//3D player
	modelID = 0;
  	window.google.earth.fetchKml(ge, car0URL, function(obj) { fetchModelKMLCallback(obj); });	

	
	//start the display loop
	google.earth.addEventListener(ge,'frameend',generalLoop);	

	loadTripIntoPlayer(g_hashCode);
}//end function

//call back when google failed to create
//precondition
var testKMLPtr;
function ge_failureCallback(object) {
	alert("error has occurred");
}//end function


//callback function when kml is fetched	
//precondition: a KML object that is fetched
//postcondition: corresponding actions are performed
function fetchModelKMLCallback(fetchedKML) {
	  // console.log(" called?");

   // Alert if no KML was found at the specified URL.
   if (!fetchedKML) {
      setTimeout(function() {
         alert('Bad or null KML');
      }, 0);
      alert('Bad or null KML!!!');
      return;
   } else {
		// Add the fetched KML into this Earth instance.
		testKMLPtr = fetchedKML;
		ge.getFeatures().appendChild(testKMLPtr); //
  		//ge.getFeatures().appendChild(fetchedKML); 
 	}//fi
	var tmpPM;
   // Walk through the KML to find the  object
   walkKmlDom(fetchedKML, function() {   
	if (this.getType() == 'KmlPlacemark' &&
		this.getGeometry() &&
		this.getGeometry().getType() == 'KmlModel') {
			
			tmpPM = this;
		
		}//fi
   },false,true);		 

  // console.log(" same? " + (tmpPM == player3DPM));
   	

	playerModel = tmpPM.getGeometry();
	var loc = ge.createLocation(''); 
	loc.setLatitude(25.04110); 
	loc.setLongitude(121.614672); 
	playerModel.setLocation(loc); 
	

	var playerScale = ge.createScale('');
	
	if (modelID == 0) {
		playerScale.set(4,4,4);
	} else if (modelID == 1){
		playerScale.set(7,7,7);
	} else if (modelID == 2) {
		playerScale.set(3,3,3);
	} else if (modelID == 3) {
		playerScale.set(3,3,3);
	}//fi
	
	
	
	playerModel.setScale(playerScale);
	player3DPM = ge.createPlacemark(''); 
	player3DPM.setGeometry(playerModel);
	 //console.log(" finished?");	
	 
	ge.getFeatures().appendChild(player3DPM); 
     	  
}//end method */



//The general frame update listener
//Precondition: initial cam animation and logo are displayed and setup
//Postcondition: the master loop begins
function generalLoop(){

	if ((new Date()).getTime() - timeObj < 3 ) {		
		return;		
	}//fi
	if (currentState == 3) {
		updateDisplay();
		timeObj = (new Date()).getTime();
	}//fi

	

}//end listener	





//update player's location
//Preconditions: player gps data variables are created
//Postconditions: player gps data are read from server and set to the variable
//var baseURL = "https://plash.iis.sinica.edu.tw:8080/GetTripDataComponent?latest_pt_only=true&field_mask=0010000000000000&userid=";
function updateDisplay() {

	if (counter < step) {
		pathLine.getCoordinates().pushLatLngAlt(player1Lat, player1Lon, 0);
		pathLine.getCoordinates().pushLatLngAlt(player1Lat+latInc, player1Lon+lonInc, 0);			
		player1Lat += latInc;
		player1Lon += lonInc;			
		
		if (Math.abs(projHeading - playerHeading ) > Math.abs(headingInc)) {
			playerHeading += headingInc;
			if (playerHeading > 180 ) {
				playerHeading = -180;
				//alert("greater than one hundred and eighty degrees occured!!");
			} else if ( playerHeading < -180) {
				playerHeading = 180;
				//alert("less than negative one hundred and eighty degrees occured!!");
			}//end if
		}//fi//*/
		counter++;
		
	} else {
		step = projStep;
		counter = 0;
		//get new lat lon pair
		player1Lat = TDLLat[currentPtIndex];
		player1NextLat = TDLLat[currentPtIndex+1];
		latInc = (player1NextLat-player1Lat)/step;
		
		player1Lon = TDLLon[currentPtIndex];		
		player1NextLon = TDLLon[currentPtIndex+1];		
		lonInc = (player1NextLon-player1Lon)/step;	
		
		
		
		var tmpHeading = getBearing(player1Lat, player1Lon, player1NextLat, player1NextLon);
		if (tmpHeading != -999) {
			projHeading = tmpHeading;			
		} //fi

		
		if ((projHeading - playerHeading) > 180) {
			headingInc = (projHeading - playerHeading -360)/ step * 3;
			//alert("turning other way!!");
		} else if ((projHeading - playerHeading) < -180) {
			headingInc = (projHeading - playerHeading + 360 )/ step * 3;
			//alert("turning other way!!");
		} else {
			headingInc = (projHeading - playerHeading) / step * 3;

		}//fi */
		
		playerHeading += headingInc;

		
		if (currentPtIndex < tripDataLength-1) {
			currentPtIndex ++;
		} else {
			currentPtIndex = 0;
			
		}//fi */


	}//fi


	var latLonBox = ge.createLatLonBox('');
		
	latLonBox.setBox(player1Lat+0.0002, player1Lat-0.0002, player1Lon+0.0002, player1Lon-0.0002, playerHeading);
	playerGO.setLatLonBox(latLonBox);
	
	var loc = ge.createLocation(''); 
	loc.setLatitude(player1Lat); 
	loc.setLongitude(player1Lon); 
	playerModel.setLocation(loc); 
	playerModel.setLocation(loc); 

	var playerOrientation = ge.createOrientation('');
	playerOrientation.setHeading(-playerHeading);
	playerModel.setOrientation(playerOrientation);		
		
	if (modelID == 0) {

	} else if (modelID == 1){

	} else if (modelID == 2) {
		playerOrientation.setHeading(-playerHeading);	
		
	} else if (modelID == 3) {
		playerOrientation.setHeading(-playerHeading-90);		
			
	}//fi

	
	new_cam = ge.getView().copyAsLookAt(ge.ALTITUDE_ABSOLUTE );		
	new_cam.setLatitude(player1Lat); 
	new_cam.setLongitude(player1Lon); 
	ge.getView().setAbstractView(new_cam); 


	//car glow
	if (playerGOOpacity > 0.95) {
		playerGOOpacityInc = -0.05;
	} else if (playerGOOpacity < 0.05) {
		playerGOOpacityInc = 0.05;
	}//end if
	playerGOOpacity += playerGOOpacityInc;
	playerGO.setOpacity(playerGOOpacity);
	
	
	

}//end method



//start the player
//Preconditions: Google earth instance is created. two arguments: tmpUserID, tmpTripID
//Postconditions: trip data read according to tmpUserID, tmpTripID
function startPlayer() {
	
	
	if(currentState == 0 ) {
		alert("Please load a trip first.");
		return;	
	} else if (currentState == 3 ) {		
		alert("The player is already playing! Please reset first.");
		return;	
	} else {
		currentState = 3;
		updateDisplay();
	}//fi

	
}//fi

//pause
//Precondition: none
//Postcondition: reset player
function stopPlayer() {
	if(currentState != 3 && currentState != 2 ) {
		alert("The player is not playing.");
		return;			
   } else {
   	currentState = 1;
   	currentPtIndex = 0;
   	counter = 65535;
   	
		pathLineMG.getGeometries().removeChild(pathLine);
		
		pathLine = ge.createLineString('');
		pathLine.setTessellate(true);
		pathLine.setAltitudeMode(ge.ALTITUDE_CLAMP_TO_GROUND);
		
		
		pathLineMG.getGeometries().appendChild(pathLine);
		updateDisplay();

   	
   }//fi
			
}//end method

//pause
//Precondition: none
//Postcondition: reset player
function pausePlayer() {

	if(currentState == 3 || currentState == 2 ) {
		currentState = 2;		
   } else {

		return;	
   }//fi
			
}//end method


//reset
//Precondition: none
//Postcondition: reset player
function refreshPlayer() {

	document.location.reload(true);

			
}//end method


//prepare and load trip
//precondition: two arguments: user id and trip id
//postcondition: trip data loaded
function loadTripIntoPlayer(tmphashCode) {
	
	if (currentState != 0 ){
		//TDLLat = new Array();
		//TDLLon = new Array();
		startPlayer();
		return;
	} else {
		
	}//fi
		
	if (tmphashCode != "") {
		//alert("Now retrieving trip with user id: " + tmpUserID + " and trip id: " + tmpTripID +
		//"\nWarning: erroneous user id and/or trip id may crash this program!"
		//);
	} else {
		player1UserID = -1;
		alert("Bad user id and trip id !!!");
		return;
	}//fi
	
	var my_JSON_object = {};
	//var baseURL = "https://plash3.iis.sinica.edu.tw:8080/GetTripDataComponent?field_mask=0100000000000000";
	//var reqURL = baseURL + "&userid=" + tmpUserID + "&trip_id=" + tmpTripID;
	var baseURL = "http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php";
	var reqURL = baseURL + "?hash=" + tmphashCode;
	var http_request = new XMLHttpRequest();
	

	
	http_request.open("GET", reqURL, true);
	 
	http_request.onreadystatechange = function () {
		if (http_request.readyState == 4 && http_request.status == 200) {			
		
		  my_JSON_object = JSON.parse(http_request.responseText);
											
		  window.tripDataList = my_JSON_object.CheckInDataList;	
		  tripDataList = my_JSON_object.CheckInDataList;
		  tripDataLength = tripDataList.length;
			for (var tmpInx = 0; tmpInx < tripDataLength; tmpInx++) {
				//alert(tripDataList[tmpInx].lat);
				tripDataList[tmpInx].lat = tripDataList[tmpInx].lat.valueOf() / 1000000;
				tripDataList[tmpInx].lng = tripDataList[tmpInx].lng.valueOf() / 1000000;
				if (Math.abs(tripDataList[tmpInx].lat) > 180 || Math.abs(tripDataList[tmpInx].lng) > 180  ) {
					continue;
				}//fi
				TDLLat.push(tripDataList[tmpInx].lat);
				TDLLon.push(tripDataList[tmpInx].lng);				
			}//rof

		updateDisplay();	
		currentState = 1;
		startPlayer();
		}//fi
	};

	http_request.send(null);
	
									
}//end method

//adjust speed
function adjustSpeed(newSpeed) {
	
	projStep = newSpeed;
	
}//

//change player model
function changePlayerModel(newModelID) {
	ge.getFeatures().removeChild(player3DPM); 
	ge.getFeatures().removeChild(testKMLPtr); 

	if (newModelID == 0 ) {
		window.google.earth.fetchKml(ge, car0URL, function(obj) { fetchModelKMLCallback(obj); });	
	} else if (newModelID == 1) {
		window.google.earth.fetchKml(ge, car1URL, function(obj) { fetchModelKMLCallback(obj); });	
	} else if (newModelID == 2) {
		window.google.earth.fetchKml(ge, car2URL, function(obj) { fetchModelKMLCallback(obj); });				
	} else if (newModelID == 3) {
		window.google.earth.fetchKml(ge, car3URL, function(obj) { fetchModelKMLCallback(obj); });				
	}//fi
	
	//*/
	switch (modelID) {
			
		case 0:
		  	//window.google.earth.fetchKml(ge, car0URL, function(obj) { fetchModelKMLCallback(obj); });		
			break;
		case 1:
	//	console.log("fffff callled???? " + newModelID);
		  	//window.google.earth.fetchKml(ge, car1URL, function(obj) { fetchModelKMLCallback(obj); });		
		  	  	loadKMLTest(ge, car1URL);	
			break;
		case 2:
		  	loadKMLTest(ge, car2URL);	
		 // 	window.google.earth.fetchKml(ge, car2URL, function(obj) { fetchModelKMLCallback(obj); });		
			break;
		default:
			break;
	}//end switch */
	modelID = newModelID;
}//end method

//Get bearing
//precondition: 2 points, so 2 lat and 2 lon argument
//postcondition: a bearing is returned
function getBearing(pt1Lat, pt1Lon, pt2Lat, pt2Lon) {

	
	var xComponent = pt2Lat-pt1Lat;
	var yComponent = pt2Lon-pt1Lon;	

	if (xComponent == 0 && yComponent == 0) {
		return -999;
	}; 
	//
	var dLon = yComponent*Math.PI/180;
	var tmpY = Math.sin(dLon) * Math.cos(pt2Lat);
	var tmpX = Math.cos(pt1Lat)*Math.sin(pt2Lat) -  Math.sin(pt1Lat)*Math.cos(pt2Lat)*Math.cos(dLon);
	var bearingRad = Math.atan2(tmpY, tmpX);	
	var bearingDeg = bearingRad*180/Math.PI;
	//alert("GPS and bearing : (" + pt1Lat  + " : " + pt1Lon  + ") , (" + pt2Lat  + " : " + pt2Lon  + ") : " + bearingDeg + " : " + ans2 );
	//return bearingDeg;
	
	// new method
	
	var tmp1 = Math.sin(pt1Lon-pt2Lon)*Math.cos(pt2Lat);
	var tmp2 = Math.cos(pt1Lat)*Math.sin(pt2Lat)-Math.sin(pt1Lat)*Math.cos(pt2Lat)*Math.cos(pt1Lon-pt2Lon);
	var tmp3 = Math.atan2(tmp1,tmp2);
	var ans2 = tmp3 % Math.PI;
	return ans2*180/Math.PI;

	
	//*/
	
}//end method */