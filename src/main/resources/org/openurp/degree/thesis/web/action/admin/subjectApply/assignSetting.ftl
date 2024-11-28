[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="9" height="30">为 ${writer.std.name} 分配题目列表(共有题目${subjects?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">题目名称</td>
    <td class="header" align="center" height="30">指导教师</td>
    <td class="header" align="center" height="30">面向专业</td>
    <td class="header" colspan="2" align="center" height="30">操作</td>
  </tr>
  [#list subjects as subject]
    <tr>
      <td class="data" align="center" height="30">${subject_index+1}</td>
      <td class="data" align="center" height="30">${subject.name}</td>
      <td class="data" align="center" height="30">${subject.advisor.teacher.name}</td>
      <td class="data" align="center" height="30">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
      <td class="data" align="center" height="30">[@b.a href="!assign?subjectId=" + subject.id + "&writerId=" + writer.id + "&next=" + Parameters['next']!'search'
        onclick="return bg.Go(this,null,'该操作将为该学生分配该题目，确认执行?')"]分配[/@]
      </td>
    </tr>
  [/#list]
</table>
<input type="button" onclick="history.back()" value="返回"></div>

[@b.foot/]
