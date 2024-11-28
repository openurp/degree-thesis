[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div align="center">
  [#list plans as plan]
    <br>
    <div align="center"><a href="${b.url("!print?id="+plan.id)}" style="font-size: large;" target="_blank">打印</a></div>
    [#include "planInfoTable.ftl"/]
  [/#list]
    <p>
      <input type="button" value="返回" onclick="history.back()">
    </p>
  </div>
[@b.foot/]
