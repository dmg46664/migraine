package coza.mambo.migraine.core;

import static playn.core.PlayN.*;

import playn.core.*;
import playn.core.util.Clock;
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
		_.setOffset();


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
			final MigLayout layout = new MigLayout("", "[]40[]40[]");
			final MigLayout layout2 = new MigLayout("", "[]60[]60[]");
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
						migGroup.animateToNewLayout(layout2);
					else
						migGroup.animateToNewLayout(layout);

//					migGroup.getCurrentLayout().setComponentConstraints(buttons[id], "external");

					root.pack();
					migGroup.makeInvalid();
				}
			};


			for(int i = 0 ; i < 5 ; i++)
			{
				migGroup.add(buttons[i], colrow[i]);
				layout2.addLayoutComponent(buttons[i], colrow[i]);

				buttons[i].clicked().connect(new ButtonUnitSlot(i)) ;
			}

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
