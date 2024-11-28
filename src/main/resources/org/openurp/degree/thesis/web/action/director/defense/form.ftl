[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<script type="text/javascript" src="${b.base}/static/scripts/My97DatePicker/WdatePicker.js"></script>
<div class="container-fluid"><br>
[@b.messages slash="3"/]
<style>
  .error_msg{
    color:red;
    font-size:0.8em;
  }
</style>
[@b.form name="defenseGroupForm" id="defenseGroupForm" action="!save"]
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7" >[#if defenseGroup.persisted]第${defenseGroup.idx}答辩组情况[#else]新建答辩组[/#if]
      [#if defenseGroup.members?size<3]
      <span style="color: red">提示:每个答辩组需要3名教师，当前答辩组还缺${3-defenseGroup.members?size}名教师</span>
      [/#if]
    </td>
  </tr>
  <tr>
    <td class="header" align="center" >序号</td>
    <td class="header" align="center" >教师工号</td>
    <td class="header" align="center" >姓名</td>
    <td class="header" align="center" >院系</td>
    <td class="header" align="center" >教研室</td>
    <td class="header" align="center" >答辩组职务</td>
    <td class="header" align="center"  colspan="2">操作</td>
  </tr>
  [#list defenseGroup.members?sort_by("leader")?reverse as member]
    <tr>
      <td class="data" align="center" >${member_index+1}</td>
      <td class="data" align="center" >${member.teacher.code}</td>
      <td class="data" align="center" >${member.teacher.name}</td>
      <td class="data" align="center" >${member.teacher.department.name}</td>
      <td class="data" align="center" >${(member.teacher.office.name)!}</td>
      <td class="data" align="center" >
         <select name="member_${member.teacher.id}.leader">
           <option value="1"  [#if member.leader]selected="selected"[/#if]>组长</option>
           <option value="0"  [#if !member.leader]selected="selected"[/#if]>组员</option>
         </select>
      </td>
      <td class="data" align="center" >
        [@b.a href="!removeAdvisor?defenseGroup.id="+defenseGroup.id+"&defenseMember.id="+member.id]移除[/@]
      </td>
    </tr>
  [/#list]
  [#assign newMember= 5 - defenseGroup.members?size/]
  [#if newMember>0]
  [#list 1..newMember as i]
    <tr>
      <td class="data" align="center" >${defenseGroup.members?size + i}</td>
      <td class="data" align="center"  colspan="4">
      [@b.select name="newmember_${i}.teacher.id" style="width:400px" items=advisors option=r"${item.department.name} ${item.name}(${item.code})" empty="..." label="答辩成员"]
      [/@]
      </td>
      <td class="data" align="center" >
         <select name="newmember_${i}.leader">
           <option value="0">组员</option>
           <option value="1">组长</option>
         </select>
      </td>
      <td class="data" align="center" ></td>
    </tr>
  [/#list]
  [/#if]
  </table>
  <table class="list_table" width="100%">
    <tr>
      <td class="title"  width="200px">答辩开始~结束时间：</td>
      <td class="data" >
      <input type="text" id="kssj" name="defenseGroup.beginAt" value="${(defenseGroup.beginAt?string("yyyy-MM-dd HH:mm:ss"))!}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})"/>
      <span id="kssjError" class="error_msg"></span>
      到<input type="text" id="jssj" name="defenseGroup.endAt" value="${(defenseGroup.endAt?string("yyyy-MM-dd HH:mm:ss"))!}" onclick="WdatePicker({dateFmt:'yyyy-MM-dd HH:mm:ss'})"/>
      <span id="jssjError" class="error_msg"></span>

      答辩组秘书：
      <select name="defenseGroup.secretary.id" style="width:200px" >
        <option value="">...</option>
        [#list secretaries?sort_by(["name"]) as u]
        <option value="${u.id}" [#if (u.id=(defenseGroup.secretary.id)!0)]selected="selected"[/#if]>${u.name}(${u.code})</option>
        [/#list]
      </select>
      </td>
    </tr>
    <tr>
      <td class="title" >答辩地点：</td>
      <td class="data" >
      <input type="text" id="dd" name="defenseGroup.place" value="${defenseGroup.place!}" style="width:356px"/>
      </td>
    </tr>
    <tr>
      <td class="title" >操作：</td>
      <td class="data" >
        &nbsp;&nbsp; [@b.submit value="保存"/]
        &nbsp;&nbsp;&nbsp;&nbsp;<input type="button" onclick="history.back();" value="返回">
        [#if defenseGroup.persisted]<input type="hidden" name="defenseGroup.id" value="${defenseGroup.id }">[/#if]
        </td>
    </tr>
  </table>
[/@]
<br/>
[#if defenseGroup.persisted]
<table cellspacing="0" cellpadding="0" border="0" width="100%">
  <tr>
    <td align="right">[@b.a href="!addWriterList?defenseGroup.id="+defenseGroup.id class="btn btn-outline-primary btn-sm"]选择添加[/@]
      <button class="btn btn-light btn-sm" disabled>随机添加</button>
      <div class="btn-group" role="group">
      [@b.a href="!autoAddWriter?newCount=8&defenseGroup.id="+defenseGroup.id class="btn btn-outline-primary btn-sm"]8名[/@]
      [@b.a href="!autoAddWriter?newCount=4&defenseGroup.id="+defenseGroup.id class="btn btn-outline-primary btn-sm"]4名[/@]
      [@b.a href="!autoAddWriter?newCount=2&defenseGroup.id="+defenseGroup.id class="btn btn-outline-primary btn-sm"]2名[/@]
      [@b.a href="!autoAddWriter?newCount=1&defenseGroup.id="+defenseGroup.id class="btn btn-outline-primary btn-sm"]1名[/@]
      </div>
    </td>
  </tr>
</table>
<table class="list_table" width="100%">
  <tr>
    <td class="title" colspan="7" >学生列表</td>
  </tr>
  <tr>
    <td class="header" align="center" style="widt">序号</td>
    <td class="header" align="center" >学号</td>
    <td class="header" align="center" >学生姓名</td>
    <td class="header" align="center" >指导教师</td>
    <td class="header" align="center" >专业班级</td>
    <td class="header" align="center" >论文题目</td>
    <td class="header" align="center" width="10%">操作</td>
  </tr>
    [#list writers as writer]
    <tr>
      <td class="data" align="center" >${writer_index+1 }</td>
      <td class="data" align="center" >${writer.std.code}</td>
      <td class="data" align="center" >${writer.std.name}</td>
      <td class="data" align="center" >${writer.advisor.teacher.name}</td>
      <td class="data" align="center" >${writer.major.name} ${(writer.squad.name)!}</td>
      <td class="data" align="center" >${writer.thesisTitle!}</td>
      <td class="data" align="center" >
        [@b.a href="!removeWriter?defenseGroup.id="+defenseGroup.id+"&writer.id="+writer.id onclick="return bg.Go(this,null,'确定移除${writer.std.name}')"]移除[/@]
      </td>
    </tr>
  [/#list]
</table>
[/#if]
</div>
[@b.foot/]
