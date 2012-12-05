<?php
$userid = $_POST["userid"];
$trip_id = $_POST["trip_id"];

$image = $_POST["data"];

  //  $image = base64_decode( str_replace('data:image/png;base64,', '',$data); 

	$dirname = "{$userid}/{$trip_id}/thumb/";
	if (file_exists($dirname)) {
	} else {
		mkdir($userid . "/", 0777);
		mkdir("{$userid}/{$trip_id}/", 0777);
		mkdir($dirname, 0777);
	}
	$filename = $dirname."thumb.jpg";
    $fp = fopen($filename, 'w');  
    fwrite($fp, $image);  
    fclose($fp);  

?>