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
import pythagoras.f.Dimension;
import tripleplay.ui.util.Insets;

public class NewMigLayout extends Layout {
	
	// Parsed layout constraints.
	private LC _layoutConstraints = null;
	
	// Parsed column constraints.
	private AC _columnConstraints = null;
	
	// Parsed row constraints.
	private AC _rowConstraints = null;
	
	// Set to true whenever constraints are changed.
	private boolean _dirty = true;
	
	// Map from component under management to it's constraints.
	private transient final Map<ComponentWrapper, CC> _childElementWrapperConstraintMap = new HashMap<ComponentWrapper, CC>(8);
	
	// ???
	private Grid _grid = null;
	
//	// Record the current mod count so we can check if the defaults have changed and we neede to re-draw the grid.
//	private int _lastModCount = PlatformDefaults.getModCount();
	
	public NewMigLayout() {
		this("", "", "");
	}
	
	public NewMigLayout(String layoutConstraints) {
		this(layoutConstraints, "", "");
	}
	
	public NewMigLayout(String layoutConstraints, String columnConstraints) {
		this(layoutConstraints, columnConstraints, "");
	}
	
	public NewMigLayout(String layoutConstraints, String columnConstraints, String rowConstraints) {
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
		return new Dimension(1000, 10000); // Not sure if this matters.
	}

	@Override
	public void layout(Container<?> elems, float left, float top, float width,
			float height) {
		recreateGridIfNeeded(elems);
		
		// Need to compute _ldata first.
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
		
//		// Don't need to do anything if we've got nothing to check.
//		if (elems == null) { return; }
//		
//		// If the layouts have changed we need to re-draw the grid.
//		if (_dirty) { _grid = null; }
//		
//		// If the mod count has incremented, we need to re-draw the grid.
//		int currentModCount = PlatformDefaults.getModCount();
//		if (currentModCount != _lastModCount) {
//			_grid = null;
//			_lastModCount = currentModCount;
//		}
//		
//		if (elems.isSet(Element.Flag.VALID) == false) {
//			if (_lastWasInvalid == false) {
//				_lastWasInvalid = true;
//				
//				int hash = 0;
//				
//			}
//		}
		
		// For now, just always re-create the grid.
		_grid = new Grid(new TPContainerWrapper(elems), _layoutConstraints, _rowConstraints, _columnConstraints, _childElementWrapperConstraintMap, new ArrayList<LayoutCallback>());
	}
}
