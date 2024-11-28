[@b.head/]
[@b.toolbar title="修改评阅信息"]bar.addBack();[/@]
[@b.form name="reviewForm" action=b.rest.save(thesisReview) theme="list"]
  [@b.field label="学生"]${thesisReview.writer.std.code} ${thesisReview.writer.std.name} ${thesisReview.writer.std.state.department.name}[/@]
  [@b.field label="指导老师"]${(thesisReview.writer.advisor.name)!}[/@]
  [@b.field label="建议得分"]<span id="advisor_score">${(thesisReview.advisorScore)!'--'}</span>[/@]
  [@b.number label="选题得分" name="thesisReview.subjectScore" value=thesisReview.subjectScore! onchange="updateAdvisorScore()" min="0" max="10" comment="满分10分"/]
  [@b.number label="写作规范得分"  name="thesisReview.writeScore" value=thesisReview.writeScore! onchange="updateAdvisorScore()" min="0" max="20" comment="满分20分"/]
  [@b.number label="研究能力得分" name="thesisReview.researchScore" value=thesisReview.researchScore! onchange="updateAdvisorScore()" min="0" max="50" comment="满分50分"/]
  [@b.number label="创新水平得分" name="thesisReview.innovationScore" value=thesisReview.innovationScore! onchange="updateAdvisorScore()" min="0" max="10" comment="满分10分"/]
  [@b.number label="写作态度得分" name="thesisReview.attitudeScore" value=thesisReview.attitudeScore! onchange="updateAdvisorScore()" min="0" max="10" comment="满分10分"/]
  [@b.field label="评阅老师"]${(thesisReview.crossReviewer.name)!'  '}[/@]
  [@b.number label="评阅得分" name="thesisReview.crossReviewScore" value=thesisReview.crossReviewScore!  min="0" max="100" comment="满分100分"/]
  [@b.radios label="是否同意答辩"  name="thesisReview.defensePermitted" value=thesisReview.defensePermitted! items="1:common.yes,0:common.no"/]
  [@b.textarea label="交叉评阅意见" name="thesisReview.crossReviewOpinion" value=thesisReview.crossReviewOpinion! maxlength="800" cols="80" rows="5"/]
  [@b.number label="答辩得分" name="thesisReview.defenseScore" value=thesisReview.defenseScore!  min="0" max="100" comment="满分100分"/]
  [@b.formfoot]
    [@b.submit value="保存"/]
  [/@]
[/@]
  <script>
    function updateAdvisorScore(){
       var form = document.reviewForm;
       var s = parseInt(form['thesisReview.subjectScore'].value);
       s += parseInt(form['thesisReview.writeScore'].value);
       s += parseInt(form['thesisReview.researchScore'].value);
       s += parseInt(form['thesisReview.innovationScore'].value);
       s += parseInt(form['thesisReview.attitudeScore'].value);
       jQuery("#advisor_score").html(s);
       if(s > 100){
         jQuery("#advisor_score").html("分数"+s+"超过100分了");
       }
    }
  </script>
[@b.foot/]
