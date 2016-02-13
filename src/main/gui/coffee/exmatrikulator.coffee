###
JQuery Wrapper manually added
###
exmatrikulatorInteractivity = ($) ->
  console.debug 'Assigning exmatrikulator activity...'
  overwrite = false
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

        if ["gradeInsert", "gradeInsertGroup"].indexOf(name) != -1
          gradingModal true

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
  Logic for grading modals
  ###
  gradingModal = (buttons) ->
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

    if !overwrite
      console.log "Reset form inputs"
      $ '#other-grade-selection input'
        .val ''
      $ '#pabo-grade-selection input'
        .val ''
      $ '#gradeInsertForm\\:publicComment'
        .val ''
      $ '#gradeInsertForm\\:privateComment'
        .val ''
      $ '#gradeStudent input'
        .val ''
        .focus()

      gradingModalButtons false
    else
      overwrite = false

    if buttons
      gradingModalButtons false

  gradingModalButtons = (overwrite) ->
    console.log "show overwriting?", overwrite ? "yes" : "no"

    if overwrite
      $ '#gradeInsertButtons .btn:nth-child(2)'
        .hide()
      $ '#gradeInsertButtons .btn:nth-child(3)'
        .show()
      $ '#gradeInsertOverwriteHint'
        .show()
    else
      $ '#gradeInsertButtons .btn:nth-child(2)'
        .show()
      $ '#gradeInsertButtons .btn:nth-child(3)'
        .hide()
      $ '#gradeInsertOverwriteHint'
        .hide()
  ###
  Inputs for grading
  ###
  $ '.ui-widget'
    .on 'change', '#gradeExamination select', () ->
      gradingModal false
      gradingModalButtons false

  $ '.ui-widget'
    .on 'change', '#gradeStudent input', () ->
      gradingModalButtons false
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
  Called for the events of the ajax for the student grading
  ###
  window.gradingAjaxEvent = (data) ->
    console.log data

    if data.status == "success"
      gradingModal false
    else if data.status == "begin"
      gradingModalButtons false

  window.gradingAjaxError = (err) ->
    console.log err

    if err.errorMessage == "GRADE_ALREADY_EXISTS"
      overwrite = true
      console.log "show overwriting buttons"
      gradingModalButtons true

###
If jQuery defined, wait till DOM loaded, then add our interactivity
###
if jQuery?
  jQuery document
    .ready () ->
      exmatrikulatorInteractivity jQuery