[@b.head/]
<p>
  <h5 style="text-align:center">${depart.name} ${season.name}全日制本科生毕业论文答辩安排信息汇总表
  [@b.a href="!defense?download=1&season.id="+season.id+"&depart.id="+depart.id target="_blank"]<i class="fa-solid fa-download"></i>下载[/@]
  </h5>
</p>
<table class="grid-table" style="border: 0.5px solid #006CB2;">
  <colgroup span="5"></colgroup>
  <colgroup>
    <col style="width:50px"/>
    <col style="width:100px"/>
    <col style="width:100px"/>
    <col style="width:100px"/>
    <col style="width:150px"/>
  </colgroup>
  <thead class="grid-head">
    <tr>
      <td rowspan="2" style="width:50px">序号</td>
      <td rowspan="2" style="width:120px">姓名</td>
      <td rowspan="2" style="width:120px">学号</td>
      <td rowspan="2" style="width:120px">指导教师</td>
      <td rowspan="2" >论文题目</td>
      <td colspan="6" style="width:40%">答辩安排</td>
    </tr>
    <tr>
      <td>组别</td><td colspan="3">答辩小组教师</td>
      <td>时间</td><td>地点</td>
    </tr>
  </thead>
  <tbody>
    [#list writers as dwriter]
      <tr>
        <td>${dwriter_index+1}</td>
        <td>${dwriter.writer.std.name}</td>
        <td>${dwriter.writer.std.code}</td>
        <td>${(dwriter.writer.advisor.name)!}</td>
        <td>${(dwriter.writer.thesisTitle)!}</td>
        <td>${(dwriter.group.idx)!}</td>
        [#assign mCount=0/]
        [#if dwriter.group.leaderTeacher??]
        <td>${dwriter.group.leaderTeacher.name}</td>
        [#assign mCount=mCount+1/]
        [/#if]
        [#list dwriter.group.memberTeachers as t]
        [#if mCount > 2] [#break/][/#if]
        <td>${t.name}</td>
        [#assign mCount = mCount+1/]
        [/#list]
        [#if mCount<3]
        [#list mCount+1..3 as m]<td></td>[/#list]
        [/#if]
        <td>${(dwriter.group.beginAt?string("M月dd日HH:mm"))!}-${(dwriter.group.endAt?string('HH:mm'))!}</td>
        <td>${(dwriter.group.place)!}</td>
      </tr>
    [/#list]
  </tbody>
</table>
[@b.foot/]
