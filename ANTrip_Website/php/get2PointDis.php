<?php

	$lat1 = $_GET['lat1'];
	$lat2 = $_GET['lat2'];
	$lng1 = $_GET['lng1'];
	$lng2 = $_GET['lng2'];

	 $radLat1 = deg2rad($lat1);
	 $radLat2 = deg2rad($lat2);

	 $a = $radLat1 - $radLat2;
	 $b = deg2rad($lng1) - deg2rad($lng2);

	 $s = 2*asin(sqrt( pow(sin($a*0.5),2) + cos($radLat1)*cos($radLat2)*pow(sin($b*0.5),2) ));

	 $s = $s*6378137;
	echo $s;

	// return $s;

?>