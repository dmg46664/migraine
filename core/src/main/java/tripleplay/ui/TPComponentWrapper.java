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

import coza.mambo.migraine.layout.ComponentWrapper;
import coza.mambo.migraine.layout.ContainerWrapper;
import coza.mambo.migraine.layout.PlatformDefaults;
import playn.core.PlayN;
import pythagoras.f.Rectangle;

/**
 * Wraps a TriplePlay Element so that it can be laid out by the MigLayout.
 * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
 */
public class TPComponentWrapper implements ComponentWrapper 
{
	// The element that is being wrapped, and represented by, this wrapper.
	private final Element<?> wrappedElement;
	
	/**
	 * Create a new TPComponentWrapper.
	 * @param element The element to wrap.
	 */
	public TPComponentWrapper(Element<?> element)
	{
		this.wrappedElement = element;
	}

	@Override
	public final int getBaseline(int width, int height)
	{
		return -1; // See @see coza.mambo.migraine.layout.ComponentWrapper#getBaseline(int, int)
	}

	@Override
	public final float getPixelUnitFactor(boolean isHor)
	{
		return 1.0f; // @see coza.mambo.migraine.layout.ComponentWrapper#getPixelUnitFactor(boolean)
	}

	@Override
	public final int getX()
	{
		return Math.round(wrappedElement.x());
	}

	@Override
	public final int getY()
	{
		return Math.round(wrappedElement.y());
	}

	@Override
	public final int getHeight()
	{
		Rectangle tempBounds = new Rectangle();
		return Math.round(wrappedElement.bounds(tempBounds).height());
	}

	@Override
	public final int getWidth()
	{
		Rectangle tempBounds = new Rectangle();
		return Math.round(wrappedElement.bounds(tempBounds).width());
	}

	@Override
	public final int getScreenLocationX()
	{
		return Math.round(wrappedElement.x());
	}

	@Override
	public final int getScreenLocationY()
	{
		return Math.round(wrappedElement.y());
	}

	@Override
	public final int getMinimumHeight(int sz)
	{
		return getPreferredHeight(sz);
	}

	@Override
	public final int getMinimumWidth(int sz)
	{
		return getPreferredWidth(sz);
	}

	@Override
	public final int getPreferredHeight(int sz)
	{
		return Math.round(wrappedElement._preferredSize.height) ;
	}

	@Override
	public final int getPreferredWidth(int sz)
	{
		return Math.round(wrappedElement._preferredSize.width);
	}

	@Override
	public final int getMaximumHeight(int sz)
	{
		return getPreferredHeight(sz);
	}

	@Override
	public final int getMaximumWidth(int sz)
	{
		return getPreferredWidth(sz);
	}

	@Override
	public final ContainerWrapper getParent()
	{
		return new TPContainerWrapper(wrappedElement.parent());
	}

	@Override
    public final int getHorizontalScreenDPI() {
        return PlatformDefaults.getDefaultDPI();
    }

	@Override
	public final int getVerticalScreenDPI()
	{
        return PlatformDefaults.getDefaultDPI();
	}

	@Override
	public final int getScreenWidth()
	{
		return PlayN.graphics().width() ;
	}

	@Override
	public final int getScreenHeight()
	{
		return PlayN.graphics().height();
	}

	@Override
	public final boolean hasBaseline()
	{
		Rectangle wrappeElementBounds = new Rectangle();
		wrappedElement.bounds(wrappeElementBounds);
		return getBaseline(Math.round(wrappeElementBounds.width()), 
				Math.round(wrappeElementBounds.height())) > -1;
	}

	@Override
	public final String getLinkId()
	{
		return wrappedElement.toString();
	}

	@Override
	public final void setBounds(int x, int y, int width, int height)
	{
		wrappedElement.setLocation(x, y);
		wrappedElement.setSize(width, height);
	}

	@Override
	public boolean isVisible()
	{
		return wrappedElement.isVisible();
	}

	@Override
	public final int[] getVisualPadding()
	{
		return null; // see @see coza.mambo.migraine.layout.ComponentWrapper#getVisualPadding()
	}

	@Override
	public final void paintDebugOutline(boolean showVisualPadding)
	{
		// TODO: Support debug operations
	}

	@Override
	public int getComponentType(boolean disregardScrollPane)
	{
		return checkType(disregardScrollPane);
	}

	@Override
	public int getLayoutHashCode()
	{
		int width = this.getMaximumWidth(0);
		int height = this.getMaximumHeight(0);
		int hash = width + (height << 5);

		width = this.getPreferredWidth(0);
		height = this.getPreferredHeight(0);
		hash += (width << 10) + (height << 15);

		width = this.getMinimumWidth(0);
		height = this.getMinimumHeight(0);
		hash += (width << 20) + (height << 25);

		if (wrappedElement.isVisible())
			hash += 1324511;

		String id = getLinkId();
		if (id != null)
			hash += id.hashCode();

		return hash;
	}

	private int checkType(boolean disregardScrollPane)
	{
		// TODO: Support more than just Button.
		if (wrappedElement instanceof Group) return TYPE_CONTAINER;
		else if (wrappedElement instanceof Button) return TYPE_BUTTON;
		else return TYPE_UNKNOWN;
	}

	@Override
	public final int hashCode()
	{
		return wrappedElement.hashCode();
	}
	
	@Override
	public final boolean equals(Object o)
	{
		if (o instanceof ComponentWrapper == false)	return false;
		TPComponentWrapper other = (TPComponentWrapper) o;
		return wrappedElement.equals(other.wrappedElement);
	}

	@Override
	public Object getComponent() {
		return wrappedElement;
	}
}
