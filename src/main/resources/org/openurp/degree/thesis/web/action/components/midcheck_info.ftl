
    <table class="list_table" class="info_table">
      <tr>
        <td align="center" colspan="3" class="title">
          本科生毕业论文(设计)中期检查表 </td>
      </tr>
      <tr>
        <td class="title" >姓名:</td>
        <td class="data" align="left" colspan="2">${writer.std.name}</td>
      </tr>
      <tr>
        <td class="title">学号:</td>
        <td class="data" align="left" colspan="2">${writer.std.code}</td>
      </tr>
      <tr>
        <td class="title">班级:</td>
        <td class="data" align="left" colspan="2">${(writer.squad.name)!}</td>
      </tr>
      <tr>
        <td class="title" >专业:</td>
        <td class="data" align="left" colspan="2">${writer.major.name}</td>
      </tr>
      <tr>
        <td class="title" >学院:</td>
        <td class="data" align="left" colspan="2">${writer.std.state.department.name}</td>
      </tr>
      <tr>
        <td class="title">指导老师:</td>
        <td class="data" align="left" colspan="2">${writer.advisor.teacher.name}</td>
      </tr>
      [#if midtermCheck??]
      <tr>
        <td class="title" >写作进度(参照任务书):</td>
        <td class="data" align="left" colspan="2">
          ${midtermCheck.proceeding!}
        </td>
      </tr>
      <tr>
        <td class="title" width="150px">检查项目</td>
        <td class="title" align="left" width="30%">评定情况</td>
        <td class="title" align="left">备注</td>
      </tr>
      [#list midtermCheck.details as detail]
      <tr>
        <td class="title">${detail.item.name}</td>
        <td class="data" align="left" >${detail.passed?string('通过','不通过')}</td>
        <td class="data" align="left">${detail.auditOpinion!}</td>
      </tr>
      [/#list]
      <tr>
        <td class="title">检查结论</td>
        <td class="data" align="left">${midtermCheck.status!}</td>
        <td class="data" align="left">${midtermCheck.conclusion!}</td>
      </tr>
      [#else]
      [/#if]
    </table>
