[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
[@b.toolbar title="论文成绩查询"]bar.addBack();[/@]
  [#if thesisReview??]
    <table class="list_table" width="100%">
      <tr>
        <td class="title" height="30" colspan="2">${writer.std.name}的论文成绩</td>
      </tr>
      <tr>
        <td class="title" style="width:120px" height="30">学号姓名</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.writer.std.code} ${thesisReview.writer.std.name} ${thesisReview.writer.std.state.department.name}</td>
      </tr>
      <tr>
        <td class="title" height="30">指导老师</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.writer.advisor.name)!}</td>
      </tr>
      <tr>
        <td class="title" height="30">指导老师建议得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.advisorScore)!'--'}</td>
      </tr>
      <tr>
        <td class="title" height="30">选题得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.subjectScore!}</td>
      </tr>
      <tr>
        <td class="title" height="30">写作规范得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.writeScore!}</td>
      </tr>
      <tr>
        <td class="title" height="30">研究能力得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.researchScore!}</td>
      </tr>
      <tr>
        <td class="title" height="30">创新水平得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.innovationScore!}</td>
      </tr>
      <tr>
        <td class="title" height="30">写作态度得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.attitudeScore!}</td>
      </tr>
      <tr>
        <td class="title" height="30">评阅老师</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.crossReviewer.name)!'  '}</td>
      </tr>
      <tr>
        <td class="title" height="30">评阅得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.crossReviewScore)!'  '}</td>
      </tr>
      <tr>
        <td class="title" height="30">是否同意答辩</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.defensePermitted?string('是','否'))!'  '}</td>
      </tr>
      <tr>
        <td class="title" height="30">交叉评阅意见</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.crossReviewOpinion)!'  '}</td>
      </tr>
      <tr>
        <td class="title" height="30">答辩得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${(thesisReview.defenseScore)!'  '}</td>
      </tr>
      <tr>
        <td class="title" height="30">最终得分</td>
        <td class="data" style="text-align:left;padding-left:20px">${thesisReview.finalScore!} ${thesisReview.finalScoreText!}</td>
      </tr>
    </table>
  [#else]
    尚没有你的答辩成绩
  [/#if]
[@b.foot/]
