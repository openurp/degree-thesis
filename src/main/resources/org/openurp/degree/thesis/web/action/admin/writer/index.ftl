[@b.head/]
[@b.toolbar title='学生管理维护' id='studentBar']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="studentSearchForm" id="studentSearchForm" action="!search" title="ui.searchForm" target="studentList"]
        [#include "searchForm.ftl"/]
      [/@]
    </div>
    <div class="search-list">[@b.div id="studentList"/]</div>
</div>
[@b.foot/]
