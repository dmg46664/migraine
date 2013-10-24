package tripleplay.ui;
/*
 * License (BSD):
 * ==============
 *
 * Copyright (c) 2004, Mikael Grev, MiG InfoCom AB. (miglayout (at) miginfocom (dot) com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 * Neither the name of the MiG InfoCom AB nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific
 * prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * @version 1.0
 * @author Mikael Grev, MiG InfoCom AB
 *         Date: 2006-sep-08
 */

import coza.mambo.migraine.layout.*;
import pythagoras.f.Dimension;
import pythagoras.f.Rectangle;
import tripleplay.ui.util.Insets;

//import javax.swing.*;
//import javax.swing.Timer;
import java.io.*;
import java.util.*;

/** A very flexible layout manager.
 * <p>
 * Read the documentation that came with this layout manager for information on usage.
 */
public final class MigLayout extends Layout
{
	// ******** Instance part ********

	/** The component to string constraints mappings.
	 */
	private final Map<Element, Object> scrConstrMap = new IdentityHashMap<Element, Object>(8);

	/** Hold the serializable text representation of the constraints.
	 */
	private Object layoutConstraints = "", colConstraints = "", rowConstraints = "";    // Should never be null!

	// ******** Transient part ********

	private transient ContainerWrapper cacheParentW = null;

	private transient final Map<ComponentWrapper, CC> ccMap = new HashMap<ComponentWrapper, CC>(8);
	private transient Timer debugTimer = null;

	private transient LC lc = null;
	private transient AC colSpecs = null, rowSpecs = null;
	private transient Grid grid = null;
	private transient int lastModCount = PlatformDefaults.getModCount();
	private transient int lastHash = -1;
	private transient Dimension lastInvalidSize = null;
	private transient boolean lastWasInvalid = false;  // Added in 3.7.1. May have regressions
	private transient Dimension lastParentSize = null;

	private transient ArrayList<LayoutCallback> callbackList = null;

	private transient boolean dirty = true;

	public CopyCache getCopyCache(Element c) {
		CopyCache cc = copyCacheMap.get(c);
		if(c != null && c._preferredSize != null
				&& c._preferredSize != cc._preferredSize)
			cc._preferredSize = c._preferredSize;
		return cc;
	}

	public ComponentWrapper createNewComponentWrapper(Element<?> element) {
		TPComponentWrapper component = new TPComponentWrapper(element, this);
		if(!this.copyCacheMap.containsKey(element))
			this.copyCacheMap.put(element, new CopyCache());
		return component;
	}

	public Map<Element, CopyCache> getCopyCacheMap() {
		return copyCacheMap;
	}

	/**
	 * The reason CopyCache wraps _preferedSize is
	 * because we weren't sure whether there were
	 * other variables to wrap.
	 */
	protected class CopyCache
	{
		public Dimension _preferredSize;
		public int x;
		public int y;



