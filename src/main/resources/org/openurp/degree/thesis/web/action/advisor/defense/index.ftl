[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript" src="${b.base}/static/scripts/util.js"></script>
<div class="container-fluid"><br>
[@b.messages slash="3"/]
  <table cellspacing="0" cellpadding="0" border="0" width="100%">
    <tr>
      <td align="right">
      [@b.a href="/director/defense" class="btn btn-sm btn-primary"]分组管理[/@]&nbsp;&nbsp;&nbsp;
      </td>
    </tr>
  </table>
  [#list myGroups as group]
    [#include "groupInfo.ftl"/]
    <br>
  [/#list]

  [#if myGroups?size==0]
    <p style="color:red">尚未建立任何分组</p>
  [/#if]
</div>

[@b.foot/]
