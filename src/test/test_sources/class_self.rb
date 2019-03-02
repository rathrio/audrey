class Dog
  class << self
    def foobar(baz)
      baz.reject(&:even?)
    end
  end
end

Dog.foobar (1..5).to_a
