require 'sinatra/base'
require 'redis'
require 'oj'
require 'benchmark'

class Server < Sinatra::Application
  use Rack::Logger

  REDIS = Redis.new(driver: :hiredis)
  AUDREY_PROJECT_ID = ENV['AUDREY_PROJECT_ID']

  SAMPLES = REDIS.smembers("audrey:#{AUDREY_PROJECT_ID}:samples").map { |sample_json| Oj.load(sample_json) }

  before do
    content_type 'application/json'
  end

  get '/samples' do
    category = params['category']
    source = params['source']
    root_node_id = params['root_node_id']
    identifier = params['identifier']

    if category.nil? && source.nil? && root_node_id.nil? && identifier.nil?
      return SAMPLES.to_json
    end

    SAMPLES.select do |s|
      s['identifier'] == identifier &&
        s['category'] == category &&
        # s['source'].end_with?(source) &&
        s['rootNodeId'] == root_node_id
    end.to_json
  end
end
