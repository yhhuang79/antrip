<?php
		$tripArray =  $_POST['_tripArray'];
		$_unit = $_POST['_unit'];

		$MinToMicromin = 60000;
		$MinToMicromin_s = 1000;
		$MinToMicromin_m = 60000;
		$MinToMicromin_h = 3600000;

		//function calTrip($_tripArray, $_unit){

			$_tripArray = json_decode($tripArray);

		//echo $_tripArray[0]->lat;
		//echo "alert(".$_tripArray[0].lat.")";
		
			$dis=1;
			$table_dis = "";
			$unit = "m";
			
			$s = "$(\"tr[id=calculate_col]\").eq(0).html(\"<td></td>\");";
			
			$s .= "$(\"tr[id=altitude_table]\").eq(0).html(\"<th scope='row'>\"+g_str_altitude_table+\"</th>\");";
			$s .= "$(\"tr[id=speed_table]\").eq(0).html(\"<th scope='row'>\"+g_str_speed_table+\"</th>\");";
			$s .= "$(\"tr[id=checkin_table]\").eq(0).html(\"<th scope='row'>\"+g_str_checkin_table+\"</th>\");";

			$length = count($_tripArray)-1;
			$endTime = strtotime( $_tripArray[$length]->timestamp);
			//$s .= $endTime.";";
			$startTime = strtotime( $_tripArray[0]->timestamp);
			//$s .= $startTime.";";
			$interval = abs($endTime- $startTime)*1000;

			if($_unit!=null){
				$unit = $_unit;
				if($unit=="s"){
					$unit = "\"+g_str_s+\"";
					$MinToMicromin = $MinToMicromin_s;
					$table_dis = round(($interval/$MinToMicromin)/100);
				}
				else if($unit=="m"){
					$unit = "\"+g_str_m+\"";
					$MinToMicromin = $MinToMicromin_m;
					$table_dis = round(($interval/$MinToMicromin)/10);
				}
				else if($unit=="h"){
					$unit = "\"+g_str_h+\"";
					$MinToMicromin = $MinToMicromin_h;
					$table_dis = round(($interval/$MinToMicromin));
				}
			}
			else{
				if($interval<$MinToMicromin || ($interval/$MinToMicromin)<=2){
					$unit = "\"+g_str_s+\"";
					$MinToMicromin = $MinToMicromin_s;
					$table_dis = round(($interval/$MinToMicromin)/100);
					$s .= "$('#radio1').attr('checked', 'checked');";
				}
				else if(($interval/$MinToMicromin)>=200){
					$unit = "\"+g_str_h+\"";
					$MinToMicromin = $MinToMicromin_h;
					$table_dis = round(($interval/$MinToMicromin));
					$s .= "$('#radio3').attr('checked', 'checked');";
				}
				else{
					$unit = "\"+g_str_m+\"";
					$MinToMicromin = $MinToMicromin_m;
					$table_dis = round(($interval/$MinToMicromin)/10);
					$s .= "$('#radio2').attr('checked', 'checked');";
				}
			}		
			$index=1;
			$checkinNum = 0;
			for($i=0;$i<(count($_tripArray)-$dis);){
				$length = count($_tripArray)-1;
				$endTime = strtotime( $_tripArray[$i+$dis]->timestamp);
				$startTime = strtotime( $_tripArray[$i]->timestamp);
				$interval = abs($endTime- $startTime)*1000;

				if(($i+$dis) < count($_tripArray) && $_tripArray[$i+$dis-1]->CheckIn != 'undefined' && $_tripArray[$i+$dis-1]->CheckIn != null){
					$checkinNum++;
				}
				if($interval>$MinToMicromin){

					$lat1 = $_tripArray[$i]->lat;
					$lat2 = $_tripArray[$i+$dis]->lat;
					$lng1 = $_tripArray[$i]->lng;
					$lng2 = $_tripArray[$i+$dis]->lng;
					 $radLat1 = deg2rad($lat1);
					 $radLat2 = deg2rad($lat2);
					 $a = $radLat1 - $radLat2;
					 $b = deg2rad($lng1) - deg2rad($lng2);
					 $response = 2*asin(sqrt( pow(sin($a*0.5),2) + cos($radLat1)*cos($radLat2)*pow(sin($b*0.5),2) ));
					 $response = $response*6378137;

					$append_speedcontent = "<td>".round($response*3600/$interval)."</td>";
					$append_checkincontent = "<td>".$checkinNum."</td>";
					if($index%$table_dis!=0){
						$s .= "$(\"tr[id=calculate_col]\").eq(0).append(\"<th class='c_table_font' scope='col'>&nbsp;</th>\");";
					}
					else{
						$s .= "$(\"tr[id=calculate_col]\").eq(0).append(\"<th class='c_table_font' scope='col'>".$index.$unit."</th>\");";
					}
					$s .= "$(\"tr[id=speed_table]\").eq(0).append(\"".$append_speedcontent."\");";
					$s .= "$(\"tr[id=checkin_table]\").eq(0).append(\"".$append_checkincontent."\");";
					$index++;
					$checkinNum = 0;

					$i=$i+$dis;
					$dis=1;
				}
				else{
					$dis++;
				}
			}

			echo $s;

?>