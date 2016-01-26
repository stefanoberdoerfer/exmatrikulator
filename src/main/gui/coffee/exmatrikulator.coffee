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
  ###
  Show/hide modals
  ###
  window.exModal = ->
    for name in arguments
      # If name starts with :, hide the modal
      if name.indexOf(':') == 0
        modal = name.substring 1

        PF modal
          .hide()
      else
        PF name
          .show()

    return false

exmatrikulatorInteractivity jQuery if jQuery?