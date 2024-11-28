[@b.toolbar title="答辩组信息"]
  bar.addBack();
[/@]
[#assign group=defenseGroup/]
 <div class="card">
   <div class="card-header">
      <div style="float:left">
      <h5> <span [#if group.staffCount<3]style="color:red" title="答辩组人数需三人以上"[/#if]>第${group.idx}组</span> &nbsp;&nbsp;[#list group.members as m][#if m.leader]${m.teacher.name}<span style="font-size:0.8rem;color: #999;">组长</span>&nbsp;[/#if][/#list]
      [#if group.secretary??]${group.secretary.name}<span style="font-size:0.8rem;color: #999;">秘书</span>[/#if]
      </h5>
      </div>
      <div style="float:right" >
           [@b.a href="!reportZip?id="+group.id class='btn btn-primary btn-sm' target="_blank"]<i class="fa-solid fa-file-word"></i>下载评分表[/@]
           <a href="${b.url('!report?id='+group.id)}" class='btn btn-primary btn-sm'><i class="fa-solid fa-file-excel"></i>下载答辩表</a>
           [@b.a href="!edit?id="+group.id class='btn btn-primary btn-sm']<i class="fa fa-edit"></i>修改[/@]
             [@b.a href="!remove?defenseGroup.id="+group.id onclick="return confirm('确认删除该论文答辩分组？')"  class='btn btn-danger btn-sm']
            <i class="fa-solid fa-xmark"></i>删除[/@]
         [@b.a href="!paperZip?id="+group.id class='btn btn-primary btn-sm' target="_blank"]<i class="fa-solid fa-file-zipper"></i>下载论文[/@]
         [@b.a href="!inputScore?id="+group.id class='btn btn-primary btn-sm']<i class="fa-solid fa-star"></i>录入成绩[/@]
      </div>
   </div>
   <div class="card-body">
    <h6><span style="font-size:0.8rem;color: #999;">答辩组成员：</span>[#list group.members as m][#if !m.leader]${m.teacher.name}&nbsp;[/#if][/#list]
    <span style="font-size:0.8rem;color: #999;">答辩时间：</span>[#if group.beginAt??]${group.beginAt?string("MM月dd日HH:mm")}-${group.endAt?string("HH:mm")}[/#if]
    <span style="font-size:0.8rem;color: #999;">答辩地点：</span>${group.place!}
    </h6>
    <table cellspacing="0" cellpadding="0"  width="100%" style="border-top: 1px solid;border-bottom: 1px solid;">
      <tr style="height:23px;text-align:center;border:1px;border-bottom: 1px solid;">
        <td style="width:50px">序号</td>
        <td style="width:100px">学号</td>
        <td style="width:100px">姓名</td>
        <td style="width:150px">专业班级</td>
        <td style="width:100px">指导教师</td>
        <td>论文题目</td>
        <td style="width:100px">评阅教师</td>
        <td style="width:80px">指导教师评分</td>
        <td style="width:80px">评阅得分</td>
        <td style="width:80px">答辩成绩</td>
        <td style="width:80px">最终成绩</td>
      </tr>
        [#list group.orderedWriters as writer]
        <tr style="height:23px;text-align:center">
          <td>${writer_index+1}</td>
          <td>${writer.std.code}</td>
          <td>${writer.std.name}</td>
          <td>${writer.major.name} ${(writer.squad.name)!}</td>
          <td>${writer.advisor.teacher.name}</td>
          <td>${writer.thesisTitle!}</td>
          <td>${(reviews.get(writer).crossReviewer.name)!}</td>
          <td>${(reviews.get(writer).advisorScore)!}</td>
          <td>${(reviews.get(writer).crossReviewScore)!}</td>
          <td>${(reviews.get(writer).defenseScore)!}</td>
          <td>${(reviews.get(writer).finalScore)!}</td>
        </tr>
      [/#list]
    </table>
    [#if group.notices?size>0]<br>[/#if]
    [#list group.notices?sort_by("updatedAt")?reverse as notice]
    <div class="card" >
      <div class="card-body">
        <h6>${notice.title}&nbsp;<span style="font-size:0.8rem;color: #999;">${notice.updatedAt?string("yy-MM-dd HH:mm")}</span></h6>
        <p class="card-text">${notice.contents}</p>
      </div>
    </div>
    [/#list]

   </div>
 </div>
