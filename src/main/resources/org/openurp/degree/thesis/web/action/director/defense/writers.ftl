[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7" height="30">本教研室学生答辩分组情况</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">专业班级</td>
    <td class="header" align="center" height="30">论文题目</td>
    <td class="header" align="center" height="30">指导教师</td>
    <td class="header" align="center" height="30">所在答辩组</td>
  </tr>
  [#list inGroupWriters as writer]
    <tr>
      <td class="data" align="center" height="30">${writer_index+1 }</td>
      <td class="data" align="center" height="30">${writer.std.code}</td>
      <td class="data" align="center" height="30">${writer.std.name}</td>
      <td class="data" align="center" height="30">${writer.major.name} ${(writer.squad.name)!}</td>
      <td class="data" align="center" height="30">${writer.thesisTitle!}</td>
      <td class="data" align="center" height="30">${writer.advisor.teacher.name}</td>
      <td class="data" align="center" height="30">第${(groups.get(writer).idx)!}组</td>
    </tr>
  [/#list]
  [#list noGroupWriters?sort_by("code") as writer]
    <tr>
      <td class="data" align="center" height="30">${writer_index+1 }</td>
      <td class="data" align="center" height="30">${writer.std.code}</td>
      <td class="data" align="center" height="30">${writer.std.name}</td>
      <td class="data" align="center" height="30">${writer.major.name} ${(writer.squad.name)!}</td>
      <td class="data" align="center" height="30">${writer.thesisTitle!}</td>
      <td class="data" align="center" height="30">${writer.advisor.teacher.name}</td>
      <td class="data" align="center" height="30">--</td>
    </tr>
  [/#list]
</table>

</div>
[@b.foot/]
