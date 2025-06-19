[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
  [@b.toolbar title="上传论文抽检材料"]
    bar.addBack();
  [/@]
  [@b.form name="form" action="!uploadDoc" theme="list"]
    [@b.field label="抽检界别"] ${season.graduateIn}[/@]
    [@b.radios label="材料类型" items=docTypes name="docType.id"/]
    [@b.file name="file" extensions="zip" maxSize="550M" label="选择文件"/]
    [@b.formfoot]
      <input type="hidden" name="thesisCheck.season.id" value="${season.id}"/>
      [@b.submit value="提交"/]
    [/@]
  [/@]
</div>
[@b.foot/]
