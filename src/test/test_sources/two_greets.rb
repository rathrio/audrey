module NicePerson
  def self.greet(target)
    "Hi #{target}"
  end
end

module NotSoNicePerson
  def self.greet(target)
    "Move along, #{target}"
  end
end

NicePerson.greet("Haidar")
NotSoNicePerson.greet("Spongebob")