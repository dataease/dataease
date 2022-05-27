//>>built
define("dojo/_base/array dojo/_base/declare dojo/_base/window dojo/dom-class dojo/dom-construct dojo/dom-geometry dojo/dom-style dojo/dom-attr dijit/_Contained dijit/_Container dijit/_WidgetBase ./TabBarButton dojo/has dojo/has!dojo-bidi?dojox/mobile/bidi/TabBar".split(" "),function(h,q,r,f,t,m,n,u,v,w,x,z,l,y){n=q(l("dojo-bidi")?"dojox.mobile.NonBidiTabBar":"dojox.mobile.TabBar",[x,w,v],{iconBase:"",iconPos:"",barType:"tabBar",closable:!1,center:!0,syncWithViews:!1,tag:"ul",fill:"auto",selectOne:!0,
baseClass:"mblTabBar",_fixedButtonWidth:76,_fixedButtonMargin:17,_largeScreenWidth:500,buildRendering:function(){this.domNode=this.srcNodeRef||t.create(this.tag);u.set(this.domNode,"role","tablist");this.reset();this.inherited(arguments)},postCreate:function(){if(this.syncWithViews){var c=function(a,e,p,k,b,d){(e=h.filter(this.getChildren(),function(g){return g.moveTo==="#"+a.id||g.moveTo===a.id})[0])&&e.set("selected",!0)};this.subscribe("/dojox/mobile/afterTransitionIn",c);this.subscribe("/dojox/mobile/startView",
c)}},startup:function(){this._started||(this.inherited(arguments),this.resize())},reset:function(){var c=this._barType;if("object"===typeof this.barType){this._barType=this.barType["*"];for(var a in this.barType)if(f.contains(r.doc.documentElement,a)){this._barType=this.barType[a];break}}else this._barType=this.barType;a=function(e){return e.charAt(0).toUpperCase()+e.substring(1)};c&&f.remove(this.domNode,this.baseClass+a(c));f.add(this.domNode,this.baseClass+a(this._barType))},resize:function(c){var a;
var e=c&&c.w?c.w:m.getMarginBox(this.domNode).w;var p=this._fixedButtonWidth,k=this._fixedButtonMargin,b=h.map(this.getChildren(),function(g){return g.domNode});f.toggle(this.domNode,"mblTabBarNoIcons",!h.some(this.getChildren(),function(g){return g.iconNode1}));f.toggle(this.domNode,"mblTabBarNoText",!h.some(this.getChildren(),function(g){return g.label}));var d=0;if("tabBar"==this._barType)if(this.containerNode.style.paddingLeft="",d=Math.floor((e-(p+2*k)*b.length)/2),"always"==this.fill||"auto"==
this.fill&&(e<this._largeScreenWidth||0>d))for(f.add(this.domNode,"mblTabBarFill"),a=0;a<b.length;a++)b[a].style.width=100/b.length+"%",b[a].style.margin="0";else{for(a=0;a<b.length;a++)b[a].style.width=p+"px",b[a].style.margin="0 "+k+"px";0<b.length&&(l("dojo-bidi")&&!this.isLeftToRight()?(b[0].style.marginLeft="0px",b[0].style.marginRight=d+k+"px"):b[0].style.marginLeft=d+k+"px");this.containerNode.style.padding="0px"}else{for(a=0;a<b.length;a++)b[a].style.width=b[a].style.margin="";a=this.getParent();
if("always"==this.fill)for(f.add(this.domNode,"mblTabBarFill"),a=0;a<b.length;a++)b[a].style.width=100/b.length+"%","segmentedControl"!=this._barType&&"standardTab"!=this._barType&&(b[a].style.margin="0");else{if(this.center&&(!a||!f.contains(a.domNode,"mblHeading"))){d=e;for(a=0;a<b.length;a++)d-=m.getMarginBox(b[a]).w;d=Math.floor(d/2)}l("dojo-bidi")&&!this.isLeftToRight()?(this.containerNode.style.paddingLeft="0px",this.containerNode.style.paddingRight=d?d+"px":""):this.containerNode.style.paddingLeft=
d?d+"px":""}}c&&c.w&&m.setMarginBox(this.domNode,c)},getSelectedTab:function(){return h.filter(this.getChildren(),function(c){return c.selected})[0]},onCloseButtonClick:function(c){return!0}});return l("dojo-bidi")?q("dojox.mobile.TabBar",[n,y]):n});