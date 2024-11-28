[@b.head/]
[@b.toolbar title="批量修改题目"]bar.addBack();[/@]
[@b.form name="subjectEditForm" action="!batchUpdate" theme="list"]
  [@b.field label="题目"]
    <div style="width:80%;overflow:hidden">
      <ul>[#list subjects?sort_by("name") as subject]
        <li style="float: left;list-style-type: decimal;margin-left: 5px;margin-right: 20px;">
          ${subject.advisor.name} ${subject.name}([#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list])
        </li>
          [/#list]
      </ul>
    </div>
  [/@]
  [@b.select multiple="true" label="面向专业" name="major.id" required="true"  items=majors style="width:500px"/]
  [@b.formfoot]
    <input name="subjectIds" type="hidden" value="[#list subjects as a]${a.id}[#sep],[/#list]"/>
    [@b.submit value="保存"/]
  [/@]
[/@]
<br/><br/><br/><br/><br/><br/><br/>
[@b.foot/]
