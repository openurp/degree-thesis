[@b.toolbar title="论文材料统计"]
  bar.addBack();
[/@]
[@b.messages slash="3"/]
<table class="grid-table">
  <thead class="grid-head">
    <th style="width:60px">序号</th>
    <th>院系</th>
    <th>论文数</th>
    <th>开题报告</th>
    <th>答辩评分表</th>
    <th style="width:300px">论文压缩包</th>
  </thead>
  <tbody>
  [#-- w[0]:id,w[1]:department_code,w[2]:department_name --]
    [#list papers as w]
    <tr>
      <td>${w_index+1}</td>
      <td>${w[2]}</td>
      <td>${w[3]}</td>
      <td>${proposals.get(w[0])!}</td>
      <td>${defenses.get(w[0])!}</td>
      <td>
        [#if zipTimes.get(w[0])??]
         [@b.a href="!downloadDepartZip?seasonId="+seasonId+"&departmentId="+w[0] target="_blank"]下载(${zipSizes.get(w[0])/1024.0/1024.0}MB ${(zipTimes.get(w[0])?string('MM-dd HH:mm'))})[/@]&nbsp;
         [@b.a href="!genZip?seasonId="+seasonId+"&departmentId="+w[0]]生成压缩包[/@]
        [#else]
          [@b.a href="!genZip?seasonId="+seasonId+"&departmentId="+w[0]]生成压缩包[/@]
        [/#if]
      </td>
    </tr>
    [/#list]
  </tbody>
</table>
