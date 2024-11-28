[@b.head/]
[@b.toolbar title='任务书管理']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="writerSearchForm" action="!search" title="ui.searchForm" target="commitmentList" theme="search"]
        [@b.select name="writer.season.id" label="毕业界别" items=seasons required="true"/]
        [@b.textfield name="writer.std.code" label="学号"/]
        [@b.textfield name="writer.std.name" label="姓名"/]
        [@b.textfield name="writer.advisor.teacher.name" label="教师姓名"/]
        [@b.textfield name="writer.std.state.grade.code" label="年级"/]
        [@b.select name="writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="writer.std.state.squad.name" label="班级"/]
        [@b.select name="confirmed" label="状态" items={'1':'已确认','0':'未确认'} empty="所有"/]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("writerSearchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="commitmentList"/]</div>
</div>
[@b.foot/]
