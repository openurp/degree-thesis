[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="5" height="30">其他老师题目列表  总计:${subjects?size}个</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">题目名称</td>
    <td class="header" align="center" height="30">指导教师</td>
    <td class="header" align="center" height="30">面向专业</td>
  </tr>
  [#list subjects as subject]
    <tr>
      <td class="data" align="center" height="30">${subject_index+1 }</td>
      <td class="data" align="center" height="30">
        [@b.a href="!info?id="+subject.id]${subject.name}[/@]
      </td>
      <td class="data" align="center" height="30">
        <a href="${b.base}/admin/js/view.htm?id=${subject.advisor.id}" target="_blank">${(subject.advisor.teacher.name)!}</a>
      </td>
       <td class="data" align="center" height="30">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
    </tr>
  [/#list]
</table>
</div>

[@b.foot/]
