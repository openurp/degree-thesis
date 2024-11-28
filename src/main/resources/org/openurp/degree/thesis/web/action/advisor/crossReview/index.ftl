[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
    <font color="red">指导教师完成评分后，才可进行交叉评分。</font>
[@b.messages slash="3"/]
[#if offices?size >0 ]
  <table cellspacing="0" cellpadding="0" border="0" width="100%">
    <tr>
      <td align="right">
      [@b.a href="/director/cross-review"]教研室入口（[#list offices as office]${office.name}[#sep],[/#list]）[/@]&nbsp;&nbsp;&nbsp;
      </td>
    </tr>
  </table>
[/#if]
    <table class="list_table" width="100%">
      <tr>
        <td class="title" colspan="13" height="30">本科生毕业论文(设计) 交叉评阅</td>
      </tr>
      <tr>
        <td class="header" rowspan="2">序号</td>
        <td class="header" rowspan="2">学号</td>
        <td class="header" rowspan="2">姓名</td>
        <td class="header" rowspan="2">专业班级</td>
        <td class="header" rowspan="2">论文题目</td>
        <td class="header" colspan="3">反抄袭结果</td>
        <td class="header" width="50" rowspan="2">校外送审成绩</td>
        <td class="header" rowspan="2">指导教师</td>
        <td class="header" rowspan="2">指导教师建议成绩</td>
        <td class="header" rowspan="2">交叉评阅成绩</td>
        <td class="header" width="100" colspan="2" rowspan="2">操作</td>
      </tr>
      <tr>
        <td class="header">检查结果</td>
        <td class="header">复检结果</td>
        <td class="header">复检时间</td>
      </tr>
        [#list reviews as review]
        [#assign writer = review.writer/]
        <tr>
            <td class="data">${review_index+1 }</td>
            <td class="data">${writer.std.code}</td>
            <td class="data">${writer.std.name}</td>
            <td class="data">${writer.major.name} ${(writer.squad.name)!}</td>
            <td class="data">
              [@b.a href="paper!doc?writer.id="+writer.id target="_blank"]<i class="fa-solid fa-file"></i> ${writer.thesisTitle!}[/@]
            </td>
            <td class="data">
            [#list checks.get(writer)?if_exists as c]
              [#if !c.recheck]${c.passed?string('是','否')}[/#if]
            [/#list]
            </td>
            <td class="data">
            [#list checks.get(writer)?if_exists as c]
              [#if c.recheck]${c.passed?string('是','否')}[/#if]
            [/#list]
            </td>
            <td class="data">
            [#list checks.get(writer)?if_exists as c]
              [#if c.recheck]${c.checkOn?string('yyyy-MM-dd')}[/#if]
            [/#list]
            </td>
          <td class="data">---</td>
          <td class="data">${(review.writer.advisor.name)!}</td>
          <td class="data">
              ${review.advisorScore!'---'}
          </td>
          <td class="data">
              ${review.crossReviewScore!'---'}
          </td>

          <td class="data">
            [#if review.advisorScore??]
              [@b.a href="!reviewSetting?thesisReview.id=${review.id}"]评分[/@]
            [/#if]
          </td>
        </tr>
      [/#list]
    </table>
</div>
[@b.foot/]
