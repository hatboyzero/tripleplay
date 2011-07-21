//
// Triple Play - utilities for use in ForPlay-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package com.threerings.ui;

import java.util.List;
import java.util.Map;

import pythagoras.f.Dimension;
import pythagoras.f.IDimension;

/**
 * Lays out elements in a horizontal or vertical group. Separate policies are enforced for on-axis
 * and off-axis sizing.
 *
 * <p> On-axis, the available space is divided up as follows: non-stretched elements are given
 * their preferred size, and remaining space is divided up among the stretched elements
 * proportional to their configured weight (which defaults to one). If no stretched elements exist,
 * elements are aligned as specified. </p>
 *
 * <p> Off-axis sizing can be configured to either size elements to their preferred size, stretch
 * them all to a uniform size (equal to the preferred size of the largest element), or to stretch
 * them all to the size allotted to the container. When elements are not stretched to fill the size
 * allotted to the container, they may be aligned as desired. </p>
 */
public abstract class AxisLayout extends Layout
{
    /** Specifies alignments of widgets, start is left/top and end is right/bottom. */
    public static enum Align {
        START {
            public float computeOffset (float size, float extent) {
                return 0;
            }
        },
        CENTER {
            public float computeOffset (float size, float extent) {
                return (extent - size) / 2;
            }
        },
        END {
            public float computeOffset (float size, float extent) {
                return extent - size;
            }
        };

        public abstract float computeOffset (float size, float extent);
    };

    /** Specifies the off-axis layout policy. */
    public static enum Policy {
        DEFAULT {
            public float computeSize (float size, float maxSize, float extent) {
                return size;
            }
        },
        STRETCH {
            public float computeSize (float size, float maxSize, float extent) {
                return extent;
            }
        },
        EQUALIZE {
            public float computeSize (float size, float maxSize, float extent) {
                return maxSize;
            }
        };

        public abstract float computeSize (float size, float maxSize, float extent);
    };

    /** Defines axis layout constraints. */
    public static final class Constraint implements Layout.Constraint {
        public final boolean stretch;
        public final float weight;

        public Constraint (boolean stretch, float weight) {
            this.stretch = stretch;
            this.weight = weight;
        }

        public float computeSize (float size, float totalWeight, float availSize) {
            return stretch ? (availSize * weight / totalWeight) : size;
        }
    }

    /** A vertical axis layout. */
    public static class Vertical extends AxisLayout {
        /** Configures this layout's on-axis alignment to top. */
        public Vertical alignTop () {
            alignOn(Align.START);
            return this;
        }

        /** Configures this layout's on-axis alignment to bottom. */
        public Vertical alignBottom () {
            alignOn(Align.END);
            return this;
        }

        /** Configures this layout's off-axis alignment to left. */
        public Vertical alignLeft () {
            alignOff(Align.START);
            return this;
        }

        /** Configures this layout's off-axis alignment to right. */
        public Vertical alignRight () {
            alignOff(Align.END);
            return this;
        }

        @Override public Dimension computeSize (
            List<Element> elems, Map<Element, Layout.Constraint> constraints,
            float hintX, float hintY) {
            Metrics m = computeMetrics(elems, constraints, hintX, hintY);
            return new Dimension(m.maxWidth, m.totalHeight + _gap * (m.count-1));
        }

        @Override public void layout (
            List<Element> elems, Map<Element, Layout.Constraint> constraints,
            float width, float height) {

            Metrics m = computeMetrics(elems, constraints, width, height);
            float stretchHeight = Math.max(0, height - _gap * (m.count-1) - m.fixHeight);
            float y = (m.stretchCount > 0) ? 0 : _alignOn.computeOffset(m.totalHeight, height);
            for (Element elem : elems) {
                if (!elem.isVisible()) continue;
                IDimension psize = elem.getPreferredSize(width, height); // will be cached
                Constraint c = getConstraint(constraints, elem);
                float ewidth = _offPolicy.computeSize(psize.getWidth(), m.maxWidth, width);
                float eheight = c.computeSize(psize.getHeight(), m.totalWeight, stretchHeight);
                elem.resize(ewidth, eheight);
                elem.setLocation(_alignOff.computeOffset(ewidth, width), y);
                y += (eheight + _gap);
            }
        }
    }

    /** A horizontal axis layout. */
    public static class Horizontal extends AxisLayout {
        /** Configures this layout's on-axis alignment to left. */
        public Horizontal alignLeft () {
            alignOn(Align.START);
            return this;
        }

        /** Configures this layout's on-axis alignment to right. */
        public Horizontal alignRight () {
            alignOn(Align.END);
            return this;
        }

        /** Configures this layout's off-axis alignment to top. */
        public Horizontal alignTop () {
            alignOff(Align.START);
            return this;
        }

        /** Configures this layout's off-axis alignment to bottom. */
        public Horizontal alignBottom () {
            alignOff(Align.END);
            return this;
        }

