/**
http://rank.maplestory.nexon.com/MapleStory/Page/Gnx.aspx?URL=Ranking%2fTotalRanking
**/

(function() {

var doc = document.getElementsByTagName("a");
var ele = null;
for(var i=0;i<doc.length;i+=1){
    if(doc[i].innerHTML == "%nick"){
        ele = doc[i];
        break;
    }
}
if(ele == null){
	return null;
}
var str = ele.getAttribute("onclick");
return str.substring(str.indexOf("(")+1,str.indexOf(";")-1).split("\"").join("");
})();