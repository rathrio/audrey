def foobar(x)
  if x.even?
    return 42
  end

  x + 42
end

(0..100).each { |i| foobar i }
