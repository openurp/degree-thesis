[#macro display longName width]
<div style="overflow: hidden;text-overflow: ellipsis;width: ${width}px;display: inline-block;white-space: nowrap;" title="${longName}">${longName}</div>
[/#macro]
[@b.grid items=archives var="archive"]
  [@b.gridbar]
    bar.addItem("驳回",action.multi("reject","确定驳回?"));
    bar.addItem("直接审核通过",action.multi("pass","直接审核通过?"));
    bar.addItem("单个审核",action.single("auditSetting",null,null,"_blank"));
    bar.addItem("初始化名单",action.method("init"));
    bar.addItem("导出",
       action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,writer.std.state.major.name:专业,"+
       "writer.thesisTitle:题目,writer.advisor.teacher.name:指导教师,paper.researchField:研究方向,"+
        "paper.keywords:关键词,paper.language.name:撰写语种,paper.thesisType:毕业论文类型,"+
        "review.finalScore:论文分数,review.finalScoreText:论文成绩,"+
        "confirmed:导师审核,archived:学院审核,feedback:反馈意见",null,'fileName=归档信息'));
    bar.addItem("打包下载",action.multi("downloadArchives",null,null,"_blank"));
    var m = bar.addMenu("高级..");
    m.addItem("批量设置签字日期",action.method('batchUpdateForm'));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="100px"/]
    [@b.col property="writer.std.name"  title="姓名" width="130px"/]
    [@b.col property="writer.std.state.major.name"  title="专业 方向 班级" width="20%" ]
      ${archive.writer.std.state.major.name} ${(archive.writer.std.state.direction.name)!} ${(archive.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.thesisTitle" title="题目"]
      <div class="text-ellipsis">
      [#if archive.uploadAt??]
        [@b.a href="!auditSetting?archive.id="+archive.id target="_blank"]${archive.writer.thesisTitle}[/@]
      [#else]
        ${archive.writer.thesisTitle!}
      [/#if]
      </div>
    [/@]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="100px"/]
    [@b.col title="上传时间" property="uploadAt" width="120px"]
      ${((archive.uploadAt)?string('MM-dd HH:mm'))!}
    [/@]
    [@b.col title="导师审核" property="confirmed" width="80px"]
      [#if archive.confirmed??]
      ${(archive.confirmed)?string("通过","不通过")}
      [/#if]
    [/@]
    [@b.col title="学院审核" property="archived" width="80px"]
      [#if archive.uploadAt??]
        [#if archive.archived??]
          ${archive.archived?string("同意归档","驳回修改")}
        [#else]
         未审核
        [/#if]
      [/#if]
    [/@]
  [/@]
[/@]
