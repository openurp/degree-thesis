[@b.form name="subjectForm" action="!search" title="ui.searchForm" target="subjectList" theme="search"]
  [@b.textfield name="subject.name" label="题目"/]
  [@b.textfield name="subject.advisor.teacher.name" label="指导教师"/]
  [@b.select name="subject.advisor.teacher.department.id" label="所在院系" items=departs/]
  [@b.select name="subject.advisor.teacher.office.id" label="教研室"  items=offices/]
  [@b.select name="subject.depart.id" label="面向院系" items=departs/]
  [@b.select name="major.id" label="面向专业" items=majors/]
  [@b.radios label="院系匹配" name="departMatched"  value="1" items={'1':'吻合','0':'不吻合'} /]
[/@]
<script>
  $(document).ready(function() {
    bg.form.submit("subjectForm");
  });
</script>
