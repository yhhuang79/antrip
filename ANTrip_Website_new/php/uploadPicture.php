<?php
$userid = $_POST["userid"];
$trip_id = $_POST["trip_id"];

if ($_FILES["file"]["error"] > 0) {
	$msg = array('message'=> $cresponse->{'message'}, 'result'=> $_FILES["file"]["error"]);
	echo json_encode($msg);
} else {
	$dirname = "{$userid}/{$trip_id}/";
	if (file_exists($dirname)) {
	} else {
		mkdir($userid . "/", 0777);
		mkdir($dirname, 0777);
	}
	if (file_exists($dirname . $_FILES["file"]["name"])) {
		$msg = array('message'=> $cresponse->{'message'}, 'result'=>  $_FILES["file"]["name"] . " already exists.");
		echo json_encode($msg);
	} else {
		move_uploaded_file($_FILES["file"]["tmp_name"],
		$dirname . $_FILES["file"]["name"]);
		$url = "http://plash2.iis.sinica.edu.tw/picture/" . $dirname . $_FILES["file"]["name"];
		$msg = array('message'=> $cresponse->{'message'}, 'result'=> $cresponse->{'result'}, 'url'=> "http://plash2.iis.sinica.edu.tw/picture/" . $dirname . $_FILES["file"]["name"]);
		echo json_encode($msg);
	}
}

?>

