[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<link href="${b.base}/static/stylesheets/tinybox.css"  rel="stylesheet" type="text/css">
<script type="text/javascript"  src="${b.base}/static/scripts/util.js"></script>
<script type="text/javascript"  src="${b.base}/static/scripts/tinybox.js"></script>
<script type="text/javascript">
  function selectAll() {
    var elements = document.getElementsByName("lstmId");
    for (var i = 0; i < elements.length; i++) {
      elements[i].checked = !elements[i].checked;
    }
  }
</script>
<script type="text/javascript">
  $(document).ready(function() {
    $("body").ajaxError(function() {
      var content = "<p>服务器错误，请联系管理员！消息提示将在5秒后关闭！</p>";
      TINY.box.show(content, 0, 800, 0, 0, 5);
    });
    $("#button").click(function() {
      $("#button").attr("disabled", "disabled");
      $("#button").attr("value", "正在提交...");
      var error = false;
      var i = 0;
      var j = 0;
      $("input[name='majorId']:checked").each(function() {
        i++;
      });
      $("input[type='checkbox'][name='lstmId']:checked").each(function() {
        j++;
      });
      if (i == 0) {
        error = true;
        $("#zyIdError").html("请选择课题面向专业");
        $("#button").attr("disabled", false);
        $("#button").attr("value", "添加选中项");
      } else {
        $("#zyIdError").html("");
      }
      if (j == 0) {
        error = true;
        $("#tmError").html("请选择需要添加的题目");
        $("#button").attr("disabled", false);
        $("#button").attr("value", "添加选中项");
      } else {
        $("#tmError").html("");
      }
      if (!error) {
        bg.form.submit(document.getElementById("form"));
      }
    });
  });
</script>

<div class="container-fluid">
  <form name="form" id="form" action="${b.url('!addFromHistory')}" method="post">
    <input type="hidden" name="subject.advisor.id" value="${advisor.id}"/>
    <input type="hidden" name="subject.depart.id" value="${advisor.teacher.department.id}"/>
    <table cellspacing="1" cellpadding="0" border="1" width="100%">
      <tr>
        <td class="title" colspan="5" height="30">${advisor.teacher.name}指导老师的毕业论文(设计)可以添加的历史题目列表总计:${lstmList?size}个</td>
      </tr>
      <tr>
        <td class="header" align="center" height="30">序号</td>
        <td class="header" align="center" height="30">题目名称</td>
        <td class="header" align="center" height="30">毕业界别</td>
        [#if lstmList?size >0 ]
          <td class="header" align="center" height="30" width="40"><a
            href="javascript:void(0)" onclick="selectAll();">反选</a></td>
        [/#if]
      </tr>

      [#list  lstmList as lstm]
        <tr>
          <td class="data" align="center" height="30">${lstm_index+1}</td>
          <td class="data" align="center" height="30">${lstm.name}</td>
          <td class="data" align="center" height="30">${lstm.season.name}</td>
          <td class="data" align="center" height="30"><input type="checkbox" name="lstmId" value="${lstm.id}"></td>
        </tr>
      [/#list]

      <tr>
        <td class="data" colspan="5" height="30" align="right"><span
          id="tmError"></span> [#if lstmList?size>0]
            <input type="button" id="button" value="添加选中项">
          [/#if] &nbsp;&nbsp;<input type="button" onclick=history.back();
          value="返回"></td>
      </tr>
    </table>
    <table cellspacing="1" cellpadding="0" border="1" width="100%">
      <tr>
        <td class="title" height="30">面向专业：</td>
      </tr>
      <tr>
        <td class="data" height="30">
        [#assign majorSelectType="radio"/]
        [#list majors as zy][#if zy.name?contains("法学")][#assign majorSelectType="checkbox"/][/#if][/#list]
          [#list majors as zy]
            &nbsp;&nbsp;<input type="${majorSelectType}" id="${zy.id}" name="majorId" value="${zy.id}" /><label for="${zy.id}">${zy.name}</label>
            [#if zy_index % 4 == 3]<br/>[/#if]
          [/#list]<span id="zyIdError"></span>
        </td>
      </tr>
    </table>
  </form>
</div>

[@b.foot/]
