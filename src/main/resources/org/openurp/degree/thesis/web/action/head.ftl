[@b.head/]
<link href="${b.base}/static/stylesheets/style.css?ver=4" rel="stylesheet" type="text/css">
<script>
bg.load(["my97"]);
</script>
[#macro display longName width]
<div style="overflow: hidden;text-overflow: ellipsis;width: ${width}px;display: inline-block;white-space: nowrap;" title="${longName}">${longName}</div>
[/#macro]
