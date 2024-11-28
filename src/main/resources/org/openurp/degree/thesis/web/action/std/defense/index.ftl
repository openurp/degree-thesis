[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[#if defenseGroup??]
[#assign group=defenseGroup/]
 <div class="card">
   <div class="card-header">
      <div style="float:left">
      <h5> <span [#if group.staffCount<3]style="color:red" title="答辩组人数需三人以上"[/#if]>第${group.idx}组</span> &nbsp;&nbsp;[#list group.members as m][#if m.leader]${m.teacher.name}<span style="font-size:0.8rem;color: #999;">组长</span>&nbsp;[/#if][/#list]
      [#if group.secretary??]${group.secretary.name}<span style="font-size:0.8rem;color: #999;">秘书</span>[/#if]
      </h5>
      </div>
   </div>
   <div class="card-body">
    <h6><span style="font-size:0.8rem;color: #999;">答辩组成员：</span>[#list group.members as m][#if !m.leader]${m.teacher.name}&nbsp;[/#if][/#list]
      <span style="font-size:0.8rem;color: #999;">答辩时间：</span>[#if group.beginAt??]${group.beginAt?string("MM月dd日HH:mm")}-${group.endAt?string("HH:mm")}[/#if]
      <span style="font-size:0.8rem;color: #999;">答辩地点：</span>${group.place!}
    </h6>
    <table cellspacing="0" cellpadding="0"  width="100%" style="border-top: 1px solid;border-bottom: 1px solid;">
      <tr style="height:23px;text-align:center;border:1px;border-bottom: 1px solid;">
        <td>序号</td>
        <td>学号</td>
        <td>学生姓名</td>
        <td>专业班级</td>
        <td>指导教师</td>
        <td>论文题目</td>
      </tr>
        [#list group.orderedWriters as writer]
        <tr style="height:23px;text-align:center;[#if me==writer]background-color:#fff59d[/#if]" >
          <td>${writer_index+1 }</td>
          <td>${writer.std.code}</td>
          <td>${writer.std.name}</td>
          <td>${writer.major.name} ${(writer.squad.name)!}</td>
          <td>${(writer.advisor.teacher.name)!}</td>
          <td>${writer.thesisTitle!}</td>
        </tr>
      [/#list]
    </table>
    [#if group.notices?size>0]<br>[/#if]
    [#list group.notices?sort_by("updatedAt")?reverse as notice]
    <div class="card" >
      <div class="card-body">
        <h6>${notice.title}&nbsp;<span style="font-size:0.8rem;color: #999;">${notice.updatedAt?string("yy-MM-dd HH:mm")}</span></h6>
        <p class="card-text">${notice.contents}</p>
      </div>
    </div>
    [/#list]
   </div>
 </div>
[#else]
  <p style="margin:auto;text-align: center;padding-top: 50px;color:red">您尚未被分配到任一答辩组</p>
[/#if]
[@b.foot/]
