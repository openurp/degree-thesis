[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
<p style="width:800px;text-align:left"><font color="red">
说明：没有显示初选已被选中的题目。点击“题目补选情况”可以查看当前该题目有哪些学生已补选。如果已补选学生数超过了1个，你仍可选该题目，
但无法保证教师审查时会将你选中,若该教师有题目未被选满可能将你转到其他题目。</font>
</p>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="9" height="30">补选题目列表(共有题目${candinates?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">题目名称</td>
    <td class="header" align="center" height="30">指导教师</td>
    <td class="header" align="center" height="30">面向专业</td>

    <td class="header" colspan="2" align="center" height="30">操作</td>
  </tr>
  [#list candinates as subject]
    <tr>
      <td class="data" align="center" height="30"><a href="${b.base}/common/subject/${subject.id}" target="_blank">${subject.name}</a></td>
      <td class="data" align="center" height="30"><a href="${b.base}/common/advisor/${subject.advisor.id}" target="_blank">${subject.advisor.teacher.name}</a></td>
      <td class="data" height="30" align="center">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
      <td class="data" align="center" height="30">[@b.a href="!doApply?round=2&subjectId="+subject.id onclick="if(confirm('选择此论文为你的题目，确定执行吗？')){return bg.Go(this,null)}else{return false;}"]选择[/@]</td>
      <td class="data" align="center" height="30">[@b.a href="!applies?round=2&subjectId="+subject.id target="_blank"]该题目补选情况[/@]</td>
    </tr>
  [/#list]
</table>
</div>
[@b.foot/]
