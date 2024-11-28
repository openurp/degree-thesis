[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.toolbar title="录入答辩组成绩"]
  bar.addBack();
[/@]
[#list myGroups as group]
[@b.form action="!saveScore" name="defenseScoreForm"+group.idx theme="list"]
  <input type="hidden" name="group.id" value="${group.id}"/>
     <table cellspacing="0" cellpadding="0" border="1" style="width:100%" class="list_table">
        <thead>
          <tr>
            <td class="title" colspan="10" >第${group.idx}组学生答辩评分</td>
          </tr>
          <tr>
            <td class="header"  width="50">序号</td>
            <td class="header"  width="100">学号</td>
            <td class="header"  width="75">姓名</td>
            <td class="header"  width="100">专业班级</td>
            <td class="header" >题目名称</td>
            <td class="header"  width="75">指导教师</td>
            <td class="header"  width="100">答辩评分</td>
          </tr>
        </thead>
        <tbody>
          [#list group.orderedWriters as writer]
          <tr>
              <td class="data" >${writer_index+1}</td>
              <td class="data" >${writer.std.code}</td>
              <td class="data" >${writer.std.name}</td>
              <td class="data" >${(writer.std.major.name)!} ${(writer.std.squad.name)!}</td>
              <td class="data" >${writer.thesisTitle!}</td>
              <td class="data" >${(writer.advisor.name)!}</td>
              <td class="data"  align="center">
                [#assign review=reviews.get(writer)/]
                <input type="hidden" name="review.id" value="${review.id}"/>
                <input type="text" class="fs" name="defense_score_${review.id}" placeholder="${writer.std.name}" size="5" value="${review.defenseScore!}"/>
              </td>
          </tr>
          [/#list]
        </tbody>
    </table>
    <p style="text-align:center;padding-top:10px">
      [@b.submit value="提交"/]
    </p>
  [/@]
[/#list]
</div>
  <p>
    <font color="red">注:得分以百分制记。</font>
  </p>
[@b.foot/]
