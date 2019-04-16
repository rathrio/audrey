function add(x, y) {
  return x + y;
}

for (let i = 0; i < 1000; i++) {
  // with numbers
  add(1, 2);
}

for (let i = 0; i < 1000; i++) {
  // with strings
  add("hi", " there");
}

for (let i = 0; i < 1000; i++) {
  // because JS
  add("hi", 42);
}
