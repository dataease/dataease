// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("require exports ../../../core/maybe ../../../core/promiseUtils ../../../core/Handles ../../../core/watchUtils".split(" "),function(k,l,c,d,n,p){let q=function(){function m(a){this._parentViewModel=a;this._loadingTask=this._visualization=null;this._handles=new n}var e=m.prototype;e.initialize=function(){this._handles.add(p.init(this._parentViewModel,"view",()=>this._load()))};e.destroy=function(){this._handles.destroy();this._handles=null;c.isSome(this._loadingTask)&&(this._loadingTask.abort(),
this._loadingTask=null);c.isSome(this._visualization)&&(this._visualization.destroy(),this._visualization=null)};e._load=function(){c.isSome(this._loadingTask)&&this._loadingTask.abort();this._loadingTask=d.createTask(async a=>{const b=this._parentViewModel;if("2d"===b.view.type){var h=new Promise(function(f,g){k(["./ElevationProfileVisualization2D"],f,g)});this._visualization=new ((await d.whenOrAbort(h,a)).ElevationProfileVisualization2D)(b)}else"3d"===b.view.type&&(h=new Promise(function(f,g){k(["./ElevationProfileVisualization3D"],
f,g)}),this._visualization=new ((await d.whenOrAbort(h,a)).ElevationProfileVisualization3D)(b))})};return m}();l.ElevationProfileVisualization=q;Object.defineProperty(l,"__esModule",{value:!0})});