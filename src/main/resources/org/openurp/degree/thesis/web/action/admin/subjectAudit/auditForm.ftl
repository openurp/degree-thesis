[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript">
  $(document).ready(function(){
    $("#zt1").click(function(){
      $("#scyj").val("通过审查");
    });
    $("#zt2").click(function(){
      $("#scyj").val("未通过审查");
    });
  });
</script>
<div class="container-fluid">
[@b.form name="form" action='!audit']批量审查论文题目
<table class="list_table" width="100%">
  <tr>
    <td class="title" height="30" width="100">审查:</td>
    <td class="data" height="30"><input type="radio" id="zt1" name="newStatus" value="100" checked="checked"><label for="zt1">审查通过</label>
      <input type="radio" id="zt2" name="newStatus" value="99"><label for="zt2">审查未通过</label></td>
  </tr>
  <tr>
    <td class="title" height="100" width="100">审查意见：</td>
    <td class="data" height="100" width="500"><textarea id="scyj" name="auditOpinion" rows="10" cols="50">审查通过</textarea></td>
  </tr>
  <tr>
    <td class="title" height="30">操作：</td>
    <td class="data" height="30">&nbsp;&nbsp;
      [@b.submit value="审查"/]&nbsp;&nbsp;&nbsp;&nbsp;
      <input type="button" onclick="history.back()" value="返回"></td>
  </tr>
</table>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="3" height="30">
      <input type="hidden" name="status" value="${Parameters['status']}">
      &nbsp;&nbsp;毕业论文(设计)题目列表&nbsp;&nbsp;总计:${subjects?size}个</td>
  </tr>
  <tr>
     <td class="header" align="center" height="30" width="70%">题目名称</td>
     <td class="header" align="center" height="30" width="15%">面向专业</td>
     <td class="header" align="center" height="30" width="15%">指导教师</td>
  </tr>
  [#list subjects as subject]
    <tr>
      <td class="data" align="center" height="30"><input type="hidden" name="subject.id" value="${subject.id }"><a href="${b.url('!info?id='+subject.id)}">${subject.name}</a></td>
      <td class="data" align="center" height="30">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
      <td class="data" align="center" height="30"><a href="${b.base}/js/view.htm?id=${subject.advisor.id}">${subject.advisor.teacher.name}</a></td>
    </tr>
  [/#list]
</table>

[/@]
</div>
[@b.foot/]
