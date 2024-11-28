[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
  [@b.toolbar title="上传论文"]
    bar.addBack();
  [/@]
  [@b.form name="form" action="!upload" theme="list"]
    [@b.field label="学生"]${writer.std.code} ${writer.std.name}[/@]
    [@b.textfield label="论文题目" required="true" name="thesisPaper.title" value=writer.thesisTitle! style="width:400px"/]
    [@b.radios label="是否优秀论文"  name="thesisPaper.excellent" value=(thesisPaper.excellent)!false items="1:common.yes,0:common.no" required="true"/]
    [@b.file name="paper_file" extensions="docx,doc,pdf" maxSize="30M" label="选择论文正文"/]
    [@b.file name="proposal_file" extensions="pdf" maxSize="20M" label="选择开题报告" comment="非必填"/]
    [@b.file name="defense_file" extensions="pdf" maxSize="20M" label="选择答辩评分表" comment="非必填"/]
    [@b.formfoot]
      <input type="hidden" name="writer.id" value="${writer.id}"/>
      [@b.submit value="提交"/]
    [/@]
  [/@]
</div>
[@b.foot/]
