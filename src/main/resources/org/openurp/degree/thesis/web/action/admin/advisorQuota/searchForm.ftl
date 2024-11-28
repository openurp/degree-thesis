[@b.form name="advisorSearchForm" action="!search" title="ui.searchForm" target="advisorList" theme="search"]
  [@b.textfield name="advisor.teacher.staff.code" label="工号" maxlength="5000"/]
  [@b.textfield name="advisor.teacher.name" label="姓名"/]
  [@b.select name="advisor.teacher.department.id" label="所在院系" items=departs/]
  [@b.select name="advisor.teacher.office.id" label="教研室"  items=offices/]
  [@b.select name="depart.id" label="指导院系" items=departs/]
  [@b.select label="是否可用"  name="active" value="1" items={'1':'是','0':'否'} /]
[/@]
<script>
  $(document).ready(function() {
    bg.form.submit("advisorSearchForm");
  });
</script>
