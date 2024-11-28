[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
[#assign roundName][#if round==1]初选[#elseif round==2]补选[#else]最终[/#if][/#assign]
<div class="container-fluid">
<div style="text-align:right">[@b.a href="!search?round="+round]已选列表[/@]</div>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="5" height="30">学生未参加${roundName}列表(共有学生${writers?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">院系</td>
    <td class="header" align="center" height="30">专业班级</td>
  </tr>
  [#list writers as writer ]
    <tr>
      <td class="data" align="center" height="30">${writer_index+1}</td>
      <td class="data" align="center" height="30">${writer.std.code}</td>
      <td class="data" align="center" height="30"><a href="${b.base}/writer/view.htm?id=${writer.id}">${writer.std.name}</a></td>
      <td class="data" align="center" height="30">${(writer.department.name)!}</td>
      <td class="data" align="center" height="30">${writer.major.name}${(writer.squad.name)!}</td>
    </tr>
  [/#list]
</table>
</div>
[@b.foot/]
