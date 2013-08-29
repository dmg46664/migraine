package tripleplay.ui;

import pythagoras.f.Dimension;
import pythagoras.f.Vector;
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
	}

	public void makeInvalid()
	{
		this.invalidate();
	}

	public Animator getAnimator()
	{
		return this.root().iface().animator();
	}

	public void animateToNewLayout(MigLayout layout) {
		//record all positions of the elements of the layout
		//set the new layout
		//trigger the actual layout of the new layout
		//pop an Animation onto the Animator in order to tween
		setCurrentLayout(layout);
	}

	class Switch extends Animation
	{
		Map<Element, pythagoras.f.Vector> vecMap = new HashMap<Element, Vector>();

		public Switch(MigLayout startLyt, MigLayout destinationLyt)
		{
			//calculate list of diff-vectors
			Set<Element> keySet = startLyt.getCopyCacheMap().keySet();
			Map<Element, MigLayout.CopyCache> startMap = startLyt.getCopyCacheMap();
			Map<Element, MigLayout.CopyCache> destMap = destinationLyt.getCopyCacheMap();
			for(Element e : keySet)
			{
				MigLayout.CopyCache startCC = startMap.get(e);
				MigLayout.CopyCache destCC = destMap.get(e);
//				e.x()
//				Vector vec = new Vector(startCC._preferredSize.)
//				vecMap.put(e, )
			}
		}

		private float _position = 0;
		float lastTime = 0;
		/** milliseconds */
		public float startTime = 0 ;
		public float duration = 2000;

		@Override
		protected float apply(float time)
		{
			if (lastTime == 0)
				lastTime = time - startTime; //start position at the beginning

//			_movie.paint(time - lastTime); TODO: Do Translation
			lastTime = time ;

			if (_position > duration)//finish
			{
				reset();
				return 0;
			}

			return duration - _position ;
		}

		private void reset() {
			lastTime = 0;
			_position = 0;
		}
	}

//
//	@Override protected LayoutData createLayoutData (float hintX, float hintY) {
//		return new ElementsLayoutData();
//	}

//	protected class ElementsLayoutData extends LayoutData {
//
//
//		@Override public void layout (float left, float top, float width, float height) {
//			// layout our children
//			_layout.layout(Elements.this, left, top, width, height);
//			// layout is only called as part of revalidation, so now we validate our children
//			for (Element<?> child : _children) child.validate();
//		}
//	}

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
			Dimension results = null;
			for(Layout lay : layouts)
			{
				Dimension d = layout.computeSize(MigGroup.this, hintX, hintY);
				if (lay == layout)
					results = d;


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

	/**
	 * overriding so that we don't call clearLayoutData...
	 * @param width
	 * @param height
	 * @return
	 */
	@Override
	protected MigGroup setSize (float width, float height) {
		boolean changed = _size.width != width || _size.height != height;
		_size.setSize(width, height);
		// if we have a cached preferred size and this size differs from it, we need to clear our
		// layout data as it may contain computations specific to our preferred size

		//TODO so this is really hacky, unforetold consequences. Waiting for someone to beat me with a stick.

		if (_preferredSize != null && !_size.equals(_preferredSize))
		{
			clearLayoutData();
		}

		if (changed) invalidate();
		return this;
	}
}