[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
  <form name="form" id="form" action="checkAll.htm" method="post">
    <div><p style="margin:0px">最近一届的学生列表  总计:${writers?size}个</p></div>
    <table class="list_table" width="100%">
      <tr>
        <td class="header">序号</td>
        <td class="header">学号</td>
        <td class="header">姓名</td>
        <td class="header">专业班级</td>
        <td class="header">题目名称</td>
        <td class="header">延期次数</td>
        <td class="header">开题报告状态</td>
        <td class="header" colspan="2">操作</td>
      </tr>
      [#list writers as writer]
        <tr>
          <td class="data">${writer_index+1 }</td>
          <td class="data">${writer.std.code}</td>
          <td class="data">${writer.std.name}</td>
          <td class="data">${writer.major.name} ${(writer.squad.name)!}</td>
          <td class="data">${writer.thesisTitle!}</td>
          <td class="data">${(writer.getOrCreateDeadline(stage).delayCount)!0}</td>
          [#if proposals.get(writer)?? && proposals.get(writer).status!='未提交审查']
            [#assign proposal= proposals.get(writer)/]
            <td class="data">${proposal.status} <div style="display:none">p.id:${proposal.id},${proposal.status.id}</div></td>
            <td class="data">[@b.a href="!info?writer.id="+writer.id]查看[/@]</td>
            <td class="data">[@b.a href="!info?audit=1&writer.id="+writer.id]审查[/@]</td>
          [#else]
            <td class="data">未撰写</td>
            <td class="data">---</td>
            <td class="data">---</td>
          [/#if]
        </tr>
      [/#list]
    </table>
  </form>

  <div style="margin-top:20px"><p style="margin:0px">往届的学生列表  总计:${historyWriters?size}个</p></div>
  <table class="list_table" width="100%">
    <tr>
      <td class="header">序号</td>
      <td class="header">学号</td>
      <td class="header">姓名</td>
      <td class="header">专业班级</td>
      <td class="header">题目名称</td>
      <td class="header">延期次数</td>
      <td class="header">操作</td>
    </tr>
    [#list historyWriters as writer]
    <tr>
      <td class="data">${writer_index+1 }</td>
      <td class="data">${writer.std.code}</td>
      <td class="data">${writer.std.name}</td>
      <td class="data">${writer.major.name} ${(writer.squad.name)!}</td>
      <td class="data">${writer.thesisTitle!}</td>
      <td class="data">${(writer.getOrCreateDeadline(stage).delayCount)!0}</td>
      <td class="data">[@b.a href="!info?writer.id="+writer.id]查看[/@]</td>
    </tr>
  [/#list]
  </table>
</div>
[@b.foot/]
