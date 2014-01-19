package tripleplay.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import coza.mambo.migraine.layout.AC;
import coza.mambo.migraine.layout.CC;
import coza.mambo.migraine.layout.ComponentWrapper;
import coza.mambo.migraine.layout.ConstraintParser;
import coza.mambo.migraine.layout.Grid;
import coza.mambo.migraine.layout.LC;
import coza.mambo.migraine.layout.LayoutCallback;
import coza.mambo.migraine.layout.LayoutUtil;
import pythagoras.f.Dimension;
import tripleplay.ui.util.Insets;

/**
 * Implementation of MigLayout for TriplePlay.
 * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
 */
public class MigLayout extends Layout {
	
	// Parsed layout constraints.
	private LC _layoutConstraints = null;
	
	// Parsed column constraints.
	private AC _columnConstraints = null;
	
	// Parsed row constraints.
	private AC _rowConstraints = null;
	
	// Set to true whenever constraints are changed so that we know to re-create the grid.
	// Not used right now - we're always re-creating the grid.
	@SuppressWarnings("unused")
	private boolean _dirty = true;
	
	// Map from component under management to it's constraints.
	private transient final Map<ComponentWrapper, CC> _childElementWrapperConstraintMap = new HashMap<ComponentWrapper, CC>(8);
	
	// Representation of the grid that components are added to. Does the layout heavy lifting.
	private Grid _grid = null;
	
	/**
	 * Create a new MigLayout.
	 */
	public MigLayout() {
		this("", "", "");
	}
	
	/** 
	 * Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as "".
	 */
	public MigLayout(String layoutConstraints) {
		this(layoutConstraints, "", "");
	}
	
	/** 
	 * Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as "".
	 * @param colConstraints The constraints for the columns in the grid. <code>null</code> will be treated as "".
	 */
	public MigLayout(String layoutConstraints, String columnConstraints) {
		this(layoutConstraints, columnConstraints, "");
	}
	
	/** 
	 * Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as "".
	 * @param colConstraints The constraints for the columns in the grid. <code>null</code> will be treated as "".
	 * @param rowConstraints The constraints for the rows in the grid. <code>null</code> will be treated as "".
	 */
	public MigLayout(String layoutConstraints, String columnConstraints, String rowConstraints) {
		setLayoutConstraints(layoutConstraints);
		setColumnConstraints(columnConstraints);
		setRowConstraints(rowConstraints);
	}
	
	/** Sets the layout constraints for the layout manager instance as a String.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constraints The layout constraints as a String pr {@link net.miginfocom.layout.LC} representation. <code>null</code> is converted to <code>""</code> for storage.
	 * @throws RuntimeException if the constraint was not valid.
	 */
	public void setLayoutConstraints(Object constraints)
	{
		if (constraints == null || constraints instanceof String) {
			_layoutConstraints = ConstraintParser.parseLayoutConstraint(ConstraintParser.prepare((String) constraints));
		} else if (constraints instanceof LC) {
			_layoutConstraints = (LC) constraints;
		} else {
			throw new IllegalArgumentException("Illegal constraint type: " + constraints.getClass().toString());
		}
		_dirty = true;
	}
	
	/** Sets the column layout constraints for the layout manager instance as a String.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The column layout constraints as a String or {@link net.miginfocom.layout.AC} representation. <code>null</code> is converted to <code>""</code> for storage.
	 * @throws RuntimeException if the constraint was not valid.
	 */
	public void setColumnConstraints(Object constr)
	{
		if (constr == null || constr instanceof String) {
			_columnConstraints = ConstraintParser.parseColumnConstraints(ConstraintParser.prepare((String) constr));
		} else if (constr instanceof AC) {
			_columnConstraints = (AC) constr;
		} else {
			throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
		}
		_dirty = true;
	}
	
	/** Sets the row layout constraints for the layout manager instance as a String.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The row layout constraints as a String or {@link net.miginfocom.layout.AC} representation. <code>null</code> is converted to <code>""</code> for storage.
	 * @throws RuntimeException if the constraint was not valid.
	 */
	public void setRowConstraints(Object constr)
	{
		
		if (constr == null || constr instanceof String) {
			_rowConstraints = ConstraintParser.parseRowConstraints(ConstraintParser.prepare((String) constr));
		} else if (constr instanceof AC) {
			_rowConstraints = (AC) constr;
		} else {
			throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
		}
		_dirty = true;
	}
	
	/**
	 * Sets the layout constraint for an element that is being handled by this layout manager.
	 * @param element The element to set the constraint for.
	 * @param layoutConstraint The component constraint as a String of {@link coza.mambo.migraine.layout.CC} <code>null</code> is treated as empty string.
	 * @throws RuntimeException if the constraint was not valid.
	 * @throws IllegalArgumentException If the component is not handling the component.
	 */
	public void addLayoutComponent(Element<?> element, Object layoutConstraint) {
		TPComponentWrapper componentWrapper = new TPComponentWrapper(element);
		if (layoutConstraint == null || layoutConstraint instanceof String) {
			_childElementWrapperConstraintMap.put(componentWrapper, ConstraintParser.parseComponentConstraint(ConstraintParser.prepare((String) layoutConstraint)));
		} else if (layoutConstraint instanceof CC) {
			_childElementWrapperConstraintMap.put(componentWrapper, (CC) layoutConstraint);
		} else {
			throw new IllegalArgumentException("Constraints must be String or ComponentConstraint: " + layoutConstraint.getClass().toString());
		}
		_dirty = true;
	}
	
	public void removeLayoutComponent(Element<?> element) {
		_childElementWrapperConstraintMap.remove(element);
		_grid = null; // To clear references.
	}

	@Override
	public Dimension computeSize(Container<?> elems, float hintX, float hintY) {
        for(Element<?> element : elems) {
                element.preferredSize(hintX,hintY);
        }
        return getSizeImpl((Elements<?>)elems, LayoutUtil.PREF);
	}

	@Override
	public void layout(Container<?> elems, float left, float top, float width,
			float height) {
		recreateGridIfNeeded(elems);
		
		// Needed to compute _ldata.
		elems.computeSize(width, height);
		
		Insets insets = elems._ldata.bg.insets;
		
		int [] bounds = new int[] {
				Math.round(insets.left()),
				Math.round(insets.top()),
				Math.round(elems._size.width() - insets.left() - insets.right()),
				Math.round(elems._size.height() - insets.top() - insets.bottom())
		};
		
		if (_grid.layout(bounds,  _layoutConstraints.getAlignX(), _layoutConstraints.getAlignY(), false, true)) {
			_grid = null;
			recreateGridIfNeeded(elems);
			_grid.layout(bounds,  _layoutConstraints.getAlignX(), _layoutConstraints.getAlignY(), false, false);
		}
	}
	
	private void recreateGridIfNeeded(Container<?> elems) {
		// For now, just always re-create the grid.
		_grid = new Grid(new TPContainerWrapper(elems), _layoutConstraints, _rowConstraints, _columnConstraints, _childElementWrapperConstraintMap, new ArrayList<LayoutCallback>());
	}
	
	// Implementation method that does the job.
    private Dimension getSizeImpl(Elements<?> parent, int sizeType)
    {
            recreateGridIfNeeded((Container<?>) parent);

            Insets i = parent._ldata.bg.insets;

            int w = LayoutUtil.getSizeSafe(_grid != null ? _grid.getWidth() : null, sizeType) + Math.round(i.left() + i.right());
            int h = LayoutUtil.getSizeSafe(_grid != null ? _grid.getHeight() : null, sizeType) + Math.round(i.top() + i.bottom());

            return new Dimension(w, h);
    }
}
