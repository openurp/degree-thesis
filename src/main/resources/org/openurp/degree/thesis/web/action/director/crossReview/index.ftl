[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
[@b.messages slash="3"/]
    <table class="list_table" width="100%">
      <tr>
        <td class="title" colspan="11" height="30">教师列表</td>
      </tr>
      <tr>
        <td class="header" >序号</td>
        <td class="header" >教师工号</td>
        <td class="header" >姓名</td>
        <td class="header" >教研室</td>
        <td class="header" >指导专业</td>
        <td class="header" >指导学生数</td>
        <td class="header" >交叉评阅数</td>
        <td class="header" >交叉评阅学生</td>
      </tr>
        [#list advisors as advisor]
        <tr>
            <td class="data">${advisor_index+1 }</td>
            <td class="data">${advisor.code}</td>
            <td class="data">${advisor.name}</td>
            <td class="data">${(advisor.office.name)!}</td>
            <td class="data">[#if teacherMajors.get(advisor)??][#list teacherMajors.get(advisor) as m]${m.name}[#sep],[/#list][/#if]</td>
            <td class="data">[#if adviseStats.get(advisor)??]${adviseStats.get(advisor)?size}[#else]0[/#if]</td>
            <td class="data">[#if reviewStats.get(advisor)??]${reviewStats.get(advisor)?size}[#else]0[/#if]</td>
            <td class="data">
            [#if reviewStats.get(advisor)??]
              [#list reviewStats.get(advisor) as review]${review.writer.std.name}[#sep],&nbsp;[/#list]
            [/#if]
            </td>
        </tr>
      [/#list]
    </table>

<br>
<div>
    [@b.submit value="选择学生，批量设置评阅老师" formId="reviewListForm"/]
    [@b.a href="!randomAssign" onclick="return bg.Go(this,null,'确定为没有评阅教师的学生随机分配教研室的教师?')" class="btn btn-sm btn-outline-primary"]随机分配评阅教师为空的学生[/@]
</div>
   [@b.form action="!batchAssignSetting" name="reviewListForm"]

    <table class="list_table" width="100%">
      <tr>
        <td class="title" colspan="11" height="30">交叉评阅情况</td>
      </tr>
      <tr>
        <td class="header"></td>
        <td class="header">序号</td>
        <td class="header">学号</td>
        <td class="header">姓名</td>
        <td class="header">指导老师</td>
        <td class="header">专业班级</td>
        <td class="header">论文题目</td>
        <td class="header">交叉评阅教师</td>
      </tr>
        [#list reviews as review]
        [#assign writer = review.writer/]
        <tr>
            <td class="data"><input type="checkbox" name="thesisReview.id" value="${review.id}"></td>
            <td class="data">${review_index+1}</td>
            <td class="data">${writer.std.code}</td>
            <td class="data">${writer.std.name}</td>
            <td class="data">${(writer.advisor.name)!}</td>
            <td class="data">${writer.major.name} ${(writer.squad.name)!}</td>
            <td class="data">${writer.thesisTitle!}</td>
            <td class="data">[@b.a href="!batchAssignSetting?thesisReview.id="+review.id]${(review.crossReviewer.name)!'修改'}[/@]</td>
        </tr>
      [/#list]
    </table>
    [/@]
</div>
[@b.foot/]
