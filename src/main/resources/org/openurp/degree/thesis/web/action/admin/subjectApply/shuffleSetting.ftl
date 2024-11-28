[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.messages slash="3"/]
<p style="width:800px;text-align:center"><font color="red">说明："随机分配剩余题目"功能会为未选到题目学生随机分配一个题目，请在补选抽签结束后慎重使用。</font>
</p>
  <table class="list_table" width="100%">
    <tr>
      <td class="title" colspan="5" >选题抽签</td>
    </tr>
    [#list departs?sort_by("code") as depart]
    [#assign freeSubjectCount=freeSubjectCountMap[depart.id?string]!0/]
    [#assign firstNeedShuffled=needShuffledMap['1'][depart.id?string]!0/]
    [#assign secondNeedShuffled=needShuffledMap['2'][depart.id?string]!0/]
    [#assign lastNeedShuffled=needShuffledMap['99'][depart.id?string]!0/]
    <tr>
      <td class="header">
         ${depart_index+1}
      </td>
      <td class="header">
         ${depart.name}
      </td>
      <td class="header" align="center" >
      [#if  firstNeedShuffled >0 ]
        [@b.a href="!shuffle?round=1&departId="+depart.id onclick="return bg.Go(this,null,'该操作将执行【初选】题目抽签，确认执行?')"]初选题目抽签(${firstNeedShuffled})[/@]
      [#else]
      初选学生均已确定选题
      [/#if]
      </td>
      <td class="header" align="center">
      [#if secondNeedShuffled>0]
        [@b.a href="!shuffle?round=2&departId="+depart.id onclick="return bg.Go(this,null,'该操作将执行【补选】题目抽签，确认执行?')"]补选题目抽签(${secondNeedShuffled})[/@]
      [#else]
       补选学生均已确定选题
      [/#if]
      </td>
      <td class="header" align="center" >
        [@b.a href="!shuffle?round=99&departId="+depart.id onclick="return bg.Go(this,null,'确定随机分配剩余题目?')"]随机分配剩余题目(${lastNeedShuffled}名学生 ${freeSubjectCount}个题目)[/@]
      </td>
    </tr>
    [/#list]
  </table>
  <br>

</div>
[@b.foot/]
