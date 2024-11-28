function scores_focus (e) {
  if($(e).val()=='0'){
    $(e).numberbox('setValue','60');
    $(e).select();
  }
}
function scores_change(){
  if(!$(this).hasClass('changed'))$(this).addClass('changed');//记录输入框是否被修改
}
function save_scores_limit(servPath){

  var arr=[];
  $('.scores_td input.changed').each(function(index,val){
    arr.push($(this).attr('xs_id')+'='+$(this).val());
  });
  if(arr.length==0){
    alert('没有可更新的数据!');
    return;
  }

  showSubmitMask('正在保存限制分数...');
  var xy_id=request('xy_id');
  if(xy_id=='')xy_id=0;

  $.post(servPath+'/ms/saveScoresLimit.htm', {xy_id:xy_id,scoresStr: arr.join(',')}, function(data) {
    var j=$.parseJSON(data);
    alert(j.msg);
    if(j.state){
      $('input.changed').removeClass('changed');
    }
    hideSubmitMask();
  });
}
//管理员获取该学院的限制分数列表,并赋值给相应的INPUT
function scores_limit_load(servPath){
  var xy_id=request('xy_id');
  if(xy_id==''){
    alert('没有学院Id,无法获取到分数限制!');
    return;
  }
  //获取该学院的限制分数列表
  $.get(servPath+'/ms/listScoresLimit.htm', {xy_id:xy_id}, function(data) {
    var j=$.parseJSON(data);
    if(!j.state){
      alert(j.msg);
      return;
    }
    var arr=j.msg.replace(']','').replace('[','').split(',');
    $.each(arr, function(index, val) {
       var myArr=val.split('=');
       var input=$('input[xs_id="'+$.trim(myArr[0])+'"]');
       $(input).val($.trim(myArr[1]));
       $(input).css('border','solid 1px #6e6e6e');
       /*$(input).numberbox('setValue',$.trim(myArr[1]));
       $(input).removeClass('changed');//清除修改的标志，和手动修改的区分开*/
    });
    //easyui-numberbox
    $('input[xs_id]').numberbox({
      min:0,max:60,onChange:scores_change
    });
  });
}
