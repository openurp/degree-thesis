[@b.head/]
<div class="container-fluid">
     [@b.messages slash="3"/]
    <div class="card card-primary card-outline" id="archive_writer_${writer.id}">
      <div class="card-header">
        <h5 class="card-title">${writer.std.name}的论文归档资料<small><span class="text-muted ml-4">提交于:${archive.uploadAt?string('yyyy-MM-dd HH:mm')}</span></small></h5>
          <span class="text-muted">
            指导老师审核状态:[#if archive.confirmed!false]已经确认 ${(archive.confirmAt?string("yyyy-MM-dd HH:mm"))!}[#else]尚未确认[/#if]
          </span>
          [#if archive.feedback??]
          <span class="text-muted">
            审核反馈意见:${archive.feedback!}
          </span>
          [/#if]
          <div class="card-tools">
            [@b.a href="!downloadArchive?archive.id="+archive.id target="_blank"]<i class="fa-solid fa-file-zipper"></i> 打包下载[/@]
          </div>
      </div>
      <div class="card-body">
        <div class="row">
          <div class="col-md-5">
            <strong>论文题目</strong>
            <p class="text-muted">${paper.title}</p>
          </div>
          <div class="col-md-2">
            <strong>关键词</strong>
            <p class="text-muted">${paper.keywords}</p>
          </div>
          <div class="col-md-2">
            <strong>研究方向</strong>
            <p class="text-muted">${paper.researchField}</p>
          </div>
          <div class="col-md-1">
            <strong>语言</strong>
            <p class="text-muted">${paper.language.name}</p>
          </div>
          <div class="col-md-1">
            <strong>导师</strong>
            <p class="text-muted">${(archive.writer.advisor.name)!} [#if (archive.writer.advisor.mobile)??]<i class="fa-solid fa-phone" title="${archive.writer.advisor.mobile}"></i>[/#if]</p>
          </div>
          <div class="col-md-1">
            <strong>作者</strong>
            <p class="text-muted">${(archive.writer.name)!} [#if archive.writer.mobile??]<i class="fa-solid fa-phone" title="${(archive.writer.mobile)!}"></i>[/#if]</p>
          </div>
        </div>

        <div class="row" style="border-top: 1px solid rgba(0,0,0,.125);">
          <div class="col-md-2" style="border-right: 1px solid rgba(0,0,0,.125);">
            <ul style="padding-left: 0px;list-style: none;">
            [#assign orderedDocs = docs?sort_by(["docType","idx"])/]
            [#list orderedDocs as doc]
              <li style="display: flex;align-items: center">
                <div style="flex: 1">[@b.a href="!preview?doc.id="+doc.id target="pdf_preview_area"]${doc_index+1} ${doc.docType.name}[/@]</div>
                <div>[@b.a href="!downloadDoc?doc.id="+doc.id target="_blank"]<i class="fa-solid fa-paperclip"></i>[/@]</div>
              </li>
            [/#list]
            </ul>
            [@b.form name="auditForm" action="!audit" theme="list"]
              [@b.radios label="是否通过"  name="archive.archived" value=archive.archived! items="1:common.yes,0:common.no" required="true"/]
              [@b.textarea label="反馈意见" name="archive.feedback" value="archive.feedback" value=archive.feedback! rows="5" placeholder="不通过时，反馈意见"/]
              [@b.formfoot]
                <input type="hidden" name="archive.id" value="${archive.id}"/>
                [@b.submit/]
              [/@]
            [/@]
          </div>

          <div class="col-md-10" >
           <iframe scrolling="auto" id="pdf_preview_area" style="min-height: 700px;" width="100%" frameborder="0" src="${b.url('!preview?doc.id='+orderedDocs?first.id)}"></iframe>
          </div>
        </div>

      </div>
    </div>

</div>
[@b.foot/]
