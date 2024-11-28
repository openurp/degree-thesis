[@b.head/]
<div class="container-fluid">
  [@b.form name="statForm" action="!stat" target="statList" ]
    [@b.select name="fromSeason.id" label="从" items=seasons value=seasons?last/]
    [@b.select name="toSeason.id" label="到" items=seasons value=seasons?last/]
    [@b.select name="writer.advisor.teacher.department.id" items=departs label="教师所在学院" empty="..."  style="width:200px"/]
    [@b.select name="writer.advisor.teacher.staff.title.id" items=titles label="职称" empty="..." multiple="true" style="width:200px"/]
    [@b.textfield name="writer.advisor.teacher.staff.code" label="工号" placeholder="多个工号使用半角逗号分割" maxlength="800" style="width:200px"/]
    [@b.radios name="statByAdvisor" value="1" items="1:不分界别,0:分界别" label="统计口径"/]
    [@b.submit value="统计"/]
  [/@]
  [@b.div id="statList"/]
</div>
[@b.foot/]