		public void setLocation(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

//	private final CopyCache copyCache = new CopyCache();
	private Map<Element, CopyCache> copyCacheMap = new HashMap<Element, CopyCache>();

	/** Constructor with no constraints.
	 */
	public MigLayout()
	{
		this("", "", "");
	}


	/**
	 * This should call .preferredSize on all elements as well as using the
	 * returned values of .preferredSize to calculate the size of this component and
	 * return.
	 * @param elems
	 * @param hintX
	 * @param hintY
	 * @return
	 */
	@Override
	public Dimension computeSize(Container<?> elems, float hintX, float hintY) {

		//make sure computsize has been called on all sub elements
		for(Element element : elems)
		{
			element.preferredSize(hintX,hintY);
		}

		return getSizeImpl((Elements)elems, LayoutUtil.PREF);
	}

	/**
	 *
	 * @param elems
	 * @param left
	 * @param top
	 * @param width
	 * @param height
	 */
	@Override
	public void layout(Container<?> elems, float left, float top, float width, float height) {
		this.layoutContainer((Elements)elems);
	}

	/** Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as "".
	 */
	public MigLayout(String layoutConstraints)
	{
		this(layoutConstraints, "", "");
	}

	/** Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as "".
	 * @param colConstraints The constraints for the columns in the grid. <code>null</code> will be treated as "".
	 */
	public MigLayout(String layoutConstraints, String colConstraints)
	{
		this(layoutConstraints, colConstraints, "");
	}

	/** Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as "".
	 * @param colConstraints The constraints for the columns in the grid. <code>null</code> will be treated as "".
	 * @param rowConstraints The constraints for the rows in the grid. <code>null</code> will be treated as "".
	 */
	public MigLayout(String layoutConstraints, String colConstraints, String rowConstraints)
	{
		setLayoutConstraints(layoutConstraints);
		setColumnConstraints(colConstraints);
		setRowConstraints(rowConstraints);
	}

	/** Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as an empty constraint.
	 */
	public MigLayout(LC layoutConstraints)
	{
		this(layoutConstraints, null, null);
	}

	/** Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as an empty constraint.
	 * @param colConstraints The constraints for the columns in the grid. <code>null</code> will be treated as an empty constraint.
	 */
	public MigLayout(LC layoutConstraints, AC colConstraints)
	{
		this(layoutConstraints, colConstraints, null);
	}

	/** Constructor.
	 * @param layoutConstraints The constraints that concern the whole layout. <code>null</code> will be treated as an empty constraint.
	 * @param colConstraints The constraints for the columns in the grid. <code>null</code> will be treated as an empty constraint.
	 * @param rowConstraints The constraints for the rows in the grid. <code>null</code> will be treated as an empty constraint.
	 */
	public MigLayout(LC layoutConstraints, AC colConstraints, AC rowConstraints)
	{
		setLayoutConstraints(layoutConstraints);
		setColumnConstraints(colConstraints);
		setRowConstraints(rowConstraints);
	}

	/** Returns layout constraints either as a <code>String</code> or {@link coza.mambo.migraine.layout.LC} depending what was sent in
	 * to the constructor or set with {@link #setLayoutConstraints(Object)}.
	 * @return The layout constraints either as a <code>String</code> or {@link coza.mambo.migraine.layout.LC} depending what was sent in
	 * to the constructor or set with {@link #setLayoutConstraints(Object)}. Never <code>null</code>.
	 */
	public Object getLayoutConstraints()
	{
		return layoutConstraints;
	}

	/** Sets the layout constraints for the layout manager instance as a String.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The layout constraints as a String pr {@link coza.mambo.migraine.layout.LC} representation. <code>null</code> is converted to <code>""</code> for storage.
	 * @throws RuntimeException if the constraint was not valid.
	 */
	public void setLayoutConstraints(Object constr)
	{
		if (constr == null || constr instanceof String) {
			constr = ConstraintParser.prepare((String) constr);
			lc = ConstraintParser.parseLayoutConstraint((String) constr);
		} else if (constr instanceof LC) {
			lc = (LC) constr;
		} else {
			throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
		}
		layoutConstraints = constr;
		dirty = true;
	}

	/** Returns the column layout constraints either as a <code>String</code> or {@link coza.mambo.migraine.layout.AC}.
	 * @return The column constraints either as a <code>String</code> or {@link coza.mambo.migraine.layout.AC} depending what was sent in
	 * to the constructor or set with {@link #setColumnConstraints(Object)}. Never <code>null</code>.
	 */
	public Object getColumnConstraints()
	{
		return colConstraints;
	}

	/** Sets the column layout constraints for the layout manager instance as a String.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The column layout constraints as a String or {@link coza.mambo.migraine.layout.AC} representation. <code>null</code> is converted to <code>""</code> for storage.
	 * @throws RuntimeException if the constraint was not valid.
	 */
	public void setColumnConstraints(Object constr)
	{
		if (constr == null || constr instanceof String) {
			constr = ConstraintParser.prepare((String) constr);
			colSpecs = ConstraintParser.parseColumnConstraints((String) constr);
		} else if (constr instanceof AC) {
			colSpecs = (AC) constr;
		} else {
			throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
		}
		colConstraints = constr;
		dirty = true;
	}

	/** Returns the row layout constraints either as a <code>String</code> or {@link coza.mambo.migraine.layout.AC}.
	 * @return The row constraints either as a <code>String</code> or {@link coza.mambo.migraine.layout.AC} depending what was sent in
	 * to the constructor or set with {@link #setRowConstraints(Object)}. Never <code>null</code>.
	 */
	public Object getRowConstraints()
	{
		return rowConstraints;
	}

	/** Sets the row layout constraints for the layout manager instance as a String.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The row layout constraints as a String or {@link coza.mambo.migraine.layout.AC} representation. <code>null</code> is converted to <code>""</code> for storage.
	 * @throws RuntimeException if the constraint was not valid.
	 */
	public void setRowConstraints(Object constr)
	{
		if (constr == null || constr instanceof String) {
			constr = ConstraintParser.prepare((String) constr);
			rowSpecs = ConstraintParser.parseRowConstraints((String) constr);
		} else if (constr instanceof AC) {
			rowSpecs = (AC) constr;
		} else {
			throw new IllegalArgumentException("Illegal constraint type: " + constr.getClass().toString());
		}
		rowConstraints = constr;
		dirty = true;
	}

	/** Returns a shallow copy of the constraints map.
	 * @return A  shallow copy of the constraints map. Never <code>null</code>.
	 */
	public Map<Element, Object> getConstraintMap()
	{
		return new IdentityHashMap<Element, Object>(scrConstrMap);
	}

	/** Sets the constraints map.
	 * @param map The map. Will be copied.
	 */
	public void setConstraintMap(Map<Element, Object> map)
	{
		scrConstrMap.clear();
		ccMap.clear();
		for (Map.Entry<Element, Object> e : map.entrySet())
			setComponentConstraintsImpl(e.getKey(), e.getValue(), true);
	}

	/** Returns the component constraints as a String representation. This string is the exact string as set with {@link }
	 * or set when adding the component to the parent component.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param comp The component to return the constraints for.
	 * @return The component constraints as a String representation or <code>null</code> if the component is not registered
	 * with this layout manager. The returned values is either a String or a {@link coza.mambo.migraine.layout.CC}
	 * depending on what constraint was sent in when the component was added. May be <code>null</code>.
	 */
	public Object getComponentConstraints(Element comp)
	{
//		synchronized(comp.parent().getTreeLock()) {
		//TODO not synchronized no more???
			return scrConstrMap.get(comp);
//		}
	}

	/** Sets the component constraint for the component that already must be handled by this layout manager.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The component constraints as a String or {@link coza.mambo.migraine.layout.CC}. <code>null</code> is ok.
	 * @param comp The component to set the constraints for.
	 * @throws RuntimeException if the constraint was not valid.
	 * @throws IllegalArgumentException If the component is not handling the component.
	 */
	public void setComponentConstraints(Element comp, Object constr)
	{
		setComponentConstraintsImpl(comp, constr, false);
	}

	/** Sets the component constraint for the component that already must be handled by this layout manager.
	 * <p>
	 * See the class JavaDocs for information on how this string is formatted.
	 * @param constr The component constraints as a String or {@link coza.mambo.migraine.layout.CC}. <code>null</code> is ok.
	 * @param comp The component to set the constraints for.
	 * @param noCheck Doe not check if the component is handled if true
	 * @throws RuntimeException if the constraint was not valid.
	 * @throws IllegalArgumentException If the component is not handling the component.
	 */
	private void setComponentConstraintsImpl(Element comp, Object constr, boolean noCheck)
	{
		Group parent = (Group) comp.parent();
		//TODO removed locking, hopefully nothing breaks.
//		synchronized(parent != null ? parent.getTreeLock() : new Object()) { // 3.7.2. No sync if not added to a hierarchy. Defeats a NPE.
			if (noCheck == false && scrConstrMap.containsKey(comp) == false)
				throw new IllegalArgumentException("Component must already be added to parent!");

			ComponentWrapper cw = createNewComponentWrapper(comp);

			if (constr == null || constr instanceof String) {
				String cStr = ConstraintParser.prepare((String) constr);

				scrConstrMap.put(comp, constr);
				ccMap.put(cw, ConstraintParser.parseComponentConstraint(cStr));

			} else if (constr instanceof CC) {

				scrConstrMap.put(comp, constr);
				ccMap.put(cw, (CC) constr);

			} else {
				throw new IllegalArgumentException("Constraint must be String or ComponentConstraint: " + constr.getClass().toString());
			}

			dirty = true;
//		}
	}

	/** Returns if this layout manager is currently managing this component.
	 * @param c The component to check. If <code>null</code> then <code>false</code> will be returned.
	 * @return If this layout manager is currently managing this component.
	 */
	public boolean isManagingComponent(Element c)
	{
		return scrConstrMap.containsKey(c);
	}

	/** Adds the callback function that will be called at different stages of the layout cylce.
	 * @param callback The callback. Not <code>null</code>.
	 */
	public void addLayoutCallback(LayoutCallback callback)
	{
		if (callback == null)
			throw new NullPointerException();

		if (callbackList == null)
			callbackList = new ArrayList<LayoutCallback>(1);

		callbackList.add(callback);
	}

	/** Removes the callback if it exists.
	 * @param callback The callback. May be <code>null</code>.
	 */
	public void removeLayoutCallback(LayoutCallback callback)
	{
		if (callbackList != null)
			callbackList.remove(callback);
	}

	/** Sets the debugging state for this layout manager instance. If debug is turned on a timer will repaint the last laid out parent
	 * with debug information on top.
	 * <p>
	 * Red fill and dashed red outline is used to indicate occupied cells in the grid. Blue dashed outline indicate
	 * component bounds set.
	 * <p>
	 * Note that debug can also be set on the layout constraints. There it will be persisted. The value set here will not. See the class
	 * JavaDocs for information.
	 * @param parentW The parent to set debug for.
	 * @param b <code>true</code> means debug is turned on.
	 */
	private void setDebug(final ComponentWrapper parentW, boolean b)
	{
		//TODO
//		if (b && (debugTimer == null || debugTimer.getDelay() != getDebugMillis())) {
//			if (debugTimer != null)
//				debugTimer.stop();
//
//			ContainerWrapper pCW = parentW.getParent();
//			final Element parent = pCW != null ? (Element) pCW.getComponent() : null;
//
//			debugTimer = new Timer(getDebugMillis(), new MyDebugRepaintListener());
//
//			if (parent != null) {
//				SwingUtilities.invokeLater(new Runnable() {
//					public void run() {
//						Group p = (Group) parent.parent();
//						if (p != null) {
//							if (p instanceof JComponent) {
//								((JComponent) p).revalidate();
//							} else {
//								parent.invalidate();
//								p.validate();
//							}
//						}
//					}
//				});
//			}
//
//			debugTimer.setInitialDelay(100);
//			debugTimer.start();
//
//		} else if (!b && debugTimer != null) {
//			debugTimer.stop();
//			debugTimer = null;
//		}
	}

	/** Returns the current debugging state.
	 * @return The current debugging state.
	 */
	private boolean getDebug()
	{
		return debugTimer != null;
	}

	/** Returns the debug millis. Combines the value from {@link coza.mambo.migraine.layout.LC#getDebugMillis()} and {@link coza.mambo.migraine.layout.LayoutUtil#getGlobalDebugMillis()}
	 * @return The combined value.
	 */
	private int getDebugMillis()
	{
		int globalDebugMillis = LayoutUtil.getGlobalDebugMillis();
		return globalDebugMillis > 0 ? globalDebugMillis : lc.getDebugMillis();
	}

	/** Check if something has changed and if so recreate it to the cached objects.
	 * @param parent The parent that is the target for this layout manager.
	 */
	private void checkCache(Elements parent)
	{
		if (parent == null)
			return;

		if (dirty)
			grid = null;

		cleanConstraintMaps(parent);

		// Check if the grid is valid
		int mc = PlatformDefaults.getModCount();
		if (lastModCount != mc) {
			grid = null;
			lastModCount = mc;
		}


		if (parent.isSet(Element.Flag.VALID) == false) {
			if (lastWasInvalid == false) {
				lastWasInvalid = true;

				int hash = 0;
				boolean resetLastInvalidOnParent = false; // Added in 3.7.3 to resolve a timing regression introduced in 3.7.1
				for (ComponentWrapper wrapper : ccMap.keySet()) {
					Element component = (Element) wrapper.getComponent();
					//TODO doesn't support dynamically adjustable area.
//					if (component instanceof JTextArea || component instanceof JEditorPane)
//						resetLastInvalidOnParent = true;

					component.preferredSize(0,0);//TODO, right arguments?

					hash ^= wrapper.getLayoutHashCode();
					hash += 285134905;
				}
				if (resetLastInvalidOnParent)
					resetLastInvalidOnParent(parent);

				if (hash != lastHash) {
					grid = null;
					lastHash = hash;
				}

				Dimension ps = parent._size ;


				if (lastInvalidSize == null || !lastInvalidSize.equals(ps)) {
					if (grid != null)
						grid.invalidateContainerSize();
					lastInvalidSize = ps;
				}
			}
		} else {
			lastWasInvalid = false;
		}

		ContainerWrapper par = checkParent(parent);

		setDebug(par, getDebugMillis() > 0);

		if (grid == null)
			grid = new Grid(par, lc, rowSpecs, colSpecs, ccMap, callbackList);

		dirty = false;
	}

	/** Checks so all components in ccMap actually exist in the parent's collection. Removes
	 * any references that don't.
	 * @param parent The parent to compare ccMap against. Never null.
	 */
	HashSet<Element> parentCompSet = new HashSet<Element>() ;
	private void cleanConstraintMaps(Elements parent)
	{
		parentCompSet.clear();
		for(int i = 0 ; i < parent._children.size(); i++)
		{
			Element element = parent.childAt(i);
			parentCompSet.add(element);
		}
		Iterator<Map.Entry<ComponentWrapper, CC>> it = ccMap.entrySet().iterator();
		while(it.hasNext()) {
			Element c = (Element) it.next().getKey().getComponent();
			if (parentCompSet.contains(c) == false) {
				it.remove();
				scrConstrMap.remove(c);
			}
		}
	}

	/**
	 * @since 3.7.3
	 */
	private void resetLastInvalidOnParent(Elements parent)
	{
		while (parent != null) {
			Layout layoutManager = parent._layout;
			if (layoutManager instanceof MigLayout) {
				((MigLayout) layoutManager).lastWasInvalid = false;
			}
			parent = (Group) parent.parent();
		}
	}

	private ContainerWrapper checkParent(Elements parent)
	{
		if (parent == null)
			return null;

		if (cacheParentW == null || cacheParentW.getComponent() != parent)
			cacheParentW = new TPContainerWrapper(parent);

		return cacheParentW;
	}

	private long lastSize = 0;


	public void layoutContainer(final Elements parent)
	{
		//TODO getting rid of the syncs makes me nervous...
//		synchronized(parent.getTreeLock()) {
			checkCache(parent);


			Insets i = parent._ldata.bg.insets;

			Rectangle rectangle = new Rectangle();
			parent.bounds(rectangle);
			int[] b = new int[] {
					Math.round(i.left()),
					Math.round(i.top()),
					Math.round(rectangle.width() - i.left() - i.right()),
					Math.round(rectangle.height() - i.top() - i.bottom())
			};

			if (grid.layout(b, lc.getAlignX(), lc.getAlignY(), getDebug(), true)) {
				grid = null;
				checkCache(parent);
				grid.layout(b, lc.getAlignX(), lc.getAlignY(), getDebug(), false);
			}

			long newSize = grid.getHeight()[1] + (((long) grid.getWidth()[1]) << 32);
			if (lastSize != newSize) {
				lastSize = newSize;
				final ContainerWrapper containerWrapper = checkParent(parent);

				//TODO can't handle layouts when window is not visible
				adjustWindowSize(containerWrapper);
//				Window win = ((Window) SwingUtilities.getAncestorOfClass(Window.class, (Component)containerWrapper.getComponent()));
//				if (win != null) {
//				   if (win.isVisible()) {
//					   SwingUtilities.invokeLater(new Runnable() {
//						   public void run() {
//							   adjustWindowSize(containerWrapper);
//						   }
//					   });
//				   } else {
//					   adjustWindowSize(containerWrapper);
//				   }
//				}
			}
			lastInvalidSize = null;
//		}
	}

	/** Checks the parent window/popup if its size is within parameters as set by the LC.
	 * @param parent The parent who's window to possibly adjust the size for.
	 */
	private void adjustWindowSize(ContainerWrapper parent)
	{

		//assume for now that the window size is not adjustable....

//		BoundSize wBounds = lc.getPackWidth();
//		BoundSize hBounds = lc.getPackHeight();
//
//		if (wBounds == BoundSize.NULL_SIZE && hBounds == BoundSize.NULL_SIZE)
//			return;
//
//		Group packable = getPackable((Element) parent.getComponent());
//
//		if (packable != null) {
//
//			Element pc = (Element) parent.getComponent();
//
//			Elements c = pc instanceof Group ? (Group) pc : pc.parent();
//			for (; c != null; c = c.parent()) {
//				Layout layout = c._layout;
////				if (layout instanceof BoxLayout || layout instanceof OverlayLayout)
////					((Layout) layout).invalidateLayout(c);
//			}
//
//			Dimension prefSize = packable._preferredSize;
//			int targW = constrain(checkParent(packable), Math.round(packable._size.width),
//					Math.round(prefSize.width), wBounds);
//			int targH = constrain(checkParent(packable), Math.round(packable._size.height),
//					Math.round(prefSize.height), hBounds);
//
//			Point p = packable.isShowing() ? packable.getLocationOnScreen() : packable.getLocation();
//
//			int x = Math.round(p.x - ((targW - packable.getWidth()) * (1 - lc.getPackWidthAlign())));
//			int y = Math.round(p.y - ((targH - packable.getHeight()) * (1 - lc.getPackHeightAlign())));
//
//			if (packable instanceof JPopupMenu) {
//				JPopupMenu popupMenu = (JPopupMenu) packable;
//				popupMenu.setVisible(false);
//				popupMenu.setPopupSize(targW, targH);
//				Component invoker = popupMenu.getInvoker();
//				Point popPoint = new Point(x, y);
//				SwingUtilities.convertPointFromScreen(popPoint, invoker);
//				((JPopupMenu) packable).show(invoker, popPoint.x, popPoint.y);
//
//				packable.setPreferredSize(null); // Reset preferred size so we don't read it again.
//
//			} else {
//				packable.setBounds(x, y, targW, targH);
//			}
//		}
	}

	/** Returns a high level window or popup to pack, if any.
	 * @return May be null.
	 */
	private Group getPackable(Element comp)
	{
//		JPopupMenu popup = findType(JPopupMenu.class, comp);

		//TODO just get top group for now
		return findType(Group.class, comp);



//		if (popup != null) { // Lightweight/HeavyWeight popup must be handled separately
//			Group popupComp = popup;
//			while (popupComp != null) {
//				if (popupComp.getClass().getName().contains("HeavyWeightWindow"))
//					return popupComp; // Return the heavyweight window for normal processing
//				popupComp = popupComp.parent();
//			}
//			return popup; // Return the JPopup.
//		}
//
//		return findType(Window.class, comp);
	}

	public static <E> E findType(Class<E> clazz, Element comp)
	{
		while (comp != null && !clazz.isInstance(comp))
			comp = comp.parent();

		return (E) comp;
	}


	private int constrain(ContainerWrapper parent, int winSize, int prefSize, BoundSize constrain)
	{
		if (constrain == null)
			return winSize;

		int retSize = winSize;
		UnitValue wUV = constrain.getPreferred();
		if (wUV != null)
			retSize = wUV.getPixels(prefSize, parent, parent);

		retSize = constrain.constrain(retSize, prefSize, parent);

		return constrain.getGapPush() ? Math.max(winSize, retSize) : retSize;
	}

	public Dimension minimumLayoutSize(Group parent)
	{
		//TODO sync
//		synchronized(parent.getTreeLock()) {
			return getSizeImpl(parent, LayoutUtil.MIN);
//		}
	}

	public Dimension preferredLayoutSize(Group parent)
	{
//		synchronized(parent.getTreeLock()) {
           if (lastParentSize == null || !parent._size.equals(lastParentSize)) {
               for (ComponentWrapper wrapper : ccMap.keySet()) {
                   Element c = (Element) wrapper.getComponent();

	               //TODO can't handle dynamic aspect ratios or growable sizes
//                   if (c instanceof JTextArea || c instanceof JEditorPane ||
//		                   (c instanceof JComponent &&
//				                   Boolean.TRUE.equals(((JComponent)c).getClientProperty("migLayout.dynamicAspectRatio")))) {
//                       layoutContainer(parent);
//                       break;
//                   }
               }
           }

           lastParentSize = parent._size;
           return getSizeImpl(parent, LayoutUtil.PREF);
//		}
	}

	public Dimension maximumLayoutSize(Group parent)
	{
		return new Dimension(Short.MAX_VALUE, Short.MAX_VALUE);
	}

	// Implementation method that does the job.
	private Dimension getSizeImpl(Elements parent, int sizeType)
	{
		checkCache(parent);

		Insets i = parent._ldata.bg.insets;

		int w = LayoutUtil.getSizeSafe(grid != null ? grid.getWidth() : null, sizeType) + Math.round(i.left() + i.right());
		int h = LayoutUtil.getSizeSafe(grid != null ? grid.getHeight() : null, sizeType) + Math.round(i.top() + i.bottom());

		return new Dimension(w, h);
	}

	public float getLayoutAlignmentX(Group parent)
	{
		return lc != null && lc.getAlignX() != null ? lc.getAlignX().getPixels(1, checkParent(parent), null) : 0;
	}

	public float getLayoutAlignmentY(Group parent)
	{
		return lc != null && lc.getAlignY() != null ? lc.getAlignY().getPixels(1, checkParent(parent), null) : 0;
	}

	public void addLayoutComponent(Element comp, Object constraints)
	{

		//TODO sync
//		synchronized(comp.getParent().getTreeLock()) {
			setComponentConstraintsImpl(comp, constraints, true);
//		}
	}

	public void removeLayoutComponent(Element comp)
	{
		//TODO more nervousnous
//		synchronized(comp.parent().getTreeLock()) {
			scrConstrMap.remove(comp);
			ccMap.remove(new TPComponentWrapper(comp));
			grid = null; // To clear references
//		}
	}

	public void invalidateLayout(Group target)
	{
//		if (lc.isNoCache())  // Commented for 3.5 since there was too often that the "nocache" was needed and the user did not know.
		dirty = true;

		// the validity of components is maintained automatically.
	}

	// ************************************************
	// Persistence Delegate and Serializable combined.
	// ************************************************

	//TODO recondsider persistence stuff (implements Externalizable)
//	private Object readResolve() throws ObjectStreamException
//	{
//		return LayoutUtil.getSerializedObject(this);
//	}
//
//	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
//	{
//		LayoutUtil.setSerializedObject(this, LayoutUtil.readAsXML(in));
//	}
//
//	public void writeExternal(ObjectOutput out) throws IOException
//	{
//		if (this.getClass() == MigLayout.class)
//			LayoutUtil.writeAsXML(out, this);
//	}
//
//	private class MyDebugRepaintListener implements Runnable
//	{
//		public void actionPerformed(ActionEvent e)
//		{
//			if (grid != null) {
//				Component comp = (Component) grid.getContainer().getComponent();
//				if (comp.isShowing()) {
//					grid.paintDebug();
//					return;
//				}
//			}
//			debugTimer.stop();
//			debugTimer = null;
//		}
//	}

}