        @Override public Dimension computeSize (
            List<Element> elems, Map<Element, Layout.Constraint> constraints,
            float hintX, float hintY) {
            Metrics m = computeMetrics(elems, constraints, hintX, hintY);
            return new Dimension(m.totalWidth + _gap * (m.count-1), m.maxHeight);
        }

        @Override public void layout (
            List<Element> elems, Map<Element, Layout.Constraint> constraints,
            float width, float height) {

            Metrics m = computeMetrics(elems, constraints, width, height);
            float stretchWidth = Math.max(0, width - _gap * (m.count-1) - m.fixWidth);
            float x = (m.stretchCount > 0) ? 0 : _alignOn.computeOffset(m.totalWidth, width);
            for (Element elem : elems) {
                if (!elem.isVisible()) continue;
                IDimension psize = elem.getPreferredSize(width, height); // will be cached
                Constraint c = getConstraint(constraints, elem);
                float ewidth = c.computeSize(psize.getWidth(), m.totalWeight, stretchWidth);
                float eheight = _offPolicy.computeSize(psize.getHeight(), m.maxHeight, height);
                elem.resize(ewidth, eheight);
                elem.setLocation(x, _alignOff.computeOffset(eheight, height));
                x += (ewidth + _gap);
            }
        }
    }

    /**
     * Creates a vertical axis layout with default alignments (center, center), gap (5), and
     * off-axis sizing policy (preferred size).
     */
    public static Vertical vertical () {
        return new Vertical();
    }

    /**
     * Creates a horizontal axis layout with default alignments (center, center), gap (5), and
     * off-axis sizing policy (preferred size).
     */
    public static Horizontal horizontal () {
        return new Horizontal();
    }

    /**
     * Returns a layout constraint indicating that the associated element should be stretched to
     * consume extra space, with weight 1.
     */
    public static Constraint stretched () {
        return UNIFORM_STRETCHED;
    }

    /**
     * Returns a layout constraint indicating that the associated element should be stretched to
     * consume extra space, with the specified weight.
     */
    public static Constraint stretched (float weight) {
        return new Constraint(true, weight);
    }

    /**
     * Configures the on-axis alignment of this layout.
     */
    public AxisLayout alignOn (Align align) {
        _alignOn = align;
        return this;
    }

    /**
     * Configures the off-axis alignment of this layout.
     */
    public AxisLayout alignOff (Align align) {
        _alignOff = align;
        return this;
    }

    /**
     * Configures the off-axis sizing policy for this layout.
     */
    public AxisLayout offPolicy (Policy policy) {
        _offPolicy = policy;
        return this;
    }

    /**
     * Configures this layout to stretch all elements to the available size on the off-axis.
     */
    public AxisLayout offStretch () {
        return offPolicy(Policy.STRETCH);
    }

    /**
     * Configures this layout to stretch all elements to the size of the largest element on the
     * off-axis.
     */
    public AxisLayout offEqualize () {
        return offPolicy(Policy.EQUALIZE);
    }

    /**
     * Configures the inter-element gap, in pixels.
     */
    public AxisLayout gap (int gap) {
        _gap = gap;
        return this;
    }

    protected Metrics computeMetrics (
        List<Element> elems, Map<Element, Layout.Constraint> constraints,
        float hintX, float hintY) {

        Metrics m = new Metrics();
        for (Element elem : elems) {
            if (!elem.isVisible()) continue;
            m.count++;

            IDimension psize = elem.getPreferredSize(hintX, hintY);
            float pwidth = psize.getWidth(), pheight = psize.getHeight();
            m.totalWidth += pwidth;
            m.totalHeight += pheight;
            m.maxWidth = Math.max(m.maxWidth, pwidth);
            m.maxHeight = Math.max(m.maxHeight, pheight);

            Constraint c = getConstraint(constraints, elem);
            if (c.stretch) {
                m.stretchCount++;
                m.totalWeight += c.weight;
            } else {
                m.fixWidth += pwidth;
                m.fixHeight += pheight;
            }
        }

        return m;
    }

    protected static Constraint getConstraint (Map<Element, Layout.Constraint> cmap, Element elem) {
        Constraint c = (cmap == null) ? null : (Constraint)cmap.get(elem);
        return (c == null) ? UNSTRETCHED : c;
    }

    protected static class Metrics {
        public int count;

        public float totalWidth;
        public float totalHeight;

        public float maxWidth;
        public float maxHeight;

        public float fixWidth;
        public float fixHeight;

        public int stretchCount;
        public float totalWeight;
    }

    protected Align _alignOn = Align.CENTER, _alignOff = Align.CENTER;
    protected int _gap = 5;
    protected Policy _offPolicy = Policy.DEFAULT;

    protected static final Constraint UNSTRETCHED = new Constraint(false, 1);
    protected static final Constraint UNIFORM_STRETCHED = new Constraint(true, 1);
}
