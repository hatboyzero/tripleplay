//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import playn.core.PlayN;

import pythagoras.f.IDimension;

/**
 * The root of a display hierarchy. An application can have one or more roots, but they should not
 * overlap and will behave as if oblivious to one another's existence.
 */
public class Root extends Elements<Root>
{
    /**
     * Sizes this root element to its preferred size.
     */
    public Root pack () {
        IDimension psize = preferredSize(0, 0);
        setSize(psize.width(), psize.height());
        return this;
    }

    /**
     * Sizes this root element to the specified width and its preferred height.
     */
    public Root packToWidth (float width) {
        IDimension psize = preferredSize(width, 0);
        setSize(width, psize.height());
        return this;
    }

    /**
     * Sizes this root element to the specified height and its preferred width.
     */
    public Root packToHeight (float height) {
        IDimension psize = preferredSize(0, height);
        setSize(psize.width(), height);
        return this;
    }

    /**
     * Sets the size of this root element.
     */
    public Root setSize (float width, float height) {
        _size.setSize(width, height);
        invalidate();
        return this;
    }

    /**
     * Sets the size of this root element and its translation from its parent.
     */
    public Root setBounds (float x, float y, float width, float height) {
        setSize(width, height);
        setLocation(x, y);
        return this;
    }

    protected Root (Interface iface, Layout layout, Stylesheet sheet) {
        super(layout);
        setStylesheet(sheet);
        _iface = iface;
    }

    @Override protected Root root () {
        return this;
    }

    protected final Interface _iface;
    protected boolean _valid;
    protected Element<?> _active;
}
