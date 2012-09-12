<!--
	//Load Language files.
	var g_lang="Chinese";
	function setLanguage(forceAsync){
		if(g_lang=="English"){
			loadjscssfile("lib/lang/lang_English.js", "js", forceAsync);
		}
		else if(g_lang=="Chinese"){
			loadjscssfile("lib/lang/lang_Chinese.js", "js", forceAsync);
		}
	}

	function getRootPath(){ 
		var strFullPath=window.document.location.href; 
		var strPath=window.document.location.pathname; 
		var pos=strFullPath.indexOf(strPath); 
		var prePath=strFullPath.substring(0,pos); 
		var postPath=strPath.substring(0,strPath.substr(1).indexOf('/')+1); 
		return(prePath+postPath); 
	} 
//-->