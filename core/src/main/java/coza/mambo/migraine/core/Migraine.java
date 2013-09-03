package coza.mambo.migraine.core;

import static playn.core.PlayN.*;

import playn.core.*;
import playn.core.util.Clock;
import pythagoras.f.Dimension;
import react.UnitSlot;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

public class Migraine extends Game.Default {

	private Interface iface;

	private final static int UPDATE_RATE = 33;
	private final Clock.Source clock = new Clock.Source(UPDATE_RATE);
	private Route route;

	public Migraine() {
		super(UPDATE_RATE); // call update every 33ms (30 times per second)
	}


	enum Route {
		TRIPLEPLAY, MIGLAYOUT, MIGRAINE
	};



	@Override
	public void init() {
		// create and add background image layer
		Image bgImage = assets().getImage("images/bg.png");
		ImageLayer bgLayer = graphics().createImageLayer(bgImage);
		graphics().rootLayer().add(bgLayer);

		//for debugging
		_.initialize();


		GroupLayer layer = graphics().createGroupLayer();
		iface = new Interface(); //used to take in delegate as argument
		Stylesheet sheet = SimpleStyles.newSheet();
		final Root root = iface.createRoot(AxisLayout.vertical().gap(5), sheet);
		layer.add(root.layer);
		root.addStyles(Style.HALIGN.left);



		/*===================================*/
		//change this enum to see the Miglayout vs migraine examples.
		/*===================================*/
		route = Route.MIGRAINE;



		if (route == Route.MIGRAINE)
		{
			final MigLayout layout = new MigLayout("", "50[]40[]40[]", "50[][]");
			final MigLayout layout2 = new MigLayout("", "50[]200[]200[]", "50[]200[]");
			final MigGroup migGroup = new MigGroup(layout);
			root.add(migGroup);
			final String e = "";

			final Button buttons[] = new Button[5];
			buttons[0] = new Button("A");
			buttons[1] = new Button("B");
			buttons[2] = new Button("C");
			buttons[3] = new Button("D");
			buttons[4] = new Button("E");

			String colrow[] = new String[5];
			colrow[0] = "cell 0 0";
			colrow[1] = "cell 1 0";
			colrow[2] = "cell 2 0";
			colrow[3] = "cell 0 1";
			colrow[4] = "cell 1 1";

			int[] colours = new int[5];
			colours[0] = Color.rgb(255, 0, 255);
			colours[1] = Color.rgb(255, 255, 0);
			colours[2] = Color.rgb(0, 255, 255);
			colours[3] = Color.rgb(255, 100, 100);
			colours[4] = Color.rgb(0, 100, 100);

			class ButtonUnitSlot extends UnitSlot
			{
				int id ;

				public ButtonUnitSlot(int id)
				{
					this.id = id;
				}
				@Override
				public void onEmit() {
					if (migGroup.getCurrentLayout() == layout)
						migGroup.animateToNewLayout(layout2, buttons[id]);
					else
						migGroup.animateToNewLayout(layout, buttons[id]);

//					migGroup.getCurrentLayout().setComponentConstraints(buttons[id], "external");

//
				}
			};


			for(int i = 0 ; i < 5 ; i++)
			{
				migGroup.add(buttons[i], colrow[i]);
				layout2.addLayoutComponent(buttons[i], colrow[i]);

				buttons[i].clicked().connect(new ButtonUnitSlot(i)) ;
				setStyle(buttons[i], colours[i]);
				final Button button = buttons[i];
				button.setConstraint(new Layout.Constraint(){

					@Override
					public void adjustPreferredSize(Dimension psize, float hintX, float hintY) {
						super.adjustPreferredSize(psize, hintX, hintY);
						//needed to spin the button around the origin.
						//but know that this affects it's positioning.
						button.layer.setOrigin(psize.width()/2, psize.height()/2);
					}
				});
			}
			migGroup.makeChildrenInvalid();

			migGroup.addLayout(layout2);

		}
		else if(route == Route.MIGLAYOUT)
		{
			MigGroup migraineButtons;
			MigLayout layout = new MigLayout("", "[]40[]40[]");
			root.add(migraineButtons = new MigGroup(layout));
			final String e = "";
			migraineButtons.add(new Button("A"), e);
			migraineButtons.add(new Button("BC"), e);
			migraineButtons.add(new Button("C"), "wrap");
			migraineButtons.add(new Button("DEF"), e);
			migraineButtons.add(new Button("E"), e);
		}
		else //tripleplay
		{
			Group group = new Group(new AxisLayout.Horizontal());
			root.add(group);
			group.add(new Button("A"));
			group.add(new Button("B"));
			group.add(new Button("C"));
		}

		PlayN.graphics().rootLayer().add(layer);
		root.pack();
	}

	public void setStyle(Button button, int bgColor)
	{
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
		// the background automatically paints itself, so no need to do anything here!
		clock.paint(alpha);
		iface.paint(clock);
	}
}
