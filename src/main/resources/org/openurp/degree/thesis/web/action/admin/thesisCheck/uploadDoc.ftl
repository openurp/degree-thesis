[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
  [@b.toolbar title="上传评分表结果"]
    bar.addBack();
  [/@]
  [#list messages as message]
    <p>${message}</p>
  [/#list]
  [@b.form name="form" action="!uploadDefenseDoc" theme="list"]
    [@b.field label="毕业界别"] ${season.graduateOn}[/@]
    [@b.file name="file" extensions="zip" maxSize="100M" label="选择文件"/]
    [@b.formfoot]
      <input type="hidden" name="thesisCheck.writer.season.id" value="${season.id}"/>
      [@b.submit value="提交"/]
    [/@]
  [/@]
</div>
[@b.foot/]
