[#ftl]
[@b.head/]
[@b.toolbar title="论文反抄袭检测"/]
<div class="search-container">
    <div class="search-panel">
        [@b.form name="checkSearchForm" action="!search" target="checklist" title="ui.searchForm" theme="search"]
            [@b.select name="copyCheck.writer.season.id" label="毕业界别" items=seasons required="true"/]
            [@b.textfields names="copyCheck.writer.std.code;学号"/]
            [@b.textfields names="copyCheck.writer.std.name;姓名"/]
            [@b.select name="copyCheck.writer.std.state.department.id" label="院系" items=departs/]
            [@b.select label="是否通过"  name="copyCheck.passed" items={'1':'通过','0':'未通过'} /]
            [@b.select label="是否复检"  name="copyCheck.recheck" items={'0':'初检','1':'复检'}/]
            <input type="hidden" name="orderBy" value="copyCheck.writer.std.code"/>
        [/@]
    </div>
    <div class="search-list">[@b.div id="checklist"/]
  </div>
</div>
<script>
  $(document).ready(function() {
    bg.form.submit("checkSearchForm");
  });
</script>
[@b.foot/]
