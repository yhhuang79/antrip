<!--
var select = $('#langOption');
if(select.prop) {
  var options = select.prop('options');
}
else {
  var options = select.attr('options');
}

if(options!=null){
	$("#langOption option").remove();

	$.each(newOptions, function(val, text) {
		options[options.length] = new Option(text, val);
	});
	// select default language index
	if(g_lang==null){
		$("#langOption option[0]").attr('selected', 'selected');
	}
	else{
		$("#langOption option[value='"+g_lang+"']").attr('selected', 'selected');
	}
}

$.cookie("lang", g_lang);

$("#radio_second").html("<p align=center>"+g_str_second+"</p>");
$("#radio_minute").html("<p align=center>"+g_str_minute+"</p>");
$("#radio_hour").html("<p align=center>"+g_str_hour+"</p>");

$("#id_orderby").html(g_str_orderby);

$("#radio_trip_id_label").html("<p align=center>"+g_str_radio_trip_id+"</p>");
$("#radio_trip_st_label").html("<p align=center>"+g_str_radio_trip_st+"</p>");
$("#radio_trip_length_label").html("<p align=center>"+g_str_radio_length_st+"</p>");

$('#sym_loginarea').css("background-image", url+im+"login_area.png)");
$("#img_banner_logo").attr('src','images/banner_logo_'+g_lang+'.png');

sid = $.cookie("sid");
if(sid==null){
	$("#sym_logintext").html(g_str_welcome_anonmous+"<br/>"+g_str_or+"<a class='listview' href='javascript:showRegisterDialog();' >"+g_str_register+"</a>");
}
else{
	username = $.cookie("username");
	$("#sym_logintext").text(g_str_welcome+username);
}

$("#img_open_trip").attr('src',im+'b_open_trip.png');
$("#img_seq_trip").attr('src',im+'b_seq_trip.png');
$("#img_add_note").attr('src',im+'b_add_note.png');
$("#img_exp_trip").attr('src',im+'b_export_trip.png');

$('#username').attr('placeholder',g_str_id);
$('#password').attr('placeholder',g_str_password);

$("#img_ant").attr('src',im+'ant.png');
$("#img_des_ant").attr('src',im+'ant_24.png');

$("div[id*=tripsum]").html("");

if($("div[id*=tripsum]").length!=0){
	$("div[id*=tripsum]").append(g_str_numberoftrip+g_tripnum);
}

$("#reloadtoDefault").html("<p align=center>"+g_str_reloadtoDefault+"</p>");
$("#copyStaticMap").html("<p align=center>"+g_str_copythelink+"</p>");
$("#copyDynamicMap").html("<p align=center>"+g_str_copythedlink+"</p>");
$("#OpenCloseMarker").html("<p align=center>"+g_str_opencloseMarker+"</p>");

$("#overlay").html(g_str_loading);

$(".class_btInMap").css({color: "#000000"});
$(".class_btInMap").css("background","rgba(220,72,71,0.5)");

//UpdateCalculation();

//-->