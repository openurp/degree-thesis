[@b.head/]
[@b.toolbar title='论文评阅']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="writerSearchForm" action="!search" title="ui.searchForm" target="reviewList" theme="search"]
        [@b.select name="thesisReview.writer.season.id" label="毕业界别" items=seasons required="true"/]
        [@b.textfield name="thesisReview.writer.std.code" label="学号"/]
        [@b.textfield name="thesisReview.writer.std.name" label="姓名"/]
        [@b.textfield name="thesisReview.writer.advisor.teacher.name" label="教师姓名"/]
        [@b.select name="thesisReview.writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="thesisReview.writer.std.state.major.name" label="专业"/]
        [@b.textfield name="thesisReview.writer.std.state.squad.name" label="班级"/]
        [@b.textfield name="thesisReview.crossReviewManager.name" label="负责人"/]
        [@b.textfield name="thesisReview.crossReviewer.name" label="评阅人"/]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("writerSearchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="reviewList"/]</div>
</div>
[@b.foot/]
