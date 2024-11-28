[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div align="center">[@b.messages slash="3"/]</div>
  <div align="center">
    <p style="width: 90%; text-align: center">
      <font color="red"> 说明：学生需按照工作计划撰写并提交开题报告。“未通过审查”状态下可继续撰写；
        “审查中”及“审查通过”状态下不能撰写。审查通过后如仍需修改，请联系指导教师驳回。 </font>
    </p>
    <table class="list_table" width="90%">
      <tr>
        <td class="title" colspan="7" height="30">开题报告</td>
      </tr>
      <tr>
        <td class="header" align="center" height="30" width="150">材料名称</td>
        <td class="header" align="center" height="30" width="150">撰写时间</td>
        <td class="header" align="center" height="30" colspan="2" width="150">操作</td>
        <td class="header" align="center" height="30" width="150">审查状态</td>
      </tr>
      <tr>
        <td class="data" align="center" height="30">开题报告</td>
        [#if proposal??]
          <td class="data" align="center" height="30">${proposal.submitAt?string('yyyy-MM-dd HH:mm')}</td>
        [#else]
          <td class="data" align="center" height="30">开题报告未填写</td>
        [/#if]
        <td class="data" align="center" height="30">[@b.a href="!edit"]撰写[/@]</td>
        <td class="data" align="center" height="30">[#if proposal??][@b.a href="!info"]查看[/@][/#if]</td>
        <td class="data" align="center" height="30">[#if proposal??]${proposal.status}[/#if]</td>
      </tr>
    </table>
    <p style="width: 800px; text-align: center">
      <font color="red">${message}</font>
    </p>
    <p style="width: 800px; text-align: center">
      <font color="red">${delay!}</font>
    </p>
  </div>
[@b.foot/]
