[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid">
[@b.messages slash="3"/]
<table class="list_table" width="800">
  <tr>
    <td class="title" colspan="9">剩余题目列表(共有题目${subjects?size}个)</td>
  </tr>
  <tr>
    <td class="header" align="center" width="40px">序号</td>
    <td class="header" align="center">题目名称</td>
    <td class="header" align="center">院系</td>
    <td class="header" align="center" width="80px">指导教师</td>
    <td class="header" align="center">面向专业</td>
    <td class="header" align="center" width="250px">选择学生</td>
    <td class="header" colspan="3" align="center">操作</td>
  </tr>

  [#list subjects as subject]
    <tr>
      <td class="data" align="center">${subject_index+1 }</td>
      <td class="data" align="center">${subject.name}</td>
      <td class="data" align="center">${subject.depart.name}</td>
      <td class="data" align="center">${subject.advisor.teacher.name}</td>
      <td class="data" align="center">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</td>
      <td class="data" align="center">
        <select name="writer.id" onchange="changeWriter(this.value,'${subject.id}')">
           <option value="">...</option>
           [#if writerMap.get(subject.depart)??]
           [#list writerMap.get(subject.depart) as writer]
             [#if subject.majors?seq_contains(writer.std.major)]
           <option value="${writer.id}">${writer.std.code} ${writer.std.name}</option>
             [/#if]
           [/#list]
           [/#if]
        </select>
      </td>
      <td class="data" align="center">
      <button class="btn btn-sm btn-primary"onclick="choose()">分配</button>
      </td>
    </tr>
  [/#list]
</table>
[@b.form name="assignForm" action="!assign"]
  <input type="hidden" name="writer.id" value=""/>
  <input type="hidden" name="subjectId" value=""/>
  <input type="hidden" name="next" value="remainder"/>
[/@]

<script>
  function changeWriter(writerId,subjectId){
    var form = document.assignForm;
    form['writer.id'].value=writerId;
    form['subjectId'].value=subjectId;
  }

  function choose(){
    var form = document.assignForm;
    if(!form['writer.id'].value){
       alert("请选择学生")
       return false;
    }
    bg.form.submit(form);
  }
</script>
</div>
