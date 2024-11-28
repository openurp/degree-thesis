[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<link href="${b.base}/static/stylesheets/tinybox.css"  rel="stylesheet" type="text/css">
<script type="text/javascript"  src="${b.base}/static/scripts/util.js"></script>
<script type="text/javascript"  src="${b.base}/static/scripts/tinybox.js"></script>
<script type="text/javascript">
  function checkSubject(form){
      $("#button").attr("disabled", "disabled");
      $("#button").attr("value", "正在提交...");
      var error = false;
      $("span").html("");
      if ($.trim($("#researchField").val()).length == 0) {
        $("#researchFieldError").html("研究方向不能为空");
        error = true;
      }
      if ($.trim($("#mc").val()).length == 0) {
        $("#mcError").html("题目名称不能为空");
        error = true;
      }
      if ($("#mc").val().length > 150) {
        $("#mcError").html("题目名称不能超过150字");
        error = true;
      }
      var i = 0;
      $("input[name='majorId']:checked").each(function() {i++;});
      if (i == 0) {
        error = true;
        $("#zyIdError").html("面向专业不能为空");
      }

      if ($("#nr").val().length > 500) {
        $("#nrError").html("主要内容不能超过500字");
        error = true;
      }
      if ($("#xytj").val().length > 500) {
        $("#xytjError").html("现有条件不能超过500字");
        error = true;
      }
      if ($("#dxsyq").val().length > 500) {
        $("#dxsyqError").html("对学生要求不能超过500字");
        error = true;
      }
      if (!error) {
        $.ajax({
         type : "post",
          url : "${b.url("!checkName")}.json",
          data : {"subject_id":"${subject.id!0}","subject.name":$('#mc').val(),"season_id":"${subject.season.id}"},
          async : false,
          success : function(result){
            if (!result) {
              $("#mcError").html("该题目已经存在");
              $("#button").attr("disabled",false);
              $("#button").attr("value","修改");
              error=true;
            }
          }
         });
      } else {
        $("#button").attr("disabled",false);
        $("#button").attr("value", "修改");
      }
      return !error;
   }
</script>

<div class="container-fluid">
    [@b.form name="subjectForm" action="!save" onsubmit="checkSubject"]
      [#if subject.persisted]
      <input type="hidden"  id="subject_id" name="subject.id" value="${subject.id}"/>
      [/#if]
      <table class="list_table" width="100%">
        <tr>
          <td class="title" height="30" colspan="2">修改毕业论文(设计)题目</td>
        </tr>
        <tr>
          <td class="title" height="30" width="130">题目名称(<font color="red">必填</font>)：
          </td>
          <td class="data" height="30">&nbsp;&nbsp;
            <input type="text" id="mc" name="subject.name" size="50" value="${subject.name!}" /><span class="text-muted">（150字以内）</span>
            <span id="mcError" style="color:red"></span>
          </td>
        </tr>
        <tr>
          <td class="title" height="30">研究方向(<font color="red">必填</font>)：</td>
          <td class="data" height="30">&nbsp;&nbsp;
            <input type="text" id="researchField" name="subject.researchField" size="50" value="${subject.researchField!}" /><span class="text-muted">（150字以内）</span>
            <span id="researchFieldError" style="color:red"></span>
          </td>
        </tr>
        <tr>
          <td class="title" height="30">面向院系(<font color="red">必填</font>)：</td>
          <td class="data" height="30">
            <select name="subject.depart.id" >
              [#list departs as depart]
                <option value="${depart.id}" [#if depart.id=(subject.depart.id)!0]selected="selected"[/#if]>${depart.name}</option>
              [/#list]
            </select>
          </td>
        </tr>
        <tr>
          <td class="title" height="30">面向专业(<font color="red">必填</font>)：</td>
          <td class="data" height="30">
            [#assign majorSelectType="radio"/]
            [#list majors as zy][#if zy.name?contains("法学")][#assign majorSelectType="checkbox"/][/#if][/#list]
            [#list majors as zy]
              &nbsp;&nbsp;<input type="${majorSelectType}" id="${zy.id}" name="majorId" value="${zy.id}" /><label for="${zy.id}">${zy.name}</label>
              [#if zy_index % 4 == 3]<br/>[/#if]
            [/#list]
            <br> <span id="zyIdError" style="color:red"></span>
              <script>
              [#list subject.majors as zy]
                $("#${zy.id}").attr("checked", true);
              [/#list]
              </script>
          </td>
        </tr>
        <tr>
          <td class="title" height="30" colspan="2">以下为选填内容</td>
        </tr>
        <tr>
          <td class="title" height="30" width="100">主要内容（包括课题的任务与主要内容、综合能力训练、工作量、题目难度和广度等）：</td>
          <td class="data" height="30">&nbsp;&nbsp;<span class="text-muted">（500字以内）</span><br>
            <span class="text-muted">&nbsp;&nbsp;主要内容，应把课题的任务与主要内容、综合能力训练、工作量、题目难度和广度等方面都进行阐述。</span><br>
            <textarea rows="5" cols="100" name="subject.contents" id="nr">${subject.contents!}</textarea><br>
            <span id="nrError"></span>
          </td>
        </tr>
        <tr>
          <td class="title" height="30" width="100">现有条件（包括资料的准备.经费落实.实验条件.场地条件等）：</td>
          <td class="data" height="30">&nbsp;&nbsp;<span class="text-muted">（500字以内）</span><br>
            <span class="text-muted">&nbsp;&nbsp;现有条件：包括资料的准备.经费落实.实验条件.场地条件等方面都进行阐述。</span><br>
            <textarea rows="5" cols="100" name="subject.conditions" id="xytj">${subject.conditions!}</textarea><br>
            <span id="xytjError"></span>
          </td>
        </tr>
        <tr>
          <td class="title" height="30" width="100">对学生的要求：</td>
          <td class="data" height="30">&nbsp;&nbsp;<span class="text-muted">（150字以内）</span><br>
            <textarea rows="3" cols="100" name="subject.requirements" id="dxsyq">${subject.requirements!}</textarea><br>
            <span id="dxsyqError"></span>
          </td>
        </tr>

        <tr>
          <td class="title" height="30">操作：</td>
          <td class="data" height="30">&nbsp;&nbsp;
           [@b.submit value="保存"/]
           &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" onclick="history.back()" value="返回"></td>
        </tr>
      </table>
    [/@]
  </div>
[@b.foot/]
