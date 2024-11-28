[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="4" height="30">${office.name} 教师列表</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">教师工号</td>
    <td class="header" align="center" height="30">姓名</td>
    <td class="header" align="center" height="30">所在答辩组</td>
  </tr>
  [#list users?sort_by('code') as user]
    <tr>
      <td class="data" align="center" height="30">${user_index+1}</td>
      <td class="data" align="center" height="30">${user.code}</td>
      <td class="data" align="center" height="30">${user.name}</td>
      <td class="data" align="center" height="30">
      [#list groups.get(user)?if_exists as g]
        <a href="${b.url('!info?id='+g.id)}">第${g.idx}组</a>&nbsp;
      [/#list]
      </td>
    </tr>
  [/#list]
</table>
</div>
[@b.foot/]
