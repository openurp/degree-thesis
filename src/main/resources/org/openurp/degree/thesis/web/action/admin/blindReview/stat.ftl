[@b.toolbar title="盲审论文提交统计"]
  bar.addBack();
[/@]
[@b.messages slash="3"/]
<table class="grid-table">
  <thead class="grid-head">
    <th style="width:60px">序号</th>
    <th>院系</th>
    <th>人数</th>
    <th style="width:300px">论文压缩包</th>
  </thead>
  <tbody class="grid-body">
    [#list papers as w]
    <tr>
      <td>${w_index+1}</td>
      <td>${w[1]}</td>
      <td>${w[2]}</td>
      <td>
        [#if zipTimes.get(w[0])??]
         [@b.a href="!downloadZip?seasonId="+seasonId+"&departmentId="+w[0] target="_blank"]下载(${(zipTimes.get(w[0])?string('yyyy-MM-dd HH:mm'))})[/@]&nbsp;
         [@b.a href="!genZip?seasonId="+seasonId+"&departmentId="+w[0]]生成压缩包[/@]
        [#else]
        [@b.a href="!genZip?seasonId="+seasonId+"&departmentId="+w[0]]生成压缩包[/@]
        [/#if]
      </td>
    </tr>
    [/#list]
  </tbody>
</table>
