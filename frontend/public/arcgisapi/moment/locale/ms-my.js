//>>built
(function(c,a){"object"===typeof exports&&"undefined"!==typeof module&&"function"===typeof require?a(require("../moment")):"function"===typeof define&&define.amd?define(["../moment"],a):a(c.moment)})(this,function(c){return c.defineLocale("ms-my",{months:"Januari Februari Mac April Mei Jun Julai Ogos September Oktober November Disember".split(" "),monthsShort:"Jan Feb Mac Apr Mei Jun Jul Ogs Sep Okt Nov Dis".split(" "),weekdays:"Ahad Isnin Selasa Rabu Khamis Jumaat Sabtu".split(" "),weekdaysShort:"Ahd Isn Sel Rab Kha Jum Sab".split(" "),
weekdaysMin:"Ah Is Sl Rb Km Jm Sb".split(" "),longDateFormat:{LT:"HH.mm",LTS:"HH.mm.ss",L:"DD/MM/YYYY",LL:"D MMMM YYYY",LLL:"D MMMM YYYY [pukul] HH.mm",LLLL:"dddd, D MMMM YYYY [pukul] HH.mm"},meridiemParse:/pagi|tengahari|petang|malam/,meridiemHour:function(a,b){12===a&&(a=0);if("pagi"===b)return a;if("tengahari"===b)return 11<=a?a:a+12;if("petang"===b||"malam"===b)return a+12},meridiem:function(a,b,d){return 11>a?"pagi":15>a?"tengahari":19>a?"petang":"malam"},calendar:{sameDay:"[Hari ini pukul] LT",
nextDay:"[Esok pukul] LT",nextWeek:"dddd [pukul] LT",lastDay:"[Kelmarin pukul] LT",lastWeek:"dddd [lepas pukul] LT",sameElse:"L"},relativeTime:{future:"dalam %s",past:"%s yang lepas",s:"beberapa saat",ss:"%d saat",m:"seminit",mm:"%d minit",h:"sejam",hh:"%d jam",d:"sehari",dd:"%d hari",M:"sebulan",MM:"%d bulan",y:"setahun",yy:"%d tahun"},week:{dow:1,doy:7}})});