[@b.toolbar title="随机答辩分组"]
  bar.addBack();
[/@]
[@b.form name="randomGroupForm" action="!randomGroupSchedule" theme="list"]
  [@b.field label="负责人"]${manager.name}[/@]
  [@b.number label="分组个数" name="groupCount" value="2"/]
  [@b.field label="教师"+ teachers?size +"名"]
    [#list teachers as teacher]<input type="checkbox" value="${teacher.id}" name="teacher.id" checked="checked">${teacher.name}[#sep]&nbsp;[/#list]
  [/@]
  [@b.field label="学生"+ writers?size +"名"]
    <div style="padding-left: 6.25rem;">
    [#list majorWriters?keys as k]
    <input type="checkbox" value="${k.id}" name="major.id" checked="checked"><span>${k.name} ${majorWriters.get(k)?size}人</span>: [#list majorWriters.get(k) as writer]${writer.std.name}[#sep]&nbsp;[/#list] <br>
    [/#list]
    </div>
  [/@]
  [@b.formfoot]
    <input type="hidden" name="manager.id" value="${manager.id}"/>
    [@b.submit value="预览结果随机分组结果"/]
  [/@]
[/@]
