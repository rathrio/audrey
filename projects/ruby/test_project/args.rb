def method1(a)
  a
end

def method2(*a)
  a
end

def method3(a:)
  a
end

method1('hello')
method2('hello', 'there')
method3(a: 'hello')
