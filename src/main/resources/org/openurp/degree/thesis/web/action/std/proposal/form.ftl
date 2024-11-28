[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<link href="${b.base}/static/stylesheets/tinybox.css"  rel="stylesheet" type="text/css">
<script type="text/javascript"  src="${b.base}/static/scripts/util.js"></script>
<script type="text/javascript"  src="${b.base}/static/scripts/tinybox.js"></script>
  <div align="center">
    [@b.form id="form" name="form" action="!submit"]
      <input type="hidden" name="justSave" value="1"/>
      <table class="list_table" width="90%">
        <tr>
          <td class="title" colspan="2">撰写开题报告</td>
        </tr>
        <tr>
          <td class="title"  width="100">选题名称：</td>
          <td >&nbsp;&nbsp;${(writer.thesisTitle)!}</td>
        </tr>
        <tr>
          <td class="title"  width="100">选题研究的目的和意义：</td>
          <td >&nbsp;&nbsp;<br>
            <textarea rows="15" cols="120" name="proposal.meanings" id="mdyy">${proposal.meanings! }</textarea><br>
            <span id="mdyyError"></span>
          </td>
        </tr>
        <tr>
          <td class="title"  width="100">选题的研究现状：</td>
          <td >&nbsp;&nbsp;<br>
            <textarea rows="15" cols="120" name="proposal.conditions" id="xz">${proposal.conditions! }</textarea><br>
            <span id="xzError"></span>
          </td>
        </tr>
        <tr>
          <td class="title"  width="100">论文提纲：</td>
          <td >&nbsp;&nbsp;<br> <textarea
              rows="15" cols="120" name="proposal.outline" id="tg">${proposal.outline!}</textarea><br> <span
            id="tgError"></span>
          </td>
        </tr>
        <tr>
          <td class="title"  width="100">参考文献：</td>
          <td >&nbsp;&nbsp;<br> <textarea
              rows="15" cols="120" name="proposal.references" id="ckwx">${proposal.references!}</textarea><br> <span
            id="ckwxError"></span>
          </td>
        </tr>
        <tr>
          <td class="title"  width="100">选题研究方法：</td>
          <td >&nbsp;&nbsp;<br> <textarea
              rows="5" cols="120" name="proposal.methods" id="yjff">${proposal.methods! }</textarea><br> <span
            id="yjffError"></span>
          </td>
        </tr>
        <tr>
          <td class="title" >操作：</td>
          <td>
            &nbsp;&nbsp;<input type="hidden" name="id" value="${proposal.id!}"/>
            <input type="button" id="button" value="保存" onclick="bg.form.submit(this.form);return false;">
            &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" value="提交" onclick="form['justSave'].value='0';bg.form.submit(this.form);return false;" id="sub"/>&nbsp;&nbsp;&nbsp;&nbsp;
            <input type="button" onclick="history.back()" value="返回"></td>
        </tr>
      </table>
      <p style="width:800px;text-align:left"><font color="red">
说明：点击“保存”按钮把填写的内容保存在系统中。
点击“提交”按钮会把开题报告提交给指导老师审查。</font>
</p>
[/@]
  </div>
[@b.foot/]
