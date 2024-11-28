[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
  [@b.toolbar title="上传论文"]
    bar.addBack();
  [/@]
  [@b.form name="form" action="!upload" theme="list"]
    [@b.field label="学生 题目"] ${writer.std.name} ${writer.thesisTitle!}[/@]
    [@b.file name="file" extensions="pdf" maxSize="20M" label="选择文件"/]
    [@b.formfoot]
      <input type="hidden" name="writer.id" value="${writer.id}"/>
      [@b.submit value="提交"/]
    [/@]
  [/@]
</div>
[@b.foot/]
