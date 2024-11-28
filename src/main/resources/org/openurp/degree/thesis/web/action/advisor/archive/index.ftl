[@b.head/]
  <div class="container-fluid">
  <header>
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
      <div class="brand">学生论文归档资料审核</div>
      <ul class="navbar-nav ml-auto">
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" href="#" role="button" data-toggle="dropdown" aria-expanded="false" id="term_nav">
            ${plan.season.name}
          </a>
          <div class="dropdown-menu">
            [#list plans as plan]
            [@b.a class="dropdown-item" href="!index?plan.id=${plan.id}"]${plan.season.name}[/@]
            [/#list]
          </div>
        </li>
      </ul>
    </nav>
  </header>

    [#if withoutArchives?size>0]
    <div class="card">
      <div class="card-header">
        有${withoutArchives?size}同学的缺失归档资料
      </div>
      <div class="card-body">
        <p class="card-text">
        <span style="color:red">[#list withoutArchives as w]${w.name}[#if w_has_next],[/#if][/#list]</span>
        [#list archives?keys as writer]<a href="#archive_writer_${writer.id}">${writer.std.name}</a>[#if writer_has_next]&nbsp;[/#if][/#list]
        </p>
      </div>
    </div>
    <br>
    [/#if]

    [#assign turnNames=["0","一","二","三","四"] /]
    [#list archives?keys as writer]
    <div class="card card-primary card-outline" id="archive_writer_${writer.id}">
      <div class="card-header">
        <h5 class="card-title">${writer.std.name}的论文归档资料<small><span class="text-muted ml-4">提交于:${(archives.get(writer).uploadAt?string('yyyy-MM-dd HH:mm'))!}</span></small></h5>
        <div class="card-tools">
          [@b.a class="btn btn-sm btn-outline-primary" href="!auditSetting?writer.id="+writer.id target="_blank"]审核[/@]
        </div>
      </div>
      <div class="card-body">
        <strong>材料清单</strong>
        <p class="text-muted">
        [#list docs.get(writer)?sort_by(["docType","idx"]) as doc]
          ${doc_index+1} ${doc.docType.name}&nbsp;
        [/#list]
        </p>
        <strong>审核状态</strong>
        <p class="text-muted">
          [#if archives.get(writer).confirmed!false]已经确认 ${(archives.get(writer).confirmAt?string("yyyy-MM-dd HH:mm"))!}[#else]尚未确认[/#if]
        </p>
        [#if archives.get(writer).feedback??]
        <strong>审核反馈意见</strong>
        <p class="text-muted">
          ${(archives.get(writer).feedback)!}
        </p>
        [/#if]
      </div>
    </div>
    <br>
   [/#list]
  </div>
[@b.foot/]
