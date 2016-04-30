 $(window).scroll(function() {
   if($(window).scrollTop() + ($(window).height() + 300) > $(document).height()) {
       u1stgames.core.loadMoreNow();
   }
});
