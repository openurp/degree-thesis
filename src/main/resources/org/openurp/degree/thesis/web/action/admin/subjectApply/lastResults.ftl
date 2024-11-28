[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.messages slash="3"/]
[@b.form action="!search?round=99"]
<table cellspacing="0" cellpadding="0" border="0" width="100%">
  <tr>
    <td>学号:<input type="text" align="left" name="apply.writer.std.code" value="${Parameters['apply.writer.std.code']!}"/>&nbsp;&nbsp;</td>
    <td>姓名:<input type="text" align="left" name="apply.writer.std.name" value="${Parameters['apply.writer.std.name']!}"/></td>
    <td>专业:<input type="text" align="left" name="apply.writer.std.state.major.name" value="${Parameters['apply.writer.std.state.major.name']!}"/></td>
    <td>班级:<input type="text" align="left" name="apply.writer.std.state.squad.name" value="${Parameters['apply.writer.std.state.squad.name']!}"/></td>
    <td>指导教师:<input type="text" align="left" name="apply.writer.advisor.teacher.name" value="${Parameters['apply.writer.advisor.teacher.name']!}"/></td>
    <td align="left">[@b.submit value="查询"/]
      [@b.a href="!remainder?round=99"]没有分配的题目[/@]
      [@b.a href="!absence?round=99"]未选到题目学生[/@]
    </td>
  </tr>
</table>
[/@]
<table class="list_table" class="list_table">
  <tr>
    <td class="title" colspan="8" height="30">学生最终选题列表(共有学生${applies?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">专业班级</td>
    <td class="header" align="center" height="30">最终题目</td>
    <td class="header" align="center" height="30" width="80px">指导老师</td>
    <td class="header" align="center" height="30" width="100px" colspan="2">操作</td>
  </tr>
  [#list applies as apply]
    <tr>
      <td class="data" align="center" height="30">${apply_index+1}</td>
      <td class="data" align="center" height="30">${apply.writer.std.code}</td>
      <td class="data" align="center" height="30">[@display apply.writer.std.name 60/]</td>
      <td class="data" align="center" height="30">[@display apply.writer.major.name+" "+ (apply.writer.squad.name)!'--' 120/]</td>
      <td class="data" align="center" height="30">${apply.last.name}</td>
      <td class="data" align="center" height="30">${(apply.last.advisor.teacher.name)!}</td>
      <td class="data" align="center" height="30">
        [@b.a href="!cancel?round=99&subjectApply.id="+apply.id onclick="if(confirm('该操作将移除该学生的最终题目，确定执行吗?')){return bg.Go(this,null)}else{return false;}"]移除[/@]
      </td>
      <td class="data" align="center" height="30">
        [@b.a href="!assignSetting?next=search&writerId="+apply.writer.id]更换题目[/@]</td>
    </tr>
  [/#list]
</table>
</div>
[@b.foot/]
