###
JQuery Wrapper manually added
###
exmatrikulatorInteractivity = ($) ->
  console.debug 'Assigning exmatrikulator activity...'
  ###
  Toggle: Navigation
  ###
  $ '#nav .semester-links > li > a'
    .on "click", (e) ->
      e.preventDefault()

      item = $ this
        .parent 'li'

      isActive = item
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
  ###
  Dropdown toggle
  ###
  $ '.dropdown-toggle'
    .on 'click', (e) ->
      e.preventDefault()

      show = $ this
               .next '.dropdown-menu'
               .is ':visible'

      $ '.dropdown-menu'
        .hide()

      if !show
        $ this
          .next '.dropdown-menu'
          .show()

  $ '.dropdown-menu a'
    .on 'click', () ->
      $ '.dropdown-menu'
        .hide()
  ###
  Returns a function to be used in ajax requests to set the autofocus on
  the input that is surrounded by the given id
  ###
  window.autofocus = (id) ->
    return () ->
      $ '#' + id
        .find 'input'
        .first()
        .focus()

      return false

  window.autofocusGradeInsert = window.autofocus("gradeStudent")
  window.autofocusGradeGroupInsert = window.autofocus("gradeGroup")

###
If jQuery defined, wait till DOM loaded, then add our interactivity
###
if jQuery?
  jQuery document
    .ready () ->
      exmatrikulatorInteractivity jQuery