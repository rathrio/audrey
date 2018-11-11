Audrey
======

**O**n **D**emand **R**untime **I**nformation.

Prerequisites
-------------

* [GraalVM](https://www.graalvm.org/downloads/)
* [Java](https://www.java.com/en/download/) (optional if you want to use GraalVM
    to build Audrey)
* [Gradle](https://gradle.org/install/)

Building
--------

Build a JAR with:

```bash
./gradlew build
```

This should have generated a JAR at `build/libs/audrey-1.0-SNAPSHOT`.

To let your GraalVM installation know about Audrey, place it in the jre tools
directory. Say, for example, your GraalVM installation can be found at
`/Users/radi/graalvm-ee-1.0.0-rc8/Contents/Home`, you should be able to do the
following from this project's root folder.

Create a folder named "audrey" in tools:

```bash
mkdir /Users/radi/graalvm-ee-1.0.0-rc8/Contents/Home/jre/tools/audrey
```

Place the JAR in there:

```
cp build/libs/audrey-1.0-SNAPSHOT.jar /Users/radi/graalvm-ee-1.0.0-rc8/Contents/Home/jre/tools/audrey/
```

or create a symbolic link in order to have GraalVM load the most recent JAR
built with `./gradlew build`:

```bash
ln -s /path/to/audrey/build/libs/audrey-1.0-SNAPSHOT.jar /Users/radi/graalvm-ee-1.0.0-rc8/Contents/Home/jre/tools/audrey/audrey.jar
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
