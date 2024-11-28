[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
    [@b.toolbar title="修改论文题目"]
      bar.addBack();
    [/@]
    [@b.form name="form" action="!save" theme="list"]
      [@b.field label="学生"]${writer.std.code} ${writer.std.name}[/@]
      [@b.field label="指导老师"]${(writer.advisor.teacher.name)!'--'}[/@]
      [@b.textfield required="true" maxlength="150" label="题目名称" style="width:400px" value=writer.thesisTitle! name="thesisTitle" comment="（150字以内）"/]
      [@b.textfield required="true" maxlength="50" label="研究方向" value=(writer.researchField)! name="researchField" comment="（50字以内）"/]
      [@b.formfoot]
        [@b.submit/]
        [@b.reset/]
      [/@]
    [/@]
  </div>
[@b.foot/]
