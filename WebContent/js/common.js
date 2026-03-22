(function ($) {
    $(document).ready(function () {
        $('#cssmenu > ul > li > a').click(function () {
            $('#cssmenu li').removeClass('active');
            $(this)
                .closest('#cssmenu li')
                .addClass('active');
            var checkElement = $(this).next();
            if ((checkElement.is('ul')) && (checkElement.is(':visible'))) {
                $(this)
                    .closest('#cssmenu li')
                    .removeClass('active');
                checkElement.slideUp('normal');
            }
            if ((checkElement.is('ul')) && (!checkElement.is(':visible'))) {
                $('#cssmenu ul ul:visible').slideUp('normal');
                checkElement.slideDown('normal');
            }
            if ($(this).closest('li').find('ul').children().length == 0) {
                return true;
            } else {
                return false;
            }
        });
    });
})(jQuery);

$(document).ready(function(){
  //gnb
  $('#gnb').on({
		'mouseenter focusin':function() {
			$(".gnb_bg").stop().animate({"height":"180px"},500);
			$("#gnb .depth2").stop().animate({"height":"270px"},500);
			$("#gnb .depth2 > li").on("mouseenter focusin", function(){
				$("#gnb .depth2 li").not(this).removeClass("on");
				$(this).addClass("on");
			});
		},'mouseleave focusout':function(){
			$("#gnb .depth2").stop().animate({"height":"78px"},500);
			$("#gnb .depth2 li").removeClass("on");
			$(".gnb_bg").stop().animate({"height":"0"},600);
		}
	});

// lnb
  $('#lnb li.active').addClass('open').children('ul').show();
  $('#lnb li.has_sub>a').on('click', function(){
      $(this).removeAttr('href');
      var element = $(this).parent('li');
      if (element.hasClass('open')) {
          element.removeClass('open');
          element.find('li').removeClass('open');
          element.find('ul').slideUp(200);
      }
      else {
          element.addClass('open');
          element.children('ul').slideDown(200);
          element.siblings('li').children('ul').slideUp(200);
          element.siblings('li').removeClass('open');
          element.siblings('li').find('li').removeClass('open');
          element.siblings('li').find('ul').slideUp(200);
      }
  });

  //search select
  var selectTarget = $('.search_select select');
  selectTarget.on('blur', function(){
      $(this).parent().removeClass('focus');
  });
  selectTarget.change(function(){
      var select_name = $(this).children('option:selected').text();
      $(this).siblings('label').text(select_name);
  });

  //find select
  var selectTarget2 = $('.find_select select');
  selectTarget2.on('blur', function(){
      $(this).parent().removeClass('focus');
  });
  selectTarget2.change(function(){
      var select_name = $(this).children('option:selected').text();
      $(this).siblings('label').text(select_name);
  });
  var selectTarget3 = $('.choice_select select');
  selectTarget3.on('blur', function(){
      $(this).parent().removeClass('focus');
  });
  selectTarget3.change(function(){
      var select_name = $(this).children('option:selected').text();
      $(this).siblings('label').text(select_name);
  });

	//tab
	$(".tab_content").hide();
  $(".tab_content:first").show();
	$(".tab_menu a").click(function(event) {
		event.preventDefault(); //주소에 #숨김
		$(this).parent().addClass("current");
		$(this).parent().siblings().removeClass("current");
		var tab = $(this).attr("href");
		$(".tab_content").not(tab).css("display", "none");
		$(tab).fadeIn();
	});
});

