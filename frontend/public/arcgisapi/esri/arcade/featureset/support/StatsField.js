// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["./shared","../../../core/sql/WhereClause","./sqlUtils"],function(h,f,k){return function(){function e(){}var g=e.prototype;g.clone=function(){const a=new e;a.field=this.field;a.tofieldname=this.tofieldname;a.typeofstat=this.typeofstat;a.workingexpr=this.workingexpr;return a};e.parseStatField=function(a,c,d){const b=new e;b.field=a;a=f.WhereClause.create(c,d);if("function"===a.parseTree.type)if(0===a.parseTree.args.value.length)c={name:a.parseTree.name,expr:null};else{if(1<a.parseTree.args.value.length)throw Error("Statistic does not have 1 or 0 Parameters");
c=f.WhereClause.create(k.toWhereClauseFromTree(a.parseTree.args.value[0],h.FeatureServiceDatabaseType.Standardised,a.parameters),a.fieldsIndex);c={name:a.parseTree.name,expr:c}}else c=null;if(null===c)throw Error("Invalid Statistic Function");d=c.name.toUpperCase().trim();if("MIN"===d){if(b.typeofstat="MIN",b.workingexpr=c.expr,null===a)throw Error("Invalid Statistic Function Parameters");}else if("MAX"===d){if(b.typeofstat="MAX",b.workingexpr=c.expr,null===a)throw Error("Invalid Statistic Function Parameters");
}else if("COUNT"===d)b.typeofstat="COUNT",b.workingexpr=c.expr;else if("STDEV"===d){if(b.typeofstat="STDDEV",b.workingexpr=c.expr,null===a)throw Error("Invalid Statistic Function Parameters");}else if("SUM"===d){if(b.typeofstat="SUM",b.workingexpr=c.expr,null===a)throw Error("Invalid Statistic Function Parameters");}else if("MEAN"===d){if(b.typeofstat="AVG",b.workingexpr=c.expr,null===a)throw Error("Invalid Statistic Function Parameters");}else if("AVG"===d){if(b.typeofstat="AVG",b.workingexpr=c.expr,
null===a)throw Error("Invalid Statistic Function Parameters");}else if("VAR"===d){if(b.typeofstat="VAR",b.workingexpr=c.expr,null===a)throw Error("Invalid Statistic Function Parameters");}else throw Error("Invalid Statistic Function");return b};g.toStatisticsName=function(){switch(this.typeofstat.toUpperCase()){case "MIN":return"min";case "MAX":return"max";case "SUM":return"sum";case "COUNT":return"count";case "VAR":return"var";case "STDDEV":return"stddev";case "AVG":return"avg";default:return"count"}};
return e}()});