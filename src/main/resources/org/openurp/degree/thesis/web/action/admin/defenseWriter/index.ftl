[@b.head/]
[@b.toolbar title='学生答辩分组明细']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="writerSearchForm" action="!search" title="ui.searchForm" target="writerList" theme="search"]
        [@b.select name="defenseWriter.writer.season.id" label="毕业界别" items=seasons value=Parameters['defenseWriter.writer.season.id']! required="true"/]
        [@b.textfield name="defenseWriter.writer.std.code" label="学号"/]
        [@b.textfield name="defenseWriter.writer.std.name" label="姓名"/]
        [@b.select name="defenseWriter.writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="defenseWriter.writer.std.state.squad.name" label="班级"/]
        [@b.textfield name="defenseWriter.writer.advisor.teacher.name" label="指导教师"/]
        [@b.select label="反抄袭"  name="copyCheck" value="" items={'1':'通过','0':'未通过'} /]
        [@b.select label="论文"  name="hasPaper" value="" items={'1':'已上传','0':'未上传'} /]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("writerSearchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="writerList"/]</div>
</div>
[@b.foot/]
