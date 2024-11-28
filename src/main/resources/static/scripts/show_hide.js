var imgDir = "images/";

function CommonLoad()
{
  if ("function" == typeof(process_button))
  {
    with( document )
    {
      ondblclick = onmousedown = onmouseup = onmouseover = onmouseout = onkeyup = onclick = onselectstart = process_button;
    }
  }
}
function Window_Load()
{
  if ("function" == typeof(CommonLoad)) CommonLoad();
  //初始化时，显示标题
    //parent.topmain.rows="80,33,*";
    //document.all.tags( "FONT" )[0].innerHTML = "隐藏标题";
    //alert(document.all.tags( "FONT" )[0].innerHTML);
    //document.all.tags( "IMG" )[0].src = "mainFrame/img/hide_top.gif";
    //document.action = "HIDE_Top";
  //初始话时，显示菜单
    //top.bottommain.cols = "180,*";
    //document.all.tags( "FONT" )[0].innerHTML = "隐藏菜单";
    //document.all.tags( "IMG" )[0].src = "mainFrame/img/hide_menu.gif";
    //document.action = "HIDE";
  //nScreenWidth = window.screen.width;
  //if ( 800 >= nScreenWidth || !(false) )
}

function process_button()
{
  try
  {
  var oEl = event.srcElement;
  event.cancelBubble = true;
  while( -1 == oEl.className.indexOf( "Btn" ) )  //check if it is button
  {
    oEl = oEl.parentElement;
    if( !oEl ) return;
  }
  //the className is named of XXXBtnOff or XXXBtnUp......
  var baseClass = oEl.className.substring( 0 , oEl.className.indexOf( "Btn" ) + 3 );  //get the className
  var btnImage = null;
  if( oEl.IMG ) btnImage = oEl.all.tags( "IMG" )[0];  //check if there is a img to change
  switch( event.type )
  {
    case "mouseout" :
      if( oEl.contains( event.toElement ) ) return;
      if( btnImage )
      {
        btnImage.src =  btnImage.src.substring( 0 , btnImage.src.length - 5 ) + "1.gif";
      }
      if( oEl.STATE )
      {
        oEl.className = baseClass + oEl.STATE;
      }
      else
      {
        oEl.className = baseClass + "Off";
      }
      break;
    case "mouseover" :
      if( oEl.contains( event.fromElement ) ) return;
      if( btnImage )
      {
        btnImage.src =  btnImage.src.substring( 0 , btnImage.src.length - 5 ) + "2.gif";
      }
      if( oEl.STATE )
      {
        var tmpState = oEl.STATE == "Off" ? "Up" : "Down";
        oEl.className = baseClass + tmpState;
      }
      else
      {
        oEl.className = baseClass + "Up";
      }
      break;
    case "mousedown" :
      if (event.button == 1)
      oEl.className = baseClass + "Down";
      break;

    case "mouseup" :
      if( oEl.STATE )
      {
        oEl.STATE = oEl.STATE == "Off" ? "On" : "Off";
        var tmpState = oEl.STATE == "On" ? "Down" : "Up";
        oEl.className = baseClass + tmpState;
      }
      else
      {
        oEl.className = baseClass + "Up";
      }
      break;

    case "click" :
      event.returnValue = doButtonClick( oEl );
      break;

    case "dblclick" :
      event.returnValue = doButtonClick( oEl );
      break;

    case "keyup" :
      if( 13 == event.keyCode )
      {
        if( oEl.STATE )
        {
          oEl.STATE = oEl.STATE == "Off" ? "On" : "Off";
          var tmpState = oEl.STATE == "On" ? "Down" : "Up";
          oEl.className = baseClass + tmpState;
        }
        else
        {
          oEl.className = baseClass + "Up";
        }
        event.returnValue = doButtonClick( oEl );
      }
      break;

    case "selectstart" :
      if( oEl )
      {
        event.returnValue = false;
        return false;
      }
      break;
    default :
      break;

  }
  }
  catch(e)
  {
  }
}

function doButtonClick( oEl )
{
  if( "function" == typeof( fnCustomAction ) )
  {
    return fnCustomAction( oEl );
  }
}

