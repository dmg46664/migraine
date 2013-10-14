package coza.mambo.migraine.core;

import playn.core.*;
import playn.core.util.Clock;
import tripleplay.game.ScreenStack;
import tripleplay.game.UIAnimScreen;
import tripleplay.ui.*;
import tripleplay.ui.layout.AxisLayout;

/**
 * A simple example of using MigLayout in a TriplePlay application.
 * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
 */
public class Migraine extends Game.Default {
	
	/**
	 * Screen to display.
	 */
	private class DemoScreen extends UIAnimScreen {
		
		// The root of this screens UI.
		private Root _root;
		
		@Override
		public void wasAdded() {
			super.wasAdded();
			_root = iface.createRoot(AxisLayout.vertical().gap(0).offStretch(), SimpleStyles.newSheet(), layer);
			_root.setSize(width(), height());
			
			// Create a MigGroup with two columns and two rows.
			// The columns both take up 50% of the available width.
			// The top row takes 10% of the available height, while the bottom takes 90%.
			// The fill constraint makes this layout fill its container.
			MigGroup group = new MigGroup(new MigLayout("fill", "[50%][50%]", "[10%][90%]"));

			// Create four groups and add them to a specific cell in the grid of the MigLayout.
			// The "grow" constraint makes these elements fill their cells.
			Group topLeft = new Group(AxisLayout.vertical());
			topLeft.setStyles(Style.BACKGROUND.is(Background.solid(0xFFFF0000)));
			group.add(topLeft, "cell 0 0, grow");
			
			Group topRight = new Group(AxisLayout.vertical());
			topRight.setStyles(Style.BACKGROUND.is(Background.solid(0xFF00FF00)));
			group.add(topRight, "cell 0 1, grow");
			
			Group bottomLeft = new Group(AxisLayout.vertical());
			bottomLeft.setStyles(Style.BACKGROUND.is(Background.solid(0xFF0000FF)));
			group.add(bottomLeft, "cell 1 0, grow");
			
			Group bottomRight = new Group(AxisLayout.vertical());
			bottomRight.setStyles(Style.BACKGROUND.is(Background.solid(0xFF000000)));
			group.add(bottomRight, "cell 1 1, grow");
			
			_root.add(group.setConstraint(AxisLayout.stretched()));
		}
	}

	private final static int UPDATE_RATE = 50;
	private final Clock.Source _clock = new Clock.Source(UPDATE_RATE);
	
	// Use a screenstack to display our custom screen.
	private final ScreenStack _screens = new ScreenStack();
	
	public Migraine() {
		super(UPDATE_RATE);
	}

	@Override
	public void init() {
		// Push the custom screen on to the screenstack to display it.
		_screens.push(new DemoScreen());
	}

	@Override
	public void update(int delta) {
		_clock.update(delta);
		_screens.update(delta);
	}

	@Override
	public void paint(float alpha) {
		_clock.paint(alpha);
		_screens.paint(_clock);
	}
}
