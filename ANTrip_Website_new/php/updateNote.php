<?php

  $username="postgres";
  $password="root";
  $database="postgistemplate";

  $lusername=$_GET['username'];
  $lpassword=$_GET['password'];
  $userid = $_GET['sid'];
  $trip_id = $_GET['trip_id'];
  $lat=$_GET['lat'];
  $lng = $_GET['lng'];
  $record_id = $_GET['record_id'];

  $link = pg_Connect("host=140.109.18.129 dbname='$database' user='$username' password='$password'");
  if($link){

			$result = pg_exec($link, "UPDATE user_location.extras SET latitude='$lat', longitude='$lng' WHERE userid='$userid' AND trip_id='$trip_id' AND type='1' AND record_id='$record_id'");
			if($result==null)
				echo 0;
			else
				echo 1;

  	pg_close($link);
  }

?>