function fnCustomAction( oEl )    //判断按钮所要执行的操作
{
  if( "HIDE" == oEl.action )  //隐藏菜单
  {
    top.bottommain.cols = "0,*";
    oEl.all.tags( "FONT" )[0].innerHTML = "显示菜单";
    oEl.all.tags( "IMG" )[0].src = imgDir+"show_menu.gif";
    oEl.action = "SHOW";
    return true;
  }
  else if( "SHOW" == oEl.action ) //显示菜单
  {
    top.bottommain.cols = "180,*";
    oEl.all.tags( "FONT" )[0].innerHTML = "隐藏菜单";
    oEl.all.tags( "IMG" )[0].src = imgDir+"hide_menu.gif";
    oEl.action = "HIDE";
    return true;
  }
  else if( "HIDE_Top" == oEl.action )  //隐藏标题
  {
    parent.topmain.rows="0,24,*";
    oEl.all.tags( "FONT" )[0].innerHTML = "显示标题";
    oEl.all.tags( "IMG" )[0].src = imgDir+"show_top.gif";
    oEl.action = "SHOW_Top";
    return true;
  }
  else if( "SHOW_Top" == oEl.action )  //显示标题
  {
    parent.topmain.rows="80,24,*";
    oEl.all.tags( "FONT" )[0].innerHTML = "隐藏标题";
    oEl.all.tags( "IMG" )[0].src = imgDir+"hide_top.gif";
    oEl.action = "HIDE_Top";
    return true;
  }
  else if ( "PERSONAL" == oEl.action )   // 个性化桌面
  {
    parent.location.href = "logout.htm"
  }
  else if ( "BACK" ==oEl.action )    //返回按钮
  {
    top.frames["mainFrame"].location.href = "index.html"
  }
  else if ( "HELP" ==oEl.action )    //帮助按钮
  {
    get_help();
  }
  return true;
}

var show_help  =  false;
var w_help;
function get_help()
{
  screenWidth    =  window.screen.availWidth;
  screenHeight  =  window.screen.availHeight;
  try
  {
    urlHelp = getFrameUrl();
    astrLocation = window.location.href.split("/");
    strLoc = "";
    for ( i = 5; i < astrLocation.length; i++ )
    {
      strLoc = strLoc + "../";
    }
    if ( false == show_help || "object" != typeof( w_help.clientInformation ) )
    {
      if ( screenWidth > 800 )
      {
        winHelpWidth = 230;
      }
      else
      {
        winHelpWidth = 180;
      }
      top.window.resizeTo( screenWidth - winHelpWidth + 4, screenHeight + 8 );
      top.window.moveTo( -4, -4 );
      w_help = window.open( strLoc + "help.do" , "_blank" , "top=0,left=" + (screenWidth - winHelpWidth) + ",width=" + (winHelpWidth-10) + ",height=" + ( screenHeight - 60 + 4) + ",location=no,scrollbars=1,toolbar=no,resizable=1" );
      w_help.resizeTo( winHelpWidth, screenHeight + 8 );
      w_help.moveTo( screenWidth - winHelpWidth, 0 );
      show_help = true;
    }
    else
    {
      if ( "object" == typeof( w_help ) )
      {
        strWinHelpUrl = w_help.frames[1].location.href;
        strOldUrl    = strWinHelpUrl.substring( strWinHelpUrl.indexOf("id=")+3, strWinHelpUrl.length );
        if ( strOldUrl != urlHelp )
        {
          w_help.frames[1].location.href = strLoc + "public/help/helpmain.jsp" ;
        }
        w_help.focus();
//        w_help.close();
      }
//      show_help = false;
    }
  }
  catch(e)
  {
  }
}

window.onunload = shutoffall;
function shutoffall()
{
  if ( "object" == typeof( w_help ) )
  {
    top.window.resizeTo( screenWidth , screenHeight + 8 );
    w_help.close();
  }
}

function getFrameUrl()
{
  var strTemp = new Array();
  var strTemp2= new Array();
  var strTemp2=window.location.href.split('/');
  if ( ("index.jsp" == strTemp2[strTemp2.length-1]) || ( 0 == strTemp2[strTemp2.length-1].length) )
  {
    return "index.jsp";
  }
  else
  {
    num     = top.frames.length;
    curFrame   = top.frames( num-1 );
    url     = curFrame.location.href;
    if ( (-1 != url.indexOf("noteframe.jsp")) || (-1 != url.indexOf("jwmaster.jsp")) || (-1 != url.indexOf("teacher.jsp")) || (-1 != url.indexOf("masterfun.jsp")) || (-1 != url.indexOf("bzr.jsp")) || (-1 != url.indexOf("lecturewrite.jsp")) )
    {
      strTemp = url.split('?');
    }
    else
    {
      strTemp[0] = url;
    }
    strTemp    = strTemp[0].split('/');
    urlTemp    = strTemp.slice( 4, strTemp.length );
    url = urlTemp.join("/");
    url = url.replace( '&',';' );
    //alert( url );
    return url;
  }
}
