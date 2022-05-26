/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/throttle",[],function(){return function(b,c){var a=!0;return function(){a&&(a=!1,b.apply(this,arguments),setTimeout(function(){a=!0},c))}}});