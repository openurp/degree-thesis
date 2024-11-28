[@b.head/]
[@b.grid items=plans var="plan"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
    [#if plans?size==0]
    bar.addItem("复制上届的时间",action.method("cloneLast"));
    [/#if]
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col title="时间"]
    [#assign times = plan.times?sort_by(["stage","id"])/]

    [#list times?chunk(4) as timelist]
      [#list timelist as time]
        ${time.stage.name} ${time.beginOn}~${time.endOn}[#if time_has_next]&nbsp;[/#if]
      [/#list]
      <br>
    [/#list]

    [#if plan.departPlans?size>0]
    <div class="container">
    [#assign rows = ((plan.departPlans?size+1)/2)?int/]
    [#assign departPlans =plan.departPlans?sort_by(["department","code"])/]
    [#assign planCols = departPlans?chunk(rows)/]

    <table class="grid-table" width="100%">
      [#list 0..rows-1 as  row]
        <tr>
            <td width="30px">${row_index+1}</td>
            [#assign dp = planCols[0][row]/]
            <td width="20%">${(dp.department.name)!}</td>
            <td>${dp.status}</td>
            <td>[@b.a href="!print?id="+dp.id target="_blank"]查看[/@]</td>
            <td>
              [@b.a href="!check?id="+dp.id]审查[/@]
              [@b.a href="!removeDepartPlan?departPlan.id="+dp.id onclick="if(confirm('确认删除${dp.department.name}工作计划?')){ return bg.Go(this)} else return false;"]删除[/@]
            </td>
            [#if (planCols[1][row])??]
            [#assign dp = planCols[1][row]/]
            <td width="30px">${row_index+rows+1}</td>
            <td width="20%">${(dp.department.name)!}</td>
            <td>${dp.status}</td>
            <td>[@b.a href="!print?id="+dp.id target="_blank"]查看[/@]</td>
            <td>
              [@b.a href="!check?id="+dp.id]审查[/@]
              [@b.a href="!removeDepartPlan?departPlan.id="+dp.id onclick="if(confirm('确认删除${dp.department.name}工作计划?')){ return bg.Go(this)} else return false;"]删除[/@]
            </td>
            [#else]
            <td></td><td></td><td></td><td></td><td></td>
            [/#if]
        </tr>
      [/#list]
    </table>
    </div>
    [/#if]
    [/@]
  [/@]
[/@]

[#if plans?size>0]
<br>

[/#if]
[@b.foot/]
