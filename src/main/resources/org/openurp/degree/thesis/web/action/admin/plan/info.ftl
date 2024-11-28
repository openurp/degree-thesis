[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]

<div class="container-fluid">
    [@b.toolbar title="返回"]
       bar.addBack();
    [/@]
    <div align="center"><a href="${b.url('!print?id='+plan.id)}" style="font-size: large;" target="_blank">打印</a></div>
    [#include "planInfoTable.ftl"/]
    <p style="text-align:center">
      <input type="button" value="返回" onclick="history.back()">
    </p>
  </div>
[@b.foot/]
