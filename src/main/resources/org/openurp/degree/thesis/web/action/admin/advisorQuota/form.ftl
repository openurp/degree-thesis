[@b.head/]
[@b.toolbar title="修改指导教师信息"]bar.addBack();[/@]
[@b.form name="newAdvisorForm" action=b.rest.save(advisor) theme="list"]
  [@b.field label="教师"]${advisor.teacher.code} ${advisor.teacher.name} ${advisor.teacher.department.name}[/@]
  [@b.field label="职称"]${(advisor.teacher.staff.title.name)!'无'}[/@]
  [@b.number label="带学生上限" name="advisor.maxWriters" required="true" value=advisor.maxWriters!/]
  [@b.formfoot]
    [@b.submit value="保存"/]
  [/@]
[/@]
<br/><br/><br/><br/><br/><br/><br/>
[@b.foot/]
