<?php
	//require_once ('JSON.php');
	//$j = new Services_JSON(); 
	if ($handle = opendir("../".$_GET['path'])) {
		$return_data = array();
		$i=0;
		while (false !== ($file = readdir($handle))) {
			if (strpos( $file, '.JPG') || strpos( $file, '.jpg')){
				$return_data[$i++]=$file;
			}
		}
		$jsonString = json_encode($return_data);
		echo $jsonString;
		closedir($handle);
	}
	else{
		echo $handle."handle is null";
	}
?>