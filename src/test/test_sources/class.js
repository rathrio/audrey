class Person {
    constructor(name) {
        this.name = name;
    }

    sayHiTo(otherPerson) {
        return `Hi ${otherPerson.name}, my name is ${this.name}`;
    }
}

const person1 = new Person("Boris");
const person2 = new Person("Haidar");
person1.sayHiTo(person2);
