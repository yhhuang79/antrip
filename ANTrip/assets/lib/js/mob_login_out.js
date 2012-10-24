<!--
	function Login(){
		var username = $("#username").val();
		var org_password = $("#password").val();
		var password = hex_md5(org_password);
		if(username==""&&org_password==""){
			alert(g_str_lostinsertdata);
			return;
		}
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/login.php',
				data:{username: username, password: password},							 
				type: 'GET', dataType: 'jsonp', cache: false,
		success:function(result){
			if(result.sid != "0"){ 
				$.cookie("sid", result.sid);
				if(window.antrip){
					window.antrip.setCookie("usrname", username.toString());
					window.antrip.setCookie("sid", result.sid.toString());
				}
				$.mobile.changePage("#map");
			} else { 
				alert(g_str_loginFailed);
			}
		}
		});
	}

	function Logout(){
		var facebookid = $.cookie("facebookid");
		if(window.antrip){
			facebookid = window.antrip.getCookie("facebookid");
		}
		if(facebookid){
			FB.logout(function(response) {
				$.cookie("facebookid", null);
				if(window.antrip){
					isRecording = window.antrip.removeCookie("facebookid");
				}
			}); 
		}
		g_triplistReset = true;
		$.cookie("sid", null);
		$("#username").attr("value","");
		$("#password").attr("value","");
		if(window.antrip){
			window.antrip.removeCookie("trip_id");
			window.antrip.removeCookie("sid");
			window.antrip.logout();
		}
		$.mobile.changePage("#login");
		$.mobile.hidePageLoadingMsg();
	}
	function registerID(){
		var UN=$("#reg_username").val();
		var org_password = $("#reg_password").val();
		var password = hex_md5(org_password);
		var EM= $("#reg_email").val();
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/SignUp.php',
							data:{username: UN, password: password, email: EM},			
								type: 'GET', dataType: 'jsonp', cache: false,
						success:function(result){	
							if(result.sid != "0"){ 
								$("#username").attr("value",$("#reg_username").val());
								$("#password").attr("value",$("#reg_password").val());
								Login();
								return;
							} else { 
								alert("Register Fail");
								return false;
							}
						}
		});
	}

	function verifyRegister(){
		if($("#reg_username").val()==""||
			$("#reg_email").val()==""||
			$("#reg_password").val()==""||
			$("#reg_re_password").val()==""){
			alert("Some data are missing!");
			return false;
		}
		else if($("#reg_password").val() != $("#reg_re_password").val()){
			alert(g_str_pswnotsync);
			return false;
		}
		else{
			return registerID();
		}
	}
//-->