migraine
========

Migraine is a MigLayout derived library for providing additional layout options for TriplePlay widgets.

## Reason for being

So if you read [the following thread](https://groups.google.com/forum/#!topic/playn/2g5yLWFefHU) on the playn/tripleplay forums, you'll see I needed a framework of solving the problem to layout widgets in various configurations, and then to animate between those configurations. To do this ideally with the ability to have fine grain control over the way in which they animate, while being able to utilize the traditional constraints techniques that existing layout managers afford for the various configuration. I.e. not to start laying out widgets from scratch using absolute layouts and what-not.

The problem is that most layout apis, tripleplay and miglayout included, represent their data something like the following: _[created with asciiflow](http://www.asciiflow.com/)_
```
                                                +----------+
                                     +--------->| Widget A |
                                     |          +----------+
                                     |
       +-------+       +--------+    |          +----------+
       | Group +------>| Layout +----+--------->| Widget B |
       +-------+       +--------+    |          +----------+
                                     |
                                     |          +----------+
                                     +--------->| Widget C |
                                                +----------+
```

Where the Widget is responsible for drawing/maintaining the real state on screen, as well as the cached state as determined by the layout manager. However this is implemented, the point being that the cached state and the widget is tightly coupled.

Migraine is an attempt to bolt the very capable layout manager MigLayout onto the PlayN tripleplay ui library, while at the same time decoupling the cached state and the widget as determined by the layout manager from the actual widget itself. This allows for multiple layout managers being active simultaneously, while allowing the widget to animate between them or do whatever else.

```
                                          +---------+
                                     +--->| Cache A |<-----------+
                                     |    +---------+            |
                                     |                           |
                       +--------+    |    +---------+            |
                +----->| Layout +----+--->| Cache B |            |
                |      +--------+    |    +---------+            |
                |                    |                           |    +----------+
                |                    |    +---------+            +----+ Widget A |<--+
                |                    +--->| Cache C |                 +----------+   |
    +-------+   |                         +---------+                                |
    | Group +---+                                                     +----------+   |
    +-------+   |                         +---------+            +----+ Widget B |<--+
                |                    +--->| Cache A |            |    +----------+   |
                |                    |    +---------+            |                   |
                |                    |                           |                   |
                |      +--------+    |    +---------+            |                   |
                +----->| Layout +----+--->| Cache B |<-----------+                   |
                |      +--------+    |    +---------+                                |
                |                    |                                               |
                |                    |    +---------+                                |
                |                    +--->| Cache C |                                |
                |                         +---------+                 +----------+   |
                |                                                +----+ Widget C |<--+
                |                                                |    +----------+   |
                |                                                |                   |
                |                        +-----------+           |                   |
                |                        | Animation |<----------+                   |
                |        Active          +-----------+                               |
                |      +--------+                                                    |
                +----->| Layout +----------------------------------------------------+
                       +--------+

```

(More or less).



## Usage, Implentation and Limitations Details

* It can be used as a simple MigLayout wrapper, or in it's more complex form. See above.
* Put main classes in tripleplay.ui on account of Element._preferredSize,_size,setSize(),setLocation() all being protected.
* Commented out all synchronized(comp.parent().getTreeLock()) in original wrapper as wasnt' sure what to block on in tripleplay.
* Added the class CopyCache which hackily copies the Element._prefferedSize in case switching layouts removes this
and it is needed by the new layout. An instance of CopyCache is managed by each layout separately. This is actually a
 necessary requirement of Migraine.
* Overrid Element.setSize() in MigGroup to hackily prevent setBounds() from running undesirably for our uses.