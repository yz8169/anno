var zhRunning="正在运行"
var zhInfo="信息"

function p(s) {
    return s < 10 ? '0' + s: s;
}

function getCurrentTime() {
    var myDate = new Date();
    var year=myDate.getFullYear();
    var month=myDate.getMonth()+1;
    var date=myDate.getDate();
    var h=myDate.getHours();
    var m=myDate.getMinutes();
    var s=myDate.getSeconds();
    var now=year+p(month)+p(date)+p(h)+p(m)+p(s);
    return now
}



