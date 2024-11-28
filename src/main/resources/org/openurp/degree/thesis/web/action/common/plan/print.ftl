[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
    <table width="100%" class="grid-table">
      <caption style="caption-side: top;text-align: center;">${plan.department.name}毕业论文(设计)工作计划</caption>
      <thead class="grid-head">
        <tr>
          <th width="100px">&nbsp;&nbsp;工作阶段</th>
          <th width="300px">&nbsp;&nbsp;安排</th>
          <th>&nbsp;&nbsp;说明</th>
        </tr>
      </thead>
      [#include "stage_descriptions.ftl"/]
      <tbody class="grid-body">
      [#list plan.times?sort_by(["stage","id"]) as time]
      [#assign description=descriptions[time.stage.id?string]!''/]
      [#if time.stage.id==17 || time.stage.id==18][#continue/][/#if]
      [#assign stage=time.stage.name/]
      [#if time.stage.id==16]
        [#assign description=descriptions[15?string]!''/]
        [#assign stage="教师指导"/]
      [/#if]
      <tr>
        <td>&nbsp;&nbsp;${stage}</td>
        <td align="left">&nbsp;&nbsp;${time.beginOn}到${time.endOn}</td>
        <td align="left"><p>&nbsp;&nbsp;${description}</p></td>
      </tr>
      [/#list]
      </tbody>
    </table>
  </div>
  <div align="center" id="printJH" style="font-size: large;"><a href="javascript:printJH();" >打印</a></div>
  <script type="text/javascript">
  function printJH(){
    document.getElementById("printJH").style.display= "none";
    window.print();
  }
  </script>
[@b.foot/]
