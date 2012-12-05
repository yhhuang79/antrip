<?php

  $username="postgres";
  $password="root";
  $database="postgistemplate";

  $lusername=$_GET['username'];
  $lpassword=$_GET['password'];
  $userid = $_GET['sid'];
  $trip_id = $_GET['trip_id'];

  $link = pg_Connect("host=140.109.18.129 dbname='$database' user='$username' password='$password'");
  if($link){
  	$result = pg_exec($link, "SELECT text FROM user_location.extras WHERE userid='$userid' AND trip_id='$trip_id' AND type='3'");
  
  	if($result==null)
		echo 0;
  	else
  		//$row = pg_fetch_array($result, 0);
		//$row = null;
		$resultArray = array();
		$index=0;
		while ($row = pg_fetch_array($result, null, PGSQL_ASSOC)) {
			$resultArray[$index++] = $row;
		}
  		if(count($resultArray)<=0)
			echo 0;
  		else{
			echo json_encode($resultArray);
			// Free resultset
			pg_free_result($result);
		}
  	pg_close($link);
  }

?>
