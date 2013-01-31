<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<meta property="og:type" content="site" /> 
	<?php $url = "http://plash2.iis.sinica.edu.tw/api/GetCheckInData.php";  $para = "?hash=".$_GET['hash']; $ch = curl_init(); curl_setopt($ch, CURLOPT_URL,$url.$para); curl_setopt ($ch, CURLOPT_RETURNTRANSFER, 1); curl_setopt ($ch, CURLOPT_CONNECTTIMEOUT,10); $result= curl_exec ($ch);  curl_close ($ch);  $json = json_decode(utf8_encode($result), true);
	foreach( $json['CheckInDataList'] as $obj ){ if($obj['CheckIn']) {echo "<meta property=\"og:image\" content=\"http://plash2.iis.sinica.edu.tw/picture/".$json["userid"]."/".$json['trip_id']."/thumb.s/".$obj['CheckIn']['picture_uri']."\" />\r\n";} } ?>

	<meta property="og:title" content="ANTrip雲水途誌:<?php echo utf8_decode($json['tripName']); ?>"/>
	<?php $url = "http://plash2.iis.sinica.edu.tw/antrip/php/getNote.php";  $para = "?sid=".$json["userid"]."&trip_id=".$json["trip_id"]; $ch = curl_init(); curl_setopt($ch, CURLOPT_URL,$url.$para); curl_setopt ($ch, CURLOPT_RETURNTRANSFER, 1); curl_setopt ($ch, CURLOPT_CONNECTTIMEOUT,10); $result= curl_exec ($ch);  curl_close ($ch);  $json_s = json_decode(utf8_encode($result), true); foreach( $json_s as $obj ){ if($obj['uri']) {echo "<meta property=\"og:image\" content=\"http://plash2.iis.sinica.edu.tw/picture/".$json["userid"]."/".$json['trip_id']."/".$obj['uri']."\" />\r\n";} } ?>
	<meta property="og:image" content="http://plash2.iis.sinica.edu.tw/antrip/images/banner_logo.png" />
	<meta property="og:site_name" content="ANTrip雲水途誌" />
	<?php $url = "http://plash2.iis.sinica.edu.tw/antrip/php/getDescription.php";  $para = "?sid=".$json["userid"]."&trip_id=".$json["trip_id"]; $ch = curl_init(); curl_setopt($ch, CURLOPT_URL,$url.$para); curl_setopt ($ch, CURLOPT_RETURNTRANSFER, 1); curl_setopt ($ch, CURLOPT_CONNECTTIMEOUT,10); $result= curl_exec ($ch);  curl_close ($ch);  $json_d = json_decode(utf8_encode($result), true); foreach( $json_d as $obj ){ if($obj['text']) {echo "<meta property=\"og:description\" content=\"".$obj['text']."\" />\r\n";} } ?>
	<title>ANTrip雲水途誌</title>
	<!--Adobe Edge Runtime-->
	<script type="text/javascript" src="http://code.jquery.com/jquery-1.7.2.min.js"></script>
	<script type="text/javascript" src="lib/js/getURLVar.js"></script>
	<script type="text/javascript" src="lib/js/basicParameter.js"></script>
	<script type="text/javascript" src="lib/js/basicFunction.js"></script>
	<script language="javascript">
	<!--  
	// <![CDATA[
		var meta = document.createElement('meta');
		meta.setAttribute("property", "og:title");
		meta.setAttribute("content", location.href);
		(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(meta);

		meta.setAttribute("property", "og:url");
		meta.setAttribute("content", location.href);
		(document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(meta);    
	// ]]>
	//	$('head meta[name=ogURL]').attr('content', location.href);
		$(document).ready(function(){
			$("#c-c-iframe").css('visibility','hidden');
			var userid = $.getUrlVar('userid');
			var trip_id = $.getUrlVar('trip_id');
			var g_lang = $.getUrlVar('g_lang');
			var g_calUnit = $.getUrlVar('g_calUnit');
			var g_hash = $.getUrlVar('hash');
			var strShowDLink = getRootPath()+"/showDTripPage.html?hash="+g_hash+"&g_lang="+g_lang+"&g_calUnit="+g_calUnit;
			$("#c-c-iframe").attr('src',strShowDLink);  
			 $("#c-c-iframe").load(function(){
				var framewidth = 1100;
				var frameheight = 1030;
				
				var swidth = $.getUrlVar('width');
				var sheight = $.getUrlVar('height');
				var scale = 1;
				if(swidth==null){
					swidth = framewidth;
					scale = swidth / framewidth;
					if(sheight==null){
						sheight = frameheight;
					}
					else{
						scale = sheight / frameheight;
					}
				}
				else{
					scale = swidth / framewidth;
				}
				$("#c-c-iframe").width(framewidth);  
				$("#c-c-iframe").height(frameheight); 
				//alert(1/scale);
				$('iframe').css('-webkit-transform', 'scale('+scale+')');
				$('iframe').css('-webkit-transform-origin', '0 0');
				$('iframe').css('-moz-transform', 'scale('+scale+')');
				$('iframe').css('-moz-transform-origin', '0 0');
				$('iframe').css('-ms-transform', 'scale('+scale+')');
				$('iframe').css('-ms-transform-origin', '0 0');
				$('iframe').css('-o-transform', 'scale('+scale+')');
				$('iframe').css('-o-transform-origin', '0 0');
				$('iframe').css('transform', 'scale('+scale+')');
				$('iframe').css('transform-origin', '0 0');
				$("#c-c-iframe").css('visibility','visible');
			}); 
		});
	-->
	</script>
	<!--Adobe Edge Runtime End-->
</head>
<body id="body" style="width:100%;height:100%;display:table;margin-top:0px;margin-left:0px;">
<div align="center" style="margin:auto;width:100%;text-align:center;display:table-cell;vertical-align:middle;">
<iframe style="visibility:hidden;"  scrolling="no"  src="" frameborder="0" id="c-c-iframe"></iframe>
</div>
</body>
</html>