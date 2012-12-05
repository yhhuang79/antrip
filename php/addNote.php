<?php

  $username="postgres";
  $password="root";
  $database="postgistemplate";

 // $lusername=$_POST['username'];
 // $lpassword=$_POST['password'];
  $userid = $_POST['sid'];
  $content= urldecode($_POST['content']);
  $trip_id = $_POST['trip_id'];
  $lat=$_POST['lat'];
  $lng = $_POST['lng'];

	if ($_FILES["file"]["error"] > 0) {
		$msg = array('message'=> $cresponse->{'message'}, 'result'=> $_FILES["file"]["error"]);
		echo json_encode($msg);
	} else if($_FILES["file"]["name"]!=""){
		$dirname = "{$userid}/{$trip_id}/";
		if (file_exists($dirname)) {
		} else {
			mkdir($userid . "/", 0777);
			mkdir($dirname, 0777);
		}
		if (file_exists($dirname . $_FILES["file"]["name"])) {
			//$msg = array('message'=> $cresponse->{'message'}, 'result'=>  $_FILES["file"]["name"] . " already exists.");
			//echo json_encode($msg);
		} else {
			move_uploaded_file($_FILES["file"]["tmp_name"], $dirname . $_FILES["file"]["name"]);
			$url = "http://plash2.iis.sinica.edu.tw/picture/" . $dirname . $_FILES["file"]["name"];
			$msg = array('message'=> $cresponse->{'message'}, 'result'=> $cresponse->{'result'}, 'url'=> "http://plash2.iis.sinica.edu.tw/picture/" . $dirname . $_FILES["file"]["name"]);
		}
	}

  $link = pg_Connect("host=140.109.18.129 dbname='$database' user='$username' password='$password'");
  if($link && ($content || $_FILES["file"]["name"])){
			$filename = $_FILES["file"]["name"];
			if($filename!=""){
				$result = pg_exec($link, "INSERT INTO user_location.extras (userid, trip_id, latitude, longitude, text, type, uri) VALUES ('$userid', '$trip_id', '$lat', '$lng', '$content', '1', '$filename')");
			}
			else{
				$result = pg_exec($link, "INSERT INTO user_location.extras (userid, trip_id, latitude, longitude, text, type, uri) VALUES ('$userid', '$trip_id', '$lat', '$lng', '$content', '1', NULL)");
			}
		  
			if($result==null)
				echo 0;
			else{
				$resultSelect = pg_exec($link, "SELECT record_id FROM user_location.extras WHERE userid='$userid' AND trip_id='$trip_id' AND type='1' ORDER BY record_id DESC LIMIT 1");
				
				if($resultSelect){
						$rowSelect = pg_fetch_array($resultSelect, 0);
						if($rowSelect==null){
							echo 0;
						}
						else{
							echo $rowSelect[0];
						}
				}
				else{
					echo 0;
				}
			}
  	pg_close($link);
  }
?>
