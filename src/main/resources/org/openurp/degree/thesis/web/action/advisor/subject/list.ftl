[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
[@b.messages slash="3"/]
<table width="100%">
  <tr>
    <td>
      [#list departPlans as departPlan]
        [#assign subjectTime = departPlan.getStageTime(SubjectStage)/]
        ${departPlan.department.name} 开题日期 <span [#if !openedDeparts?seq_contains(departPlan.department)]class="text-muted"[/#if]>${subjectTime.beginOn}~${subjectTime.endAt?string("yyyy-MM-dd HH:mm")}</span>
      [#sep],[/#list]
    </td>
    [#if openedDeparts?size>0]
    <td colspan="4" align="right">[@b.a href="!editNew"]增加题目[/@]</td>
    <td colspan="5" align="right">[@b.a href="!history"]从历史题目库中添加[/@]</td>
    [/#if]
  </tr>
</table>

<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="8">我的毕业论文(设计)题目列表  总计:${subjects?size}个</td>
  </tr>
  <tr>
    <td class="header" align="center">序号</td>
    <td class="header" align="center">题目名称</td>
    <td class="header" align="center">研究方向</td>
    <td class="header" align="center">面向院系</td>
    <td class="header" align="center">面向专业</td>
    <td class="header" align="center">状态</td>
    <td class="header" colspan="2" align="center">操作</td>
  </tr>

  [#list subjects as subject]
    <tr style="text-align:center;">
      <td class="data">${subject_index+1}</td>
      <td class="data">[@b.a href='!info?id='+subject.id]${subject.name}[/@]</td>
      <td class="data">${subject.researchField!"--"}</td>
      <td class="data">${subject.depart.name}</td>
      <td class="data">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
      <td class="data">${subject.status}</td>
      <td class="data" width="50">
        [#if openedDeparts?seq_contains(subject.depart)]
          [@b.a href="!edit?id="+subject.id]修改[/@]
        [/#if]
      </td>
      <td class="data" align="center" width="50">
        [#if openedDeparts?seq_contains(subject.depart)]
         [@b.a href="!remove?id=" + subject.id onclick="if(confirm('该操作将删除该课题的所有信息，确定执行吗?')){return bg.Go(this,null)}else{return false;}"]删除[/@]
        [/#if]
      </td>
    </tr>
  [/#list]
</table>
</div>
[@b.foot/]
