# Migraine

Migraine is a MigLayout derived library for providing additional layout options for [TriplePlay widgets](https://github.com/threerings/tripleplay).

# Status

The MigLayout implementation works, however my implementation is very simple - I've removed all of the layout caching logic for instance to make the port easier for me to understand.

If you run in to issues, the best way to debug problems is to get a sample Java project running with the [original MigLayout](https://code.google.com/p/miglayout/) and step through both side-by-side to see where any discrepencies are.

Pull requests are welcome!

# Usage

To use the library in your TriplePlay enabled project, simply clone the repo and install the migrane-core jar in your local maven repository.

    cd core
    mvn install

From there you can include the library in your project by adding the following to the `pom.xml` in your `core` sub-module.

    <dependency>
      <groupId>coza.mambo.migraine</groupId>
      <artifactId>migraine-core</artifactId>
      <version>1.0-SNAPSHOT</version>
    </dependency>

# Author

I am not the original author of the project, although I have re-written the majority of the interface between TriplePlay and the MigLayout code for my own liking. Props to [Daniel Gerson](https://github.com/dmg46664) for the original work.