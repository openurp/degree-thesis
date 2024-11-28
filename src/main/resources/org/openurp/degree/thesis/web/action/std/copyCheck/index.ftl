[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[#list checks as check]
 <div class="card">
   <div class="card-header">
      <div style="float:left">
      <h5>论文检测结果 &nbsp;&nbsp;${writer.std.name} ${writer.thesisTitle!}</h5>
      </div>
   </div>
   <div class="card-body">
    <h6>
    <span style="font-size:0.8rem;color: #999;">${check.recheck?string('复检','初检')}：</span>${check.passed?string('通过','未通过')}
    <span style="font-size:0.8rem;color: #999;">检测日期：</span>${check.checkOn?string('yyyy-MM-dd')}
    [#if check.report??]
    <span style="font-size:0.8rem;color: #999;">总字数：</span>${check.wordCount}
    <span style="font-size:0.8rem;color: #999;">去除引用文献复制比：</span>${(check.copyRatio*100)?string("0.000")}%
    [@b.a href="!report" target="_balnk"]下载此报告[/@]
    [/#if]
    </h6>
    [#if check.report??]
    <iframe scrolling="auto" src="${b.url('!view')}" style="min-height: 800px;" width="100%" height="100%" frameborder="0"></iframe>
    [/#if]
   </div>
 </div>
[/#list]

[#if checks?size==0]
  <p>您尚未进行论文检测</p>
[/#if]
</div>
[@b.foot/]
