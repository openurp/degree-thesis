[@b.grid items=subjects var="subject"]
  [@b.gridbar]
    bar.addItem("${b.text("action.new")}",action.add());
    bar.addItem("${b.text("action.modify")}",action.edit());
    bar.addItem("${b.text("action.delete")}",action.remove("确认删除?"));
    bar.addItem("设置专业",action.multi("batchUpdateSetting"));
    bar.addItem("导入",action.method('importForm'));
    bar.addItem("导出",action.exportData("name:名称,researchField:研究方向,advisor.teacher.name:指导教师,advisor.teacher.department.name:指导老师所在院系,advisor.teacher.office.name:教研室,depart.name:面向院系,majorNames:面向专业,status:状态",null,'fileName=指导老师信息'));
    bar.addItem("所有教师",action.method("teachers",null,"emptyOnly=0"));
    bar.addItem("未添加题目",action.method("teachers",null,"emptyOnly=1"));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col title="名称" property="name"]
      [@b.a target="_blank" href="!info?id="+subject.id]${subject.name}[/@]
    [/@]
    [@b.col title="研究方向" property="researchField" width="200px"/]
    [@b.col title="指导教师" property="advisor.teacher.name" width="100px"/]
    [@b.col title="教研室" property="advisor.teacher.office.name" width="130px"/]
    [@b.col title="面向院系" property="depart.name" width="150px"]
      ${subject.depart.shortName!subject.depart.name}
    [/@]
    [@b.col title="面向专业" width="120px"]
      <div class="text-ellipsis">[#list subject.majors as m]${m.name}[#if m_has_next],[/#if][/#list]</div>
    [/@]
    [@b.col title="审查状态" property="status" width="120px"/]
  [/@]
[/@]
