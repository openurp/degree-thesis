[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<style>
pre{
    white-space:pre-wrap;
}
</style>
<div align="center">
  <table class="list_table" width="90%">
    <tr>
      <td class="title" colspan="2">开题报告</td>
    </tr>
    <tr>
      <td class="title" style="width:130px">选题名称：</td>
      <td class="data" align="left">${(writer.thesisTitle)!}</td>
    </tr>
    <tr>
      <td class="title" >选题研究的目的和意义：</td>
      <td class="data" align="left">
       <pre>${proposal.meanings!}</pre>
      </td>
    </tr>
    <tr>
      <td class="title" >选题的研究现状：</td>
      <td class="data" align="left"><pre>${proposal.conditions!}</pre></td>
    </tr>
    <tr>
      <td class="title" >论文提纲：</td>
      <td class="data" align="left"><pre>${proposal.outline!}</pre></td>
    </tr>
    <tr>
      <td class="title" >参考文献：</td>
      <td class="data" align="left"><pre>${proposal.references!}</pre></td>
    </tr>
    <tr>
      <td class="title" >选题研究方法：</td>
      <td class="data" align="left"><pre>${proposal.methods!}</pre></td>
    </tr>
    <tr>
      <td class="title" >审查状态：</td>
      <td class="data" align="left">${proposal.status}</td>
    </tr>
    <tr>
      <td class="title" >审查意见：</td>
      <td class="data" align="left">${proposal.advisorOpinion!}</td>
    </tr>
    <tr>
      <td class="title" height="30">操作：</td>
      <td class="data" height="30">&nbsp;&nbsp; <input type="button"
        onclick="history.back()" value="返回"></td>
    </tr>
  </table>
</div>
[@b.foot/]
