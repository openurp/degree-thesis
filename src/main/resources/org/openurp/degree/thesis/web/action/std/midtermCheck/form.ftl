[@b.head/]
<script src="${b.base}/static/scripts/jer_normal.js"></script>
<div class="container-fluid">
  [@b.toolbar title="填写期中检查，写作进度"]
    bar.addBack();
  [/@]
  [@b.form action="!save" theme="list"]
    [@b.field label="姓名"]${writer.std.name}[/@]
    [@b.field label="学号"]${writer.std.code}[/@]
    [@b.field label="班级"]${writer.squad.name}[/@]
    [@b.field label="专业"]${writer.major.name}[/@]
    [@b.field label="学院"]${writer.std.state.department.name}[/@]
    [@b.field label="指导老师"]${writer.advisor.teacher.name}[/@]
    [@b.field label="论文题目"]${writer.thesisTitle!}[/@]
    [@b.field label="写作进度" ]
      <div class="jer_info_line">参照任务书,字数不得少于50个字,已输入<span id="word_count">0</span>字</div>
      <textarea rows="10" cols="90" name="proceeding" id="proceeding" onkeyup="keydown_word_count(this)">${(midtermCheck.proceeding)!}</textarea>
    [/@]
    [@b.formfoot]
      [@b.reset/]&nbsp;[@b.submit value="提交"/]
    [/@]
  [/@]
  <script>
  jQuery(document).ready(function($) {
    keydown_word_count($('#proceeding'));
  });
  </script>
</div>
[@b.foot/]
