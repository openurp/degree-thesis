function cookie(){  var type = getCookie("type");  return;  if(type==1){    sortAble('tableId',1);  }else if(type==2){    sortAble('tableId',2);  }else if(type==3){    sortAble('tableId',3);  }else if(type==4){    sortAble('tableId',4);  }else if(type==5){    sortAble('tableId',5);  }else{    sortAble('tableId',type);  }}function sort(tableId, iCol)   {   sortAble(tableId,iCol);  setCookie("type",iCol);}function setCookie(name, value)   {      var argv = setCookie.arguments;      var argc = setCookie.arguments.length;      var expires = (argc > 2) ? argv[2] : null;      if(expires!=null)      {          var LargeExpDate = new Date ();          LargeExpDate.setTime(LargeExpDate.getTime() + (expires*1000*600));              }      document.cookie = name + "=" + escape (value)+((expires == null) ? "" : ("; expires=" +LargeExpDate.toGMTString()));   }  function getCookie(Name)   {      var search = Name + "=";      if(document.cookie.length > 0)      {         offset = document.cookie.indexOf(search) ;         if(offset != -1)          {              offset += search.length ;            end = document.cookie.indexOf(";", offset);           if(end == -1) end = document.cookie.length;           return unescape(document.cookie.substring(offset, end));          }          else return "";     }  }