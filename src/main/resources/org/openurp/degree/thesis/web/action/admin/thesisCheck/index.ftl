[@b.head/]
[@b.toolbar title='教育部学生论文抽检数据上报']
[/@]
<div class="search-container">
    <div class="search-panel">
      [@b.form name="searchForm" action="!search" title="ui.searchForm" target="checkList" theme="search"]
        [@b.select name="thesisCheck.season.id" label="抽检界别" items=seasons required="true"/]
        [@b.textfield name="thesisCheck.writer.std.code" label="学号" maxlength="99999999"/]
        [@b.textfield name="thesisCheck.writer.std.name" label="姓名"/]
        [@b.textfield name="thesisCheck.advisor" label="教师姓名"/]
        [@b.select name="thesisCheck.writer.std.state.department.id" label="院系" items=departs/]
        [@b.textfield name="thesisCheck.writer.std.state.squad.name" label="班级"/]
        [@b.select name="thesisCheck.paperDoc.filePath" label="论文格式" items={'doc':'.doc','pdf':'.pdf','wps':'.wps'} empty="..."/]
        [@b.textfield name="thesisCheck.title" label="论文题目"/]
        [@b.select name="sameTitle" label="上报题目" items={'1':'与写作一致','0':'与写作不一致'} empty="..."/]
        [@b.textfield name="thesisCheck.language.name" label="撰写语言"/]
        [@b.textfield name="thesisCheck.keywords" label="关键词"/]
        [@b.textfield name="thesisCheck.researchField" label="研究领域"/]
        [@b.select label="材料齐全" name="docOk" value="" items={'1':'是','0':'否'} empty="..." /]
      [/@]
      <script>
        $(document).ready(function() {
          bg.form.submit("searchForm");
        });
      </script>
    </div>
    <div class="search-list">[@b.div id="checkList"/]</div>
</div>
[@b.foot/]
