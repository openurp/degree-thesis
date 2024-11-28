[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">

<table cellspacing="0" cellpadding="0" border="0" width="100%">
  <tr>
    <td class="title">第${defenseGroup.idx}答辩组情况</td>
  </tr>
</table>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="4">教师列表</td>
  </tr>
  <tr>
    <td class="header" align="center">序号</td>
    <td class="header" align="center">教师学院</td>
    <td class="header" align="center">姓名</td>
    <td class="header" align="center">答辩组职务</td>
  </tr>
  [#list defenseGroup.members?sort_by("leader")?reverse as member]
    <tr>
      <td class="data" align="center">${member_index+1}</td>
      <td class="data" align="center">${member.user.department.name}</td>
      <td class="data" align="center">${member.user.name}</td>
      <td class="data" align="center">${member.leader?string('组长','组员')}</td>
    </tr>
  [/#list]
  [#if defenseGroup.secretary??]
    <tr>
      <td class="data" align="center">${defenseGroup.members?size+1}</td>
      <td class="data" align="center">${defenseGroup.secretary.department.name}</td>
      <td class="data" align="center">${defenseGroup.secretary.name}</td>
      <td class="data" align="center">答辩秘书</td>
    </tr>
  [/#if]
</table>
<br/>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="8">学生列表</td>
  </tr>
  <tr>
    <td class="header" align="center">序号</td>
    <td class="header" align="center">学号</td>
    <td class="header" align="center">姓名</td>
    <td class="header" align="center">专业班级</td>
    <td class="header" align="center">指导教师</td>
    <td class="header" align="center">论文题目</td>
  </tr>
    [#list writers as writer]
    <tr>
      <td class="data" align="center">${writer_index+1 }</td>
      <td class="data" align="center">${writer.std.code}</td>
      <td class="data" align="center">${writer.std.name}</td>
      <td class="data" align="center">${writer.major.name} ${(writer.squad.name)!}</td>
      <td class="data" align="center">${writer.advisor.teacher.name}</td>
      <td class="data" align="center">${writer.thesisTitle!}</td>
    </tr>
  [/#list]
</table>
<br/>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7">答辩时间、地点</td>
  </tr>
  <tr>
    <td class="data" colspan="7">
      ${defenseGroup.beginAt?string('MM月dd日HH:mm')} -  ${defenseGroup.endAt?string('MM月dd日HH:mm')}
      ${defenseGroup.place!}
    </td>
  </tr>
</table>
</div>
[@b.foot/]
