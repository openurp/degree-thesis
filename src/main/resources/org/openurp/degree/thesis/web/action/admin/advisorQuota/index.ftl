[@b.head/]
[@b.toolbar title='指导教师篇数管理' id='advisorBar']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="advisorSearchForm" id="advisorSearchForm" action="!search" title="ui.searchForm" target="advisorList"]
        [#include "searchForm.ftl"/]
      [/@]
    </div>
    <div class="search-list">[@b.div id="advisorList"/]</div>
</div>
[@b.foot/]
