function init() {
    function makeOlder(person) {
        person.age += 1;
        return person;
    }

    const person = { name: 'boris', age: '17' };
    makeOlder(person);
    return person;
}

init();
