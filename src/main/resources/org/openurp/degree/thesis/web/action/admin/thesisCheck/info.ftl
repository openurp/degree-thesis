[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script src="${b.base}/static/scripts/jer_normal.js"></script>
<div align="center">
  <table class="list_table" width="90%">
    <tr>
      <td class="title" height="30" colspan="2">${writer.std.name}的开题报告</td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">选题名称：</td>
      <td class="data" height="30" align="left">${writer.thesisTitle!}</td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">选题研究的目的和意义：</td>
      <td class="data" height="30" align="left">
       <pre>${proposal.meanings!}</pre>
      </td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">选题的研究现状：</td>
      <td class="data" height="30" align="left"><pre>${proposal.conditions!}</pre></td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">论文提纲：</td>
      <td class="data" height="30" align="left"><pre>${proposal.outline!}</pre></td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">参考文献：</td>
      <td class="data" height="30" align="left"><pre>${proposal.references!}</pre></td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">选题研究方法：</td>
      <td class="data" height="30" align="left"><pre>${proposal.methods!}</pre></td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">审查状态：</td>
      <td class="data" height="30" align="left">${proposal.status}</td>
    </tr>
    <tr>
      <td class="title" height="30" width="100">审查意见：</td>
      <td class="data" height="30" align="left">${proposal.advisorOpinion!}</td>
    </tr>
    <tr>
      <td class="title" height="30">操作：</td>
      <td class="data" height="30">&nbsp;&nbsp; <input type="button"   onclick="history.back()" value="返回"></td>
    </tr>
  </table>
[@b.foot/]
