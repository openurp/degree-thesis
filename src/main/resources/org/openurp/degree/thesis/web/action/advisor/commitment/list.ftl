[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
      <table class="list_table" width="100%" id="tableId">
        <thead>
          <tr>
            <td class="title" colspan="10" >任务书确认情况</td>
          </tr>
          <tr>
            <td class="header" align="center"   >序号</td>
            <td class="header" align="center" >学号</td>
            <td class="header" align="center" >学生姓名</td>
            <td class="header" align="center" >专业班级</td>
            <td class="header" align="center" >题目名称</td>
            <td class="header" align="center"  width="60">状态</td>
            <td class="header" align="center"  width="100px">延期次数</td>
            <td class="header" align="center"  width="60px">操作</td>
          </tr>
        </thead>
        <tbody>
          [#list writers as writer]
            <tr>
              <td class="data" align="center" >${writer_index+1}</td>
              <td class="data" align="center" >${writer.std.code}</td>
              <td class="data" align="center">${writer.std.name}</td>
              <td class="data" align="center" >${writer.major.name} ${(writer.squad.name)!}</td>
              <td class="data" align="center" >${writer.thesisTitle!}</td>
              [#assign deadline = writer.getOrCreateDeadline(stage)/]
                [#if (commitments.get(writer).confirmed)!false]
                  <td class="data" align="center" >已确认</td>
                  <td class="data" align="center" >
                   [#if deadline.endAt??]延期${deadline.delayCount}次<br>至${deadline.endAt?string("yy-MM-dd")}
                   [#else]
                     ${deadline.delayCount}
                   [/#if]
                  </td>
                  <td class="data" align="center"><a href="${b.url("!info?writer.id="+writer.id)}" target="_blank">查看</a></td>
                [#else]
                  <td class="data" align="center">未确认</td>
                  <td class="data" align="center" >
                   [#if deadline.endAt??]延期${deadline.delayCount}次<br>至${deadline.endAt?string("yy-MM-dd")}
                   [#else]
                     ${deadline.delayCount}
                   [/#if]
                  </td>
                  <td class="data" align="center" >
                    [#if commitments.get(writer)??]<a href="${b.url("!info?writer.id="+writer.id)}" target="_blank">查看</a>
                    [#else]--[/#if]
                  </td>
                [/#if]
            </tr>
          [/#list]
        </tbody>
      </table>
  </div>
[@b.foot/]
