function validateExp(exp,id,message)
{
  if(!exp.test($("#"+id).val()))
  {
    $("#"+id+"Error").html(message);
    return false;
  }else{
    $("#"+id+"Error").html('');
    return true;
  }

}
