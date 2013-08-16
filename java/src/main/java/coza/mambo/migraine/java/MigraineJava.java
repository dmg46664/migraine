package coza.mambo.migraine.java;

import playn.core.PlayN;
import playn.java.JavaPlatform;

import coza.mambo.migraine.core.Migraine;

public class MigraineJava {

  public static void main(String[] args) {
    JavaPlatform.Config config = new JavaPlatform.Config();
    // use config to customize the Java platform, if needed
    JavaPlatform.register(config);
    PlayN.run(new Migraine());
  }
}
