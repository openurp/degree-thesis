[@b.head/]
[@b.toolbar title='学生论文']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="writerSearchForm" action="!search" title="ui.searchForm" target="proposalList" theme="search"]
        [@b.select name="thesisPaper.writer.season.id" label="毕业界别" items=seasons required="true"/]
        [@b.textfield name="thesisPaper.writer.std.code" label="学号" maxlength="30000"/]
        [@b.textfield name="thesisPaper.writer.std.name" label="姓名"/]
        [@b.textfield name="thesisPaper.writer.std.state.grade.code" label="年级"/]
        [@b.textfield name="thesisPaper.writer.advisor.teacher.name" label="教师姓名"/]
        [@b.select name="thesisPaper.writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="thesisPaper.writer.std.state.squad.name" label="班级"/]
        [@b.date name="uploadBeginOn" label="上传从"/]
        [@b.date name="uploadEndOn" label="到"/]
        [@b.textfield name="thesisPaper.title" label="题目"/]
        [@b.select name="thesisPaper.filePath" label="格式" items={'doc':'.doc','docx':'.docx','pdf':'.pdf','wps':'.wps'} empty="..."/]
        [@b.select label="需要复检"  name="needRecheck" items={'1':'是','0':'否'}/]
        [@b.select label="是否终稿"  name="thesisPaper.finalized" items={'1':'是','0':'否'}/]
        [@b.select label="导师审核"  name="thesisPaper.advisorPassed" items={'1':'通过','0':'未通过','null':'未审核'}/]
        [@b.select label="优秀论文"  name="thesisPaper.excellent" items={'1':'是','0':'否'}/]
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
