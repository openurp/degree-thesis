[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
<table cellspacing="0" cellpadding="0" border="0" width="800">
  <tr>
    <td class="title" height="30">第${defenseGroup.idx}答辩组情况<font color="red"></font></td>
  </tr>
</table>
<table class="list_table" width="800">
  <tr>
    <td class="title" colspan="4" height="30">教师列表</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">教师学院</td>
    <td class="header" align="center" height="30">姓名</td>
    <td class="header" align="center" height="30">答辩组职务</td>
  </tr>
  [#list defenseGroup.members as member]
    <tr>
      <td class="data" align="center" height="30">${member_index+1}</td>
      <td class="data" align="center" height="30">${member.user.department.name}</td>
      <td class="data" align="center" height="30">${member.user.name}</td>
      <td class="data" align="center" height="30">${member.leader?string('组长','组员')}</td>
    </tr>
  [/#list]
</table>
<br/>
<table class="list_table" width="800">
  <tr>
    <td class="title" colspan="8" height="30">学生列表</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">专业班级</td>
    <td class="header" align="center" height="30">指导教师</td>
    <td class="header" align="center" height="30">论文题目</td>
  </tr>
  [#list defenseGroup.orderedWriters as writer]
    <tr>
      <td class="data" align="center" height="30">${writer_index+1 }</td>
      <td class="data" align="center" height="30">${writer.std.code}</td>
      <td class="data" align="center" height="30">${writer.std.name}</td>
      <td class="data" align="center" height="30">${writer.major.name} ${(writer.squad.name)!}</td>
      <td class="data" align="center" height="30">${writer.advisor.teacher.name}</td>
      <td class="data" align="center" height="30">${writer.thesisTitle!}</td>
    </tr>
  [/#list]
</table>
<br/>
<table class="list_table" width="800">
  <tr>
    <td class="title" colspan="7" height="30">答辩时间、地点</td>
  </tr>
  <tr>
    <td class="data" colspan="7" height="30">${defenseGroup.beginAt?string('MM月dd日HH:mm')} -  ${defenseGroup.endAt?string('MM月dd日HH:mm')}</td>
  </tr>
</table>
<input type="button" value="返回" onclick="history.back();">
</div>
[@b.foot/]
