[@b.head/]
[@b.toolbar title='学生签名信息']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="writerSearchForm" action="!search" title="ui.searchForm" target="proposalList" theme="search"]
        [@b.select name="signature.writer.season.id" label="毕业界别" items=seasons required="true"/]
        [@b.textfield name="signature.writer.std.code" label="学号" maxlength="30000"/]
        [@b.textfield name="signature.writer.std.name" label="姓名"/]
        [@b.textfield name="signature.writer.std.state.grade.code" label="年级"/]
        [@b.textfield name="signature.writer.advisor.teacher.name" label="教师姓名" maxlength="30000"/]
        [@b.select name="signature.writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="signature.writer.std.state.squad.name" label="班级"/]
        [@b.select label="学生签名"  name="writerSigned" items={'':'...','1':'已签名','0':'未签名'} empty="..."/]
        [@b.select label="导师签名"  name="advisorSigned" items={'':'...','1':'已签名','0':'未签名'} empty="..."/]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("writerSearchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="proposalList"/]</div>
</div>
[@b.foot/]
