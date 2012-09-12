<!--	
	/*! global usage function*/
	var g_forceAsync = false;
	function loadjscssfile(filename, filetype, forceAsync){
		if(forceAsync!=null){
			g_forceAsync = forceAsync;
		}
		 if (filetype=="js"){ //if filename is a external JavaScript file
			   if (forceAsync==true || /Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent)  || /MSIE (\d+\.\d+);/.test(navigator.userAgent)){ // for firefox
				  var fileref=document.createElement('script');
				  fileref.setAttribute("type","text/javascript");
				  fileref.setAttribute("src", filename);
				  document.getElementsByTagName("head")[0].appendChild(fileref);
			   }
			   else{
				   require(filename);
			   }
		 }
		 else if (filetype=="css"){ //if filename is an external CSS file
			 if (/Firefox[\/\s](\d+\.\d+)/.test(navigator.userAgent)){ // for firefox
					//alert("firefox");
					$("<link/>", {
					   rel: "stylesheet",
					   type: "text/css",
					   href: filename
					}).appendTo("head");
			 }
			 else if(/MSIE (\d+\.\d+);/.test(navigator.userAgent)){ // for explore
					//alert("IE");
					if(document.createStyleSheet) {
						//alert("IE load failed on" + filename);
						document.createStyleSheet(filename);
					}
			 }
			 else{
				  var fileref=document.createElement("link");
				  fileref.setAttribute("rel", "stylesheet");
				  fileref.setAttribute("type", "text/css");
				  fileref.setAttribute("href", filename);
				  if (typeof fileref!="undefined"){
					 // alert(fileref);
					  document.getElementsByTagName("head")[0].appendChild(fileref);
				  }
			 }
		 }
	}
	function require(script) {		
		$.ajax({
			url: script,
			dataType: "script",
			async: false,           // <-- this is the key
			success: function () {
				// all good...
			},
			error: function () {
				//alert("failed on " + script);
				throw new Error("Could not load script " + script);
			}
		});
	}
//-->