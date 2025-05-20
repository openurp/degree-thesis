[@b.head/]
[@b.toolbar title="批量设置论文材料中的签字日期"]
  bar.addBack();
[/@]
<style>
fieldset.listset li > label.title{
  min-width: 8rem;
}
</style>
[@b.form action="!batchUpdateTime" theme="list"]
  [@b.field label="界别"]${season.name}[/@]
  [@b.field label="批量操作提示"]
    <span class="alert alert-warning" style="display: inline-block;margin: 0px;">批量操作本届（所有）学生的论文材料签字日期，谨慎操作！</span>
  [/@]
  [@b.date label="任务书确认日期" name="commitment.updatedAt" required="false"/]
  [@b.date label="开题报告通过日期" name="proposal.confirmAt" required="false"/]
  [@b.date label="论文导师评阅日期" name="thesisReview.advisorReviewAt" required="false"/]
  [@b.date label="交叉评阅日期" name="thesisReview.crossReviewAt" required="false"/]
  [@b.formfoot]
    <input type="hidden" name="archive.writer.season.id" value="${season.id}"/>
    [@b.submit /]
  [/@]
[/@]
[@b.foot/]
