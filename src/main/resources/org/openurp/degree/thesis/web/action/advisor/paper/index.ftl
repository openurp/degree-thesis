[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
      <table class="list_table" width="100%">
        <tr>
          <td class="title" colspan="9">论文上传情况</td>
        </tr>
        <tr>
          <td class="header">序号</td>
          <td class="header" >学号</td>
          <td class="header">姓名</td>
          <td class="header">专业班级</td>
          <td class="header">题目名称</td>
          <td class="header">提交时间</td>
          <td class="header">状态</td>
          <td class="header">操作</td>
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
                [@b.a href="!doc?writer.id="+writer.id target="_blank"]<i class="fa-solid fa-file"></i> ${writer.thesisTitle!}[/@]
              [#else]
              ${writer.thesisTitle!}
              [/#if]
            </td>
            [#if papers.get(writer)??]
              [#assign paper = papers.get(writer)/]
              <td class="data">${paper.submitAt?string('MM-dd HH:mm')}</td>
              <td class="data" style="text-align:center;">[#if paper.advisorPassed??]${paper.advisorPassed?string("通过","不通过")}[#else]未审核[/#if]&nbsp;[@b.a href="!doc?id="+paper.id target="_blank"]<i class="fa-solid fa-download"></i>下载[/@]</td>
              <td class="data">[@b.a href="!auditForm?paper.id="+paper.id]审核[/@]</td>
            [#else]
              <td class="data">--</td>
              <td class="data">未提交</td>
              <td class="data">--</td>
            [/#if]
          </tr>
        [/#list]
      </table>
</div>
[@b.foot/]
