	function initRegisterDialog(){
		$("#dialog-register").dialog({
			autoOpen: false,
			bgiframe: false,
			draggable: false,
			resizable: false,
			height:600,
			width: 800,
			modal: true,
			title:g_str_register,
			show: { effect: 'drop', direction: "up"},
			open: function (event, ui) {
				$('.ui-dialog-buttonpane').css({
					'background-image':url+im+"typenotearea.png)",
					'background-position':"center center",
					'background-repeat': 'no-repeat',
				});
				$("button").css({
					color: "#000000",
				});
				$('.ui-dialog .ui-dialog-content').css({
					'background': '#ffffff',
					'background-image':url+im+"typenotearea.png)",
					'background-position':"center center",
					'background-repeat': 'no-repeat',
				});
				$(this).attr('title',g_str_register);
				$('#reg_username').attr('placeholder',g_str_id);
				$('#reg_email').attr('placeholder',g_str_email);
				$('#reg_password').attr('placeholder',g_str_password);
				$('#reg_re_password').attr('placeholder',g_str_re_password);
				$('#email_forgotten').html(g_str_email_forgotten);
				$('#sym_video').hide();
			},
			close: function() {
				 $('#sym_video').show();
			},
			buttons: [{
						text: g_str_create_account,
						click: function() {
								  verifyRegister();
							   }
					},
					{
						text: g_str_cancel,
						click: function() {
								  $( this ).dialog( "close" );
								  $('#sym_video').show();
								  return;
							   }
					}
			]
		});
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
								$("#dialog-register").dialog( "close" );
								$('#sym_video').show();
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

	function Login(){
		var username = $("#username").val();
		var org_password = $("#password").val();
		var password = hex_md5(org_password);
		if(username==""&&org_password==""){
			alert(g_str_lostinsertdata);
		}
		$.ajax({url:'http://plash2.iis.sinica.edu.tw/api/login.php',
				data:{username: username, password: password},							 
				type: 'GET', dataType: 'jsonp', cache: false,
		success:function(result){
			if(result.sid != "0"){ 
				$.cookie("sid", result.sid, { expires: 1});
				$.cookie("username", username, { expires: 1});
				top.window.location = "frame.html";
			} else { 
				alert(g_str_loginfail);
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

	function showRegisterDialog(){
		var sid = $.cookie("sid");
		if(sid == null ){
			$("#dialog-register").dialog( "open" );
		}
		else{
			alert(g_str_hasregistered);
		}
	}