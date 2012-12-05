<?php

  $username="postgres";
  $password="root";
  $database="postgistemplate";

  $lusername=$_GET['username'];
  $lpassword=$_GET['password'];
  $userid = $_GET['sid'];
  $content=$_GET['content'];
  $trip_id = $_GET['trip_id'];

  $link = pg_Connect("host=140.109.18.129 dbname='$database' user='$username' password='$password'");
  if($link){
	$resultSelect = pg_exec($link, "SELECT * FROM user_location.extras WHERE userid='$userid' AND trip_id='$trip_id' AND type='2' AND text='$content'");
	if($resultSelect){
		$rowSelect = pg_fetch_array($resultSelect, 0);
		if($rowSelect == null){
			$result = pg_exec($link, "INSERT INTO user_location.extras (userid, trip_id, latitude, longitude, text, type) VALUES ('$userid', '$trip_id', null, null, '$content', '2')");
		  
			if($result==null)
				echo 0;
			else
				$row = pg_fetch_array($result, 0);
				if($row==null)
					echo 0;
				else
					echo 1;
		}
	}
  	pg_close($link);
  }

?>
