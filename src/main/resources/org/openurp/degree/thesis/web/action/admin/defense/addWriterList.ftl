[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.toolbar title="选择学生"]
  bar.addBack();
[/@]
[@b.form name="writerForm" action="!addWriterList"]
  <input type="hidden" name="defenseGroup.id" value="${defenseGroup.id}"/>
  [@b.grid items=writers var="writer" style="border:0.5px solid #006CB2" filterable="true"]
    [@b.gridbar]
      bar.addItem("选中添加",action.multi("addWriter"));
    [/@]
    [@b.row]
      [@b.boxcol/]
      [@b.col title="序号" width="50px"]${writer_index+1}[/@]
      [@b.col property="std.code" width="100px" title="学号"/]
      [@b.col property="std.name" width="130px" title="姓名"/]
      [@b.col property="std.state.major.name" width="150px"  title="专业"/]
      [@b.col property="std.state.squad.name" title="班级"/]
      [@b.col property="advisor.teacher.name" width="100px" title="指导老师"/]
      [@b.col property="advisor.teacher.office.name" width="150px" title="指导老师教研室"/]
      [@b.col property="thesisTitle" title="论文题目" width="25%"  /]
    [/@]
  [/@]
[/@]
</div>
[@b.foot/]
