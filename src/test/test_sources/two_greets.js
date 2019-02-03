const nicePerson = {
    greet: function(target) {
        return "Hi " + target;
    }
};

const notSoNicePerson = {
    greet: function(target) {
        return "Move along, " + target;
    }
};


nicePerson.greet("Haidar");
notSoNicePerson.greet("Spongebob");
