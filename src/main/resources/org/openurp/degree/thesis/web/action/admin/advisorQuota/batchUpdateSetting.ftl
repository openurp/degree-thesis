[@b.head/]
[@b.toolbar title="批量修改指导教师信息"]bar.addBack();[/@]
[@b.form name="newAdvisorForm" action="!batchUpdate" theme="list"]
  [@b.field label="教师"]
    <div style="width:80%;overflow:hidden">
      <ul>[#list advisors?sort_by("name") as advisor]<li style="float: left;list-style-type: decimal;margin-left: 5px;margin-right: 20px;">${advisor.name}${advisor.maxWriters}篇</li>[/#list]</ul>
    </div>
  [/@]
  [@b.number label="带学生上限" name="maxWriters" required="true" value="8"/]
  [@b.formfoot]
    <input name="advisorIds" type="hidden" value="[#list advisors as a]${a.id}[#sep],[/#list]"/>
    [@b.submit value="保存"/]
  [/@]
[/@]
<br/><br/><br/><br/><br/><br/><br/>
[@b.foot/]
