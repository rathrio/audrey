class Dog
  attr_reader :name

  def initialize(name)
    @name = name
  end

  # @param person [String]
  def bark_at(person)
    "Wuff, #{person}, wuff!"
  end
end

dog = Dog.new('Fido')
dog.bark_at('Radi')