[@b.head/]
  [@b.form name="studentListForm" action="!search"]
    [@b.grid items=writers var="writer" sortable="true"]
      [@b.gridbar]
        bar.addItem("${b.text("action.new")}",action.add());
        bar.addItem("${b.text("action.modify")}",action.edit());
        bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
        bar.addItem("导入",action.method('importForm'));
        bar.addItem("导出",action.exportData("std.code:学号,std.name:姓名,std.gender.name:性别,std.state.grade.name:年级,std.level.name:层次,std.state.department.name:院系,std.state.major.name:专业,std.state.direction.name:专业方向,std.state.squad.name:班级,mobile:手机,advisor.teacher.code:指导教师工号,advisor.teacher.name:指导教师",null,'fileName=学生信息'));
      [/@]
      [@b.row]
        [@b.boxcol/]
        [@b.col property="std.code" title="学号" width="120px"/]
        [@b.col property="std.name" title="姓名" width="130px"/]
        [@b.col property="std.gender.name" title="性别" width="50px"/]
        [@b.col property="std.state.grade" title="年级" width="60px"/]
        [@b.col property="std.level.name" title="培养层次" width="60px"/]
        [@b.col property="advisor.teacher.name" title="指导老师" width="80px"/]
        [@b.col property="std.state.department.name" title="院系" width="100px"]
          ${writer.std.state.department.shortName!writer.std.state.department.name}
        [/@]
        [@b.col property="std.state.major.name" title="专业与方向"]
          ${(writer.std.state.major.name)!} ${(writer.std.state.direction.name)!}
        [/@]
        [@b.col property="std.state.squad.name" title="班级" /]
        [@b.col property="mobile" title="手机" width="90px"/]
      [/@]
    [/@]
  [/@]
[@b.foot/]
