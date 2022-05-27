// All material copyright ESRI, All Rights Reserved, unless otherwise specified.
// See https://js.arcgis.com/4.18/esri/copyright.txt for details.
//>>built
define(["../../core/Error","../support/utils","../statistics/support/ageUtils","../statistics/summaryStatisticsForAge"],function(d,g,h,e){function k(a){const b=Math.abs(a.avg);let c=null;h.supportedAgeUnits.some(f=>{b>2*g.unitValueInDays[f]&&(c=f);return!!c});return c}return async function(a){var b={...a,unit:"days"};const c=await e(b);if(null==c.avg)throw new d("age-unit:insufficient-info","'avg' statistics is invalid");a=k(c);if("days"===a)return{unit:a,statistics:c};b.unit=a;b=await e(b);if(null==
b.avg)throw new d("age-unit:insufficient-info","'avg' statistics is invalid");return{unit:a,statistics:b}}});