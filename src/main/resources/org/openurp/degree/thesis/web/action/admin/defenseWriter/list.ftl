[@b.grid items=defenseWriters var="defenseWriter"]
  [@b.gridbar]
    bar.addItem("导出",action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,"+
    "writer.std.state.major.name:专业,writer.thesisTitle:题目,writer.advisor.teacher.name:指导老师,group.idx:组号,"+
    "group.leader:答辩组长,group.members:答辩组成员,group.time:答辩时间,group.place:答辩地点",null,'fileName=答辩分组明细'));
    [#if ((Parameters['copyCheck'])!'1')=='0']bar.addItem("移除分组",action.remove()); [/#if]
    bar.addItem("未分组学生",action.method('emptyGroupWriter'));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="100px"/]
    [@b.col property="writer.std.name"  title="姓名" width="130px"]
      [@b.a href="defense!info?id="+defenseWriter.group.id]${defenseWriter.writer.std.name}[/@]
    [/@]
    [@b.col property="writer.std.state.department.name"  title="院系" width="130px"/]
    [@b.col property="writer.std.state.major.name"  title="专业 方向 班级" ]
      ${defenseWriter.writer.std.state.major.name} ${(defenseWriter.writer.std.state.direction.name)!} ${(defenseWriter.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.thesisTitle" title="题目"/]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="100px"/]
    [@b.col property="group.idx" title="组号" width="50px"/]
    [@b.col title="组长" width="80px"]${(defenseWriter.group.leaderTeacher.name)!}[/@]
    [@b.col title="成员" width="120px"] [#list defenseWriter.group.memberTeachers as t]${t.name}[#sep]&nbsp;[/#list][/@]
  [/@]
[/@]
