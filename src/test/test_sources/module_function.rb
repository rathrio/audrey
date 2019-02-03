module Helpers
  module_function

  def upcase(str)
    str.upcase
  end
end

class SomeClass
  include Helpers

  def public_upcase(str)
    upcase(str)
  end
end

Helpers.upcase("foobar")
SomeClass.new.public_upcase("chello")
