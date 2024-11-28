[#macro display longName width]
<div style="overflow: hidden;text-overflow: ellipsis;width: ${width}px;display: inline-block;white-space: nowrap;" title="${longName}">${longName}</div>
[/#macro]
[@b.grid items=thesisReviews var="thesisReview"]
  [@b.gridbar]
    bar.addItem("修改",action.edit());
    var mm = bar.addMenu("批量设置交叉评阅负责人...",action.multi("batchSetManagerSetting"));
    mm.addItem("设置教研室主任为负责人",action.multi("batchSetManagerByOfficeDirector"));
    bar.addItem("批量指派交叉评阅老师...",action.multi("batchAssignSetting"));
    bar.addItem("随机分配",action.multi("randomAssign"))
    bar.addItem("导出",action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,"+
    "writer.std.state.major.name:专业,writer.thesisTitle:题目,writer.advisor.teacher.name:指导老师,writer.advisor.teacher.office.name:指导教师所在教研室,advisorScore:建议得分,"+
    "crossReviewManager.name:评阅分配负责人,crossReviewer.name:交叉评阅人,crossReviewScore:交叉评阅得分",null,'fileName=评阅信息'));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="100px"/]
    [@b.col property="writer.std.name"  title="姓名" width="130px"/]
    [@b.col property="writer.std.state.major.name"  title="专业 方向 班级" ]
      ${thesisReview.writer.std.state.department.name} ${thesisReview.writer.std.state.major.name} ${(thesisReview.writer.std.state.direction.name)!} ${(thesisReview.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.thesisTitle" title="题目"/]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="100px"]
      <span title="${(thesisReview.writer.advisor.teacher.office.name)!}">${(thesisReview.writer.advisor.teacher.name)!}</span>
    [/@]
    [@b.col property="advisorScore" title="建议得分" width="100px"/]
    [@b.col property="crossReviewManager.id" title="交叉评阅负责人" width="120px"]
      ${(thesisReview.crossReviewManager.name)!}
    [/@]
    [@b.col property="crossReviewer.id" title="交叉评阅教师" width="100px"]
      <span title="${(thesisReview.crossReviewer.office.name)!}">${(thesisReview.crossReviewer.name)!}</span>
    [/@]
    [@b.col property="crossReviewScore" title="交叉评阅得分" width="100px"/]
  [/@]
[/@]
