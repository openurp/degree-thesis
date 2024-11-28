[#macro display longName width]
<div style="overflow: hidden;text-overflow: ellipsis;width: ${width}px;display: inline-block;white-space: nowrap;" title="${longName}">${longName}</div>
[/#macro]
[#assign extMap={"xls":'xls.gif',"xlsx":'xls.gif',"docx":"doc.gif","doc":"doc.gif","pdf":"pdf.gif","zip":"zip.gif","":"generic.gif"}]

[@b.grid items=thesisPapers var="thesisPaper"]
  [@b.gridbar]
    bar.addItem("下载封面",action.multi("cover",null,null,false),"action-download");
    bar.addItem("下载论文",action.multi("doc",null,null,false),"action-download");
    bar.addItem("修改",action.single("uploadForm"));
    bar.addItem("未提交明细",action.method("missing"));
    bar.addItem("按学院统计",action.method("stat"));
    bar.addItem("${b.text("action.export")}",
        action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,"+
             "writer.std.state.major.name:专业,writer.std.state.squad.name:班级,writer.thesisTitle:论文题目,"+
             "researchField:研究方向,keywords:关键词,language.name:撰写语种,thesisType:毕业论文类型,"+
             "writer.advisor.teacher.code:指导老师工号,writer.advisor.teacher.name:指导老师,writer.advisor.teacher.department.name:指导老师所在院系,submitAt:提交时间",null,'fileName=论文上传信息'));
    var m = bar.addMenu("设置指导老师审核结论")
    m.addItem("设置为通过",action.multi("updateAdvisorPassed","确定设置为通过?","&passed=1"));
    m.addItem("设置为不通过",action.multi("updateAdvisorPassed","确定设置为通过?","&passed=0"));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="100px"/]
    [@b.col property="writer.std.name"  title="姓名" width="130px"/]
    [@b.col property="writer.std.state.major.name"  title="专业 方向 班级" ]
      ${thesisPaper.writer.std.state.major.name} ${(thesisPaper.writer.std.state.direction.name)!} ${(thesisPaper.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.thesisTitle" title="题目"]
      [#if thesisPaper.filePath?starts_with("/")]
        [@b.a href="!doc?thesisPaper.id="+thesisPaper.id target="_blank"]<div class="text-ellipsis" title="${(thesisPaper.title)!}">${(thesisPaper.title)!}</div>[/@]
      [#else]
        ${thesisPaper.title}
      [/#if]
    [/@]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="100px"/]
    [@b.col title="上传时间" property="submitAt" width="100px"]
      ${thesisPaper.submitAt?string('MM-dd HH:mm')}
    [/@]
    [@b.col title="格式" property="fileExt" width="50px"]
      [#if thesisPaper.filePath?starts_with("/")]
      <image src="${b.static_url("ems","images/file/"+extMap[thesisPaper.filePath?keep_after_last(".")]?default("generic.gif"))}"/>
      [#else]
       无
      [/#if]
    [/@]
  [/@]
[/@]
