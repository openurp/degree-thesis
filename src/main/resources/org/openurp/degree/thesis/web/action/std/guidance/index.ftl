[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
[@b.messages slash="3"/]
  <div align="center" class="container-fluid">
    <table class="list_table" width="100%">
      <tr>
        <td class="title" height="30" colspan="2">对${writer.std.name}的指导记录 ${stage}</td>
      </tr>
      <tr>
        <td class="title" style="width:120px" height="30">指导时间</td>
        <td class="data">
        [#assign stageTime= plan.getStageTime(stage)/]
         ${stageTime.beginOn }到${stageTime.endOn }
        </td>
      </tr>
      [#assign turnNames=["0","一","二","三","四"] /]
      [#list guidances?sort_by("idx") as g]
      <tr>
        <td class="title" height="30">第${turnNames[g.idx]}次指导</td>
        <td  class="data"><pre>${g.contents}</pre></td>
      </tr>
      [/#list]
    </table>
    [@b.a class="btn btn-primary btn-sm" href="!edit?stage="+stage.id]修改[/@]
    <a class="btn btn-primary btn-sm" onclick="history.back()">返回</a>
  </div>
[@b.foot/]
