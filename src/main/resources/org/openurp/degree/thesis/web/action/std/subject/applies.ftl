[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7" height="30">选择此题目学生列表(共有学生${applies?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">专业班级</td>
  </tr>
  [#list applies as apply]
    <tr>
      <td class="data" align="center" height="30">${apply_index+1 }</td>
      <td class="data" align="center" height="30">${apply.writer.std.code}</td>
      <td class="data" align="center" height="30">${apply.writer.std.name}</td>
      <td class="data" align="center" height="30">${apply.writer.major.name} ${(writer.squad.name)!}</td>
    </tr>
  [/#list]
</table>
<br>
</div>
[@b.foot/]
