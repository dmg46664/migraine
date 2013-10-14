package tripleplay.ui;

import pythagoras.f.Dimension;

/**
 * A Group that knows how to use a MigLayout.
 * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
 */
public class MigGroup extends Group {

	// The layout currently being used.
	private MigLayout _layout;
	
	/**
	 * Create a new MigGroup with the given Layout.
	 * @param layout The layout to use.
	 */
	public MigGroup(MigLayout layout) {
		super(layout);
		_layout = layout;
	}

	@Override
	public MigGroup add(Element<?>... children) {
		super.add(children);
		for (Element<?> child : children) {
			_layout.addLayoutComponent(child, "");
		}
		return this;
	}

	@Override
	public MigGroup add(int index, Element<?> child) {
		super.add(index, child);
		_layout.addLayoutComponent(child, "");
		return this;
	}
	
	/**
	 * Add an element to the group, specifying it's Mig constraints.
	 * @param child The element to add.
	 * @param constraint It's constraints.
	 * @return The parent group.
	 */
	public MigGroup add(Element<?> child, Object constraint) {
		add(child);
		_layout.addLayoutComponent(child, constraint);
		return this;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected Element.LayoutData createLayoutData(float hintX, float hintY) {
		return new NewMigLayoutData(hintX, hintY);
	}
	
	/* 
	 * In triple play there is a difference between a particular layout policy
	 * (Layout) which is a set of constraints... and LayoutData, which is an 
	 * *instantiation* of that policy. given the context.
	 */
	protected class NewMigLayoutData extends ElementsLayoutData {

		@Override 
		public void layout (float left, float top, float width, float height) {
			// Layout our children.
			_layout.layout(MigGroup.this, left, top, width, height);
			// Layout is only called as part of revalidation, so now we validate our children.
			for (Element<?> child : _children) child.validate();
		}
	
		public NewMigLayoutData(float hintX, float hintY) {}
	
		/**
		 * To see how this is called, see Element.computeSize
		 * @param hintX These hints are passed in with the insets subtracted.
		 * @param hintY These hints are passed in with the insets subtracted.
		 * @return
		 */
		@Override
		public Dimension computeSize(float hintX, float hintY) {
			return _layout.computeSize(MigGroup.this, hintX, hintY);
		}
	}
}
