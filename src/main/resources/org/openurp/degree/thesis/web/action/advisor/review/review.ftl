[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
[@b.messages slash="3"/]
 [@b.form action="!saveReview?writer.id="+writer.id name="form"]
  <table class="list_table" style="width:90%;margin:auto">
      <tr>
        <td align="center" height="30" colspan="4" class="title">
          本科生毕业论文(设计)指导教师评分表</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">论文题目:</td>
        <td class="data" height="30" align="left" colspan="3">${writer.thesisTitle!}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">学生姓名:</td>
        <td class="data" height="30" align="left" colspan="3">${writer.name}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">学号:</td>
        <td class="data" height="30" align="left" colspan="3">${writer.code}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">班级:</td>
        <td class="data" height="30" align="left" colspan="3">${(writer.squad.name)!}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">专业:</td>
        <td class="data" height="30" align="left" colspan="3">${writer.major.name}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">学院:</td>
        <td class="data" height="30" align="left" colspan="3">${writer.department.name}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">指导老师:</td>
        <td class="data" height="30" align="left" colspan="3">${writer.advisor.teacher.name}</td>
      </tr>
      <tr>
        <td class="title" height="30" width="100">相关材料:</td>
        <td class="data" height="30" align="left" colspan="3"><a
          href="/rws/view.htm?xsid=${writer.id}">任务书</a>&nbsp;&nbsp;
          <a
          href="/ktbg/view.htm?xsid=${writer.id}">开题报告</a>&nbsp;&nbsp;
          <a
          href="/zqjc/view.htm?xsid=${writer.id}">中期检查</a>&nbsp;&nbsp;
          [@b.a href="paper!doc?writer.id=${writer.id}" target="_blank"]论文[/@]
          </td>
      </tr>
      [#--
      <tr>
        <td class="title" height="30" width="100">延期记录:</td>
        <td class="data" height="30" align="left" colspan="3"><p>
            <c:if test="${writer.yqjl==null}">
              无
            </c:if>
            <c:if test="${writer.yqjl!=null}">
              开题报告延期${writer.yqjl.ktbgyqcs}次,
                任务书延期${writer.yqjl.rwsyqcs}次, 中期检查延期${writer.yqjl.zqjcyqcs}次,
                论文提交延期${writer.yqjl.lwyqcs}次
            </c:if>
          </p></td>
      </tr>
      --]
      <tr>
        <td align="center" height="30" colspan="4" class="title">教师评分指标</td>
      </tr>

      <tr>
        <td class="header" height="40" width="75px">评价指标</td>
        <td class="header" height="40" width="25%">指标内涵</td>
        <td class="header" height="40" >评价标准</td>
        <td class="header" height="40" width="100px">得分</td>
      </tr>
      <tr>
        <td height="50" class="title">选题(10分)</td>
        <td height="50" class="data" align="left">选题的理论和实践价值</td>

        <td height="50" class="data" align="left">
          <p>下达任务书；教师提交论文题目。</p>
        </td>
        <td class="data" height="30" align="left"><input type="text"
          class="fs" id="subjectScore" name="review.subjectScore" value="${review.subjectScore!}" size="5" onchange="updateAdvisorScore()"/>&nbsp;&nbsp;<span
          id="xtError"></span></td>
      </tr>

      <tr>
        <td height="50" class="title">写作规范(20分)</td>
        <td height="50" class="data" align="left">写作格式是否符合要求</td>

        <td height="50" class="data" align="left">
          <p>结构:论文结构与格式符合写作规范。</p>
          <p>行文:表述准确，文理通顺。</p>
          <p>字数:论文正文不少于8000字（外语学院各专业不少于5000字）。</p>
        </td>
        <td class="data" height="30" align="left"><input type="text"
          class="fs" id="writeScore" name="review.writeScore" value="${review.writeScore!}" size="5" onchange="updateAdvisorScore()"/>&nbsp;&nbsp;<span
          id="xzgfError"></span></td>
      </tr>
      <tr>
        <td height="50" class="title">研究能力(50分)</td>
        <td height="50" class="data" align="left">对资料的搜集、综合分析及利用能力；运用研究方法手段的能力；逻辑推理能力；解决实际问题的能力。</td>
        <td height="50" class="data" align="left">
          <p>资料收集较为翔实;对所研究的问题具有较为系统的分析，论证层次清晰；结论可靠，研究成果贴近社会实际。</p>
        </td>
        <td class="data" height="30" align="left"><input type="text"
          class="fs" id="researchScore" name="review.researchScore" value="${review.researchScore!}" size="5" onchange="updateAdvisorScore()" />&nbsp;&nbsp;<span
          id="yjnlError"></span></td>
      </tr>

      <tr>
        <td height="50" class="title">创新水平(10分)</td>
        <td height="50" class="data" align="left">论文是否有创新思想与方法，或为实际问题的解决提供新的思路。</td>
        <td height="50" class="data" align="left">
          <p>研究结论富有新意，有一定的学术价值或应用价值。</p>
        </td>
        <td class="data" height="30" align="left"><input type="text"
          class="fs" id="innovationScore" name="review.innovationScore" value="${review.innovationScore!}" size="5" onchange="updateAdvisorScore()"/>&nbsp;&nbsp;<span
          id="cxspError"></span></td>
      </tr>

      <tr>
        <td height="50" class="title">写作态度(10分)</td>
        <td height="50" class="data" align="left">思想重视和时间投入</td>

        <td height="50" class="data" align="left">
          <p>态度认真，精力、时间投入充分，虚心听取指导教师指导，严格按任务书的要求完成毕业论文(设计)。</p>
        </td>
        <td class="data" height="30" align="left"><input type="text"
          class="fs" id="attitudeScore" name="review.attitudeScore" value="${review.attitudeScore!}" size="5" onchange="updateAdvisorScore()"/>
          &nbsp;&nbsp;<span  id="xztdError"></span></td>
      </tr>

      <tr>
        <td class="title" height="30" width="130">指导老师建议得分：</td>
        <td class="data" height="30" align="left" colspan="2">&nbsp;&nbsp;
          <input type="text" id="advisorScore" name="advisorScore" size="10" value="${review.advisorSelfScore!}" readonly="true"/>
          &nbsp;&nbsp;
          [#assign hasLimit =false/]
          [#list checks as check]
            [#if check.recheck][#assign hasLimit =true/][/#if]
          [/#list]
          [#if hasLimit]
          <span style="color:red">*该学生评分被限制 70 分，请评分不要超过!</span>
          [/#if]
          <span id="advisorScoreError" style="color:red"></span>&nbsp;&nbsp;
          [#if blindReview??]<span>校外送审成绩:${blindReview.score!'尚未公布'}</span>[/#if]
        </td>
        <td class="data" height="30" align="left"></td>
      </tr>
  </table>
      <div align="center">
      [@b.submit value="提交"/]&nbsp;&nbsp;  [@b.a href="!index" class="btn btn-primary btn-sm"]返回[/@]
      </div>
  [/@]
  <script>
    function updateAdvisorScore(){
       var s = parseInt(jQuery("#subjectScore").val());
       s += parseInt(jQuery("#writeScore").val());
       s += parseInt(jQuery("#researchScore").val());
       s += parseInt(jQuery("#innovationScore").val());
       s += parseInt(jQuery("#attitudeScore").val());
       jQuery("#advisorScore").val(s);
       if(s > 100){
         jQuery("#advisorScoreError").html("分数超过100分了");
       }else{
         jQuery("#advisorScoreError").html("");
       }
    }
  </script>
</div>
[@b.foot/]
