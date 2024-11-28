[@b.head/]
[@b.toolbar title='工作计划']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="planSearchForm" action="!search" title="ui.searchForm" target="proposalList" theme="search"]
        [@b.select name="plan.season.id" label="毕业界别" items=seasons required="true"/]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("planSearchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="proposalList"/]</div>
</div>
[@b.foot/]
