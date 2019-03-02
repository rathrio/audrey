module A
  class B
    def method_in_b(x)
      x
    end
  end

  def self.method_in_a(x)
    x
  end
end

A.method_in_a('hi')
A::B.new.method_in_b('there')
