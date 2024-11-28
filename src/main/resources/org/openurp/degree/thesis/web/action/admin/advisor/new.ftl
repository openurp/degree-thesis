[@b.head/]
[@b.toolbar title="增加指导教师"]bar.addBack();[/@]
[@b.form name="newAdvisorForm" action=b.rest.save(advisor) theme="list"]
  [@b.select name="advisor.teacher.id" label="工号" required="true" items=teachers option="id,description" width="300px" empty="..."/]
  [@b.select name="depart.id" label="指导院系" required="true" items=departs values=advisor.departs chosenMin="1" multiple="true" style="width:300px"/]
  [@b.startend label="有效期限"
      name="advisor.beginOn,advisor.endOn" required="true,false"
      start=advisor.beginOn end=advisor.endOn format="date"/]
  [@b.formfoot]
    [@b.submit value="增加"/]
  [/@]
[/@]
<br><br><br><br>
[@b.foot/]
