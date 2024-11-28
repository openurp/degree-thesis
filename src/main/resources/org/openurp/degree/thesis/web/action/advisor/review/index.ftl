[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
[@b.messages slash="3"/]
    <font color="red">交叉评分完成后，指导教师评分将不能修改。</font>
    <table class="list_table" width="100%">
      <tr>
        <td class="title" colspan="12" >本科生毕业论文(设计)指导教师评分</td>
      </tr>
      <tr>
        <td class="header" align="center"  rowspan="2">序号</td>
        <td class="header" align="center"  rowspan="2">学号</td>
        <td class="header" align="center"  rowspan="2">姓名</td>
        <td class="header" align="center"  rowspan="2">专业班级</td>
        <td class="header" align="center"  rowspan="2">论文题目</td>
        <td class="header" align="center"  colspan="3">反抄袭结果</td>
        <td class="header" align="center"  rowspan="2">校外送审成绩</td>
        <td class="header" align="center"  rowspan="2">指导教师建议成绩</td>
        <td class="header" align="center"  rowspan="2">论文平时成绩</td>
        <td class="header" align="center"  rowspan="2" width="50">操作</td>
      </tr>
      <tr>
        <td class="header" align="center" >检查结果</td>
        <td class="header" align="center" >复检结果</td>
        <td class="header" align="center" >复检时间</td>
      </tr>
        [#list writers as writer]
        <tr>
            <td class="data">${writer_index+1 }</td>
            <td class="data">${writer.std.code}</td>
            <td class="data">${writer.std.name}</td>
            <td class="data">${writer.major.name} ${(writer.squad.name)!}</td>
            <td class="data">
              [#if papers.get(writer)??]
                [#assign paper = papers.get(writer)/]
                [@b.a href="paper!doc?id="+paper.id target="_blank"]<i class="fa-solid fa-file"></i> ${writer.thesisTitle!}[/@]
              [#else]
              ${writer.thesisTitle!}
              [/#if]
            </td>

            <td class="data" align="center" >
            [#list checks.get(writer)?if_exists as c]
              [#if !c.recheck]${c.passed?string('是','否')}[/#if]
            [/#list]
            </td>
            <td class="data" align="center" >
            [#list checks.get(writer)?if_exists as c]
              [#if c.recheck]${c.passed?string('是','否')}[/#if]
            [/#list]
            </td>
            <td class="data" align="center" >
            [#list checks.get(writer)?if_exists as c]
              [#if c.recheck]${c.checkOn?string('yyyy-MM-dd')}[/#if]
            [/#list]
            </td>
          <td class="data" align="center" >
            [#assign blindReviewFinished=true/]
            [#if blindReviews.get(writer)??]
              [#if !blindReviews.get(writer).score??] [#assign blindReviewFinished=false/][/#if]
              ${(blindReviews.get(writer).score)!'-尚未出成绩-'}
            [/#if]
          </td>

          <td class="data" align="center" >
            ${(reviews.get(writer).advisorSelfScore)!'---'}
            [#assign midtermCheckStatus = (midtermChecks.get(writer).status.id)!0/]
            [#if (midtermCheckStatus!=30 && midtermCheckStatus!=100)]
              中期检查尚未通过
            [/#if]
          </td>
          <td class="data" align="center" >
            ${(reviews.get(writer).advisorScore)!'---'}
          </td>
          <td class="data" align="center" >
            [#if blindReviewFinished && (midtermCheckStatus==30 || midtermCheckStatus==100)]
              [@b.a href="!review?writer.id=${writer.id}"]评分[/@]
            [/#if]
          </td>
        </tr>
      [/#list]
    </table>
    <div class="jer_info" style="width: 700px">
      1、参加校外送审的论文平时成绩=指导教师建议成绩*40%+校外送审成绩*60%；<br>
     2、反抄袭检测初检未通过，复检通过的，指导教师建议成绩不得超过70分。<br>
     3、复检未通过的，不参加评阅及答辩，论文成绩作不及格处理。
    </div>
</div>
[@b.foot/]
