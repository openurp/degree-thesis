[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  [@b.messages slash="3"/]
  <div class="container-fluid">
    <br>
    <table class="list_table" class="list_table">
      <tr>
        <td class="title" colspan="10" height="30" align="center">中期检查&nbsp;&nbsp;
        </td>
      </tr>
      <tr>
        <td class="header" >序号</td>
        <td class="header" >学号</td>
        <td class="header" >姓名</td>
        <td class="header" >专业班级</td>
        <td class="header" >题目名称</td>
        <td class="header" >延期次数</td>
        <td class="header" >状态</td>
        <td class="header"  colspan="2" width="100px">操作</td>
      </tr>
      [#list writers as writer]
        <tr>
          <td class="data" >${writer_index+1}</td>
          <td class="data" >${writer.std.code}</td>
          <td class="data" >${writer.std.name}</a></td>
          <td class="data" >${writer.major.name} ${(writer.squad.name)!}</td>
          <td class="data" >${writer.thesisTitle!}</td>
          <td class="data" >${(writer.getOrCreateDeadline(stage).delayCount)!0}</td>
          [#if midtermChecks.get(writer)??]
             [#assign midtermCheck=midtermChecks.get(writer)/]
             <td class="data">
             [#if midtermCheck.status??]
             ${midtermCheck.status}
             [#else]
               [#if midtermCheck.details?size ==0]
               待检查
               [/#if]
             [/#if]
            </td>
            <td class="data"><a href="${b.url('!check?writer.id='+writer.id)}">检查</a></td>
            <td class="data"><a href="${b.url('!info?writer.id='+writer.id)}">查看</a></td>
          [#else]
            <td class="data" >未填写</td>
            <td class="data" >---</td>
            <td class="data" >---</td>
          [/#if]
        </tr>
      [/#list]
    </table>
  </div>
[@b.foot/]
