[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.messages slash="3"/]
[#assign roundName][#if round==1]初选[#elseif round==2]补选[#else]最终[/#if][/#assign]
<table width="100%">
  <tr>
    <td colspan="4" align="right">[@b.a href="!absence?round="+round]未参加${roundName}学生[/@]</td>
  </tr>
</table>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7" height="30">学生已参加${roundName}列表(共有学生${applies?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" height="30">序号</td>
    <td class="header" align="center" height="30">学号</td>
    <td class="header" align="center" height="30">学生姓名</td>
    <td class="header" align="center" height="30">专业班级</td>
    <td class="header" align="center" height="30">${roundName}题目</td>
    <td class="header" align="center" height="30">指导老师</td>
    <td class="header" align="center" height="30" width="80px">操作</td>
  </tr>
  [#list applies as apply]
    <tr>
      <td class="data" align="center" height="30">${apply_index+1}</td>
      <td class="data" align="center" height="30">${apply.writer.std.code}</td>
      <td class="data" align="center" height="30">${apply.writer.std.name}</td>

      <td class="data" align="center" height="30">${apply.writer.major.name}${(apply.writer.squad.name)!}班
      </td>
      [#if round==1]
      <td class="data" align="center" height="30">${apply.first.name}(${apply.first.majorNames})</td>
      <td class="data" align="center" height="30">${apply.first.advisor.teacher.name}</td>
      [#elseif round=2]
      <td class="data" align="center" height="30">${apply.second.name}(${apply.second.majorNames})</td>
      <td class="data" align="center" height="30">${apply.second.advisor.teacher.name}</td>
      [/#if]
      [#if apply.last??]
        <td class="data" align="center" height="30">
          [#assign cancelable=false/]
          [#if round == 1 && apply.last.id==apply.first.id]
          [#assign cancelable=true/]
          [/#if]
          [#if round ==2 && apply.second?? && apply.last.id==apply.second.id]
          [#assign cancelable=true/]
          [/#if]
          [#if cancelable]
          [@b.a href="!cancel?subjectApply.id="+apply.id onclick="if(confirm('该操作将移除该学生的最终题目，确定执行吗?')){return bg.Go(this,null)}else{return false;}"]移除[/@]
          [#else]
          --
          [/#if]
        </td>
      [#else]
        <td class="data" align="center" height="30">
        [#if round == 1]
         [@b.a href="!choose?round="+round+"&subjectApply.id="+apply.id+"&subjectId="+apply.first.id onclick="if(confirm('确定选择该题目?')){return bg.Go(this,null)}else{return false;}"]通过该题目[/@]
        [#else]
         [@b.a href="!choose?round="+round+"&subjectApply.id="+apply.id+"&subjectId="+apply.second.id onclick="if(confirm('确定选择该题目?')){return bg.Go(this,null)}else{return false;}"]通过该题目[/@]
        [/#if]
        </td>
      [/#if]
    </tr>
  [/#list]
</table>
</div>
[@b.foot/]
