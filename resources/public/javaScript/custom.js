function biggerInitial() {
    $(document).ready(function() {
        if ($(document).height() == $(window).height()) {
               u1stgames.core.loadMoreNow();
            }
    });
}

 $(window).scroll(function() {
   if($(window).scrollTop() + ($(window).height() + 300) > $(document).height()) {
       u1stgames.core.loadMoreNow();
   }
});
