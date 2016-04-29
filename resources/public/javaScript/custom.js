 $(window).scroll(function() {
   if($(window).scrollTop() + $(window).height() == $(document).height()) {
       //alert("bottom!");
       u1stgames.core.loadMoreNow();
   }
});
