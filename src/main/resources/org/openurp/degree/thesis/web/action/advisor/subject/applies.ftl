[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
  <font color="red">说明：此处学生选题结果列表所列学生是进行过选题并且已经选题成功的学生列表。</font>

<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7" height="30">学院已经选过题目学生列表(共有学生${applies?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">专业班级</td>
    <td class="header" align="center" height="30">手机</td>
    <td class="header" align="center" height="30">邮箱</td>
    <td class="header" align="center" height="30">题目名称</td>
  </tr>
  [#list applies as apply]
    <tr>
      <td class="data" align="center" height="30">${apply_index+1 }</td>
      <td class="data" align="center" height="30">${apply.writer.std.code}</td>
      <td class="data" align="center" height="30">${apply.writer.std.name}</td>
      <td class="data" align="center" height="30">${apply.writer.major.name} ${(apply.writer.squad.name)!}</td>
      <td class="data" align="center" height="30">${apply.writer.sj!}</td>
      <td class="data" align="center" height="30">${apply.writer.yx!}</td>
      <td class="data" align="center" height="30"><a href="${b.url('!info?id='+apply.last.id)}">${apply.last.name}</a></td>
    </tr>
  [/#list]
</table>
<br>
</div>
[@b.foot/]
