[#ftl]
[@b.head/]
<div class="search-container">
    <div class="search-panel">
    [@b.form name="thesisDocTypeSearchForm" action="!search" target="thesisDocTypelist" title="ui.searchForm" theme="search"]
      [@b.textfields names="thesisDocType.code;代码"/]
      [@b.textfields names="thesisDocType.name;名称"/]
      [@b.select label="是否有效"  name="active" items={"1":"是","0":"否"} value="1" empty="..."/]
      <input type="hidden" name="orderBy" value="thesisDocType.idx"/>
    [/@]
    </div>
    <div class="search-list">[@b.div id="thesisDocTypelist" href="!search?orderBy=thesisDocType.idx&active=1"/]
  </div>
</div>
[@b.foot/]
