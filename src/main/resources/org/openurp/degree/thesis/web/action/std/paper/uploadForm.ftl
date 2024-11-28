[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
  [@b.toolbar title="上传论文"]
    bar.addBack();
  [/@]
  [@b.form name="form" action="!upload" theme="list"]
    [@b.field label="提交日期"]${(stageTime.beginOn)}~${stageTime.endOn}[/@]
    [@b.field label="论文题目"]${writer.thesisTitle!} <span style="color:red">确保题目和论文正文一致</span>[/@]
    [#if displayFinalized]
      [@b.field label="终稿提示"][#if !defaultFinalized]<span style="color:red">尚未定稿</span>[/#if]只有选择终稿时，才会推送给指导教师进行审批，否则影响审核进度。终稿上传成功，及时关注审核进度，必要时联系指导老师。[/@]
      [@b.radios label="是否终稿"  name="finalized" value=defaultFinalized items="1:common.yes,0:common.no" required="true" comment="为终稿时，系统会提醒指导老师进行审核。审核通过前可以更新。"/]
    [#else]
      [@b.field label="论文版本"]提交论文初稿[/@]
    [/#if]

    [@b.file name="file" extensions="doc,docx" maxSize="30M" label="选择文件"/]
    [@b.formfoot]
      [@b.submit value="提交"/]
    [/@]
  [/@]
</div>
[@b.foot/]
