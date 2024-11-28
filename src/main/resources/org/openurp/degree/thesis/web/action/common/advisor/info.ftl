[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div class="container">
   [@b.card class="card-info card-primary card-outline" id="profile_card"]
    [#assign title]<i class="fas fa-user"></i> ${advisor.teacher.name}个人简介和联系方式[/#assign]
    [@b.card_header class="border-transparent" title=title ]
    [/@]
    [@b.card_body class="p-0"]
      <table class="infoTable" width="100%">
        <tr>
          <td class="title" width="100">姓名：</td>
          <td>${advisor.teacher.name}</td>
          <td class="title" width="100">所在院系：</td>
          <td>${advisor.teacher.department.name}</td>
        </tr>
        <tr>
          <td class="title" width="100">职称：</td>
          <td colspan="3">${(advisor.teacher.title.name)!}</td>
        </tr>
        <tr>
          <td class="title" width="100">教师简介：</td>
          <td colspan="3">
            <pre>${advisor.description!}</pre>
          </td>
        </tr>
        <tr>
          <td class="title" width="100">手机：</td>
          <td>${advisor.mobile!}</td>
          <td class="title" width="100">邮箱 ：</td>
          <td><a href="mailto:${advisor.email!}">${advisor.email!}</a></td>
        </tr>
      </table>
    [/@]
  [/@]
</div>
[@b.foot/]
