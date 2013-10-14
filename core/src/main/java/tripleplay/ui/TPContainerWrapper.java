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

/**
 * Wraps a TriplePlay Container so that it can be laid out by a MigLayout.
 * @author Aidan Nagorcka-Smith (aidanns@gmail.com)
 */
public final class TPContainerWrapper extends TPComponentWrapper implements ContainerWrapper
{
	// The container that is being wrapped.
	private Container<?> wrappedContainer;
	
	/**
	 * Create a new TPContainerWrapper.
	 * @param container The Container to be w
	 */
	public TPContainerWrapper(Container<?> container) {
		super(container);
		wrappedContainer = container;
	}

	@Override
	public ComponentWrapper[] getComponents() {
		ComponentWrapper[] children = new ComponentWrapper[wrappedContainer.childCount()];
		for (int i = 0; i < wrappedContainer.childCount(); i++) {
			children[i] = new TPComponentWrapper(wrappedContainer.childAt(i));
		}
		return children;
	}

	@Override
	public int getComponentCount() {
		return wrappedContainer.childCount();
	}

	@Override
	public boolean isLeftToRight() {
		return true;
	}

	@Override
	public void paintDebugCell(int x, int y, int width, int height) {
		// TODO: Support debugging.
	}

	@Override
	public Object getLayout() {
		return wrappedContainer;
	}
}
