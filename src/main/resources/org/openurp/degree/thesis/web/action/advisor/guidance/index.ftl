[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
  <div class="container-fluid"><br>
    [#if withoutGuidances?size>0]
    <div class="card">
      <div class="card-header">
        有${withoutGuidances?size}同学的缺失指导记录
      </div>
      <div class="card-body">
        <p class="card-text">
        <span style="color:red">[#list withoutGuidances as w]${w.name}[#if w_has_next],[/#if][/#list]</span>
        [#list guidances?keys as writer]<a href="#guidance_writer_${writer.id}">${writer.std.name}</a>[#if writer_has_next]&nbsp;[/#if][/#list]
        </p>
      </div>
    </div>
    <br>
    [/#if]

    [#assign turnNames=["0","一","二","三","四"] /]
    [#list guidances?keys as writer]
    <div class="card" id="guidance_writer_${writer.id}">
      [#assign glist = guidances.get(writer)?sort_by("stage")/]
      <div class="card-header">
        ${writer.std.name}的指导记录
      </div>
      <div class="card-body">
        [#assign gmap={}/]
        [#list glist as g][#assign gmap=gmap+{g.stage.id+"_"+g.idx:g}/][/#list]
        [#list gmap?keys?sort as gkey]
        [#assign g= gmap[gkey]/]
        <h7>${g.stage} 第${turnNames[g.idx]}次指导 ${g.updatedAt?string('YYYY-MM-dd HH:mm:ss')}</h7>
        <p class="card-text"><pre>${g.contents}</pre></p>
        [/#list]
      </div>
    </div>
    <br>
   [/#list]
  </div>
[@b.foot/]
