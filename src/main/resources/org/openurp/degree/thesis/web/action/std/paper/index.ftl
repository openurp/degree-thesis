[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">

[@b.card class="card-info card-primary card-outline"]
  [@b.card_header title=writer.thesisTitle!'--' ]
     [@b.card_tools]
       [@b.a href="!uploadForm"]<i class="fa-solid fa-upload"></i> [#if file??]重新上传[#else]上传[/#if][/@]
       [#if file??]
       [@b.a href="!attachment" target="_blank"]<i class="fa-solid fa-download"></i> 下载论文[/@]
       [/#if]
     [/@]
  [/@]
  [@b.card_body class="p-0"]
    [@b.grid items=submissions var="sub" caption="历史版本" class="border-1px border-colored"]
      [@b.row]
       [@b.col title="序号" width="50px"]${sub_index+1}[/@]
       [@b.col title="名称" ][@b.a href="!submission?submission.id="+sub.id target="_blank"]${sub.title}[/@][/@]
       [@b.col title="上传时间" width="150px"]${sub.updatedAt?string("yyyy-MM-dd HH:mm:ss")}[/@]
       [@b.col title="是否终稿" property="finalized" width="80px"]
         ${sub.finalized?string("终稿","初稿")}
       [/@]
       [@b.col title="审核结论" property="finalized" width="100px"]
         [#if sub.finalized]
           [#if sub.advisorPassed??]
             ${sub.advisorPassed?string("通过","修改后提交")}
           [#else]
             未审核
           [/#if]
         [#else]
         --
         [/#if]
       [/@]
       [@b.col title="指导老师意见" property="advisorOpinion" width="300px"/]
       [@b.col title="指导老师修订附件" width="150px"]
        [#if sub.revisionPath??]
        [@b.a href="!advisorRevision?submission.id="+sub.id target="_blank"]下载修订附件[/@]
        [/#if]
       [/@]
      [/@]
    [/@]
      [@b.messages slash="3"/]
    <p style="width: 800px; white-space: pre-wrap;color:red;">
      ${delay!}
      提交论文时请注意：
      1、论文题目必须与选题结果中的题目一致，不一致的，请在选题结果中对题目进行修改;
      2、学生提交的论文名按照"学号",例如"0305133.docx"，仅支持word格式。
      3、初稿论文上传时间：${draftStageTime.beginOn}~${draftStageTime.endAt?string("yyyy-MM-dd HH:mm")}。
      4、终稿论文上传时间：${finalStageTime.beginAt?string("yyyy-MM-dd HH:mm")}~${finalStageTime.endAt?string("yyyy-MM-dd HH:mm")}。
      5、提交论文初稿，指导老师只能给与指导建议，无法审核。
      6、论文终稿提交后，指导老师审核前或者审核不通过，可以自行更新。审核通过后，还需更新的，请联系老师审核驳回。
      8、只有经由指导老师审核通过的论文，才会提交反抄袭检测。
      7、上传论文注意截止日期和时间。
    </p>

  [/@]
[/@]

</div>

[@b.foot/]
