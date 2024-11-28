[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
[@b.form name="form" action="!review" onsubmit="checkDefensePermitted"]
  <div align="center">
    [#assign writer = review.writer/]
    <table class="list_table">
      <tr>
        <td align="center"  colspan="2" class="title">
          本科生毕业论文(设计)评阅表</td>
      </tr>
      <tr>
        <td class="title"  width="130px">论文题目</td>
        <td class="data"  style="text-align:left">${writer.thesisTitle!}</td>
      </tr>
      <tr>
        <td class="title" >学生姓名</td>
        <td class="data"  style="text-align:left">${writer.std.name}</td>
      </tr>
      <tr>
        <td class="title" >学号</td>
        <td class="data"  style="text-align:left">${writer.std.code}</td>
      </tr>
      <tr>
        <td class="title" >班级</td>
        <td class="data"  style="text-align:left">${(writer.squad.name)!}</td>
      </tr>
      <tr>
        <td class="title" >专业</td>
        <td class="data"  style="text-align:left">${(writer.std.major.name)!}</td>
      </tr>
      <tr>
        <td class="title" >学院</td>
        <td class="data"  style="text-align:left">${(writer.std.department.name)!}</td>
      </tr>
      <tr>
        <td class="title" >指导老师</td>
        <td class="data"  style="text-align:left">${(writer.advisor.name)!}</td>
      </tr>
      <tr>
        <td class="title" >相关材料</td>
        <td class="data"  style="text-align:left">
         [@b.a href="commitment!info?writer.id="+writer.id target="_blank"]任务书[/@]&nbsp;&nbsp;
         [@b.a href="proposal!info?writer.id="+writer.id target="_blank"]开题报告[/@]&nbsp;&nbsp;
         [@b.a href="midterm-check!info?writer.id="+writer.id target="_blank"]中期检查[/@]&nbsp;&nbsp;
         [@b.a href="paper!doc?writer.id="+writer.id target="_blank"]论文[/@]
        </td>
      </tr>
      <tr>
        <td class="title" >指导教师建议得分</td>
        <td class="data"  style="text-align:left">
        ${review.advisorScore!}
        </td>
      </tr>
      <tr>
        <td class="title" >评阅意见</td>
        <td class="data"  style="text-align:left">
          <div class="jer_info_line">评阅意见必须在50-600字之间</div>
          <textarea rows="10" cols="80" id="crossReviewOpinion" name="thesisReview.crossReviewOpinion">${review.crossReviewOpinion!}</textarea>
        </td>
      </tr>
      <tr>
        <td class="title" >论文(设计)得分</td>
        <td class="data"  style="text-align:left">
        <input type="text" name="thesisReview.crossReviewScore" id="crossReviewScore" value="${review.crossReviewScore!}"/>
        </td>
      </tr>
      <tr>
        <td class="title" >是否同意答辩</td>
        <td class="data"  style="text-align:left">
        [#assign sftydb="3"/]
        [#if review.defensePermitted??]
          [#assign sftydb=review.defensePermitted?string("1","0")/]
        [/#if]
        <input type="radio" name="thesisReview.defensePermitted" id="sftydb1" [#if sftydb='1'] checked="checked"[/#if] value="1"/> <label for="sftydb1">是</label>
        <input type="radio" name="thesisReview.defensePermitted" id="sftydb2" [#if sftydb='0'] checked="checked"[/#if]value="0"/> <label for="sftydb2">否</label>
        </td>
      </tr>
      <tr>
        <td class="title" >操作</td>
        <td class="data"  style="text-align:left">
        <input type="hidden" name="thesisReview.id" value="${review.id}">
        [@b.submit value="提交"/]&nbsp;&nbsp;
        <input type="button" onclick=history.back(); value="返回">
        </td>
      </tr>
    </table>
    <p><font color="red">注：评阅教师在参考指导教师建议得分的基础上，给出论文评阅分，以百分制评分。</font></p>
  </div>
[/@]
<script>
  function checkDefensePermitted(){
     if(!document.getElementById("crossReviewScore").value){
       alert("请输入评阅成绩");
       return false;
     }
     var opinion = document.getElementById("crossReviewOpinion").value
     if(opinion.length<50){
       alert("请输入50-500字的评阅意见");
       return false;
     }
     if(!(document.getElementById("sftydb1").checked || document.getElementById("sftydb2").checked)){
        alert("请选择是否同意答辩");
        return false;
     }else{
        return true;
     }
  }
</script>
</div>
[@b.foot/]
