require_relative "utils.rb"

class ResetPasswordTest < CapybaraTestCase
  def test_a_valid_reset
    visit "/unregistered/password-reset.xhtml"
    within ".form" do
      fill_in "j_idt12:credentialUser", :with => "user@uni-bremen.de"
      click_button "Reset password"
    end

    assert_text "Email with password reset instructions has been sent"
  end

  def test_b_duplicated_reset
    visit "/unregistered/password-reset.xhtml"
    within ".form" do
      fill_in "j_idt12:credentialUser", :with => "user@uni-bremen.de"
      click_button "Reset password"
    end

    assert_text "Reset token was already created pleeas wait until it expires"
  end

  def test_invalid_user_reset
    visit "/unregistered/password-reset.xhtml"
    within ".form" do
      fill_in "j_idt12:credentialUser", :with => "passwordResetInvalid@foo.com"
      click_button "Reset password"
    end

    assert_text "Cannot create a reset token for this user"
  end
end
