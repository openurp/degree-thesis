[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
  <form action="${b.url('!save?id='+midtermCheck.id)}" method="post" id="form">
      <table class="list_table" width="100%">
        <tr>
          <td align="center" colspan="3" class="title">本科生毕业论文(设计)中期检查表</td>
        </tr>
        <tr>
          <td class="title" >姓名:</td>
          <td class="data" style="text-align:left" colspan="2">${writer.std.name}</td>
        </tr>
        <tr>
          <td class="title" >学号:</td>
          <td class="data" style="text-align:left" colspan="2">${writer.std.code}</td>
        </tr>
        <tr>
          <td class="title" >班级:</td>
          <td class="data" style="text-align:left" colspan="2">${(writer.squad.name)!}</td>
        </tr>
        <tr>
          <td class="title" >专业:</td>
          <td class="data" style="text-align:left" colspan="2">${writer.major.name}</td>
        </tr>
        <tr>
          <td class="title" >学院:</td>
          <td class="data" style="text-align:left" colspan="2">${writer.std.state.department.name}</td>
        </tr>
        <tr>
          <td class="title" >指导老师:</td>
          <td class="data" style="text-align:left" colspan="2">${writer.advisor.teacher.name}</td>
        </tr>
        <tr>
          <td class="title" >写作进度(参照任务书):</td>
          <td class="data" style="text-align:left" colspan="2">${midtermCheck.proceeding!}</td>
        </tr>
        <tr>
          <td class="title" width="150px">检查项目</td>
          <td class="title" style="text-align:left" width="200px">评定情况</td>
          <td class="title" style="text-align:left">备注</td>
        </tr>
        [#list items?sort_by("code") as item]
          [#assign detail= midtermCheck.getDetail(item)?if_exists/]
        <tr>
          <td class="title" >${item.name}<font color="red">(必选)</font></td>
          <td class="data" style="text-align:left" colspan="1">
            <input type="radio" name="item${item.id}_passed" id="item${item.id}_1" value="1"><label for="item${item.id}_1">通过</label>
            <input type="radio" name="item${item.id}_passed" id="item${item.id}_2" value="0"><label for="item${item.id}_2">不通过</label>
          </td>
          <td class="data" style="text-align:left" colspan="3">
            <textarea rows="1" cols="80" name="item${item.id}_bz" id="item${item.id}_bz" placeholder="填写不通过原因">${detail.auditOpinion!}</textarea>
          </td>
        </tr>
        [/#list]
      </table>
      <div align="center">
      <input type="button" id="button" value="提交">&nbsp;&nbsp;  <input type="button" onclick=history.back(); value="返回">
      </div>
  </form>
</div>

<script type="text/javascript">
  $(document).ready(function() {
    [#list midtermCheck.details as d]
      [#if d.passed]$("#item${d.item.id}_1").attr("checked",true);[#else]$("#item${d.item.id}_2").attr("checked",true);[/#if]
    [/#list]
    $("#button").click(function(){
      var size=$("input:radio:checked").length;
      if(size==$("input:radio").length/2){
        $("#form").submit();
      }else{
        alert("请勿遗漏评定项目");
      }
    });
  });
</script>
[@b.foot/]
