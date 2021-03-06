//$("#div_langOption").html("<select id='langOption'></select>");
//alert("Chinese");
var newOptions = {
    'Chinese' : '中文',
    'English' : '英文'
};

var g_str_loading = "讀取中..."
var g_str_welcome = "歡迎! ";
var g_str_welcome_anonmous = "歡迎來到雲水途誌! 請登入...";
var g_str_register = " 註冊新帳號";
var g_str_or = "或";
var g_str_id = "帳號";
var g_str_password = "密碼";
var g_str_email = "郵件信箱";
var g_str_re_password = "確認密碼";
var g_str_create_account = "建立帳號";
var g_str_cancel ="取消";
var g_str_email_forgotten = "(如果你將來忘記密碼，這信箱將有助於我們重新寄送密碼予你)";

var g_str_delete = "確定要刪除此旅誌嗎?";

var g_str_radio_trip_id = "建立時間";
var g_str_radio_trip_st = "紀錄開始點";
var g_str_radio_length_st = "紀錄總長度";
var g_str_start = "開始時間";
var g_str_end = "結束時間";
var g_str_length = "總里程數";

var g_str_orderby = "排序:"
var g_str_s = "秒";
var g_str_m = "分";
var g_str_h = "時";

var g_str_second = "秒";
var g_str_minute = "分";
var g_str_hour = "時";

var g_str_checkin_table = "打卡數量";
var g_str_speed_table = "速度(單位: 公里/時)";
var g_str_altitude_table = "高度(單位: 公尺)";

$("#span_langtitle").html("選擇語言");

$("#banner_text").html("寫下旅誌! 改變視界!");

$('#sym_logingroup').css("background-image", url+im+"background_Chinese.png)");

$("#ub_home").attr('src',im+'ub_home_Chinese.png');
$('#ub_home').attr('name','ub_home_Chinese');
$("#ub_trip_history").attr('src',im+'ub_trip_history_Chinese.png');
$('#ub_trip_history').attr('name','ub_trip_history_Chinese');
$("#ub_trip_management").attr('src',im+'ub_trip_management_Chinese.png');
$('#ub_trip_management').attr('name','ub_trip_management_Chinese');
$("#ub_friend").attr('src',im+'ub_friend_Chinese.png');
$('#ub_friend').attr('name','ub_friend_Chinese');
$("#ub_download").attr('src',im+'ub_download_Chinese.png');
$('#ub_download').attr('name','ub_download_Chinese');
$("#ub_about").attr('src',im+'ub_about_Chinese.png');
$('#ub_about').attr('name','ub_about_Chinese');

$(".login_bt_link").html("<p>" + "登入"+"</p>");
$(".logout_bt_link").html("<p>" + "登出"+"</p>");
$(".fblogin_bt_link").html("<p>" + "Facebook登入"+"</p>");
$(".pre_page_link").html("<p>" +"<<"+"</p>");
$(".next_page_link").html("<p>" +">>"+"</p>");
$(".pre_trip_link").html("<p>" +"<<"+"</p>");
$(".next_trip_link").html("<p>" +">>"+"</p>");
$("#open_trip_tip").html("開啟旅誌");
$("#seg_trip_tip").html("編輯旅誌");
$("#add_note_tip").html("編輯註解");
$("#exp_trip_tip").html("輸出旅誌");

$(".takemarker_bt_link").html("打卡");

g_str_numberoftrip = "旅誌數量:";

g_str_loginfirst = "請先登入!";
g_str_loginfail = "登入失敗";
g_str_lostinsertdata = "請輸入使用者的名字以及密碼!";
g_str_firstpage = "此頁已是第一頁!";
g_str_lastpage = "此頁已是最後一頁!";
g_str_firstrip = "此旅誌已是第一個旅誌!";
g_str_lastrip = "此旅誌已是最後一個旅誌!";
g_str_notripdisplay = "沒有任何旅誌可以呈現";

g_str_tripnote = "旅誌註解";
g_str_tripnotedes = "請對此旅誌標註:";
g_str_tripdescription = "請點擊這裡輸入你對此旅誌的描述";

g_str_triplist = "旅誌列表";

var g_str_savetrip = "輸出旅誌";
var g_str_tripname = "旅誌名稱";
var g_str_type_tripname = "請輸入你的旅誌名稱:";

var g_str_notrip = "你目前沒有任何旅誌!"
var g_str_nofriend = "你目前沒有任何朋友!";

var g_str_bored= '無聊';
var g_str_sad= '難過';
var g_str_sleepy= '想睡';
var g_str_peaceful= '和平';
var g_str_relaxed= '放鬆';
var g_str_pleased= '愉悅';
var g_str_happy= '快樂';
var g_str_excited= '興奮';
var g_str_angry= '生氣';
var g_str_nervous= '緊張';
var g_str_calm= "平靜";

var g_str_copythelink = "產生目前靜態地圖";
var g_str_copythedlink = "產生目前動態地圖";
var g_str_reloadtoDefault = "回到原始設定";
var g_str_opencloseMarker = "打開或關閉所有打卡紀錄資料";

var g_str_downloadlink = "https://play.google.com/store/apps/details?id=tw.plash.antrip&hl=zh_TW";

var g_str_newTrip_1 = "一個新地圖: ";
var g_str_newTrip_2 = " 建立成功!";

var g_str_cutwithoutpoint = "不能在切割的線段裡沒有任何紀錄點!";
var g_str_cuttripfirst = "請先至少切割一次你的旅程!";

$("#about_page").html("<h1>關於本網站</h1><h3><p>這是在Google Play上的應用軟體ANTrip的瀏覽器版本。</p><p>ANTrip是一款免費的路徑記錄器！可以讓您和親朋好友輕鬆分享旅程，也可以讓您方便地記錄走過的點點滴滴！喜歡趴趴走、喜歡分享的您，千萬別錯過ANTrip。</p><p><a href='http://goo.gl/odpfO' target='blank'>詳細介紹請參考我們在Google Play的首頁</a></p><p>以下為您介紹本網站的使用方法:</p></h3><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_home'));\">首頁</a></h2><h3>登入首頁後會為您呈現前一次紀錄的旅程</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_trip_history'));\">旅程歷史</a></h2><h3>可以檢閱您所記錄過的所有旅程</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_trip_management'));\">旅程編輯</a></h2><h3>可以管理您所記錄過的所有旅程</h3></li><li><h2><a href=\"\" onClick=\"javascript:ChangeToUsedIcon($('#ub_friend'));\">我的朋友</a></h2><h3>可以查看好友分享給您的旅程以及新增好友</h3></li><li><h2><a href='http://goo.gl/odpfO' target='blank'>下載</a></h2><h3>前往我們在Google Play上的首頁</h3></li><h1>關於本軟體</h1><h3><p><a href='Manual.docx' >英文版使用說明</a></p><p><a href='Manual_cht.docx' >中文版使用說明</a></p></h3>");

$(".class_sologan").html("\"如果有人在一張地圖上,標示出我曾行經的所有旅程<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;並且用一條線連接起來,也許會連成一隻蟻頭人身的怪物\"<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;by 畢卡索");

if(typeof loadjscssfile== 'function'){
	if(g_forceAsync!=null && g_forceAsync==true){
		loadjscssfile("lib/js/langBasicSetting.js", "js", g_forceAsync);
	}
	else{
		loadjscssfile("lib/js/langBasicSetting.js", "js");
	}
}

