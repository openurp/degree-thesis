[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript" src="${b.base}/static/scripts/util.js"></script>
<script type="text/javascript" src="${b.base}/static/scripts/tinybox.js"></script>
<script type="text/javascript">
  $(document).ready(function(){
    $("#button").click(function(){
      var error=false;
      $(":text").each(function(){
        $(this).val($.trim($(this).val()));
        if(!$(this).val().match(/^(\d{4})(-)(\d{2})\2(\d{2})$/)){
          error = true;
        }
      });
      if(!error){
        $("#form").submit();
      }else{
        alert("输入数据不能为空或格式不正确！");
      }
    });
  });

</script>

<div class="container-fluid"><br>

<form id="form" name="form" action="${b.url('!save?id='+plan.id)}" method="post">
  <table width="100%" class="list_table">
      <tr>
      <td align="center" height="30" colspan="4" class="title">制定毕业论文(设计)工作计划</td>
      </tr>
    <tr>
      <td class="header" height="30" width="150">工作阶段</td>
      <td class="header" height="30" width="25%">安排（开始~结束日期）</td>
      <td class="header" height="30">说明
      </td>
    </tr>
    [#include "../plan/stage_descriptions.ftl"/]
    [#list plan.times as time]
    [#if time.stage.id==17][#continue/][/#if]
    [#if time.stage.subCount=0]
    <tr>
      <td height="30" class="title">${time.stage.name}</td>
      <td height="30"  class="data">&nbsp;
          <input id="${time.stage.id}_beginOn" name="${time.stage.id}_beginOn" onclick="WdatePicker({})" type="text" style="width:120px" value="${time.beginOn}"/>
          ~<input id="${time.stage.id}_endOn" name="${time.stage.id}_endOn" onclick="WdatePicker({})" type="text" style="width:120px" value="${time.endOn}"/>
      </td>
      <td class="data" height="30" align="left"><p>${descriptions[time.stage.id?string]!}</p></td>
    </tr>
    [/#if]
    [/#list]
    <tr>
      <td class="title" height="30">操作:</td>
      <td class="data" height="30" colspan="2">
        <input type="button" id="button" name="button" value="保存" />
        &nbsp;&nbsp;<input type="button" value="返回" onclick="history.back()">
      </td>
    </tr>
  </table>
</form>
</div>
[@b.foot/]
