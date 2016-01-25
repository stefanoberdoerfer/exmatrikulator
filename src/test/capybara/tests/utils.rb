require "uri"

class CapybaraTestCase < Test::Unit::TestCase
  include Capybara::DSL

  def assert_path(expected)
    host_base_path = URI(Capybara.app_host).path
    unless host_base_path.empty?
      expected = host_base_path << expected
    end

    path = current_path
    if path and path.include? ";"
      path = path.slice 0, path.index(";")
    end

    assert_equal expected, path
  end

  def teardown
    Capybara.reset_sessions!
    Capybara.use_default_driver
  end
end
