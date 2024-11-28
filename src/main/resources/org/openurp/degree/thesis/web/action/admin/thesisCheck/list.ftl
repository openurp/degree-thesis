[#assign extMap={"xls":'xls.gif',"xlsx":'xls.gif',"docx":"doc.gif","doc":"doc.gif","pdf":"pdf.gif","zip":"zip.gif","":"generic.gif"}]

[@b.grid items=thesisChecks var="thesisCheck"]
  [@b.gridbar]
    var m1 = bar.addMenu("导入论文信息",action.method('importForm'));
    m1.addItem("导入抽检名单",action.method('importWriters'));
    bar.addItem("批量上传材料",action.method("uploadDocForm"));
    bar.addItem("修改",action.edit());
    bar.addItem("导出上报表","exportDegree()");
    bar.addItem("批量下载",action.multi("downloadFiles",null,null,false),"action-download");
    bar.addItem("按学院统计",action.method("stat"));
    var mm = bar.addMenu("其他")
    mm.addItem("匹配材料",action.method('updateDoc'));

    var form = document.searchForm;
    function exportDegree(){
      bg.form.addInput(form, "titles",
       "writer.std.project.school.code:学位授予单位代码,writer.std.project.school.name:学位授予单位名称,writerName:姓名,"+
       "writer.std.project.school.code:培养单位码,degreeMajorCode:学士学位专业代码,"+
       "degreeMajorName:学士学位专业名称,certMajorName:证书专业名称,"+
       "enrollOn:入学年月,writer.std.code:学号,examineeCode:考生号,graduateOn:毕业年月,"+
       "majorMinorDegree:是否主辅修学位,dualDegree:是否双学士学位,jointDegree:是否联合学位,jointOrgCode:联合培养单位码," +
       "secondDegree:是否第二学位,minorDegree:是否辅修学位,eduType:学位类型,"+
       "thesisType:论文类型,advisor:导师姓名,title:论文题目,keywords:论文关键词,"+
       "researchField:论文研究方向,language.name:论文撰写语种,"+
       "paperFileName:论文原文或说明文件名称,attachFileName:支撑材料文件名称,"+
       "blank.3:查重报告文件名称,firstSeason:是否本专业第一届毕业生,departNo:毕业生所在院系代码,departName:毕业生所在院系名称");
      bg.form.addInput(form, "fileName", "xxhzb_${project.school.code}_${project.school.name}_${(thesisChecks?first.departNo)!'无'}_${schoolYear}_000");
      bg.form.addInput(form, "convertToString", "1");
      bg.form.submit(form, "${b.url('!exportData')}","_self");
    }
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="100px"/]
    [@b.col property="writerName"  title="姓名" width="100px"/]
    [@b.col property="degreeMajorName"  title="学位专业" width="120px"]
    <div class="text-ellipsis" title="${(thesisCheck.degreeMajorName)!}">${(thesisCheck.degreeMajorName)!}</div>
    [/@]
    [@b.col property="enrollOn"  title="入学-毕业年月" width="120px"]
     ${(thesisCheck.enrollOn?string('yyyyMM'))!}~${(thesisCheck.graduateOn?string('yyyyMM'))!}
    [/@]
    [@b.col property="advisor" title="指导老师" width="80px"/]
    [@b.col property="language.name" title="撰写语言" width="60px"/]
    [@b.col property="researchField" title="研究方向" width="100px"/]
    [@b.col property="keywords" title="关键词" width="200px"]
      <div class="text-ellipsis" title="${(thesisCheck.keywords)!}">${(thesisCheck.keywords)!}</div>
    [/@]
    [@b.col property="title" title="题目"]
      <div class="text-ellipsis" title="${(thesisCheck.title)!}">${(thesisCheck.title)!}</div>
    [/@]
    [@b.col property="title" title="开题|答辩|正文" width="120px"]
      [#if thesisCheck.proposalDoc??]
        [@b.a href="!proposalDoc?thesisCheck.id="+thesisCheck.id target="_blank" title="开题报告"]
        <image src="${b.static_url("ems","images/file/pdf.gif")}"/>
        [/@]
      [#else]
        [@b.a href="proposal!doc?writer.id="+thesisCheck.writer.id target="_blank" title="开题报告"]
        <image src="${b.static_url("ems","images/file/doc.gif")}"/>
        [/@]
      [/#if]
      [#if thesisCheck.defenseDoc??]
        [@b.a href="!defenseDoc?thesisCheck.id="+thesisCheck.id target="_blank" title="答辩评分表"]
        <image src="${b.static_url("ems","images/file/"+extMap[thesisCheck.defenseDoc.filePath?keep_after_last(".")]?default("generic.gif"))}"/>
        [/@]
      [#else]无
      [/#if]
      [#if thesisCheck.paperDoc?? && thesisCheck.paperDoc.filePath?starts_with("/")]
        [@b.a href="!paper?thesisCheck.id="+thesisCheck.id target="_blank" title="论文正文"]
        <image src="${b.static_url("ems","images/file/"+extMap[thesisCheck.paperDoc.filePath?keep_after_last(".")]?default("generic.gif"))}"/>
        [/@]
      [#else]
         无
      [/#if]
    [/@]
  [/@]
[/@]
