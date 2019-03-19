# frozen_string_literal: true

require 'benchmark'

class Person
  def initialize(name)
    @name = name
  end

  def say_hi_to(other_person)
    "Hi, #{other_person.name}, my name is #{name}"
  end

  def name
    @name.upcase
  end
end

t = Benchmark.realtime do
  100_000.times do
    p1 = Person.new('Haidar')
    p2 = Person.new('Boris')

    p1.say_hi_to p2
  end
end

puts t
