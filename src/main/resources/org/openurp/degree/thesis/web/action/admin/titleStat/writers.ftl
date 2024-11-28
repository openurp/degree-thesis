[@b.head/]
<div class="container-fluid">
<h5 style="text-align:center">${advisor.teacher.name}指导学生论文情况</h5>
[#list seasonWriters?keys?sort?reverse as season]
  [@b.card class="card-info card-primary card-outline"]
    [#assign title]<i class="fas fa-list"></i> ${season.name}指导名单[/#assign]
    [@b.card_header class="border-transparent" title=title ]
    [/@]
    [@b.card_body class="p-0"]
      [@b.grid items=seasonWriters.get(season)?sort_by(["std","code"]) var="writer" sortable="false"]
        [@b.row]
          [@b.col title="序号" width="40px"]${writer_index+1}[/@]
          [@b.col property="std.code" title="学号" width="120px"/]
          [@b.col property="std.name" title="姓名" width="130px"/]
          [@b.col property="std.gender.name" title="性别" width="50px"/]
          [@b.col property="std.state.grade" title="年级" width="60px"/]
          [@b.col property="std.level.name" title="培养层次" width="60px"/]
          [@b.col property="std.state.department.name" title="院系" width="120px"]
            ${writer.std.state.department.shortName!writer.std.state.department.name}
          [/@]
          [@b.col property="std.state.major.name" title="专业与方向"]
            ${(writer.std.state.major.name)!} ${(writer.std.state.direction.name)!}
          [/@]
          [@b.col property="thesisTitle" title="论文题目" width="35%"/]
        [/@]
      [/@]
    [/@]
  [/@]
[/#list]
</div>
[@b.foot/]
