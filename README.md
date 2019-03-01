Audrey
======

[![Build Status](https://travis-ci.com/rathrio/audrey.svg?branch=master)](https://travis-ci.com/rathrio/audrey)
[![Coverage Status](https://coveralls.io/repos/github/rathrio/audrey/badge.svg?branch=master)](https://coveralls.io/github/rathrio/audrey?branch=master)

**O**n **D**emand **R**untime **I**nformation.

Prerequisites
-------------

* [GraalVM](https://www.graalvm.org/downloads/)
* [Gradle](https://gradle.org/install/)
* [Redis](https://redis.io)
* [Ruby >= 2.5](https://www.ruby-lang.org/en/documentation/installation/) and [Bundler](https://bundler.io) (for running some tools and language servers)

Ensure that the environment variable `$JAVA_HOME` points to your GraalVM home
folder, e.g.

```bash
export JAVA_HOME=/Users/radi/graalvm-ee-1.0.0-rc12/Contents/Home
```

Ensure that a redis server is running, e.g.

```bash
redis-cli ping
# => PONG
```

Gathering data
--------------

1. To let your GraalVM installation at `$JAVA_HOME` know about Audrey, run:

    ```bash
    ./gradlew install
    ```

    This will build a fat JAR and place it in `$JAVA_HOME/jre/tools/audrey`.

2. Verify your installation by running

    ```bash
    $JAVA_HOME/bin/js --jvm --help:tools | grep audrey
    ```

    If you see some output describing `--audrey` flags and switches, the
    installation was successful.

3. Run your application and provide the following flags to enable data-gathering
   with Audrey:

    ```bash
    $JAVA_HOME/bin/js --jvm --audrey --audrey.Project="<my_unique_project_name>" path/to/app.js 
    ```

    For example:
    
    ```bash
    $JAVA_HOME/bin/js --jvm --audrey --audrey.Project="test" --audrey.FilterPath="add.js" projects/javascript/test_project/add.js
    ```

Consult `--jvm --help:tools` for a more exhaustive list of options.

Starting a language server
--------------------------

```bash
./gradlew startServer
```

Setting up additional test projects
-----------------------------------

The `project` directory contains sub folders for each Truffle language to test
with. By default you should find a simple test project for each of them, e.g.

```
projects/javascript
└── test_project
  └── ...

projects/ruby
└── test_project
  └── ...

projects/r
└── test_project
  └── ...
```

To pull additional sample projects to test Audrey with:

```bash
git submodule update --init --recursive
```

To update them:

```bash
git submodule update --recursive --remote
```
