[#include "/org/openurp/degree/thesis/web/action/head.ftl"/]
<div align="center">
  <font color="red" size="5">下载材料前，请首先阅读下方提示内容。</font><br/>
[#assign extMap={"xls":'xls.gif',"xlsx":'xls.gif',"docx":"doc.gif","doc":"doc.gif","pdf":"pdf.gif","zip":"zip.gif","":"generic.gif"}]

<table cellspacing="0" cellpadding="0" border="0" width="650">
  <tr>
    <td width="80" style="vertical-align: top;color:red;text-align:right">论文题目</td>
    <td align="left">
      1、毕业论文选题必须与毕业论文正稿中的题目保持一致，因不一致而导致论文答辩不通过以及毕业论文归档资料存在瑕疵的，责任自负。
      请各位同学至迟于下载、打印毕业论文材料之前加以确认。如果不一致，请在，在[@b.a href="subject"]选题结果[/@]下对论文题目进行修改。
    </td>
  </tr>
  <tr>
    <td style="vertical-align: top;color:red;text-align:right">打印事项</td>
    <td align="left">2、请于答辩前下载答辩记录及评分表，答辩后，扫描成PDF再上传归档</td>
  </tr>
</table>

    <table class="list_table" style="width:600px">
      <tr>
        <td class="title"  colspan="2">毕业论文材料(一)</td>
      </tr>
      <tr>
        <td class="header" width="50">序号</td>
        <td class="header">材料名称</td>
      </tr>
      <tr>
        <td class="header">1</td>
        <td class="header">
          [@b.a href='!cover' target="_blank"]毕业论文(设计)封面[/@]
          [#if docsMap['cover']??][@b.a href="!download?doc.id="+docsMap['cover'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">2</td>
        <td class="header">
         [#if commitment_confirmed]
           [@b.a href="!commitment" target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>承诺书[/@]
         [#else]
           承诺书，尚未确认
         [/#if]
         [#if docsMap['commitment']??][@b.a href="!download?doc.id="+docsMap['commitment'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">3</td>
        <td class="header">
        [@b.a href="!task" target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>任务书[/@]
        [#if docsMap['task']??][@b.a href="!download?doc.id="+docsMap['task'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">4</td>
        <td class="header">
        [@b.a href="paper!attachment" target="_blank"]<image src="${b.static_url("ems","images/file/pdf.gif")}"/>论文正文[/@]
        [#if docsMap['thesisPaper']??],[@b.a href="!download?doc.id="+docsMap['thesisPaper'].id target="_blank"]<image src="${b.static_url("ems","images/file/pdf.gif")}"/>归档版[/@][/#if]
        </td>
      </tr>
    </table>

    <table class="list_table" style="width:600px">
      <tr>
        <td class="title"  colspan="2">毕业论文材料(二)</td>
      </tr>
      <tr>
        <td class="header"  width="50">序号</td>
        <td class="header">材料名称</td>
      </tr>
      <tr>
        <td class="header">1</td>
        <td class="header">
          [#if proposal_status.id==100]
            [@b.a href="!proposal"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>开题报告[/@]
          [#else]
            开题报告尚未通过,暂时不能下载.
          [/#if]
          [#if docsMap['proposal']??][@b.a href="!download?doc.id="+docsMap['proposal'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">2</td>
        <td class="header">
          [#if guidance1_count<2]指导记录I的次数不够，暂不能下载[#else]
          [@b.a href="!guidance1"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>指导记录Ⅰ[/@]
          [/#if]
          [#if docsMap['guidance1']??][@b.a href="!download?doc.id="+docsMap['guidance1'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">3</td>
        <td class="header">
          [@b.a href="!midtermCheck"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>中期检查表[/@]
          [#if docsMap['midtermCheck']??][@b.a href="!download?doc.id="+docsMap['midtermCheck'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">4</td>
        <td class="header">
          [#if guidance2_count<2]指导记录I的次数不够，暂不能下载[#else]
          [@b.a href="!guidance2"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>指导记录Ⅱ[/@]
          [/#if]
          [#if docsMap['guidance2']??][@b.a href="!download?doc.id="+docsMap['guidance2'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">5</td>
        <td class="header">
        [@b.a href="!copyCheck"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>反抄袭检测情况记录表[/@]
        [#if docsMap['copyCheck']??][@b.a href="!download?doc.id="+docsMap['copyCheck'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      [#if blind_reviewed]
      <tr>
        <td class="header">6</td>
        <td class="header">
          [@b.a href="!blindReview"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>校外送审情况记录表[/@]
          [#if docsMap['blindReview']??][@b.a href="!download?doc.id="+docsMap['blindReview'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      [/#if]
      <tr>
        <td class="header">7</td>
        <td class="header">
          [#if advisor_reviewed]
            [@b.a href="!advisorReview"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>指导教师评分表[/@]
          [#else]
           指导教师尚未评分
          [/#if]
          [#if docsMap['advisorReview']??][@b.a href="!download?doc.id="+docsMap['advisorReview'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">8</td>
        <td class="header">
          [#if cross_reviewed]
          [@b.a href="!crossReview"  target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>评阅表[/@]
          [#else]
           交叉评阅尚未评分
          [/#if]
          [#if docsMap['crossReview']??][@b.a href="!download?doc.id="+docsMap['crossReview'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
      <tr>
        <td class="header">9</td>
        <td class="header">
          [@b.a href="!defense" target="_blank"]<image src="${b.static_url("ems","images/file/doc.gif")}"/>答辩记录及评分表[/@]
          [#if docsMap['oralDefense']??][@b.a href="!download?doc.id="+docsMap['oralDefense'].id target="_blank"]，<image src="${b.static_url("ems","images/file/pdf.gif")}"/>[/@][/#if]
        </td>
      </tr>
    </table>

    <table class="list_table" style="width:600px">
      <tr>
        <td class="title"  colspan="2">毕业论文归档材料上传</td>
      </tr>
      <tr>
        <td class="header">答辩结束后，请将上述材料进行签名（需要签名的），制作成PDF，上传归档。</td>
      </tr>
      <tr>
        <td class="header">
          [@b.a href="sign" class="btn btn-sm btn-outline-primary"]学生签名[/@]
          [#if archive?? && archive.uploadAt??]

            [#if archive.archived!false]论文已审核归档，无需再上传[#else]
              [@b.a href="!uploadForm" class="btn btn-sm btn-outline-primary"]开始上传[/@]
              [#if archive.confirmed??]
                指导老师审核${archive.confirmed?string('通过',"不通过")}&nbsp;
              [#else]
                指导老师尚未审批
              [/#if]
              [#if archive.feedback??]<span style="color:red">${archive.feedback!}</span>[/#if]
            [/#if]

          [#else]
            [@b.a href="!uploadForm" class="btn btn-sm btn-outline-primary"]开始上传[/@]
          [/#if]
        </td>
      </tr>
    </table>
  </div>
[@b.foot/]
