[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
<table class="list_table" width="100%">
  <tr>
    <td align="center" colspan="2" class="title">
    毕业论文(设计)题目详细信息</td>
  </tr>
  <tr>
    <td class="title" width="100">题目名称:</td>
    <td class="data" align="left">${subject.name}</td>
  </tr>
  <tr>
    <td class="title" width="100">审查状态:</td>
    <td class="data" align="left"><font color="red">${subject.status}</font></td>
  </tr>
  <tr>
    <td class="title" width="100">指导老师:</td>
    <td class="data" align="left">${subject.advisor.teacher.name}</td>
  </tr>
  <tr>
    <td class="title" width="100">面向院系:</td>
    <td class="data" align="left">${subject.depart.name}</td>
  </tr>
  <tr>
    <td class="title" width="100">面向专业:</td>
    <td class="data" align="left">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
  </tr>
  <tr>
    <td width="100" class="title">现有条件:</td>
    <td class="data" align="left">${subject.conditions!}</td>
  </tr>
  <tr>
    <td class="title" height="60" width="100">主要内容:</td>
    <td class="data" align="left">${subject.contents!}</td>
  </tr>
  <tr>
    <td class="title" width="100">对学生的要求:</td>
    <td class="data" align="left">${subject.requirements!}</td>
  </tr>
    <tr>
      <td class="title" width="100">审查意见:</td>
      <td class="data" align="left">${subject.auditOpinion!}</td>
    </tr>
</table>
<input type="button" onclick="history.back();" value="返回">
</div>
[@b.foot/]
