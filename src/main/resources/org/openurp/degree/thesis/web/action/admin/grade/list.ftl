[#macro display longName width]
<div style="overflow: hidden;text-overflow: ellipsis;width: ${width}px;display: inline-block;white-space: nowrap;" title="${longName}">${longName}</div>
[/#macro]
[@b.grid items=thesisReviews var="thesisReview"]
  [@b.gridbar]
    bar.addItem("修改",action.edit());
    bar.addItem("成绩同步",action.multi("syncGrades"));
    bar.addItem("导出",action.exportData("writer.std.code:学号,writer.std.name:姓名,writer.std.state.department.name:院系,"+
    "writer.std.state.major.name:专业,writer.std.state.squad.name:班级,writer.thesisTitle:题目,writer.advisor.teacher.name:指导老师,writer.advisor.teacher.office.name:指导教师所在教研室,advisorScore:建议得分,"+
    "crossReviewManager.name:评阅分配负责人,crossReviewer.name:交叉评阅人,crossReviewScore:交叉评阅得分,defenseScore:答辩得分,finalScore:最终得分,finalScoreText:最终成绩,remark:备注",null,'fileName=论文成绩'));
    bar.addItem("分数统计",action.method("analysis"));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="10%"/]
    [@b.col property="writer.std.name"  title="姓名" width="10%"/]
    [@b.col property="writer.std.state.department.name" title="院系" width="8%"]
      ${(thesisReview.writer.department.shortName)!thesisReview.writer.department.name}
    [/@]
    [@b.col property="writer.std.state.major.name" title="专业 方向 班级" ]
      ${thesisReview.writer.std.state.major.name} ${(thesisReview.writer.std.state.direction.name)!} ${(thesisReview.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="8%"]
      <span title="${(thesisReview.writer.advisor.teacher.office.name)!}">${(thesisReview.writer.advisor.teacher.name)!}</span>
    [/@]
    [@b.col property="advisorScore" title="建议得分" width="8%"/]
    [@b.col property="crossReviewer.id" title="评阅教师" width="8%"]
      <span title="${(thesisReview.crossReviewer.office.name)!}">${(thesisReview.crossReviewer.name)!}</span>
    [/@]
    [@b.col property="crossReviewScore" title="评阅得分" width="8%"/]
    [@b.col property="defenseScore" title="答辩得分" width="8%"/]
    [@b.col property="finalScore" title="最终得分" width="8%"]
      [#if thesisReview.finalScore??]
        ${thesisReview.finalScore} ${thesisReview.finalScoreText!}
        [#if !thesisReview.courseGradeSynced!false]<sup>未同步</sup>[/#if]
      [#else]
      --
      [/#if]

    [/@]
  [/@]
[/@]
