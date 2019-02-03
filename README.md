Audrey
======

[![Build Status](https://travis-ci.com/rathrio/audrey.svg?branch=master)](https://travis-ci.com/rathrio/audrey)
[![Coverage Status](https://coveralls.io/repos/github/rathrio/audrey/badge.svg?branch=master)](https://coveralls.io/github/rathrio/audrey?branch=master)

**O**n **D**emand **R**untime **I**nformation.

Prerequisites
-------------

* [GraalVM](https://www.graalvm.org/downloads/)
* [Java](https://www.java.com/en/download/) (optional if you want to use GraalVM
    to build Audrey)
* [Gradle](https://gradle.org/install/)
* [Ruby >= 2.5](https://www.ruby-lang.org/en/documentation/installation/) and [Bundler](https://bundler.io) (for running some tools and language servers)

Building
--------

Make sure that you export an environment variable `GRAALVM` that points to your
GraalVM home folder, e.g.

```bash
export GRAALVM=/Users/radi/graalvm-ee-1.0.0-rc10/Contents/Home
```

You can then build and install Audrey with:

```bash
./gradlew install
```

Setting up sample projects
--------------------------

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
