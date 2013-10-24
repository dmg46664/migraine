package coza.mambo.migraine.java;

import coza.mambo.migraine.core._;
import playn.core.PlayN;
import playn.java.JavaPlatform;

import coza.mambo.migraine.core.Migraine;

public class MigraineJava {

  public static void main(String[] args) {
	  //for debugging
//	  _.initialize();

    JavaPlatform.Config config = new JavaPlatform.Config();
    // use config to customize the Java platform, if needed
    JavaPlatform.register(config);
    PlayN.run(new Migraine());
  }
}
