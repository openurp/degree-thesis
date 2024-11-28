[#ftl]
[@b.head/]
[@b.grid items=copyChecks var="copyCheck"]
  [@b.gridbar]
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
    bar.addItem("导入检测数据",action.method('importForm'));
    bar.addItem("上传检测报告",action.method('uploadReportForm'));
    bar.addItem("${b.text("action.export")}",action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,writer.std.state.major.name:专业,writer.std.state.squad.name:班级,writer.thesisTitle:论文题目,writer.advisor.teacher.name:指导老师,passed:是否通过,checkOn:检测日期,recheck:是否复检",null,'fileName=论文反抄袭检测信息'));
  [/@]
  [@b.row]
    [@b.boxcol /]
    [@b.col width="15%" property="writer.std.code" title="学号"/]
    [@b.col width="10%" property="writer.std.name" title="姓名"/]
    [@b.col property="writer.std.state.department.name" title="学院"/]
    [@b.col width="10%" property="writer.std.state.major.name" title="专业"/]
    [@b.col width="10%" property="passed" title="是否通过"]
      ${copyCheck.passed?string("是","否")}
    [/@]
    [@b.col width="10%" property="wordCount" title="总字数"/]
    [@b.col width="15%" property="copyRatio" title="去除引用文献复制比"]
      [#if copyCheck.copyRatio??]${(copyCheck.copyRatio*100)?string("0.000")}%[/#if]
    [/@]
    [@b.col width="10%" property="checkOn" title="检测日期"]
      [#if copyCheck.report??]
        [@b.a target="_blank" href="!report?id="+copyCheck.id]${(copyCheck.checkOn?string("MM-dd"))!}[/@]
      [#else]
      ${(copyCheck.checkOn?string("MM-dd"))!}
      [/#if]
    [/@]
  [/@]
[/@]
[@b.foot/]
