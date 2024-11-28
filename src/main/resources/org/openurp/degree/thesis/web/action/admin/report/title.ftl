[@b.head/]
<p>
  <h5 style="text-align:center">${depart.name} ${season.name}全日制本科生指导教师汇总表
  [@b.a href="!titleDoc?season.id="+season.id+"&depart.id="+depart.id target="_blank"]<i class="fa-solid fa-download"></i>下载[/@]
  </h5>
</p>
<table class="grid-table" style="border: 0.5px solid #006CB2;">
  <thead  class="grid-head">
    <tr>
      <td>职称</td>
      <td>参加指导的人数</td>
      <td>指导学生人数</td>
      <td>指导学生所占的全体学生百分比</td>
    </tr>
  </thead>
  <tbody>
  [#list titleWriters as title,twriters]
    <tr>
      <td>${title}</td>
      <td>${titleAdvisors[title]?size}</td>
      <td>${twriters?size}</td>
      <td>${(twriters?size*1.0/writers?size*1.0)*100}%</td>
    </tr>
  [/#list]
  </tbody>
</table>

[@b.foot/]
