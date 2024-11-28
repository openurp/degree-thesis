[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript" src="${b.base}/static/scripts/util.js"></script>
[@b.messages slash="3"/]
<div class="container-fluid">
<nav class="navbar navbar-expand-lg navbar-light bg-light">
  <a class="navbar-brand" href="#">答辩分组管理</a>
  <div class="navbar-collapse">
  <ul class="nav">
    <li class="nav-item">
      [@b.a href="/advisor/defense" class="nav-link" ]我的分组[/@]
    </li>
    <li class="nav-item">
      [@b.a href="!advisors" class="nav-link" target="_blank"]教师分组情况[/@]
    </li>
    <li class="nav-item">
      [@b.a href="!writers"  class="nav-link" target="_blank"]学生分组情况[/@]
    </li>
    <li class="nav-item">
      [@b.a href="!editNew" class="nav-link" ]添加分组[/@]
    </li>
    <li class="nav-item">
      [@b.a href="!randomGroupSetting" class="nav-link" ]自动分组..[/@]
    </li>
  </ul>
  </div>
</nav>
  [#list adminGroups as group]
    [#include "groupInfo.ftl"/]
    <br>
  [/#list]
</div>

[@b.foot/]
