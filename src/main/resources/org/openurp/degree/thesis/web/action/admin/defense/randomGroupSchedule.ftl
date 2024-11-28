[@b.toolbar title="随机答辩分组"]
  bar.addBack();
[/@]
[@b.form name="randomGroupForm" action="!randomGroupSchedule" theme="list"]
  [@b.field label="负责人"]${manager.name}[/@]
  [@b.number label="分组个数" name="groupCount" value="${groupCount}"/]
  [@b.field label="教师"+ teachers?size +"名"]
    [#list teachers as teacher]<input type="checkbox" value="${teacher.id}" name="teacher.id" [#if selectTeachers?seq_contains(teacher)]checked="checked"[/#if]>${teacher.name}[#sep]&nbsp;[/#list]
  [/@]
  [@b.field label="学生"+ writers?size +"名"]
    <div style="padding-left: 6.25rem;">
    [#list majorWriters?keys as k]
    <input type="checkbox" value="${k.id}" name="major.id" [#if selectMajors?seq_contains(k)]checked="checked"[/#if]><span>${k.name} ${majorWriters.get(k)?size}人</span>: [#list majorWriters.get(k) as writer]${writer.std.name}[#sep]&nbsp;[/#list] <br>
    [/#list]
    </div>
  [/@]
  [@b.formfoot]
    <input type="hidden" name="manager.id" value="${manager.id}"/>
    [@b.submit value="预览结果随机分组结果"/]
  [/@]
[/@]

[@b.form name="scheduleResultForm" action="!saveRandomGroup" theme="list"]
   <input name="groupCount" value="${groupCount}" type="hidden">

   [#list groups as group]
   <input name="group_${group_index}_teacherIds" value="[#list group.teachers as t]${t.id}[#sep],[/#list]" type="hidden">
   <input name="group_${group_index}_writerIds" value="[#list group.writers as t]${t.id}[#sep],[/#list]" type="hidden">
   <div class="card">
     <div class="card-header">
        <div style="float:left">
        <h5> <span [#if group.teachers?size<3]style="color:red" title="答辩组人数需三人以上"[/#if]>第${group_index+1}组</span> &nbsp;&nbsp;[#list group.teachers as m]${m.name}&nbsp;[/#list]</h5>
        </div>
     </div>
     <div class="card-body">
      <table cellspacing="0" cellpadding="0"  width="100%" style="border-top: 1px solid;border-bottom: 1px solid;">
        <tr style="height:23px;text-align:center;border:1px;border-bottom: 1px solid;">
          <td style="width:40px">序号</td>
          <td style="width:100px">学号</td>
          <td style="width:100px">学生姓名</td>
          <td style="width:100px">指导教师</td>
          <td>专业班级</td>
          <td>论文题目</td>
        </tr>
          [#list group.orderedWriters as writer]
          <tr style="height:23px;text-align:center">
            <td>${writer_index+1}</td>
            <td>${writer.std.code}</td>
            <td>${writer.std.name}</td>
            <td>${writer.advisor.teacher.name}</td>
            <td>${writer.major.name} ${(writer.squad.name)!}</td>
            <td>${writer.thesisTitle!}</td>
          </tr>
        [/#list]
      </table>
     </div>
   </div>
  [/#list]
  [@b.formfoot]
    <input type="hidden" name="manager.id" value="${manager.id}"/>
    [@b.submit value="保存分组结果"/]
  [/@]
[/@]
