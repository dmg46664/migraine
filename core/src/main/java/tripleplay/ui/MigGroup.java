package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.Point;
import pythagoras.f.Vector;
import tripleplay.anim.AnimBuilder;
import tripleplay.anim.Animation;
import tripleplay.anim.Animator;

import java.util.*;

/**
 * User: Daniel
 */
public class MigGroup extends Group {


	private List<MigLayout> layouts = new ArrayList<MigLayout>();
	private MigLayout layout ;

	public MigGroup(MigLayout layout) {
		super(layout);
		this.layout = layout ;
		layouts.add(layout);
	}

	/**
	 * This is called by .preferredSize, so if the LayoutData doesn't exist yet
	 * this creates the temporary ones.
	 * @param hintX
	 * @param hintY
	 * @return
	 */
	protected Element.LayoutData createLayoutData(float hintX, float hintY) {
		return new MigLayoutData(hintX, hintY);
	}

	public void addLayout(MigLayout layout2) {
		layouts.add(layout2);
	}

	public MigLayout getCurrentLayout() {
		return layout;
	}

	public void setCurrentLayout(MigLayout currentLayout) {
		this.layout = currentLayout;

		//make sure you call invalidate BEFORE you call root.pack
		//(if root.pack is needed at all presumably for changing the
		// size of the layout to catch clicks for all sub elements)
		//because otherwise when calculating the Elements above
		//MigGroup root.pack() would have already calculated
		//the _ldata's which might have cached sizes of MigGroup
		//which will then conflict with the this._preferredSize during
		//layout phase.

		this.invalidate();
		this.root().pack();
	}

	public void makeInvalid()
	{
		this.invalidate();
	}

	public Animator getAnimator()
	{
		return this.root().iface().animator();
	}

	public void animateToNewLayout(final MigLayout layout, Button button) {
		MigLayout oldLayout = getCurrentLayout();
		setCurrentLayout(layout);

		this.preferredSize(0,0);//same as root.pack
		this.validate();
		SwitchAnim switchAnim = new SwitchAnim(oldLayout, layout);
		getAnimator().add(switchAnim).then().action(new Runnable() {
			@Override
			public void run() {
				setCurrentLayout(layout);
			}
		});

		getAnimator().tweenRotation(button.layer).from(0).to((float) Math.PI*2).in(200);

		//moving the layout back
		setCurrentLayout(oldLayout);

		this.preferredSize(0,0); //same as root.pack
		this.validate();

		//need to invalidate the original button AGAIN! because while
		makeChildrenInvalid();
	}

	public void makeChildrenInvalid() {
		for (Element<?> child : _children)
		{
			child.invalidate();
			if(child instanceof MigGroup)
			{
				((MigGroup)child).makeChildrenInvalid();
			}
		}
	}

	class SwitchAnim extends Animation
	{
		Map<Element, pythagoras.f.Vector> vecMap = new HashMap<Element, Vector>();
		Map<Element, Point> elPointMap = new HashMap<Element, Point>();

		public SwitchAnim(MigLayout startLyt, MigLayout destinationLyt)
		{
			//calculate list of diff-vectors
			Set<Element> keySet = startLyt.getCopyCacheMap().keySet();
			Map<Element, MigLayout.CopyCache> startMap = startLyt.getCopyCacheMap();
			Map<Element, MigLayout.CopyCache> destMap = destinationLyt.getCopyCacheMap();
			for(Element e : keySet)
			{
				MigLayout.CopyCache startCC = startMap.get(e);
				MigLayout.CopyCache destCC = destMap.get(e);
				Vector vec = new Vector(destCC.x - startCC.x, destCC.y - startCC.y);
				vecMap.put(e, vec);
				elPointMap.put(e, new Point(startCC.x, startCC.y));
			}
		}

		float lastTime = 0;
		/** milliseconds */
		public float duration = 200;
		public float timeElapsed = -1;

		@Override
		protected float apply(float time)
		{
			if(timeElapsed == -1)
			{
//				start();
				lastTime = time;
				timeElapsed = 0;
			}
			float deltaTime = time - lastTime;
			lastTime = time;
			timeElapsed += deltaTime;

			for(Element e : vecMap.keySet())
			{
				Vector vec = vecMap.get(e);
				float deltaTimePercent = deltaTime / duration;

				Point elP = elPointMap.get(e);
				elP.x += vec.x * deltaTimePercent;
				elP.y += vec.y * deltaTimePercent;
				e.setLocation(elP.x, elP.y);
			}

			return duration - timeElapsed;
		}
	}

	/* In triple play there is a difference between a particular layout policy (Layout)
		which is a set of constraints... and LayoutData, which is an *instantiation* of that policy.
		given the context.
	   */
	protected class MigLayoutData extends ElementsLayoutData
	{

		@Override public void layout (float left, float top, float width, float height) {
			// layout our children
			getCurrentLayout().layout(MigGroup.this, left, top, width, height);
			// layout is only called as part of revalidation, so now we validate our children
			for (Element<?> child : _children) child.validate();
		}


		/**
		 * @param hintX These are just the raw hints...?
		 * @param hintY
		 */
		public MigLayoutData(float hintX, float hintY) {
			//TODO what to do with the hints?
			//Since MigLayoutData extends Elements.LayoutData
			//a background is created and so the insets are created...

		}


		/**
		 * To see how this is called, see Element.computeSize
		 * @param hintX These hints are passed in with the insets subtracted.
		 * @param hintY
		 * @return
		 */
		@Override
		public Dimension computeSize(float hintX, float hintY) {
			Dimension results = new Dimension(0,0);

			//make the size equal to the MAXIMUM of all the sub layout sizes
			//see #3P6XG

			for(Layout lay : layouts)
			{
				Dimension d = layout.computeSize(MigGroup.this, hintX, hintY);
				if(d.width > results.width)
					results.width = d.width ;

				if(d.height > results.height)
					results.height = d.height ;

			}
			return results;
		}

//		@Override
//		public void layout (float left, float top, float width, float height) {
//			layout.layout(MigGroup.this, left, top, width, height);
//		}
	}

	public MigGroup add(Element child, Object layoutData)
	{
		add(child);
		layout.addLayoutComponent(child, layoutData);
		return this;
	}

//	/**
//	 * overriding so that we don't call clearLayoutData...
//	 * @param width
//	 * @param height
//	 * @return
//	 */
	@Override
	protected MigGroup setSize (float width, float height) {
		boolean changed = _size.width != width || _size.height != height;
		_size.setSize(width, height);
		// if we have a cached preferred size and this size differs from it, we need to clear our
		// layout data as it may contain computations specific to our preferred size

		if (_preferredSize != null && !_size.equals(_preferredSize))
		{
			clearLayoutData();
		}

		if (changed) invalidate();
		return this;
	}
}