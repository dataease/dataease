//>>built
require({cache:{"url:dojox/calendar/templates/LabelRenderer.html":'\x3cdiv class\x3d"dojoxCalendarEvent dojoxCalendarLabel" onselectstart\x3d"return false;"\x3e\t\r\n\t\x3cdiv class\x3d"labels"\x3e\r\n\t\t\x3cspan data-dojo-attach-point\x3d"startTimeLabel" class\x3d"startTime"\x3e\x3c/span\x3e\r\n\t\t\x3cspan data-dojo-attach-point\x3d"summaryLabel" class\x3d"summary"\x3e\x3c/span\x3e\r\n\t\t\x3cspan data-dojo-attach-point\x3d"endTimeLabel" class\x3d"endTime"\x3e\x3c/span\x3e\r\n\t\x3c/div\x3e\t\r\n\t\x3cdiv data-dojo-attach-point\x3d"moveHandle" class\x3d"handle moveHandle" \x3e\x3c/div\x3e\r\n\x3c/div\x3e\r\n'}});
define(["dojo/_base/declare","dijit/_WidgetBase","dijit/_TemplatedMixin","./_RendererMixin","dojo/text!./templates/LabelRenderer.html"],function(a,b,c,d,e){return a("dojox.calendar.LabelRenderer",[b,c,d],{templateString:e,_orientation:"horizontal",resizeEnabled:!1,visibilityLimits:{resizeStartHandle:50,resizeEndHandle:-1,summaryLabel:15,startTimeLabel:45,endTimeLabel:30},_isElementVisible:function(f,g,h,k){switch(f){case "startTimeLabel":if(this.item.allDay&&this.item.range[0].getTime()!==this.item.startTime.getTime())return!1}return this.inherited(arguments)},
_displayValue:"inline",postCreate:function(){this.inherited(arguments);this._applyAttributes()}})});