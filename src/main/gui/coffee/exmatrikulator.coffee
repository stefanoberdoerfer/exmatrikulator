###
JQuery Wrapper manually added
###
exmatrikulatorInteractivity = ($) ->
  ###
  Toggle: Navigation
  ###
  $ 'nav > .nav.semester-links > li > a'
    .on "click", (e) ->
      e.preventDefault()

      isActive = $ this
        .parent 'li'
        .hasClass 'active'

      if isActive then  item.removeClass 'active'
      else              item.addClass 'active'

exmatrikulatorInteractivity jQuery if jQuery?