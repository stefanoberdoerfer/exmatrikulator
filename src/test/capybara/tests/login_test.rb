require_relative "utils.rb"

class LoginTest < CapybaraTestCase
  def test_valid_login
    visit "/login.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "admin@uni-bremen.de"
      fill_in "j_idt13:credentialPassword", :with => "admin"
      click_button "Login"
    end

    assert_path "/course/overview.xhtml"
  end

  def test_invalid_password
    visit "/login.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "admin@uni-bremen.de"
      fill_in "j_idt13:credentialPassword", :with => "foobar"
      click_button "Login"
    end

    assert_text "Login failed"
    assert_path "/login.xhtml"
  end

  def test_invalid_user
    visit "/login.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "foo@bar.com"
      fill_in "j_idt13:credentialPassword", :with => "foobar"
      click_button "Login"
    end

    assert_text "Login failed"
    assert_path "/login.xhtml"
  end
end
