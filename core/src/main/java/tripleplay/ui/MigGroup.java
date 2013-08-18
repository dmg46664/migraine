package tripleplay.ui;

/**
 * User: Daniel
 */
public class MigGroup extends Group {


	private final MigLayout layout;

	public MigGroup(MigLayout layout) {
		super(layout);
		this.layout = layout ;
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
