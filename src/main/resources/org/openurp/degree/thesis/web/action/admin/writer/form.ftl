[@b.head/]
[@b.toolbar title="修改学生信息1"]bar.addBack();[/@]
[@b.form name="newStdForm" action=b.rest.save(writer) theme="list"]
  [@b.field label="学生"]${writer.std.code} ${writer.std.name} ${writer.std.state.department.name}[/@]
  [@b.select label="毕业界别" name="writer.season.id"  required="true" value=writer.season items=seasons width="200px"/]
  [@b.select label="指导老师工号" name="writer.advisor.id"  value=writer.advisor! items=advisors option=r"${item.code} ${item.name}"/]
  [@b.textfield label="手机" name="writer.mobile" value=writer.mobile!/]
  [@b.email label="电子邮箱" name="writer.email" value=writer.email!/]
  [@b.formfoot]
    [@b.submit value="保存"/]
  [/@]
  <br><br><br><br>
[/@]
[@b.foot/]
