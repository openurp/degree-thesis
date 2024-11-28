<table width="100%" class="list_table">
  <tr>
    <td class="header" height="30" width="150">工作阶段</td>
    <td class="header" height="30" width="300">安排</td>
    <td class="header" height="30">说明</td>
  </tr>
    [#include "stage_descriptions.ftl"/]
    [#list plan.times?sort_by(["stage","id"]) as time]
    [#if time.stage.id==17][#continue/][/#if]
    <tr>
      <td height="30"  class="title">${time.stage.name}</td>
      <td height="30"  class="data">&nbsp;&nbsp;
        ${time.beginOn}到${time.endOn}</td>
      <td class="data" height="30" align="left"><p>${descriptions[time.stage.id?string]!}</p></td>
    </tr>
    [/#list]
</table>
