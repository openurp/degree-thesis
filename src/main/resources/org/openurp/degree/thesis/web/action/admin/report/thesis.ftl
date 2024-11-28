[@b.head/]
<p>
  <h5 style="text-align:center">${depart.name} ${season.name}届全日制本科生毕业论文信息汇总表
  [@b.a href="!thesis?download=1&season.id="+season.id+"&depart.id="+depart.id target="_blank"]<i class="fa-solid fa-download"></i>下载[/@]
  </h5>
</p>
[@b.grid items=thesisReviews var="review" sortable="false" style="border: 0.5px solid #006CB2;"]
  [@b.row]
    [@b.col title="序号" width="50px"]${review_index+1}[/@]
    [@b.col property="writer.std.name"  title="姓名" width="120px"/]
    [@b.col property="writer.std.code"  title="学号" width="120px"/]
    [@b.col property="writer.std.state.major.name"  width="150px" title="专业" ]
      ${review.writer.std.state.major.name}
    [/@]
    [@b.col property="writer.std.state.major.name" width="15%" title="班级" ]
      ${(review.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="100px"]
      <span title="${(review.writer.advisor.teacher.office.name)!}">${(review.writer.advisor.teacher.name)!}</span>
    [/@]
    [@b.col property="writer.advisor.teacher.title.name" title="职称" width="100px"/]
    [@b.col property="writer.thesisTitle" title="论文题目"/]
    [@b.col property="finalScoreText" title="成绩" width="100px"/]
  [/@]
[/@]

[@b.foot/]
