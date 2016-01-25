bundle install --path vendor/bundle
xvfb-run -a bundle exec
bundle exec ruby env.rb
