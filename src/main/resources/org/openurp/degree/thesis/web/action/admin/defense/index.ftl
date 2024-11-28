[@b.head/]
[@b.toolbar title='答辩分组']
  bar.addItem("分组明细","defenseWriters()");
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="groupSearchForm" action="!search" title="ui.searchForm" target="groupList" theme="search"]
        [@b.select name="defenseGroup.season.id" label="毕业界别" items=seasons required="true"/]
        [@b.textfield name="defenseGroup.idx" label="组号"/]
        [@b.select name="defenseGroup.department.id" label="院系" items=departs/]
        [@b.select name="defenseGroup.office.id" label="教研室" items=offices/]
        [@b.textfield name="teacherName" label="教师姓名"/]
        [@b.textfield name="writerName" label="学生姓名"/]
        [@b.select label="是否发布"  name="defenseGroup.published" items={'1':'是','0':'否'} /]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("groupSearchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="groupList"/]</div>
</div>
[@b.form name="defenseWriterForm" action="defense-writer"]
  <input type="hidden" name="defenseWriter.writer.season.id" value=""/>
[/@]
<script>
  function defenseWriters(){
    document.defenseWriterForm['defenseWriter.writer.season.id'].value=document.groupSearchForm['defenseGroup.season.id'].value;
    bg.form.submit("defenseWriterForm");
  }
</script>
[@b.foot/]
