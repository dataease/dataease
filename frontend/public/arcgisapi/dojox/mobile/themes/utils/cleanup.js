var fs = require("fs");

clean("../android");
clean("../android/dijit");
clean("../iphone");
clean("../iphone/dijit");
clean("../blackberry");
clean("../blackberry/dijit");
clean("../holodark");
clean("../holodark/dijit");
clean("../windows");
clean("../windows/dijit");
clean("../custom");
clean("../custom/dijit");

clean("../common/transitions");
clean("../common/domButtons");

// Remove css files that have a matching less file in the same folder or in common folder
function clean(folder){ 	
	var cssFiles = [];
	getFiles(folder, /.*.css$/, cssFiles);
	var lessFiles = {};
	getFiles("../common/", /.*.less$/, lessFiles);
	getFiles(folder, /.*.less$/, lessFiles);
	
	for(var i=0; i < cssFiles.length; i++){
		if(lessFiles[cssFiles[i].replace(".css", ".less")]){ 
			console.log("deleting", folder + "/" + cssFiles[i]);
			fs.unlink(folder + "/" + cssFiles[i], function(err){if(err){console.log(err);}});			
		}
	}
	
}

function getFiles(folder, pattern, dest){
	fs.readdirSync(folder).map(function(file){
		if(pattern.test(file)){
			if(dest instanceof Array){
				dest.push(file);
			}else{
				dest[file] = true;
			}
		}
	});
}
