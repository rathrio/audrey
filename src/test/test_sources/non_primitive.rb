def magnitude(w)
  Math.sqrt((w.x * w.x) + (w.y * w.y) + (w.z * w.z))
end

Vector = Struct.new(:x, :y, :z)
magnitude Vector.new(34, 12, 6)