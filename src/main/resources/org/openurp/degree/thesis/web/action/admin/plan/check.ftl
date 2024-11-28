[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript" src="${b.base}/static/scripts/util.js"></script>
<div class="container-fluid"><br>
[@b.form id="form" name="form" action="!saveCheck?id="+departPlan.id]
  <table width="100%" class="list_table">
      <tr>
      <td align="center" height="30" colspan="4" class="title">毕业论文(设计)工作计划审查</td>
      </tr>
    <tr>
      <td class="header" height="30" width="150">工作阶段</td>
      <td class="header" height="30" width="30%">安排（开始~结束日期）</td>
      <td class="header" height="30">说明
      </td>
    </tr>
    [#include "stage_descriptions.ftl"/]
    [#list departPlan.times?sort_by(["stage","id"]) as time]
    [#if time.stage.id==17][#continue/][/#if]
    <tr>
      <td height="30" class="title">${time.stage.name}</td>
      <td height="30"  class="data">
          <input id="${time.stage.id}_beginOn" name="${time.stage.id}_beginOn" onclick="WdatePicker({})" type="text" value="${time.beginOn!}"/>
          ~<input id="${time.stage.id}_endOn" name="${time.stage.id}_endOn" onclick="WdatePicker({})" type="text" value="${time.endOn!}"/>
      </td>
      <td class="data" height="30" align="left"><p>${descriptions[time.stage.id?string]!}</p></td>
    </tr>
    [/#list]
    <tr>
      <td height="30" width="200" class="title">审查:</td>
      <td class="data" height="30" width="200" colspan="2">
        <input type="radio" id="zt1" name="departPlan.status" value="${Passed.id}" checked="checked">审查通过&nbsp;&nbsp;&nbsp;
        <input type="radio" id="zt2" name="departPlan.status" value="${Rejected.id}">审查未通过
      </td>
    </tr>
    <tr>
      <td height="30" width="200" class="title">审查意见：</td>
      <td class="data" height="30" width="200" colspan="2">
        <textarea cols="80" rows="5" id="scyj" name="departPlan.auditOpinion">${departPlan.auditOpinion!}</textarea>
      </td>
    </tr>
    <tr>
      <td class="title" height="30">操作:</td>
      <td class="data" height="30" colspan="2">
        [@b.submit id="button" name="button" value="审查"/]
        &nbsp;&nbsp;<input type="button" value="返回" onclick="history.back()">
      </td>
    </tr>
  </table>
[/@]
</div>
[@b.foot/]
