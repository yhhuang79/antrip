<!--
	var g_tripArray=null;
	function UpdateCalculation(_unit){
		var MinToMicromin = 60000;
		var MinToMicromin_s = 1000;
		var MinToMicromin_m = 60000;
		var MinToMicromin_h = 3600000;
		var dis=1;
		var table_dis = "";
		var _tripArray;
		var callframe = top.frames['tripMapFrame'];
		if(callframe!=null && typeof callframe.getCuttedTrip== 'function' && callframe.getCuttedTrip()!=null){
			 _tripArray = callframe.getCuttedTrip();
		}
		else{
			 _tripArray = g_tripArray;
		}

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
				var responseData = eval(result);
				drawVisualization(responseData);
			}
		});
	}
	var g_pointIndex = null;
	function drawVisualization(dataFromAjax){
        var data = google.visualization.arrayToDataTable(dataFromAjax);
		
		var calwidth =document.body.clientWidth;
		var calheight = document.body.clientHeight;
		if(isPCMacOS()==false){
			calheight = window.innerHeight*20/100;
		}
		var callframe = top.frames['tripMapFrame'];
		if(callframe==null){
			calwidth = '800';
			calheight = '200';
		}
        // Create and draw the visualization.
        var ac = new google.visualization.ComboChart(document.getElementById('visualization'));
        ac.draw(data, {
          isStacked: true,
          width: calwidth,
          height: calheight,
		  backgroundColor: 'transparent',
          hAxis: {title: dataFromAjax[0][0]},
		  vAxes: {0: {title:g_str_altitude_table + "/"+ g_str_speed_table, textStyle:{color: 'blue'}}, 1: {title:g_str_checkin_table, textStyle:{color: 'red'}}},
          seriesType: "line",
          series: {1: {type: "bars", targetAxisIndex :1}}
        });
		//mapping google chart mouse over to trip path
		google.visualization.events.addListener(ac, 'onmouseover', usefulHandler);
		 function usefulHandler(e) {
			var myView = new google.visualization.DataView(data);
			var callframe = top.frames['tripMapFrame'];
			if(e.column==2){
				var i =1, j=2; // this column 2 is for check in value
				var pointIndex =0;
				for(;i<=e.row+1;i++){
					if(dataFromAjax[i][j]>0){
						pointIndex += dataFromAjax[i][j];
					}
				}
				if(pointIndex>0){
					pointIndex--;
					if(callframe!=null){
						callframe.OpenAllMarkerInfo(false);
						callframe.g_tripMarkerArray[pointIndex].openInfoWindow(true);
					}
					else{
						OpenAllMarkerInfo(false);
						g_tripMarkerArray[pointIndex].openInfoWindow(true);
					}
					g_pointIndex = pointIndex; // save index of checkin during mouse over. it is used for mouse click to open picture window
				}
			}
			else{
				if(callframe!=null){
					var pointIndex = Math.round((e.row/dataFromAjax.length)*callframe.g_tripPointMarkerArray.length);
					callframe.OpenAllPointInfo(false);
					callframe.g_tripPointMarkerArray[pointIndex].openInfoWindow(true);
				}
				else{
					var pointIndex = Math.round((e.row/dataFromAjax.length)*g_tripPointMarkerArray.length);
					OpenAllPointInfo(false);
					g_tripPointMarkerArray[pointIndex].openInfoWindow(true);
				}	
			}
		}
		google.visualization.events.addListener(ac, 'select', function (e) {
			var sel = ac.getSelection();
			if(sel && sel[0] && sel[0].column==2 && g_pointIndex !=null){
				if(callframe!=null){
					window.open(callframe.g_tripMarkerArray[g_pointIndex].getTitle(),"_blank ");
				}
				else{
					window.open(g_tripMarkerArray[g_pointIndex].getTitle(),"_blank ");
				}
			}
		});
	}


//	});
//-->