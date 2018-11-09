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

```bash
# Create an audrey folder in tools
mkdir /Users/radi/graalvm-ee-1.0.0-rc8/Contents/Home/jre/tools/audrey

# Place the JAR in there
cp build/libs/audrey-1.0-SNAPSHOT.jar /Users/radi/graalvm-ee-1.0.0-rc8/Contents/Home/jre/tools/audrey/
```
