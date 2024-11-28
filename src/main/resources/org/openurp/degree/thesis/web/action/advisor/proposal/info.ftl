[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script src="${b.base}/static/scripts/jer_normal.js?ver=1"></script>
<div class="container-fluid">
  <table class="list_table" width="100%">
    <tr>
      <td class="title" colspan="2">${writer.std.name}的开题报告</td>
    </tr>
    <tr>
      <td class="title" style="width:120px">选题名称：</td>
      <td class="data" style="text-align:left">${writer.theisTitle!}</td>
    </tr>
    <tr>
      <td class="title" >选题研究的目的和意义：</td>
      <td class="data" style="text-align:left">
       <pre>${proposal.meanings!}</pre>
      </td>
    </tr>
    <tr>
      <td class="title" >选题的研究现状：</td>
      <td class="data" style="text-align:left"><pre>${proposal.conditions!}</pre></td>
    </tr>
    <tr>
      <td class="title" >论文提纲：</td>
      <td class="data" style="text-align:left"><pre>${proposal.outline!}</pre></td>
    </tr>
    <tr>
      <td class="title" >参考文献：</td>
      <td class="data" style="text-align:left"><pre>${proposal.references!}</pre></td>
    </tr>
    <tr>
      <td class="title" >选题研究方法：</td>
      <td class="data" style="text-align:left"><pre>${proposal.methods!}</pre></td>
    </tr>
    [#if !Parameters['audit']??]
    <tr>
      <td class="title" >审查状态：</td>
      <td class="data" style="text-align:left">${proposal.status}</td>
    </tr>
    <tr>
      <td class="title" >审查意见：</td>
      <td class="data" style="text-align:left">${proposal.advisorOpinion!}</td>
    </tr>
    <tr>
      <td class="title" >操作：</td>
    </tr>
      <td class="data" >&nbsp;&nbsp; <input type="button"   onclick="history.back()" value="返回"></td>
    [/#if]
  </table>
[#if Parameters['audit']??]
[@b.form id="form" name="form" action="!audit"]
  <table class="list_table" width="100%">
    <tr>
      <td class="title" style="width:120px">审查结论:</td>
      <td class="data" style="text-align:left">
        <input id="zt1" name="status" type="radio" value="1" [#if passed]checked="checked"[/#if]/><label for="zt1">审查通过</label> &nbsp;&nbsp;
        <input id="zt2"  name="status" type="radio" value="0" [#if !passed]checked="checked"[/#if]/><label for="zt2">审查未通过</label></td>
    </tr>
    <tr>
      <td class="title">审查意见：</td>
      <td class="data"style="text-align:left">
        <div class="jer_info_line">字数不得少于50个字,已输入<span id="word_count">0</span>字</div>
        <textarea id="advisorOpinion" name="advisorOpinion" onkeyup="toggleSubmit(this);keydown_word_count(this)" rows="3" cols="120" >${proposal.advisorOpinion!}</textarea>
      </td>
    </tr>
    <tr>
      <td class="title" >操作：
        <input  name="proposal.id" type="hidden" value="${proposal.id}">
      </td>
      <td  class="data" style="text-align:left">&nbsp;&nbsp;
        [@b.submit id="btn_sub" value="提交" onsubmit="checkOpinion()" /]&nbsp;&nbsp;&nbsp;&nbsp;<input type="button" onclick="history.back()" value="返回"></td>
    </tr>
  </table>
[/@]
</div>
<script>
jQuery(document).ready(function($) {
  //默认显示字数
  keydown_word_count($('#advisorOpinion'));
});
function checkOpinion(){
  var enough = form['advisorOpinion'].value.length>50;
  if(!enough){ alert("审查意见字数为"+form['advisorOpinion'].value.length+"字，应多于50字");return false;}
  else return true;
}
</script>
[/#if]
[@b.foot/]
