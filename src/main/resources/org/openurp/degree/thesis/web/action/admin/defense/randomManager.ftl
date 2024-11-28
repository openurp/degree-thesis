[@b.toolbar title="院系教研室负责人"]
  bar.addBack();
[/@]
<table class="grid-table">
  <thead class="grid-head">
    <th style="width:60px">序号</th>
    <th>院系</th>
    <th>教研室</th>
    <th>负责人</th>
    <th>负责人数</th>
    <th style="width:300px">操作</th>
  </thead>
  <tbody>
    [#list reviewManagers as rm]
    [#assign teacher = rm[0]/]
    <tr>
      <td>${rm_index+1}</td>
      <td>${teacher.department.name}</td>
      <td>${(teacher.office.name)!}</td>
      <td>${teacher.name}</td>
      <td>${rm[1]}</td>
      <td>[@b.a href="!randomGroupSetting?manager.id="+teacher.id]自动分组预览[/@]</td>
    </tr>
    [/#list]
  </tbody>
</table>
