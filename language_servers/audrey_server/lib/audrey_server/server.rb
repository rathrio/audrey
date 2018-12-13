require 'sinatra/base'
require 'redis'
require 'oj'
require 'benchmark'

class Server < Sinatra::Application
  use Rack::Logger

  REDIS = Redis.new(driver: :hiredis)

  before do
    content_type 'application/json'
  end

  get '/samples' do
    category = params.fetch('category')
    source = params.fetch('source')
    root_node_id = params.fetch('root_node_id')
    identifier = params.fetch('identifier')

    samples = REDIS.smembers('audrey_samples').map { |sample_json| Oj.load(sample_json) }
    samples.select { |s|
      s['identifier'] == identifier &&
        s['category'] == category &&
        # s['source'].end_with?(source) &&
        s['rootNodeId'] == root_node_id
    }.to_json
  end
end
