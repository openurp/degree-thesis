[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<link href="${b.base}/static/stylesheets/tinybox.css"  rel="stylesheet" type="text/css">
<script src="${b.base}/static/scripts/util.js"></script>
<script src="${b.base}/static/scripts/jer_normal.js"></script>
<script>
$(document).ready(function(){
  $("body").ajaxError(function(){
    var content="<p>服务器错误，请联系管理员！消息提示将在5秒后关闭！</p>";
      TINY.box.show(content,0,800,0,0,5);
  });
  $("#button").click(function(){
    if($.trim($("#contents1").val())==''&&$.trim($("#contents2").val())==''){
      alert('至少填写一项指导内容!');
      return;
    }

    $("#button").attr("disabled",true);
    $("#button").attr("value","正在提交...");
    var error=false;
    $("#contents1Error,#contents2Error").html('');

    if($.trim($("#contents1").val())!=''){
      if($.trim($("#contents1").val()).length>500||$.trim($("#contents1").val()).length<50)
      {
        $("#contents1Error").html("指导内容必须在50-500字之间");
        error=true;
      }
    }
    if($.trim($("#contents2").val())!=''){
      if($.trim($("#contents2").val()).length>500||$.trim($("#contents2").val()).length<50)
      {
        $("#contents2Error").html("指导内容必须在50-500字之间");
        error=true;
      }
    }
    if(!error)
    {
      $("#form").submit();
    }else
    {
      $("#button").attr("disabled",false);
      $("#button").attr("value","添加");
    }
  });
});
</script>
<div class="container-fluid">
[@b.form id="form" name="form" action="!save"]
  <table class="list_table" width="100%">
    <tr>
      <td class="title" height="30" colspan="2">
      ${stage}
      </td>
    </tr>
    <tr>
      <td class="title" height="30" width="100px">指导时间</td>
      <td>
      [#assign stageTime= plan.getStageTime(stage)/]
      ${stageTime.beginOn }到${stageTime.endOn }
      </td>
    </tr>
    <tr>
      <td class="title" height="30">第一次指导</td>
      <td>
      <div class="jer_info_line">指导内容必须在50-500字之间,已输入<span class="word_count">0</span>字</div>
      <textarea rows="10" cols="120" id="contents1" name="contents1" onkeyup="keyup_word_count(this)">[#list guidances as g][#if g.idx==1]${g.contents!}[/#if][/#list]</textarea>
      <br><span id="contents1Error" ></span></td>
    </tr>
    <tr>
      <td class="title" height="30">第二次指导</td>
      <td>
      <div class="jer_info_line">指导内容必须在50-500字之间,已输入<span class="word_count">0</span>字</div>
      <textarea rows="10" cols="120" id="contents2" name="contents2" onkeyup="keyup_word_count(this)">[#list guidances as g][#if g.idx==2]${g.contents!}[/#if][/#list]</textarea>
      <br><span id="contents2Error" ></span></td>
    </tr>
    <tr>
      <td class="title" height="30">操作：</td>
      <td>
        <input type="hidden" name="stage" value="${stage.id}">
        [@b.submit value="提交"/]
        &nbsp;&nbsp;&nbsp;&nbsp;
        <input type="button" onclick="history.back()" value="返回">
      </td>
    </tr>
  </table>
  [/@]
</div>
<script>
jQuery(document).ready(function($) {
  //默认显示字数
  $('#contents1,#contents2').each(function(index, el) {
    keyup_word_count($(this));
  });
});
</script>
[@b.foot/]
