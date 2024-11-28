[@b.grid items=defenseGroups var="defenseGroup"]
  [@b.gridbar]
    bar.addItem("新建",action.add());
    bar.addItem("修改",action.edit());
    bar.addItem("查看",action.info());
    bar.addItem("删除",action.remove());
    bar.addItem("自动分组..",action.method('randomManager'));
    var m = bar.addMenu("发布分组",action.multi('publish',"发布后学生可以查看到分组结果，确定操作？","published=1"));
    m.addItem("取消发布",action.multi('publish',"确定操作？","published=0"));
    bar.addItem("录入成绩",action.single("inputScore"));
  [/@]
  [@b.row]
    [@b.boxcol/]
    [@b.col property="idx"  title="组号" width="100px"]
      [@b.a href="!info?id="+defenseGroup.id]第${defenseGroup.idx}组[/@]
    [/@]
    [@b.col property="department.name"  title="院系" /]
    [@b.col title="组长"]
      <span class="text-muted" style="font-size:0.8em">${(defenseGroup.office.name)!}</span>
      [#list defenseGroup.members as m]
      [#if m.leader]${m.teacher.name}[/#if]
      [/#list]
    [/@]
    [@b.col title="组员"]
      [#list defenseGroup.members as m][#if !m.leader]${m.teacher.name}[#sep]&nbsp;[/#if][/#list]
    [/@]
    [@b.col title="答辩时间" property="beginAt"]
      [#if defenseGroup.beginAt?? && defenseGroup.endAt??]${defenseGroup.beginAt?string('yyyy-MM-dd')} ${defenseGroup.beginAt?string('HH:mm')}~${defenseGroup.endAt?string('HH:mm')}[/#if]
    [/@]
    [@b.col title="答辩地点" property="place"/]
    [@b.col title="学生人数" ]${defenseGroup.writers?size}[/@]
    [@b.col title="是否发布" width="80px" property="published"]
      [#if defenseGroup.published]<span class="toolbar-icon action-activate"></span>已发布[#else]未发布[/#if]
    [/@]
  [/@]
[/@]
