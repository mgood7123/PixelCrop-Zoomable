# PixelCrop-Zoomable

An application designed to do one thing: zoom into a pixel

# Features

### Zoom View

each `Zoom Layer` consists of its own `Main Image`, and `Zoomed Image`

the `Main Image` is where you can zoom in on your picture

the `Zoomed Image` shows a preview of the current zoomed region

### Image Saving

each of the `Image Views` in the `Zoom View` can be saved to `Pictures/Zoomable` under the android external storage root
by tapping the `Export Main Image` and `Export Zoomable Image` button underneath the appropriate view

### Zoom Layer

the `Zoom Layer` helps with getting your zoom just right where more accuracy is needed

A `Zoom Layer` consists of `Zoom Layer Tabs`

each of these tabs consists of a `Zoom View`



just zoom in, then tap `Add Layer`

a new `Zoom View` will be created with its `Main Image` source set to the source of the `Zoomed Image` of the previous `Zoom View`



the `Zoom Layer` also acts as a `History View` when used correctly

where you can review your zoom history and make changes



changes made to one `Zoom View` affect all following `Zoom Views`

tap `Remove Layer` to remove the last added `Zoom View`
