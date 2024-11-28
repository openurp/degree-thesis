[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container-fluid"><br>
[@b.messages slash="3"/]
<table width="100%">
  <tr>
    <td>
      [#list departPlans as departPlan]
        ${departPlan.department.name}:
        [#list departPlan.currentTimes as stageTime]
          ${stageTime.stage}<span class="text-muted">${stageTime.beginOn}~${stageTime.endOn}</span>
        [/#list]
      [#sep],[/#list]
    </td>
    <td>
     <a class="btn [#if advisor.mobile??]btn-primary[#else]btn-danger[/#if] btn-sm" href="javascript:void(0)"
         onclick="jQuery('#profile_card').show()"><i class="fas fa-id-card"></i>修改个人简介</a>
     <a class="btn btn-sm btn-primary" href="/portal/user/message" target="messageZone" onclick="return bg.Go(this,null)"><i class="fas fa-comments"></i>我的消息</a>
    </td>
  </tr>
</table>

   [@b.card class="card-info card-primary card-outline" style="display:none" id="profile_card"]
    [#assign title]<i class="fas fa-file-pdf"></i> 个人联系方式和简介[/#assign]
    [@b.card_header class="border-transparent" title=title ]
    [/@]
    [@b.card_body class="p-0"]
      [@b.form name="profileForm" action="!saveProfile" theme="list"]
        [@b.textfield name="mobile" placeholder="手机" required="true" label="手机" value=advisor.mobile!/]
        [@b.textfield name="email" placeholder="邮箱" required="true" label="邮箱" value=advisor.email!  style="width:400px"/]
        [@b.textarea name="description" placeholder="个人介绍" required="true" label="个人介绍" value=advisor.description!  style="width:80%" rows="5"/]
        [@b.formfoot]
          [@b.submit value="提交"/]
        [/@]
      [/@]
    [/@]
  [/@]

  <div><p style="margin:0px">最近一届的学生列表  总计:${writers?size}个</p></div>
  <table class="list_table" width="100%" id="writer_list">
    <tr>
      <td class="header"  width="40px">序号</td>
      <td class="header"  width="100px">学号</td>
      <td class="header"  width="80px">姓名</td>
      <td class="header"  width="150px">院系</td>
      <td class="header" width="200px">专业、方向、班级</td>
      <td class="header">论文题目(研究方向)</td>
      <td class="header" width="120px">研究方向</td>
      <td class="header" width="100px">联系方式</td>
    </tr>

    [#list writers as writer]
      <tr style="text-align:center;">
        <td class="data">${writer_index+1}</td>
        <td class="data">${writer.std.code}</td>
        <td class="data" style="word-wrap: anywhere;"><a href="/portal/user/message/new?recipient.code=${writer.std.code}" target="messageZone" onclick="return bg.Go(this,'messageZone')">${writer.std.name} <i class="fas fa-comments"></i></a></td>
        <td class="data">${writer.department.name}</td>
        <td class="data">
          [#assign major_name]${writer.std.state.major.name} ${(writer.std.state.direction.name)!} ${(writer.std.state.squad.name)!}[/#assign]
          [@display major_name,400/]
        </td>
        <td class="data">
          [#if papers.get(writer)??]
            [#assign paper = papers.get(writer)/]
            [@b.a href="paper!doc?id="+paper.id target="_blank"]<i class="fa-solid fa-file"></i> ${writer.thesisTitle!}[/@]
          [#else]
          ${writer.thesisTitle!}
          [/#if]
        </td>
        <td class="data">${writer.researchField!'--'}</td>
        <td class="data">
          [#if writer.mobile??]<span data-toggle="tooltip" data-placement="top" title="${writer.mobile}"><i class="fa-solid fa-phone"></i>手机</span>[/#if]
          [#if writer.email??]<a href="mailto:${writer.email}"><i class="fa-solid fa-envelope" title="${writer.email}"></i></a>[/#if]
        </td>
      </tr>
    [/#list]
  </table>
  [@b.div id="messageZone"]  [/@]

  <div style="margin-top:20px"><p style="margin:0px">往届的学生列表  总计:${historyWriters?size}个</p></div>
  <table class="list_table" width="100%">
      <tr>
        <td class="header"  width="40px">序号</td>
        <td class="header"  width="100px">学号</td>
        <td class="header"  width="80px">姓名</td>
        <td class="header"  width="150px">院系</td>
        <td class="header" width="200px">专业、方向、班级</td>
        <td class="header" >论文题目</td>
        <td class="header" width="120px">研究方向</td>
        <td class="header"  width="100px">联系方式</td>
      </tr>
    [#list historyWriters?sort_by(["std","code"])?reverse as writer]
      <tr style="text-align:center;">
        <td class="data">${writer_index+1}</td>
        <td class="data">${writer.std.code}</td>
        <td class="data" style="word-wrap: anywhere;">${writer.std.name}</td>
        <td class="data">${writer.department.name}</td>
        <td class="data">
          [#assign major_name]${writer.std.state.major.name} ${(writer.std.state.direction.name)!} ${(writer.std.state.squad.name)!}[/#assign]
          [@display major_name,400/]
        </td>
        <td class="data">
            [@b.a href="paper!doc?writer.id="+writer.id target="_blank"]<i class="fa-solid fa-file"></i> ${writer.thesisTitle!}[/@]
        </td>
        <td class="data">${writer.researchField!'--'}</td>
        <td class="data">
          [#if writer.mobile??]<span data-toggle="tooltip" data-placement="top" title="${writer.mobile}"><i class="fa-solid fa-phone"></i>手机</span>[/#if]
          [#if writer.email??]<a href="mailto:${writer.email}"><i class="fa-solid fa-envelope" title="${writer.email}"></i></a>[/#if]
        </td>
      </tr>
    [/#list]
  </table>
</div>
<script>
  jQuery(function() {
    $('#writer_list [data-toggle="tooltip"]').tooltip();
  });
</script>
[@b.foot/]
