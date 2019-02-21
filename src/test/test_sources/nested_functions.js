function outer() {
    function inner(person) {
        person.age += 1;
        return person;
    }

    const person = { name: 'boris', age: '17' };
    inner(person);
    return person;
}

outer();
