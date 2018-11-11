class Dog {
  constructur(name) {
    this.name = name;
  }

  barkAt(person) {
    return `Wuff, ${person}, wuff!`;
  }
}

dog = new Dog('Fido');
dog.barkAt('Radi');
