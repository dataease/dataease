var fs = require("fs");
var path = require("path");
var less = require("less");

var themeFolders = ["../android", "../iphone", "../blackberry", "../holodark", "../windows", "../custom", "../ios7"];
var commonFolders = ["../common/domButtons", "../common/transitions"];

var batchQueue = [];
var batchIndex = 0;
var processProgress = 0; 

themeFolders.forEach(function(folder){ 
	batchQueue.push(function(){
		processFolder(folder, true);
		processFolder(folder + "/dijit", false);
	});
});

commonFolders.forEach(function(folder){ 
	batchQueue.push(function(){
		processFolder(folder, false);
	});
});

batch();

///////////////////////////////////////////////////////////////////////////////////////////////////

function batch(){
	if(batchIndex < batchQueue.length){
		batchQueue[batchIndex]();
		batchIndex++;
	}
}

function beginProcess(){
	processProgress++;
}
function endProcess(){
	processProgress--;
	if (processProgress == 0){
		batch();
	}
}

function processFolder(folder, usingCommonSubstitution){
	var folderFiles = getLessFiles(folder);
	
	if(usingCommonSubstitution){
		var commonFiles = getLessFiles("../common");
		var outputFile;
		commonFiles.array.forEach(function(commonFile){
			var themeFile = folderFiles.dic[commonFiles.dic[commonFile]];
			if(themeFile){
				// If there is a .less file in the theme folder, use it. 
				outputFile = themeFile.replace(".less", ".css");
				applyLess(themeFile, null, outputFile);
			}else{
				// Otherwise, fall back to the .less file which is in 'common'.
				var fileName = commonFiles.dic[commonFile];
				outputFile = folder + "/" + fileName.replace(".less", ".css");
				// dojox.mobile mirroring support
				if(fileName.indexOf("_rtl") == -1){ 
					applyLess(commonFile, '@import "' + folder + '/variables.less";', outputFile);
				}else{
					applyLess(commonFile, '@import "' + folder + '/variables_rtl.less";', outputFile);
				}
			}
		});
	}else{
		folderFiles.array.forEach(function(file){
			applyLess(file, null, file.replace(".less", ".css"));
		});
	}
}

function applyLess(file, prependText, outputFile){ 
	beginProcess();
	console.log("compiling:", file);
	
	var parser = new(less.Parser)({paths: [path.dirname(file)], filename: file, optimization: 1});
	var lessContent = fs.readFileSync(file, "utf-8");
	
	if(prependText){
		lessContent = prependText + lessContent;
	}
	parser.parse(lessContent, function(error, tree){
		if(error){
			less.writeError(error);
			process.exit(1);
		}
		var fd = fs.openSync(outputFile, "w");
		
		fs.write(fd, tree.toCSS({compress: false}).replace(/\n/g, "\r\n"), 0, "utf-8", function(f){
			fs.close(fd);
			console.log("writing:", outputFile);
			endProcess();
		});
	});
}

function getLessFiles(folder){ 
	var filesMap = {};
	var filesArray = fs.readdirSync(folder);
	filesArray = filesArray.filter(function(file){
		return file && /\.less$/.test(file) && !/variables\.less$/.test(file) && !/css3\.less$/.test(file) 
		&& !/variables_rtl\.less$/.test(file);
	});
	
	filesArray = filesArray.map(function(file){
			filesMap[file] = folder + "/" + file;
			filesMap[folder + "/" + file] = file;
			return filesMap[file]; 
	});
	
	return {array: filesArray, dic: filesMap};
}
