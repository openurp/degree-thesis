[@b.head/]
[@b.toolbar title='学生论文归档资料']
  bar.addItem("签名情况","signatureIdx()");
  function signatureIdx(){
    bg.form.submit("writerSearchForm","${b.url('signature')}","_blank");
  }
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="writerSearchForm" action="!search" title="ui.searchForm" target="proposalList" theme="search"]
        [@b.select name="archive.writer.season.id" label="毕业界别" items=seasons required="true"/]
        [@b.textfield name="archive.writer.std.code" label="学号" maxlength="30000"/]
        [@b.textfield name="archive.writer.std.name" label="姓名" maxlength="30000"/]
        [@b.textfield name="archive.writer.std.state.grade.code" label="年级"/]
        [@b.textfield name="archive.writer.advisor.teacher.name" label="教师姓名"/]
        [@b.select name="archive.writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="archive.writer.std.state.squad.name" label="班级"/]
        [@b.textfield name="archive.title" label="题目"/]
        [@b.select label="归档材料"  name="hasArchive" items={'1':'已上传','0':'未上传'} empty="..." value="1"/]
        [@b.select label="导师审核"  name="advisorConfirmed" items={'null':'未审核','1':'审核通过','0':'审核不通过'} empty="..."/]
        [@b.select label="学院审核"  name="archived" items={'null':'未审核','1':'通过','0':'不通过'} empty="..."/]
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
