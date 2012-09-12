<!--	
		var MinToMicromin = 60000;
		var MinToMicromin_s = 1000;
		var MinToMicromin_m = 60000;
		var MinToMicromin_h = 3600000;

		function calTrip(_unit){
			var dis=1;
			var table_dis = "";
			var _tripArray = getCuttedTrip();
			if(getCuttedTrip()!=null){
				 _tripArray = getCuttedTrip();
			}
			else{
				 _tripArray = g_tripArray;
			}

			var unit = "m";
			
			$("tr[id=calculate_col]").eq(0).html("<td></td>");
			$("tr[id=speed_table]").eq(0).html("<th scope='row'>Speed(unit: km/h)</th>");
			$("tr[id=checkin_table]").eq(0).html("<th scope='row'>Check-in</th>");

			var interval = Math.abs(Date.parse(_tripArray[_tripArray.length-1].timestamp) - Date.parse(_tripArray[0].timestamp));
			if(_unit!=null){
				unit = _unit;
				if(unit=="s"){
					MinToMicromin = MinToMicromin_s;
					table_dis = Math.round((interval/MinToMicromin)/100);
				}
				else if(unit=="m"){
					MinToMicromin = MinToMicromin_m;
					table_dis = Math.round((interval/MinToMicromin)/10);
				}
				else if(unit=="h"){
					MinToMicromin = MinToMicromin_h;
					table_dis = Math.round((interval/MinToMicromin));
				}
			}
			else{
				if(interval<MinToMicromin || (interval/MinToMicromin)<=2){
					unit = "s";
					MinToMicromin = MinToMicromin_s;
					table_dis = Math.round((interval/MinToMicromin)/100);
					$('input:radio')[0].checked = 'checked';
				}
				else if((interval/MinToMicromin)>=200){
					unit = "h";
					MinToMicromin = MinToMicromin_h;
					table_dis = Math.round((interval/MinToMicromin));
					$('input:radio')[2].checked = 'checked';
				}
				else{
					unit = "m";
					MinToMicromin = MinToMicromin_m;
					table_dis = Math.round((interval/MinToMicromin)/10);
					$('input:radio')[1].checked = 'checked';
				}
			}		
			var index=1;
			var checkinNum = 0;
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
							var append_speedcontent = "<td>"+Math.round(response*3600/interval)+"</td>";
							var append_checkincontent = "<td>"+checkinNum.toString()+"</td>";
							if(index%table_dis!=0){
								$("tr[id=calculate_col]").eq(0).append("<th class='c_table_font' scope='col'>&nbsp;</th>");
							}
							else{
								$("tr[id=calculate_col]").eq(0).append("<th class='c_table_font' scope='col'>"+index+unit+"</th>");
							}
							$("tr[id=speed_table]").eq(0).append(append_speedcontent);
							$("tr[id=checkin_table]").eq(0).append(append_checkincontent);
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
		}
		
		calTrip(g_unit);
		
//-->