<!--
	var g_tripArray=null;
//	$("#map_canvas").ready(function(){
	function UpdateCalculation(_unit){
		var MinToMicromin = 60000;
		var MinToMicromin_s = 1000;
		var MinToMicromin_m = 60000;
		var MinToMicromin_h = 3600000;
		var dis=1;
		var table_dis = "";
		var _tripArray;
		if(typeof getCuttedTrip== 'function' && getCuttedTrip()!=null){
			 _tripArray = getCuttedTrip();
		}
		else{
			 _tripArray = g_tripArray;
		}
	/*	var unit = "m";	
		var interval = Math.abs(Date.parse(_tripArray[_tripArray.length-1].timestamp) - Date.parse(_tripArray[0].timestamp));
		if(_unit!=null){
			unit = _unit;
			if(unit=="s"){
				unit = g_str_s;
				MinToMicromin = MinToMicromin_s;
				table_dis = Math.round((interval/MinToMicromin)/100);
			}
			else if(unit=="m"){
				unit = g_str_m;
				MinToMicromin = MinToMicromin_m;
				table_dis = Math.round((interval/MinToMicromin)/10);
			}
			else if(unit=="h"){
				unit = g_str_h;
				MinToMicromin = MinToMicromin_h;
				table_dis = Math.round((interval/MinToMicromin));
			}
		}
		else{
			if(interval<MinToMicromin || (interval/MinToMicromin)<=2){
				unit = g_str_s;
				MinToMicromin = MinToMicromin_s;
				table_dis = Math.round((interval/MinToMicromin)/100);
				$('input:radio')[0].checked = 'checked';
			}
			else if((interval/MinToMicromin)>=200){
				unit = g_str_h;
				MinToMicromin = MinToMicromin_h;
				table_dis = Math.round((interval/MinToMicromin));
				$('input:radio')[2].checked = 'checked';
			}
			else{
				unit = g_str_m;
				MinToMicromin = MinToMicromin_m;
				table_dis = Math.round((interval/MinToMicromin)/10);
				$('input:radio')[1].checked = 'checked';
			}
		}		
		var index=1;
		var checkinNum = 0;
		var s = "[";
		s += "['"+unit+"', '"+g_str_altitude_table+"', '"+g_str_checkin_table+"', '"+g_str_speed_table+"']";
		for(var i=0;i<(_tripArray.length-dis);){
			var interval = Math.abs(Date.parse(_tripArray[i+dis].timestamp) - Date.parse(_tripArray[i].timestamp));
			if((i+dis) < _tripArray.length &&typeof _tripArray[i+dis-1].CheckIn != 'undefined' && _tripArray[i+dis-1].CheckIn != null){
				checkinNum++;
			}
			if(interval>MinToMicromin){
				$.ajax({
					url: 'php/get2PointDis.php',
					async: false,
					data: {lat1: _tripArray[i].lat, lat2: _tripArray[i+dis].lat, lng1: _tripArray[i].lng, lng2: _tripArray[i+dis].lng},
					dataType:'json',
					type: 'GET',
					error: function(xhr) {
						alert('Ajax request errors');
					},
					success: function(response) {
						var append_speedcontent = Math.round(response*3600/interval);
						var append_checkincontent = checkinNum.toString();
						var alt1 = 0;
						var alt2 = 0;
						if(_tripArray[i].alt != 'undefined' && _tripArray[i].alt != null){
							alt1 = _tripArray[i].alt;
						}
						if(_tripArray[i+dis].alt != 'undefined' && _tripArray[i+dis].alt != null){
							alt2 = _tripArray[i+dis].alt;
						}
						var append_alt = (alt1 + alt2)/2;
						s += ", ["+index+", "+append_alt+", "+append_checkincontent+", "+append_speedcontent+"]";
						index++;
						checkinNum = 0;
					}
				});
				i=i+dis;
				dis=1;
			}
			else{
				dis++;
			}
		}
		s += "]";
		//alert(s);
		drawVisualization(eval(s));*/
		$.ajax({
			url: 'php/getCalData.php',
			async: false,
			data:{_tripArray :  JSON.stringify(_tripArray), _unit: _unit},
			dataType:'html',
			type: 'POST',
			error: function(xhr) {
				alert('Ajax request errors');
			},
			success:function(result){
				//alert(result);
				//eval(result);
				//$('.visualize').trigger('visualizeRefresh');
				var responseData = eval(result);
				//alert(responseData);
				drawVisualization(responseData);
			}
		});
	}

	function drawVisualization(dataFromAjax){
		// Some raw data (not necessarily accurate)
		//alert("drawVisualization");
		//alert(dataFromAjax);
        var data = google.visualization.arrayToDataTable(dataFromAjax);
      
        // Create and draw the visualization.
        var ac = new google.visualization.LineChart(document.getElementById('visualization'));
        ac.draw(data, {
          isStacked: true,
          width: 800,
          height: 200,
		  backgroundColor: 'transparent',
          hAxis: {title: dataFromAjax[0][0]}
        });
	}


//	});
//-->