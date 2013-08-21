package tripleplay.ui;

import pythagoras.f.Dimension;

/**
 * User: Daniel
 */
public class MigGroup extends Group {


	private final MigLayout layout;

	public MigGroup(MigLayout layout) {
		super(layout);
		this.layout = layout ;
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


	/* In triple play there is a difference between a particular layout policy (Layout)
		which is a set of constraints... and LayoutData, which is an *instantiation* of that policy.
		given the context.
	   */
	protected class MigLayoutData extends ElementsLayoutData
	{

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
			return layout.computeSize(MigGroup.this, hintX, hintY);
		}

//		@Override
//		public void layout (float left, float top, float width, float height) {
//			layout.layout(MigGroup.this, left, top, width, height);
//		}
	}

	public MigGroup add(Element child, Object layoutData)
	{
		add(child);
		if (layoutData instanceof String)
			layout.addLayoutComponent((String)layoutData, child);
		else
			layout.addLayoutComponent(child, layoutData);
		return this;
	}
}
