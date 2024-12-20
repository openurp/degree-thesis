[@b.toolbar title="题目统计"]
  bar.addBack();
[/@]
[@b.grid items=advisors var="advisor"]
  [@b.row]
    [@b.col title="序号" width="50px"]${advisor_index+1}[/@]
    [@b.col title="工号" width="70px" property="teacher.staff.code"][/@]
    [@b.col title="姓名" width="120px" property="teacher.name"][/@]
    [@b.col title="指导院系"][#list advisor.departs as depart]${depart.name}[#sep]&nbsp;[/#list][/@]
    [@b.col title="教研室" width="160px" property="teacher.office.name"][/@]
    [@b.col title="职称" width="100px" property="teacher.title.name"][/@]
    [@b.col title="提交题目数" width="100px"]
          [#if (subjectStats.get(advisor.id)!0)==0]<div style="color:red">0</div>
          [#else]
              [@b.a href="!search?subject.advisor.id=${advisor.id}"]${(subjectStats.get(advisor.id)!0)}[/@]
          [/#if]
    [/@]
    [@b.col title="操作"]
      [@b.a href="!editNew?subject.advisor.id="+advisor.id]新增[/@]
      [#if historyStats[advisor.id?string]?? && historyStats[advisor.id?string]>0]
      [@b.a href="!history?subject.advisor.id="+advisor.id]历史题目(${historyStats[advisor.id?string]})[/@]
      [/#if]
    [/@]
  [/@]
[/@]
