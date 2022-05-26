/*
	Copyright (c) 2004-2016, The JS Foundation All Rights Reserved.
	Available via Academic Free License >= 2.1 OR the modified BSD license.
	see: http://dojotoolkit.org/license for details
*/

//>>built
define("dojo/dnd/Target",["../_base/declare","../dom-class","./Source"],function(a,b,c){return a("dojo.dnd.Target",c,{constructor:function(){this.isSource=!1;b.remove(this.node,"dojoDndSource")}})});