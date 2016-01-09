###
JQuery Wrapper manually added
###
(($) ->
  ###
  Activate tooltips
  ###
  $('data-toggle="tooltip"').tooltip
    container: 'body'

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
  Toggle: Show/hide help
  ###
  $ '.show-help'
    .on 'click', ->
      $ this
        .hide()

      $ this
        .next '.help'
        .show()

  $ '.hide-help'
    .on 'click', ->
      help = $ this
        .parents '.help'

      help
        .hide()
      help
        .prev '.show-help'
        .show()
) jQuery