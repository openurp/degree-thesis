[@b.head/]
[@b.toolbar title='论文成绩统计结果']
  bar.addBack();
[/@]

[#if results?size>0]
<table class="grid-table">
  <thead class="grid-head">
    <tr>
      <td width="5%">序号</td>
      <td>学生</td>
      <td>姓名</td>
      <td>院系</td>
      <td>专业</td>
      <td width="15%">分数变化</td>
    </tr>
  </thead>
  <tbody>
  [#list results as r,msgs]
  <tr>
    <td>${r_index+1}</td>
    <td>${r.writer.std.code}</td>
    <td>${r.writer.std.name}</td>
    <td>${r.writer.std.state.department.name}</td>
    <td>${(r.writer.std.state.major.name)!}</td>
    <td>
      [#list results.get(r) as msg]${msg}[#sep],[/#list]
    </td>
  </tr>
  [/#list]
  </tbody>
</table>
[/#if]

<p>未取得最终成绩的原因</p>
[@b.grid items=remarks var="review"]
  [@b.row]
    [@b.col title="序号" width="5%"]${review_index+1}[/@]
    [@b.col property="writer.std.code"  title="学号" width="12%"/]
    [@b.col property="writer.std.name"  title="姓名" width="12%"/]
    [@b.col property="writer.std.state.department.name" title="院系" width="8%"]
      ${(review.writer.department.shortName)!review.writer.department.name}
    [/@]
    [@b.col property="writer.std.state.major.name" title="专业 方向 班级" ]
      ${review.writer.std.state.major.name} ${(review.writer.std.state.direction.name)!} ${(review.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="8%"]
      <span title="${(review.writer.advisor.teacher.office.name)!}">${(review.writer.advisor.teacher.name)!}</span>
    [/@]
    [@b.col property="advisorScore" title="建议得分" width="8%"/]
    [@b.col property="crossReviewer.id" title="评阅教师" width="8%"]
      <span title="${(review.crossReviewer.office.name)!}">${(review.crossReviewer.name)!}</span>
    [/@]
    [@b.col property="crossReviewScore" title="评阅得分" width="8%"/]
    [@b.col property="remark" title="备注" width="15%"/]
  [/@]
[/@]

[@b.foot/]
