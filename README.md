Yext notes
=========

To rebuild our plovr jar:

    buck build plovr && \
      cp buck-out/gen/lib__plovr-lib__output/plovr-lib.jar $ALPHA/thirdparty/jars/ && \
      jar -uf $ALPHA/thirdparty/jars/plovr-lib.jar -C third-party javascript/soyutils_usegoog.js


Plovr: A Closure build tool
===========================

plovr is a build tool that dynamically recompiles JavaScript and Closure
Template code. It is designed to simplify Closure development, and to make it
more enjoyable.

[Using Plovr](http://plovr.org/docs.html)

Plovr requires Java 7 or higher.

### Downloading Plovr

You can find Plovr JARs for download 
[on the Releases page](https://github.com/bolinfest/plovr/releases)

### Building Plovr from Source

The Plovr build requires the Java Development Kit (JDK) 7 or higher, [Buck](https://buckbuild.com/), and `zip`.

When following the [Buck installation instructions](https://buckbuild.com/setup/getting_started.html),
double-check that you are following the instructions for building Java projects
and for your operating system. If you have trouble installing Buck
(which can be non-trivial on Windows), please contact the Buck team.

To test:

```
buck fetch ...
buck test
```

To build:

```
buck fetch ...
buck build plovr
```

The output of the build will be in `buck-out/gen/plovr.jar`.

### To Upgrade Closure Library

To upgrade one of Closure Library, Compiler, or Templates, go to the official repo and find the SHA digest
of the commit you want to sync to. Then run.

```
scripts/update-repository.sh closure-library sha-digest
```

Sometimes this doesn't work because `git subtree` is buggy. If nothing updates, try running:

```
scripts/update-repository.sh closure-library master
git reset --hard origin/master
scripts/update-repository.sh closure-library sha-digest
```

This will bully `git subtree` into shape.

### To Upgrade Closure Compiler or Closure Templates

The Closure Compiler and Template depenencies are managed with Maven.

Follow the instructions in [third-party/README.md](third-party/README.md).
