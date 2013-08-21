package coza.mambo.migraine.core;

import static playn.core.PlayN.*;

import playn.core.*;
import playn.core.util.Clock;
import pythagoras.f.Dimension;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.FlowLayout;

public class Migraine extends Game.Default {

	private Interface iface;

	private final static int UPDATE_RATE = 33;
	private final Clock.Source clock = new Clock.Source(UPDATE_RATE);

	public Migraine() {
		super(UPDATE_RATE); // call update every 33ms (30 times per second)
	}




	@Override
	public void init() {
		// create and add background image layer
		Image bgImage = assets().getImage("images/bg.png");
		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
		graphics().rootLayer().add(bgLayer);

		//for debugging
		_.setOffset();


		GroupLayer layer = graphics().createGroupLayer();
		iface = new Interface(); //used to take in delegate as argument
		Stylesheet sheet = SimpleStyles.newSheet();
		Root root = iface.createRoot(AxisLayout.vertical().gap(5), sheet);
		layer.add(root.layer);
		root.addStyles(Style.HALIGN.left);

		boolean useMig = true ;

		if(useMig)
		{

			MigGroup migraineButtons;
			MigLayout layout = new MigLayout();
			root.add(migraineButtons = new MigGroup(layout));
			final String e = "";
			migraineButtons.add(new Button("A"), e);
			migraineButtons.add(new Button("B"), e);
			migraineButtons.add(new Button("C"), "wrap");
			migraineButtons.add(new Button("D"), e);
			migraineButtons.add(new Button("E"), e);
		}
		else
		{
			Group group = new Group(new AxisLayout.Horizontal());
			root.add(group);
			group.add(new Button("A"));
			group.add(new Button("B"));
			group.add(new Button("C"));
		}


		PlayN.graphics().rootLayer().add(layer);
//		root.pack(400, 100) ;
		root.pack();
	}

	@Override
	public void update(int delta) {
		clock.update(delta);
		iface.update(delta);

	}

	@Override
	public void paint(float alpha) {
		// the background automatically paints itself, so no need to do anything here!
		clock.paint(alpha);
		iface.paint(clock);
	}
}
