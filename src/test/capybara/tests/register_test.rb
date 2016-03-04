require_relative "utils.rb"

class RegisterTest < CapybaraTestCase
  def test_a_valid_register
    visit "/unregistered/registration.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "hurr@durr.com"

      fill_in "j_idt13:credentialPassword", :with => "foobar"
      fill_in "j_idt13:credentialPasswordConfirm", :with => "foobar"

      fill_in "j_idt13:credentialFirstName", :with => "Foo"
      fill_in "j_idt13:credentialLastName", :with => "Bar"

      click_button "Register"
    end

    assert_path "/login.xhtml"
    assert_text "Success"
  end

  def test_b_taken_email
    visit "/unregistered/registration.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "hurr@durr.com"

      fill_in "j_idt13:credentialPassword", :with => "foobar"
      fill_in "j_idt13:credentialPasswordConfirm", :with => "foobar"

      fill_in "j_idt13:credentialFirstName", :with => "Foo"
      fill_in "j_idt13:credentialLastName", :with => "Bar"

      click_button "Register"
    end

    assert_path "/unregistered/registration.xhtml"
    assert_text "There is already an exmatrikulator account for this email address"
  end

  def test_missing_fields
    visit "/unregistered/registration.xhtml"
    click_button "Register"

    assert_text "The email field is empty"
    assert_text "The password field is empty"

    assert_text "The password field is empty"
    assert_text "Please confirm the password"

    assert_text "You didn't specify your first name"
    assert_text "You didn't specify your last name"

    assert_path "/unregistered/registration.xhtml"
  end

  def test_non_matching_passwords
    visit "/unregistered/registration.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "lollolo@iaenit.org"

      fill_in "j_idt13:credentialPassword", :with => "password1"
      fill_in "j_idt13:credentialPasswordConfirm", :with => "password2"

      fill_in "j_idt13:credentialFirstName", :with => "Hurr"
      fill_in "j_idt13:credentialLastName", :with => "Durr"

      click_button "Register"
    end

    assert_text "Passwords do not match"
    assert_path "/unregistered/registration.xhtml"
  end

  def test_invalid_email
    visit "/unregistered/registration.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialUser", :with => "hurr durr @ foo bar"
      click_button "Register"
    end

    assert_text "This email address is invalid"
    assert_path "/unregistered/registration.xhtml"
  end

  def test_too_short_password
    visit "/unregistered/registration.xhtml"
    within ".form" do
      fill_in "j_idt13:credentialPassword", :with => "123"
      click_button "Register"
    end

    assert_text "The password is too short"
    assert_path "/unregistered/registration.xhtml"
  end
end
