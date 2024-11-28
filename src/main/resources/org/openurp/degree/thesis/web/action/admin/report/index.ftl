[@b.head/]
<div class="container-fluid">
[@b.form action="!index"]
  [@b.select name="season.id" label="毕业界别" value=season items=seasons /]
  [@b.select name="depart.id" label="院系" value=depart items=departs /]
  [@b.submit value="统计" class="btn btn-outline-primary btn-sm"/]
[/@]

[@b.tabs]
  [@b.tab  label="毕业论文信息汇总表" href="!thesis?season.id="+season.id+"&depart.id="+depart.id/]
  [@b.tab  label="答辩安排信息汇总表" href="!defense?season.id="+season.id+"&depart.id="+depart.id/]
  [@b.tab  label="指导老师统计表" href="!title?season.id="+season.id+"&depart.id="+depart.id/]
  [@b.tab  label="其他表格" href="!material?season.id="+season.id+"&depart.id="+depart.id/]
[/@]
</div>
[@b.foot/]