//select
jQuery(function($){
    // Common
    var select_root = $('div.fake_select');
    var select_value = $('.my_value');
    var select_a = $('div.fake_select>ul>li>a');
    var select_input = $('div.fake_select>ul>li>input[type=radio]');
    var select_label = $('div.fake_select>ul>li>label');
    // Radio Default Value
    $('div.my_value').each(function(){
        var default_value = $(this).next('.i_list').find('input[checked]').next('label').text();
        $(this).append(default_value);          });

    // Line
    select_value.bind('focusin',function(){$(this).addClass('outLine');});
    select_value.bind('focusout',function(){$(this).removeClass('outLine');});
    select_input.bind('focusin',function(){$(this).parents('div.fake_select').children('div.my_value').addClass('outLine');});
    select_input.bind('focusout',function(){$(this).parents('div.fake_select').children('div.my_value').removeClass('outLine');});
    // Show
    function show_option(){
        $(this).parents('div.fake_select:first').toggleClass('open');
    }
    // Hover
    function i_hover(){
        $(this).parents('ul:first').children('li').removeClass('hover');
        $(this).parents('li:first').toggleClass('hover');
    }
    // Hide
    function hide_option(){
        var t = $(this);
        setTimeout(function(){
            t.parents('div.fake_select:first').removeClass('open');
        }, 1);
    }
    // Set Input
    function set_label(){
        var v = $(this).next('label').text();
        $(this).parents('ul:first').prev('.my_value').text('').append(v);
        $(this).parents('ul:first').prev('.my_value').addClass('selected');
    }
    // Set Anchor
    function set_anchor(){
        var v = $(this).text();
        $(this).parents('ul:first').prev('.my_value').text('').append(v);
        $(this).parents('ul:first').prev('.my_value').addClass('selected');
    }
    // Anchor Focus Out
    $('*:not("div.fake_select a")').focus(function(){
        $('.a_list').parent('.fake_select').removeClass('open');
    });
    select_value.click(show_option);
    select_root.removeClass('open');
    select_root.mouseleave(function(){$(this).removeClass('open');});
    select_a.click(set_anchor).click(hide_option).focus(i_hover).hover(i_hover);
    select_input.change(set_label).focus(set_label);
    select_label.hover(i_hover).click(hide_option);
});

$(function(){
    $(".js-faqs dd").hide();
    $(".js-faqs dt").click(function () {
        $(this).next(".js-faqs dd").slideToggle(500).siblings("js-faqs dd").slideUp("slow");
        $(this).siblings().removeClass("expanded");
        $(this).toggleClass("expanded");
    });
});

//popup
function view_show(num) {
	var left = (( $(window).width() - $("#dispay_view"+num).width()) / 2 );
	var top = (( $(window).height() - $("#dispay_view"+num).height()) / 2 );
	$("#dispay_view"+num).css({'left':left,'top':top, 'position':'fixed'});
    document.getElementById("dispay_view"+num).style.display = "block";
    document.getElementById("layer_bg").style.display = "block";
 }
function view_hide(num) {
  document.getElementById("dispay_view"+num).style.display = "none";
  document.getElementById("layer_bg").style.display = "none";
}

function pop_show(num) {
	var left = (( $(window).width() - $("#pop_view"+num).width()) / 2 );
	var top = (( $(window).height() - $("#pop_view"+num).height()) / 2 );
	$("#pop_view"+num).css({'left':left,'top':top, 'position':'fixed'});
    document.getElementById("pop_view"+num).style.display = "block";
    document.getElementById("layer_bg").style.display = "block";
 }
function pop_hide(num) {
  document.getElementById("pop_view"+num).style.display = "none";
  document.getElementById("layer_bg").style.display = "none";
}

$(document).ready(function(){
  $('.popup_bg, .pop_layer').click(function(){
    $('.popup,popup02').css("display","none");
    $(this).css("display","none");
  });
});

 // window popup close
$(function(){
  $('.window_close').click(function(){
    window.open('about:blank', '_self').close();
  });
  
 // select script //
$(document).ready(function(){
    var selectTarget = $('.js-search-select select');
    selectTarget.on('blur', function(){
        $(this).parent().removeClass('focus');
    });
    selectTarget.change(function(){
        var select_name = $(this).children('option:selected').text();
        $(this).siblings('label').text(select_name);
    });
}); 
  
 // tab_nomal
 
 $(document).ready(function(){
    $(".js-tab-content").hide();
    $(".js-tab-content:first").show();
    $(".js-tab a").click(function(event) {
        event.preventDefault(); //주소에 #숨김
        $(this).parent().addClass("current");
        $(this).parent().siblings().removeClass("current");
        var tab = $(this).attr("href");
        $(".js-tab-content").not(tab).css("display", "none");
        $(tab).fadeIn();
    });
})  


$(function () {

    $(".tab_content").hide();
    $(".tab_content:first").show();

    $("ul.tabs li").click(function () {
        // $("ul.tabs li").removeClass("active").css("color", "#333");
        //$(this).addClass("active").css({"color": "darkred","font-weight": "bolder"});
         //$(this).addClass("active").css("color", "darkred");
        $(".tab_content").hide()
        var activeTab = $(this).attr("rel");
        $("#" + activeTab).fadeIn()
    });
});

  
});
