[@b.toolbar title="论文提交统计"]
  bar.addBack();
[/@]
[@b.messages slash="3"/]
<table class="grid-table">
  <thead class="grid-head">
    <th style="width:60px">序号</th>
    <th>院系</th>
    <th>总人数</th>
    <th>提交论文数</th>
    <th>剩余人数</th>
    <th style="width:300px">论文压缩包</th>
  </thead>
  <tbody>
    [#list writers as w]
    <tr>
      <td>${w_index+1}</td>
      <td>${w[2]}</td>
      <td>${w[3]}</td>
      <td>${papers.get(w[0])!0}</td>
      <td>[@b.a target="_blank" href="!missing?thesisPaper.writer.season.id="+seasonId+"&thesisPaper.writer.std.state.department.id="+w[0]]${w[3] - papers.get(w[0])!0}[/@]</td>
      <td>
        [#if zipTimes.get(w[0])??]
         [@b.a href="!downloadZip?seasonId="+seasonId+"&departmentId="+w[0] target="_blank"]下载(${(zipTimes.get(w[0])?string('yyyy-MM-dd HH:mm'))})[/@]&nbsp;
         [@b.a href="!genZip?seasonId="+seasonId+"&departmentId="+w[0]]生成压缩包[/@]
        [#else]
        [#if  (papers.get(w[0])!0) > 0]
        [@b.a href="!genZip?seasonId="+seasonId+"&departmentId="+w[0]]生成压缩包[/@]
        [/#if]
        [/#if]
      </td>
    </tr>
    [/#list]
  </tbody>
</table>
