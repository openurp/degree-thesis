[@b.head/]
[@b.toolbar title="签名"]
  bar.addBack();
[/@]
[@b.form action="!save" theme="list" name="signForm"]
  [@b.field label="签名文档"]
    1. 承诺书 2.任务书 3.反抄袭检测情况记录表
  [/@]
  [#if signature??]
    [@b.field label="已有签名"]
      <img style="width:300px;height:100px" src="${signature}"/>
    [/@]
  [/#if]
  [@b.esign id="signature" name="signature" label="签名" required="false"/]
  [@b.formfoot]
    [@b.submit/]
  [/@]
[/@]
[@b.foot/]
