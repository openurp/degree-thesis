[@b.form name="studentSearchForm" action="!search" title="ui.searchForm" target="studentList" theme="search"]
  [@b.select name="writer.season.id" label="毕业界别" items=seasons required="true"/]
  [@b.textfield name="writer.std.code" label="学号"/]
  [@b.textfield name="writer.std.name" label="姓名"/]
  [@b.textfield name="writer.std.state.major.name" label="专业"/]
  [@b.textfield name="writer.advisor.teacher.name" label="指导教师"/]
  [@b.select name="writer.std.state.department.id" label="院系" items=departs/]
  [@b.textfield name="writer.std.state.squad.name" label="班级"/]
[/@]
  <script>
    $(document).ready(function() {
      bg.form.submit("studentSearchForm");
    });
  </script>
