require "rubygems"
require "bundler/setup"

require "test/unit"
require "fileutils"
require "net/http"
require "uri"

require "capybara"
require "capybara/dsl"
require "capybara-webkit"

def connect(str_url)
  uri = URI(str_url)
  begin
    res = Net::HTTP.get_response(uri)
  rescue
    return false;
  end

  case res
  when Net::HTTPSuccess then
    return true;
  when Net::HTTPRedirection then
    return true;
  else
    return false;
  end
end

ENV["TESTHOST"] ||= "http://0.0.0.0:8080/exmatrikulator"
Capybara.app_host = ENV["TESTHOST"]

Capybara.default_driver = :webkit
Capybara.javascript_driver = :webkit

Capybara.run_server = false
Capybara::Webkit.configure do |config|
  # Enable debug mode. Prints a log of everything the driver is doing.
  # config.debug = true

  # Allow pages to make requests to any URL without issuing a warning.
  config.allow_unknown_urls

  # Don"t load images
  config.skip_image_loading
end

mvn_job = fork do
  Dir.chdir File.join "..", "..", ".."
  FileUtils.rm_rf "exmatrikulator-db"

  $stdout.reopen("/dev/null", "w")
  $stderr.reopen("/dev/null", "w")

  exec "mvn -DskipTests package embedded-glassfish:run"
end

Process.detach mvn_job
print "Waiting for application to be deployed"

timeout = 0
until connect(Capybara.app_host)
  sleep 10
  print "."

  timeout += 1
  if timeout >= 30
    puts "\n"
    abort "ERROR: Glassfished refused to start."
  end
end

# Terminate output
puts "\n\n"

base_path = File.dirname(File.absolute_path(__FILE__))
file_glob = File.join(base_path, "/tests/*_test.rb")

Dir[file_glob].each do |file|
  require file
end
