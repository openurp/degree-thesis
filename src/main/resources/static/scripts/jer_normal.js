function showSubmitMask(msg) {
  if(typeof(msg)=='undefined')msg='正在处理数据...';
    if ($('#submitMask').length == 0) $('body').append('<div id="submitMask"></div><div id="submitMask_Msg"></div>');
    $('#submitMask').css({
        opacity: .5,
        width: $(document).width(),
        height: $(document).height()
    });
    $('#submitMask_Msg').show().text(msg);
}

function hideSubmitMask() {
    $('#submitMask,#submitMask_Msg').hide();
}
//获取http参数
function request(paras)
{
    var url = location.href;
    url=url.replace('?','#');
    var paraString = url.substring(url.indexOf("#")+1,url.length).split("&");
    var paraObj = {}
    for (i=0; j=paraString[i]; i++){
    paraObj[j.substring(0,j.indexOf("=")).toLowerCase()] = j.substring(j.indexOf("=")+1,j.length);
    }
    var returnValue = paraObj[paras.toLowerCase()];
    if(typeof(returnValue)=="undefined"){
    return "";
    }else{
    return decodeURIComponent(returnValue);
    }
}
function keydown_word_count(el){
    var len=$(el).val().length;
    $('#word_count').text(len);
    /*if(len<50){
        $('#btn_sub').attr('disabled','disabled');
    }else{
        $('#btn_sub').removeAttr('disabled');
    }*/
}
function keyup_word_count(el){
    var len=$(el).val().length;
    $(el).prev('div.jer_info_line').children('span.word_count').text(len);
}
function jer_loading(r){
    if(r==undefined||r){
        if($('#jer_loading_wrap').length==0)$('body').append('<div id="jer_loading_wrap"></div><div id="jer_loading_progress"><span></span></div>');
        $('#jer_loading_wrap').css({'opacity':'0.3','width':$(document).width(),'height':$(document).height()});
        $('#jer_loading_wrap,#jer_loading_progress').show();
    }else{
        $('#jer_loading_wrap,#jer_loading_progress').hide();
    }
}

function EncodeUtf8(s1)
{
      var s = escape(s1);
      var sa = s.split("%");
      var retV ="";
      if(sa[0] != "")
      {
         retV = sa[0];
      }
      for(var i = 1; i < sa.length; i ++)
      {
           if(sa[i].substring(0,1) == "u")
           {
               retV += Hex2Utf8(Str2Hex(sa[i].substring(1,5)));

           }
           else retV += "%" + sa[i];
      }

      return retV;
}
function Str2Hex(s)
{
      var c = "";
      var n;
      var ss = "0123456789ABCDEF";
      var digS = "";
      for(var i = 0; i < s.length; i ++)
      {
         c = s.charAt(i);
         n = ss.indexOf(c);
         digS += Dec2Dig(eval(n));

      }
      //return value;
      return digS;
}
function Dec2Dig(n1)
{
      var s = "";
      var n2 = 0;
      for(var i = 0; i < 4; i++)
      {
         n2 = Math.pow(2,3 - i);
         if(n1 >= n2)
         {
            s += '1';
            n1 = n1 - n2;
          }
         else
          s += '0';

      }
      return s;

}
function Dig2Dec(s)
{
      var retV = 0;
      if(s.length == 4)
      {
          for(var i = 0; i < 4; i ++)
          {
              retV += eval(s.charAt(i)) * Math.pow(2, 3 - i);
          }
          return retV;
      }
      return -1;
}
function Hex2Utf8(s)
{
     var retS = "";
     var tempS = "";
     var ss = "";
     if(s.length == 16)
     {
         tempS = "1110" + s.substring(0, 4);
         tempS += "10" + s.substring(4, 10);
         tempS += "10" + s.substring(10,16);
         var sss = "0123456789ABCDEF";
         for(var i = 0; i < 3; i ++)
         {
            retS += "%";
            ss = tempS.substring(i * 8, (eval(i)+1)*8);

            retS += sss.charAt(Dig2Dec(ss.substring(0,4)));
            retS += sss.charAt(Dig2Dec(ss.substring(4,8)));
         }
         return retS;
     }
     return "";
}
