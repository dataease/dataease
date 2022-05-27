// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define("exports ../../core/Error ../../core/promiseUtils ../../intl/messages ../../symbols/TextSymbol ../../chunks/symbols ../../layers/support/LabelExpressionInfo ../../layers/support/LabelClass ../heuristics/clusterMinSize ../support/clusterUtils ../../views/2d/layers/support/clusterUtils".split(" "),function(p,r,m,t,u,A,v,w,x,g,y){async function z(a){const {layer:b,renderer:h,view:c}=a;await m.all([b.load(),c.when()]);a=h||b.renderer;return y.isClusterCompatibleRenderer(a)?{layer:b,renderer:a,
view:c}:m.reject(new r("clusters-label-schemes:invalid-parameters","'renderer' is not valid"))}async function q(a){const {renderer:b,view:h,statInfo:c}=a,f=null==c?void 0:c.statisticType,e=c?g.getClusterField(c.attributeInfo,f):g.clusterCountField;{a="type"===f?g.getPredominantTypeExpression("unique-value"===b.type?b.uniqueValueInfos:[],e,a.noneValueLabel):`
  $feature["${e}"];
  var value = $feature["${e}"];
  var num = Count(Text(Round(value)));
  var label = When(
    num < 4, Text(value, "#.#"),
    num == 4, Text(value / Pow(10, 3), "#.0k"),
    num <= 6, Text(value / Pow(10, 3), "#k"),
    num == 7, Text(value / Pow(10, 6), "#.0m"),
    num > 7, Text(value / Pow(10, 6), "#m"),
    Text(value, "#,###")
  );
  return label;
  `;const n=new u({haloColor:"#373837",haloSize:"1px",color:"#f0f0f0",font:{family:"Noto Sans",size:"12px",weight:"bold"}});a=new w({symbol:n,deconflictionStrategy:"none",labelPlacement:"center-center",labelExpressionInfo:new v({expression:a})})}return{name:c?`clusters-${f}-${g.getClusterFieldHash(e,f)}`:"clusters-count",labelingInfo:[a],clusterMinSize:await x(a.symbol,h),fieldName:e}}p.getLabelSchemes=async function(a){const [{renderer:b,layer:h,view:c},f]=await m.all([z(a),t.fetchMessageBundle("esri/smartMapping/t9n/smartMapping")]);
if(!b)return null;a=null;const e=[];{var n=g.getStatisticInfos(h,b,!1);const l=new Map;for(d of n)"size"===d.attributeInfo.vvType?l.set(d.statisticHash,d):l.has(d.statisticHash)||l.set(d.statisticHash,d);var d=[...l.values()]}for(var k of d)d=await q({renderer:b,view:c,statInfo:k,noneValueLabel:f.clusters.predominantNoneValue}),"size"===k.attributeInfo.vvType?a=d:e.push(d);k=await q({renderer:b,view:c});a?e.unshift(k):a=k;return{primaryScheme:a,secondarySchemes:e}};Object.defineProperty(p,"__esModule",
{value:!0})});