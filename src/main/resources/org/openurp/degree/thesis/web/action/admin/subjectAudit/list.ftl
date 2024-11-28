[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript">
  function selectAll() {
    var elements = document.getElementsByName("subject.id");
    for ( var i = 0; i < elements.length; i++) {
      elements[i].checked = !elements[i].checked;
    }
  }
</script>

<div class="container-fluid">
[@b.form name="form" id="form" action="!auditForm"]
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="9" height="30">
      <input type="hidden" name="status" value="${Parameters['status']}">
      [#if subjects?size>0]
      [@b.submit value="审查选中项"/]
      [/#if]&nbsp;&nbsp;本学院毕业论文(设计)题目列表&nbsp;&nbsp;总计:${subjects?size}个</td>
  </tr>
  <tr>
     <td class="header" align="center" height="30" width="40%">题目名称</td>
     <td class="header" align="center" height="30" width="200px">研究方向</td>
     <td class="header" align="center" height="30" width="150px">面向学院</td>
     <td class="header" align="center" height="30" width="200px">面向专业</td>
     <td class="header" align="center" height="30" width="100px">指导教师</td>
     <td class="header" align="center" height="30" width="80px">审查状态</td>
      <td class="header" align="center" height="30">操作</td>
     [#if subjects?size>0]
      <td class="header" align="center" height="30" width="40"><a
        href="javascript:void(0)" onclick="selectAll();">全选</a>
      </td>
      [/#if]
  </tr>
  [#list subjects as subject]
    <tr>
      <td class="data" align="center" height="30">[@b.a href="!info?id="+subject.id]${subject.name}[/@]</td>
      <td class="data" align="center" height="30">${subject.researchField!'--'}</td>
      <td class="data" align="center" height="30">${subject.depart.name}</td>
      <td class="data" align="center" height="30">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
      <td class="data" align="center" height="30"><a href="${b.base}/js/view.htm?id=${subject.advisor.id}">${subject.advisor.teacher.name}</a></td>
      <td class="data" align="center" height="30">${subject.status}</td>
      <td class="data" align="center" height="30" width="60">[@b.a href="!auditForm?subject.id=" + subject.id + "&status=" + Parameters['status']]审查[/@]</td>
      <td class="data" align="center" height="30"><input type="checkbox" name="subject.id" value="${subject.id }"></td>
    </tr>
  [/#list]
</table>
[/@]
</div>

[@b.foot/]
