/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/request/default",["exports","require","../has"],function(a,d,c){var b=c("config-requestProvider");b||(b="./xhr");a.getPlatformDefaultId=function(){return"./xhr"};a.load=function(a,c,e,f){d(["platform"==a?"./xhr":b],function(a){e(a)})}});