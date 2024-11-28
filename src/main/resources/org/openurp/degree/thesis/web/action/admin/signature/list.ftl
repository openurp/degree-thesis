[@b.grid items=signatures var="signature"]
  [@b.gridbar]
    bar.addItem("初始化名单",action.method("init"));
    bar.addItem("下载导师签名",action.multi("download",null,null,"_blank"));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="writer.std.code"  title="学号" width="100px"/]
    [@b.col property="writer.std.name"  title="姓名" width="130px"/]
    [@b.col property="writer.std.state.major.name"  title="专业 方向 班级" width="20%" ]
      ${signature.writer.std.state.major.name} ${(signature.writer.std.state.direction.name)!} ${(signature.writer.std.state.squad.name)!}
    [/@]
    [@b.col property="writer.thesisTitle" title="题目"]
      <div class="text-ellipsis">
        [@b.a href="!info?id="+signature.id]${signature.writer.thesisTitle!}[/@]
      </div>
    [/@]
    [@b.col property="writer.advisor.teacher.name" title="指导老师" width="100px"/]
    [@b.col title="学生签名" property="writerUrl" width="80px"]
      [#if signature.writerUrl??]
      已签名
      [/#if]
    [/@]
    [@b.col title="导师签名" property="advisorUrl" width="80px"]
      [#if signature.advisorUrl??]
         已签名
      [/#if]
    [/@]
  [/@]
[/@]
