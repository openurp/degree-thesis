[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid">
    <div style="color: red">[#if commitment.confirmed]任务书已经确认[#else]任务书尚未确认[/#if]。[#if deadline.delayCount>0]任务书已延期${deadline.delayCount}次[/#if]</div>
    [#include "../../std/commitment/contents.ftl"/]
  </div>
[@b.foot/]
