[#ftl]
[@b.head/]
[@b.toolbar title="校外送审名单"/]
<div class="search-container">
    <div class="search-panel">
        [@b.form name="reviewSearchForm" action="!search" target="reviewlist" title="ui.searchForm" theme="search"]
            [@b.select name="review.writer.season.id" label="毕业界别" items=seasons required="true"/]
            [@b.textfields names="review.writer.std.code;学号"/]
            [@b.textfields names="review.writer.std.name;姓名"/]
            [@b.select name="review.writer.std.state.department.id" label="院系" items=departs/]
            <input type="hidden" name="orderBy" value="review.writer.std.code asc"/>
        [/@]
    </div>
    <div class="search-list">[@b.div id="reviewlist"/]
  </div>
</div>
<script>
  $(document).ready(function() {
    bg.form.submit("reviewSearchForm");
  });
</script>
[@b.foot/]
