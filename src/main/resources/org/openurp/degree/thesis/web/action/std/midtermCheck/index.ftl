[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
 [@b.messages slash="3"/]
  <div class="container-fluid">
    <p style="width: 800px; text-align: center">
      <font color="red">${message}</font>
    </p>
    <form action="${b.url("!edit")}" method="post">
      <table class="list_table" class="list_table">
        <tr>
          <td class="title" colspan="5"  height="30">本科生毕业论文(设计)中期检查表</td>
        </tr>
        <tr >
          <td class="header">材料名称</td>
          <td class="header">填写时间</td>
          <td class="header" colspan="2">操作</td>
          <td class="header" >检查结论</td>
        </tr>
        <tr>
          <td class="data">中期检查表</td>
          <td class="data">
            [#if midtermCheck??]
              ${midtermCheck.submitAt?string("yyyy年MM月dd日HH时mm分ss秒")}
            [#else]
              中期检查未填写
            [/#if]
          </td>
          <td class="data">[@b.a href="!edit"]填写[/@]</td>
          <td class="data">[@b.a href="!info"]查看[/@]</td>
          <td class="data">
          [#if midtermCheck??]
            [#if midtermCheck.status??]
              ${midtermCheck.status}[#if midtermCheck.status?string=='已提交']，待指导教师审核[/#if]
            [/#if]
          [#else]
            未填写
          [/#if]
          </td>
        </tr>
      </table>
      <p style="width: 800px; text-align: center">
        <font color="red">${delay!}</font>
      </p>
    </form>
  </div>
[@b.foot/]
