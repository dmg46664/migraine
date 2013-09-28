package coza.mambo.migraine.core;

import static playn.core.PlayN.*;

import playn.core.*;
import playn.core.util.Clock;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

public class Migraine extends Game.Default {

	private Interface iface;
	private final static int UPDATE_RATE = 33;
	private final Clock.Source clock = new Clock.Source(UPDATE_RATE);
	private Route route;
	
	enum Route {
		TRIPLEPLAY, MIGLAYOUT;
	};

	public Migraine() {
		super(UPDATE_RATE); // call update every 33ms (30 times per second)
	}

	@Override
	public void init() {
		// create and add background image layer
		Image bgImage = assets().getImage("images/bg.png");
		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
		graphics().rootLayer().add(bgLayer);

		GroupLayer layer = graphics().createGroupLayer();
		iface = new Interface(); //used to take in delegate as argument
		Stylesheet sheet = SimpleStyles.newSheet();
		final Root root = iface.createRoot(AxisLayout.vertical().gap(5), sheet);
		layer.add(root.layer);
		root.addStyles(Style.HALIGN.left);

		// Change this enum to see the Miglayout vs TriplePlay examples.
		route = Route.MIGLAYOUT;

		if (route == Route.MIGLAYOUT) {
			MigGroup migraineButtons;
			MigLayout layout = new MigLayout("", "[]40[]40[]");
			root.add(migraineButtons = new MigGroup(layout));
			final String e = "";
			migraineButtons.add(new Button("A"), e);
			migraineButtons.add(new Button("BC"), e);
			migraineButtons.add(new Button("C"), "wrap");
			migraineButtons.add(new Button("DEF"), e);
			migraineButtons.add(new Button("E"), e);
		} else /* TriplePlay */ {
			Group group = new Group(new AxisLayout.Horizontal());
			root.add(group);
			group.add(new Button("A"));
			group.add(new Button("B"));
			group.add(new Button("C"));
		}

		PlayN.graphics().rootLayer().add(layer);
		root.pack();
	}

	public void setStyle(Button button, int bgColor) {
		int ulColor = 0xFFEEEEEE, brColor = 0xFFAAAAAA;
		Background butBg = Background.roundRect(bgColor, 5, ulColor, 2).inset(5, 6, 1, 6);
		Background butSelBg = Background.roundRect(bgColor, 5, brColor, 2).inset(6, 5, 1, 7);
		button.addStyles(Styles.make(Style.BACKGROUND.is(butBg))
				.addSelected(Style.BACKGROUND.is(butSelBg))
				.add(Style.COLOR.is(0xFFFFFFFF))
				.add(Style.FONT.is(PlayN.graphics().createFont("Aharoni", Font.Style.PLAIN, 75)))
				.add(Style.TEXT_EFFECT.shadow)
				.add(Style.SHADOW.is(0x20000000)).add(Style.SHADOW_X.is(1f)).add(Style.SHADOW_Y.is(1f))
				.add(Style.VALIGN.center)
				.add(Button.DEBOUNCE_DELAY.is(200)));
	}

	@Override
	public void update(int delta) {
		clock.update(delta);
		iface.update(delta);
	}

	@Override
	public void paint(float alpha) {
		clock.paint(alpha);
		iface.paint(clock);
	}
}
