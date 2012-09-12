//2006-11-05 by Jeffrey Lee
//網頁放大功能 Ver 0.9 Beta
//http://blogs.msdn.com/ie/archive/2006/01/17/514076.aspx (SELECT不Support)
//在document.onkeydown上掛上事件，設定Ctrl-M觸發
if (document.onkeydown) document.orig_onkeydown=document.onkeydown;
document.onkeydown = function() {

	if (event.ctrlKey && event.keyCode==77) {
		if (!document.all("afaZoomer")) afaCreateZoomer();
		var zoomer=document.all("afaZoomer");
		zoomer.style.top=document.body.scrollTop*1 + 30 + "px";
		zoomer.style.left=document.body.scrollLeft + "px";
		zoomer.style.display="";
		afaAutoSelZoomRatio();
	}	
	if (document.orig_onkeydown) document.orig_onkeydown();
}
//檢查自訂比例是否輸入正確?
function afaCustZoomChg() {
	var custZoom=document.all("afaCustZoomRatio");
	//用Regex比對是否符合999%格式
	if (!custZoom.value.match(/^\d{2,3}%$/)) {
		alert("自訂比例必須是999%的格式!");
		custZoom.value="100%";
		custZoom.focus();
	}
	else afaZoomPage(custZoom.value);	
}
//Enter Key to Change
function afaCustZoomKey() {
	if (event.keyCode==13) afaCustZoomChg();
}
//選單切換選項
function afaZoomerChg() {
	var ra=document.all("afaZoomRatio");
	if (ra[ra.length-1].checked) //自訂
	{
		var custZoom=document.all("afaCustZoomRatio");
		custZoom.disabled=false;
		custZoom.focus();
	} else {
		document.all("afaCustZoomRatio").disabled=true;
		for (var i=0; i<ra.length; i++) 
			if (ra[i].checked) afaZoomPage(ra[i].value);	
	}
}
//建立縮放操作框
function afaCreateZoomer() {

	var d=document.createElement("DIV");
	d.id="afaZoomer";
	d.style.position="absolute";
	d.style.display="none";	
	document.body.appendChild(d);	

	var s="";
	//s += ('<div id="afaZoomer" style="position: absolute; display:none; width:116px; height:132px; left:734px; top:202px">');
	s += ('<table style="font-size:11px; color: #ffffff;" width="115" cellpadding="0" cellspacing="0" cellpadding="5" height="130">');
	s += ('<tr height=30><td bgcolor="#000066" align=center height="20" style="color: #ffffff;" colspan=2>網頁縮放</td></tr>');
	s += ('<tr bgcolor="#336699"><td height="80" align=left color=#ffffff width="59">');
	s += ('<input type=radio name=afaZoomRatio value="50%">50%<br>');
	s += ('<input type=radio name=afaZoomRatio value="75%">75%<br>');
	s += ('<input type=radio name=afaZoomRatio value="90%">90%<br>');
	s += ('<input type=radio name=afaZoomRatio value="100%">100%');
	s += ('</td><td width="56">');
	s += ('<input type=radio name=afaZoomRatio value="110%">110%<br>');
	s += ('<input type=radio name=afaZoomRatio value="125%">125%<br>');
	s += ('<input type=radio name=afaZoomRatio value="150%">150%<br>');
	s += ('<input type=radio name=afaZoomRatio value="200%">200%<br>');
	s += ('</td></tr><tr><td colspan=2 bgcolor="#336699">');
	//s += ('<input type=radio name=afaZoomRatio value="自訂">自訂 <input id="afaCustZoomRatio" value="100%" onchange="afaCustZoomChg();" disabled=true style="font-size:11px;width:40px;" onkeydown="afaCustZoomKey();">');
	s += ('<input type=radio name=afaZoomRatio value="自訂">自訂 <input id="afaCustZoomRatio" value="100%" disabled=true style="font-size:11px;width:40px;" >');
	s += ('</td></tr><tr bgcolor="#336699"><td colspan=2>');
	s += ('<input type="checkbox" id="afaRemZoomSet" ');
	//由Cookie取得上次的縮放比
	var ratio=afaZoomReadCookie("AfaZoom");
	var bZoomed=false;
	if (ratio && ratio!="100%") {
		s+= "checked";	
		bZoomed=true;
	}
	s += ('> 記住縮放比例');
	s += ('</td></tr></table>');
	//s += ('</div>');
	d.innerHTML=s;
	//發現直接在innerHTML中指定onchang及onkeydown會造成error
	//改為事後設定
	var czr=document.all("afaCustZoomRatio");
	czr.onchange=afaCustZoomChg;
	czr.onkeydown=afaCustZoomKey;

	var ra=document.all("afaZoomRatio");
	//為所有Radio Button掛上事件
	for (var i=0; i<ra.length; i++) {
		ra[i].onclick=afaZoomerChg;
		
	}
	if (bZoomed) afaZoomPage(ratio);
}
setTimeout("afaCreateZoomer()",100);
//切換到目前的設定
function afaAutoSelZoomRatio() {
	var zoomRatio=document.body.style.zoom;
	if (zoomRatio=="") zoomRatio="100%";
	var ra=document.all("afaZoomRatio");
	var bFound=false;
	for (var i=0; i<ra.length; i++)
		if (ra[i].value==zoomRatio) {
			ra[i].checked=true;
			bFound=true;
			break;
		}
	if (!bFound) {
		ra[ra.length-1].checked=true; //最後一個選項是"自訂"
		document.all("afaCustZoomRatio").value=zoomRatio;
	}
}
//放大鏡功能
function afaZoomPage(ratio) {
	document.body.style.zoom=ratio;
	document.all("afaZoomer").style.display="none";
	var rzs=document.all("afaRemZoomSet").checked;
	if (rzs) {
		//寫成Cookie
		afaZoomCreateCookie("AfaZoom",ratio,3);	
		//window.status += "Cookie:"+ratio;
		document.all("afaRemZoomSet").checked=true;
	} else
		afaZoomEraseCookie("AfaZoom");
}

//COOKIE FUNCTIONS
//Cookie函數，改寫自 http://www.quirksmode.org/js/cookies.html
function afaZoomCreateCookie(name,value,days) {
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

function afaZoomReadCookie(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

function afaZoomEraseCookie(name) {
	afaZoomCreateCookie(name,"",-1);
}

