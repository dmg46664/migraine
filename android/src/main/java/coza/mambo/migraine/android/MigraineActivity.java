package coza.mambo.migraine.android;

import playn.android.GameActivity;
import playn.core.PlayN;

import coza.mambo.migraine.core.Migraine;

public class MigraineActivity extends GameActivity {

  @Override
  public void main(){
    PlayN.run(new Migraine());
  }
}
