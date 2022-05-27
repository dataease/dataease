// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["exports","../../../core/Error","../../support/utils","../../support/adapters/support/layerUtils"],function(h,k,l,q){function r(a){a=a.map(d=>`$feature["${d}"];`);return a.length?a.join("\n")+"\n":""}function n(a,d,e){a="number"===d?a:"date"===d?a.getTime():'$feature["'+a+'"]';return"var "+e+" \x3d "+a+";"}const p=["date","number"];h.getAgeExpressions=function(a){const {layer:d,startTime:e,endTime:f}=a;var b=q.createLayerAdapter(d),c=a.unit||"days";a=l.getDateDiffSQL(b,e,f,c);{var g=l.getDateType(b,
e);b=l.getDateType(b,f);c=[n(e,g,"startTime"),n(f,b,"endTime"),`var retVal = null;

    if (startTime != null && endTime != null) {
      startTime = Date(startTime);
      endTime = Date(endTime);
      retVal = DateDiff(endTime, startTime, "${c}");
    }

    return retVal;`];const m=[];"field"===g&&m.push(e);"field"===b&&m.push(f);g=r(m)+c.join("\n")}return{valueExpression:g,statisticsQuery:a,histogramQuery:a}};h.supportedAgeUnits="years months days hours minutes seconds".split(" ");h.verifyDates=function(a,d,e,f){const b=[];let c=null;[d,e].every(g=>{(g=l.getDateType(a,g))&&b.push(g);return!!g})?b[0]===b[1]?"field"===b[0]?d===e&&(c=new k(f,"'startTime' and 'endTime' parameters cannot be identical")):c=new k(f,"Invalid combination of 'startTime' and 'endTime' parameters"):
-1<p.indexOf(b[0])&&-1<p.indexOf(b[1])&&(c=new k(f,"Invalid combination of 'startTime' and 'endTime' parameters")):c=new k(f,"'startTime' and 'endTime' parameters must be one of these values: a date object, unix epoch time, name of a valid date field or \x3cnow\x3e");return c};Object.defineProperty(h,"__esModule",{value:!0})});