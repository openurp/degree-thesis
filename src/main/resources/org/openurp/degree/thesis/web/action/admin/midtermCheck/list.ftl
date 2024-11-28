[#macro display longName width]
<div style="overflow: hidden;text-overflow: ellipsis;width: ${width}px;display: inline-block;white-space: nowrap;" title="${longName}">${longName}</div>
[/#macro]
[@b.grid items=writers var="writer"]
  [@b.gridbar]
    bar.addItem("批量审核..",action.multi("batchAuditSetting"));
    bar.addItem("下载",action.single("doc",null,null,false),"action-download");
    bar.addItem("批量下载",action.multi("batchDoc",null,null,false),"action-download");
    bar.addItem("延期",action.multi("defer","确认延期?"));
    bar.addItem("退回",action.multi("reject","确认退回重新修改?"));
    bar.addItem("${b.text("action.export")}",action.exportData("std.code:学号,std.name:姓名,std.state.department.name:院系,std.state.major.name:专业,std.state.direction.name:专业方向,std.state.squad.name:班级,thesisTitle:论文题目,advisor.teacher.name:指导老师,midtermCheck.status:状态",null,'fileName=中期检查'));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="std.code"  title="学号" width="100px"/]
    [@b.col property="std.name"  title="姓名" width="130px"/]
    [@b.col property="std.state.major.name"  title="专业 方向 班级" ]
      ${writer.std.state.major.name} ${(writer.std.state.direction.name)!} ${(writer.std.state.squad.name)!}
    [/@]
    [@b.col property="thesisTitle" title="题目"/]
    [@b.col property="advisor.teacher.name" title="指导老师" width="100px"/]
    [@b.col title="状态" width="100px"]
      [#assign deadline = writer.getOrCreateDeadline(stage)/]
      [#if midtermChecks.get(writer)??]
      [#assign midtermCheck = midtermChecks.get(writer)/]
      <a href="${b.url("!info?id="+midtermCheck.id)}" target="_blank" title="${(deadline.submitAt?string("yyyy-MM-dd HH:mm"))!}">${midtermCheck.status!}</a>
      [#else]
      未提交审查
      [/#if]
    [/@]

    [@b.col title="操作" width="100px"]
      [#assign passed = ((proposals.get(writer).status)!'--') == "审查通过"/]
      [#if !passed]
        <a href="javascipt:void(0)"  onclick="return defer('${writer.id}')">延期</a>
        [#if deadline.endAt??]<span style="font-size:0.8em"
        title="已延期${deadline.delayCount}次至${deadline.endAt?string("yyyy-MM-dd")}">${deadline.endAt?string("M-d")}</span>[/#if]
      [/#if]
    [/@]

  [/@]
[/@]

    [@b.form name="hrefForm" theme="html" action="!defer"]
      <input name="_params" type="hidden" value="[#list Parameters?keys as k]&${k}=${Parameters[k]}[/#list]" />
      <input name="writer.id" type="hidden" value=""/>
    [/@]

<script type="text/javascript">
  function defer(id){
    if(confirm('（系统将记录该学生的延期次数）该学生开题报告可以延期3天,确认执行?')){
      document.hrefForm['writer.id'].value=id;
      bg.form.submit("hrefForm")
    }
    return false;
  }
</script>
