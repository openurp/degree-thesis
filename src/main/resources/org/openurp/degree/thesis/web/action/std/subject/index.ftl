[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.messages slash="3"/]
  <br>
    <table class="list_table" width="100%" class="list_table">
      <tr>
        <td class="title" colspan="5" height="30" align="center">${writer.std.state.department.name} ${writer.std.code} ${writer.std.name} 选题结果</td>
      </tr>
      <tr>
        <td class="header" align="center" height="30">选题轮次</td>
        <td class="header" align="center" height="30">题目</td>
        <td class="header" align="center" height="30">指导教师</td>
        <td class="header" align="center" height="30">操作</td>
      </tr>
      <tr>
        <td class="data" align="center" height="30">初选</td>
        [#if apply??&&apply.first??]
        <td class="data" align="center" height="30">${apply.first.name}</td>
        <td class="data" align="center" height="30"><a href="${b.base}/common/advisor/${apply.first.advisor.id}" target="_blank">${apply.first.advisor.teacher.name}</a></td>
        [#else]
        <td class="data" align="center" height="30"></td>
        <td class="data" align="center" height="30"></td>
        [/#if]
        <td class="data" align="center" height="30">
          [#if !(apply??&&apply.last??)]
            [@b.a href="!candinates?round=1"]初选选题[/@]
          [/#if]
          [#if plan??]
          <span class="text-muted">(${plan.getStageTime(StageRound1).beginOn}~${plan.getStageTime(StageRound1).endOn})</span>
          [/#if]
        </td>
      </tr>

      <tr>
        <td class="data" align="center" height="30">补选</td>
        [#if apply??&&apply.second??]
        <td class="data" align="center" height="30">${apply.second.name}</td>
        <td class="data" align="center" height="30"><a href="${b.base}/common/advisor/${apply.second.advisor.id}" target="_blank">${apply.second.advisor.teacher.name}</a></td>
        [#else]
        <td class="data" align="center" height="30"></td>
        <td class="data" align="center" height="30"></td>
        [/#if]
        <td class="data" align="center" height="30">
          [#if !(apply??&&apply.last??)]
          [@b.a href="!candinates?round=2"]补选选题[/@]
          [/#if]
          [#if plan??]
          <span class="text-muted">(${plan.getStageTime(StageRound2).beginOn}~${plan.getStageTime(StageRound2).endOn})</span>
          [/#if]
        </td>
      </tr>

[#if apply??&&apply.last??]
      <tr>
        <td class="data" align="center" height="30">最终</td>
        <td class="data" align="center" height="30">${apply.last.name}</td>
        <td class="data" align="center" height="30"><a target="_blank" href="${b.base}/common/advisor/${apply.last.advisor.id}">${apply.last.advisor.teacher.name}</a></td>
        <td class="data" align="center" height="30"></td>
      </tr>
    [#else]
      <tr>
        <td class="data" align="center" height="30">最终</td>
        <td class="data" align="center" height="30">--</td>
        <td class="data" align="center" height="30">--</td>
        <td class="data" align="center" height="30">--</td>
      </tr>
    [/#if]
    </table>
    <br>
    [#if writer.thesisTitle??]
      [#assign advisor=writer.advisor/]
      [@b.card class="card-info card-primary card-outline"]
        [#assign title]<i class="fas fa-file-pdf"></i> ${writer.thesisTitle!},研究方向：${(writer.researchField)!'--'}[/#assign]
        [@b.card_header class="border-transparent" title=title ]
          [@b.card_tools]
            [#if hasProposal]
              [@b.a href="!edit"
                onclick="return bg.Go(this,null,'请确保修改后的选题与最终提交的论文题目保持一致且经过指导教师的同意，因随意修改导致无成绩、归档材料有瑕疵等问题的，将追究学生的责任。')"]<i class="fas fa-edit"></i>修改[/@]
            [#else]
              [@b.a href="!edit"]<i class="fas fa-edit"></i>修改论文题目[/@]
            [/#if]
          [/@]
        [/@]
        [@b.card_body class="p-0"]
          <table class="infoTable">
            <tr>
              <td style="width:13%" class="title">指导教师:</td>
              <td> ${advisor.teacher.department.name} &nbsp;${advisor.teacher.name}</td>
            </tr>
            <tr>
              <td class="title">指导教师简介:</td>
              <td><pre>${advisor.description!}</pre></td>
            </tr>
            <tr>
              <td class="title">指导教师联系方式:</td>
              <td>${advisor.mobile!} <a href="mailto:${advisor.email!}">${advisor.email!}</a></td>
            </tr>
            <tr>
              <td class="title">现有条件:</td>
              <td>${(subject.conditions)!}</td>
            </tr>
            <tr>
              <td class="title">主要内容:</td>
              <td>${(subject.contents)!}</td>
            </tr>
            <tr>
              <td class="title">对学生的要求:</td>
              <td>${(subject.requirements)!}</td>
            </tr>
          </table>
        [/@]
      [/@]
    [/#if]
   [@b.card class="card-info card-primary card-outline" id="profile_card"]
    [#assign title]<i class="fas fa-file-pdf"></i> 修改个人联系方式[/#assign]
    [@b.card_header class="border-transparent" title=title ]
    [/@]
    [@b.card_body class="p-0"]
      [@b.form name="profileForm" action="!saveProfile" theme="list"]
        [@b.cellphone name="mobile" placeholder="手机" required="true" label="手机" value=writer.mobile!/]
        [@b.email name="email" placeholder="邮箱" required="true" label="邮箱" value=writer.email!  style="width:400px"/]
        [@b.formfoot]
          [@b.submit value="提交"/]
        [/@]
      [/@]
    [/@]
  [/@]
</div>
[@b.foot/]
