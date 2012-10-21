<!--
	//get parameters from URL
	$.urlParam = function(name){
		var results = new RegExp('[\\?&]' + name + '=([^&#]*)').exec(window.location.href);
		return results[1] || 0;
	}
	function getUrlVars()
	{
		var vars = [], hash;
		var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
		for(var i = 0; i < hashes.length; i++)
		{
			hash = hashes[i].split('=');
			vars.push(hash[0]);
			vars[hash[0]] = hash[1];
		}
		return vars;
	}

	$.extend({
		  getUrlVars: function(){
			var vars = [], hash;
			var hashes = window.location.href.slice(window.location.href.indexOf('?') + 1).split('&');
			for(var i = 0; i < hashes.length; i++)
			{
			  hash = hashes[i].split('=');
			  vars.push(hash[0]);
			  vars[hash[0]] = hash[1];
			}
			return vars;
		  },
		  getUrlVar: function(name){
			return $.getUrlVars()[name];
		  }
	});

	function getRootPath(){ 
		var strFullPath=window.document.location.href; 
		var strPath=window.document.location.pathname; 
		var pos=strFullPath.indexOf(strPath); 
		var prePath=strFullPath.substring(0,pos); 
		var postPath=strPath.substring(0,strPath.substr(1).indexOf('/')+1); 
		return(prePath+postPath); 
	} 

//-->