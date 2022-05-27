//>>built
define("dojo/_base/declare dojo/_base/lang dojo/dom-class dojo/dom-construct dojo/on dojo/aspect dojo/query dojo/has ./util/misc dojo/_base/sniff".split(" "),function(E,F,G,l,n,r,p,g,B){function t(a,b){var c=a._columnSetScrollLefts;p(".dgrid-column-set",b).forEach(function(d){d.scrollLeft=c[d.getAttribute("data-dgrid-column-set-id")]})}function u(a,b){1!==a.nodeType&&(a=a.parentNode);for(;a&&!p.matches(a,".dgrid-column-set[data-dgrid-column-set-id]",b);){if(b&&a===b||G.contains(a,"dgrid"))return null;
a=a.parentNode}return a}function v(a){var b=g("pointer");return b?(a=H[a]||a,"MS"===b.slice(0,2)?"MSPointer"+a.slice(0,1).toUpperCase()+a.slice(1):"pointer"+a):"touch"+a}function C(a,b,c){b=b.getAttribute("data-dgrid-column-set-id");a=a._columnSetScrollers[b];c=a.scrollLeft+c;a.scrollLeft=0>c?0:c}g.add("event-mousewheel",function(a,b,c){return"onmousewheel"in c});g.add("event-wheel",function(a,b,c){return"onwheel"in c});var H={start:"down",end:"up"},I=g("touch")&&function(a){return function(b,c){var d=
[n(b,v("start"),function(e){if(!a._currentlyTouchedColumnSet){var h=u(e.target,b);!h||e.pointerType&&"touch"!==e.pointerType&&2!==e.pointerType||(a._currentlyTouchedColumnSet=h,a._lastColumnSetTouchX=e.clientX,a._lastColumnSetTouchY=e.clientY)}}),n(b,v("move"),function(e){if(null!=a._currentlyTouchedColumnSet){var h=u(e.target);h&&(c.call(null,a,h,a._lastColumnSetTouchX-e.clientX),a._lastColumnSetTouchX=e.clientX,a._lastColumnSetTouchY=e.clientY)}}),n(b,v("end"),function(){a._currentlyTouchedColumnSet=
null})];return{remove:function(){for(var e=d.length;e--;)d[e].remove()}}}},J=g("event-mousewheel")||g("event-wheel")?function(a){return function(b,c){return n(b,g("event-wheel")?"wheel":"mousewheel",function(d){var e=u(d.target,b);e&&(d=d.deltaX||-d.wheelDeltaX/3)&&c.call(null,a,e,d)})}}:function(a){return function(b,c){return n(b,".dgrid-column-set[data-dgrid-column-set-id]:MozMousePixelScroll",function(d){1===d.axis&&c.call(null,a,this,d.detail)})}};return E(null,{constructor:function(){if("_editorInstances"in
this)throw Error("When used with Editor ColumnSet must be mixed in before Editor.");},postCreate:function(){var a=this;this.inherited(arguments);this.on(J(this),C);if(g("touch"))this.on(I(this),C);this.on(".dgrid-column-set:dgrid-cellfocusin",function(b){a._onColumnSetCellFocus(b,this)});"function"===typeof this.expand&&this._listeners.push(r.after(this,"expand",function(b,c){b.then(function(){var d=a.row(c[0]);a._expanded[d.id]&&t(a,d.element.connected)});return b}))},columnSets:[],createRowCells:function(a,
b,c,d,e){var h=l.create("table",{className:"dgrid-row-table"}),k=l.create("tbody",null,h);k=l.create("tr",null,k);for(var f=0,K=this.columnSets.length;f<K;f++){var q=l.create(a,{className:"dgrid-column-set-cell dgrid-column-set-"+f},k);q=l.create("div",{className:"dgrid-column-set"},q);q.setAttribute("data-dgrid-column-set-id",f);var m;if((m=c||this.subRows)&&m.length){for(var D=[],L=f+"-",w=0,M=m.length;w<M;w++){var x=m[w],y=[];y.className=x.className;for(var z=0,N=x.length;z<N;z++){var A=x[z];null!=
A.id&&0===A.id.indexOf(L)&&y.push(A)}D.push(y)}m=D}else m=void 0;q.appendChild(this.inherited(arguments,[a,b,m||this.columnSets[f],d,e]))}return h},renderArray:function(){for(var a=this.inherited(arguments),b=0;b<a.length;b++)t(this,a[b]);return a},insertRow:function(){var a=this.inherited(arguments);t(this,a);return a},renderHeader:function(){function a(){d._positionScrollers()}this.inherited(arguments);var b=this.columnSets,c=this._columnSetScrollers,d=this;this._columnSetScrollerContents={};this._columnSetScrollLefts=
{};if(c)for(e in c)l.destroy(c[e]);else this._listeners.push(r.after(this,"resize",a,!0),r.after(this,"styleColumn",a,!0)),this._columnSetScrollerNode=l.create("div",{className:"dgrid-column-set-scroller-container"},this.footerNode,"after");c=this._columnSetScrollers={};var e=0;for(c=b.length;e<c;e++)this._putScroller(b[e],e);this._positionScrollers()},styleColumnSet:function(a,b){a=this.addCssRule("#"+B.escapeCssIdentifier(this.domNode.id)+" .dgrid-column-set-"+B.escapeCssIdentifier(a,"-"),b);this._positionScrollers();
return a},configStructure:function(){this.columns={};this.subRows=[];for(var a=0,b=this.columnSets.length;a<b;a++)for(var c=this.columnSets[a],d=0;d<c.length;d++)c[d]=this._configColumns(a+"-"+d+"-",c[d]);this.inherited(arguments)},_positionScrollers:function(){var a=this.domNode,b=this._columnSetScrollers,c=this._columnSetScrollerContents,d=this.columnSets,e=0,h=0;var k=0;for(d=d.length;k<d;k++){var f=p('.dgrid-column-set[data-dgrid-column-set-id\x3d"'+k+'"]',a)[0];e=f.offsetWidth;f=f.firstChild.offsetWidth;
c[k].style.width=f+"px";b[k].style.width=e+"px";9>g("ie")&&(b[k].style.overflowX=f>e?"scroll":"auto");f>e&&h++}this._columnSetScrollerNode.style.bottom=this.showFooter?this.footerNode.offsetHeight+"px":"0";this.bodyNode.style.bottom=h?g("dom-scrollbar-height")+(g("ie")?1:0)+"px":"0"},_putScroller:function(a,b){a=this._columnSetScrollers[b]=l.create("span",{className:"dgrid-column-set-scroller dgrid-column-set-scroller-"+b+(9>g("ie")?" dgrid-scrollbar-height":"")},this._columnSetScrollerNode);a.setAttribute("data-dgrid-column-set-id",
b);this._columnSetScrollerContents[b]=l.create("div",{className:"dgrid-column-set-scroller-content"},a);n(a,"scroll",F.hitch(this,"_onColumnSetScroll"))},_onColumnSetScroll:function(a){var b=a.target.scrollLeft;a=a.target.getAttribute("data-dgrid-column-set-id");var c;this._columnSetScrollLefts[a]!==b&&(p('.dgrid-column-set[data-dgrid-column-set-id\x3d"'+a+'"],.dgrid-column-set-scroller[data-dgrid-column-set-id\x3d"'+a+'"]',this.domNode).forEach(function(d,e){d.scrollLeft=b;e||(c=d.scrollLeft)}),
this._columnSetScrollLefts[a]=c)},_setColumnSets:function(a){this._destroyColumns();this.columnSets=a;this._updateColumns()},_scrollColumnSet:function(a,b){a=a.tagName?a.getAttribute("data-dgrid-column-set-id"):a;this._columnSetScrollers[a].scrollLeft=0>b?0:b},_onColumnSetCellFocus:function(a,b){a=a.target;var c=b.getAttribute("data-dgrid-column-set-id");c=this._columnSetScrollers[c];(a.offsetLeft-c.scrollLeft+a.offsetWidth>b.offsetWidth||c.scrollLeft>a.offsetLeft)&&this._scrollColumnSet(b,a.offsetLeft)}})});