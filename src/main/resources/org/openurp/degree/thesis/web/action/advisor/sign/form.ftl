[@b.head/]
[@b.form action="!save" theme="list" name="signForm"]
  [@b.field label="签名文档"]
    1. 开题报告 2. 指导教师评分表
  [/@]
  [@b.field label="指导学生"]
    [#list writers as writer]${writer.name}[#sep]&nbsp;[/#list]
  [/@]
  [@b.esign id="signature" name="signature" label="签名" required="false"/]
  [@b.formfoot]
    [@b.submit/]
  [/@]
[/@]
[@b.foot/]
