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

        gradingFormula();

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
  Inputs for grading
  ###
  $ '#gradeExamination select'
    .on 'change', () ->
      gradingFormula()

  gradingFormula = () ->
    value = $ '#gradeExamination select'
      .val()

    if value == "-1"
      $ '#pabo-grade-selection'
        .show()
      $ '#other-grade-selection'
        .hide()
    else
      $ '#pabo-grade-selection'
        .hide()
      $ '#other-grade-selection'
        .show()
  ###
  Called to handle a request made in a modal dialog
  ###
  window.modalRequest = (name, xhr, status, args) ->
    if args.success
      if name.indexOf(':') == -1
        window.exModal ':' + name
      else
        window.exModal name
    else
      console.debug 'modalRequest called but no success'

###
If jQuery defined, wait till DOM loaded, then add our interactivity
###
if jQuery?
  jQuery document
    .ready () ->
      exmatrikulatorInteractivity jQuery