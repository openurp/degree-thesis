[@b.head/]
<p>
  <h5 style="text-align:center">
  </h5>
</p>
 <div class="card">
   <div class="card-header">
      <div style="float:left">
      <h5>${depart.name} ${season.name}全日制本科生毕业论文表格</h5>
      </div>
   </div>
   <div class="card-body">
     <ul>
       <li>[@b.a href="!leaderDoc?season.id="+season.id+"&depart.id="+depart.id target="_blank"]附件1（领导小组名单）[/@]</li>
       <li>[@b.a href="!titleDoc?season.id="+season.id+"&depart.id="+depart.id target="_blank"]附件2（指导老师统计表）[/@]</li>
       <li>[@b.a href="!material?depart.id=${depart.id}&template=report_fee1.xls&name=${depart.name} 核算汇总表一.xls" target="_blank"]核算汇总表一[/@]</li>
       <li>[@b.a href="!material?depart.id=${depart.id}&template=report_fee2.xls&name=${depart.name} 核算汇总表二.xls" target="_blank"]核算汇总表二[/@]</li>
     </ul>
   </div>
 </div>
[@b.foot/]
