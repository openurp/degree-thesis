[@b.head/]
[@b.toolbar title="学生论文指导与审核"]
  bar.addBack();
[/@]
[@b.form name="auditForm" action="!audit" theme="list"]
  [@b.field label="学生"]
   ${paper.writer.std.name}
  [/@]
  [@b.field label="论文题目"]
   [@b.a href="!doc?writer.id="+paper.writer.id target="_blank"]<i class="fa-solid fa-file"></i> ${paper.writer.thesisTitle!}[/@]
  [/@]
  [@b.field label="论文提交版本"]第${submissions?size}次提交[/@]
  [@b.field label="论文提交时间"]${paper.submitAt?string('MM-dd HH:mm')}[/@]
  [#if paper.finalized]
  [@b.radios label="终稿是否通过" name="passed"  value="1" items={'1':'通过','0':'不通过，修改后重新提交'} /]
  [/#if]
  [@b.textarea label="审核意见" name="opinion" rows="5" cols="60" required="false"/]
  [@b.file label="修订意见附件" name="file"/]

  [@b.formfoot]
    <input type="hidden" name="paper.id" value="${paper.id}"/>
    [#if submissions?size>0]
    <input type="hidden" name="submission.id" value="${submissions?sort_by("updatedAt")?reverse?first.id}"/>
    [/#if]
    [@b.submit value="提交审核意见"/]
  [/@]
[/@]
[@b.foot/]
