[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  [@b.messages/]
  [#if !writer.thesisTitle??]<div align="center">论文题目尚未确定，不能确认任务书</div>
  [#else]
  <div class="container-fluid">
    [#if commitment?? && commitment.confirmed]任务书已经确认,${commitment.updatedAt?string("yyyy-MM-dd HH:mm")}
    [#else]<div style="color: red">请及时确认任务书以免影响后续论文工作的正常进行。[@b.a href="!confirm" onclick="return bg.Go(this,null,'确认任务书?');" class="btn btn-sm btn-primary"]确认[/@]</div>
    [/#if]
    [#include "contents.ftl"/]
    [#if deadline.delayCount>0]
    <p style="width: 800px; text-align: center">
      <font color="red">你的任务书已延期${deadline.delayCount}次 ,多次延期将影响到论文最终成绩.</font>
    </p>
    [/#if]
        <input type="button" onclick=history.back(); value="返回">&nbsp;&nbsp;
        [#if !(commitment.confirmed)!false]
          [#if deadline.expired]
            任务书确认工作已经结束，若仍需确认请联系学院教学秘书做延期处理。
          [#else]
            <a id="confirm" href="${b.url("!confirm")}" onclick="return confirm('确定执行此操作吗？')" style="font-size: large;">确认</a>
            [#if deadline.endAt??]请于${deadline.endAt?string("yyyy-MM-dd HH:mm")}之前确认[/#if]
          [/#if]
        [/#if]
  </div>
  [/#if]
[@b.foot/]
