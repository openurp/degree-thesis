 <div class="card">
   <div class="card-header">
      <div style="float:left">
      <h5> <span [#if group.staffCount<3]style="color:red" title="答辩组人数需三人以上"[/#if]>第${group.idx}组</span> &nbsp;&nbsp;[#list group.members as m][#if m.leader]${m.teacher.name}<span style="font-size:0.8rem;color: #999;">组长</span>&nbsp;[/#if][/#list]
      [#if group.secretary??]${group.secretary.name}<span style="font-size:0.8rem;color: #999;">秘书</span>[/#if]
      </h5>
      </div>
      <div style="float:right" >
         [#if manageGroups?seq_contains(group)]
           <a class="btn btn-success btn-sm" data-toggle="collapse" href="#newNoticeCard${group.id}" role="button"
           aria-expanded="false" aria-controls="newNoticeCard${group.id}">
             <i class="fa fa-edit"></i> 新拟通知
           </a>
         [/#if]

         [@b.a href="!report?id="+group.id class='btn btn-primary btn-sm' target="_blank"]<i class="fa-solid fa-file-excel"></i>下载答辩表[/@]
         [#if manageGroups?seq_contains(group)]
           [@b.a href="!edit?id="+group.id class='btn btn-primary btn-sm']<i class="fa fa-edit"></i>修改[/@]
         [/#if]
         [@b.a href="!paperZip?id="+group.id class='btn btn-primary btn-sm' target="_blank"]<i class="fa-solid fa-file-zipper"></i>下载论文[/@]
         [#if manageGroups?seq_contains(group)]
         [@b.a href="!inputScore?id="+group.id class='btn btn-primary btn-sm']<i class="fa-solid fa-star"></i>录入成绩[/@]
         [/#if]
      </div>
   </div>
   <div class="card-body">

    [#if manageGroups?seq_contains(group)]
      <form id="noticeForm${group.id}" name="noticeForm${group.id}" method="POST" action="${b.url('!saveNotice')}">
        <div  id="newNoticeCard${group.id}" class="collapse">
         <div class="card">
           <div class="card-body">
            <input type="hidden" name="notice.group.id" value="${group.id}"/>
            <input type="text" id="title${group.id}" name="notice.title" value="" style="width:300px" placeholder="通知标题">
            <span id="title${group.id}Error" style="color:red"></span></p>
            <textarea id="contents${group.id}" name="notice.contents" rows="3" cols="70" placeholder="通知内容"></textarea>
            <span id="contents${group.id}Error" style="color:red"></span>
             <input type="submit" value="发布" >
            </div>
          </div>
        </div>
      </form>
      <script>
    $(document).ready(function(){
      $("#noticeForm${group.id}").submit(function(){
        var error=false;
        if(!validateExp(/^.{1,50}$/,"title${group.id}","通知标题的长度必须为1-50个汉字")){
          error=true;
        }
        if( $.trim($("#contents${group.id}").val())==""){
          $("#contents${group.id}Error").html("通知内容不能为空");
          error=true;
        }
        if($("#contents${group.id}").val().length>5000){
          $("#contents${group.id}Error").html("通知内容的长度不能超过5000个汉字");
          error=true;
        }
        if(error) return false;
        else return true;
       });
    });
    </script>
    [/#if]

    <h6><span style="font-size:0.8rem;color: #999;">答辩组成员：</span>[#list group.members as m][#if !m.leader]${m.teacher.name}&nbsp;[/#if][/#list]
    <span style="font-size:0.8rem;color: #999;">答辩时间：</span>[#if group.beginAt??]${group.beginAt?string("MM月dd日HH:mm")}-${group.endAt?string("HH:mm")}[/#if]
    <span style="font-size:0.8rem;color: #999;">答辩地点：</span>${group.place!}
    </h6>
    [#assign reviews = myGroupReviews.get(group)/]
    <table cellspacing="0" cellpadding="0"  width="100%" style="border-top: 1px solid;border-bottom: 1px solid;">
      <tr style="height:23px;text-align:center;border:1px;border-bottom: 1px solid;">
        <td >序号</td>
        <td >学号</td>
        <td >学生姓名</td>
        <td >专业班级</td>
        <td >指导教师</td>
        <td >论文题目</td>
        <td>评阅教师</td>
        <td>指导教师评分</td>
        <td>交叉评阅得分</td>
        <td>答辩得分</td>
      </tr>
        [#list group.orderedWriters as writer]
        <tr style="height:23px;text-align:center">
          <td>${writer_index+1}</td>
          <td>${writer.std.code}</td>
          <td>${writer.std.name}</td>
          <td>${writer.major.name} ${(writer.squad.name)!}</td>
          <td>${writer.advisor.teacher.name}</td>
          <td>
            [@b.a href="paper!doc?writer.id="+writer.id target="_blank"]<i class="fa-solid fa-file"></i> ${writer.thesisTitle!}[/@]
          </td>
          <td>${(reviews.get(writer).crossReviewer.name)!}</td>
          <td>${(reviews.get(writer).advisorScore)!}</td>
          <td>${(reviews.get(writer).crossReviewScore)!}</td>
          <td>${(reviews.get(writer).defenseScore)!}</td>
        </tr>
      [/#list]
    </table>
    [#if group.notices?size>0]<br>[/#if]
    [#list group.notices?sort_by("updatedAt")?reverse as notice]
    <div class="card" >
      <div class="card-body">
        <h6>${notice.title}&nbsp;<span style="font-size:0.8rem;color: #999;">${notice.updatedAt?string("yy-MM-dd HH:mm")}</span>
        [#if manageGroups?seq_contains(notice.group)]
        <a href="${b.url('!removeNotice?notice.id='+notice.id)}" onclick="return confirm('确定删除?')" class="btn btn-danger btn-sm" title="删除通知">
         <i class="fa-solid fa-trash-can"></i></a>
         [/#if]
         </h6>
         <p class="card-text">${notice.contents}</p>
      </div>
    </div>
    [/#list]

   </div>
 </div>
