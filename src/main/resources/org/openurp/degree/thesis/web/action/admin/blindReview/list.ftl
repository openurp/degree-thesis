[#ftl]
[@b.head/]
[@b.grid items=reviews var="review"]
  [@b.gridbar]
    bar.addItem("随机抽取10%",action.method("drawlot","是否确认随机抽取每个院系的10%?"));
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
    bar.addItem("上传盲审成绩",action.method('importForm'));
    bar.addItem("按学院统计",action.method("stat"));
    bar.addItem("${b.text("action.export")}",action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,writer.std.state.major.name:专业,writer.std.state.squad.name:班级,writer.thesisTitle:论文题目,writer.advisor.teacher.name:指导老师",null,'fileName=校外盲审名单'));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="writer.std.code" title="学号"/]
    [@b.col width="10%" property="writer.std.name" title="姓名"/]
    [@b.col width="15%" property="writer.std.state.department.name" title="学院"/]
    [@b.col width="15%" property="writer.std.state.major.name" title="专业"/]
    [@b.col property="writer.thesisTitle" title="题目"/]
    [@b.col width="8%" property="score" title="成绩" /]
  [/@]
[/@]
[@b.foot/]
