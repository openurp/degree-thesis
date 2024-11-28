<table width="100%" class="grid-table">
  <tr>
    <td align="center" height="30" colspan="3" class="title">
      ${plan.department.name}毕业论文(设计)工作计划</td>
  </tr>
  <tr>
    <td class="header" height="30" width="150" style="text-align:center">工作阶段</td>
    <td class="header" height="30" width="300">安排</td>
    <td class="header" height="30">说明</td>
  </tr>
  [#include "stage_descriptions.ftl"/]
  [#list plan.times?sort_by(["stage","id"]) as time]
  <tr>
    <td height="30"  class="title">${time.stage.name}</td>
    <td height="30"  class="data">&nbsp;&nbsp;
      ${time.beginOn}到${time.endOn}</td>
    <td class="data" height="30" align="left"><p>${descriptions[time.stage.id?string]!}</p></td>
  </tr>
  [/#list]
</table>
