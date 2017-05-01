My Boilerplate Android
===

Welcome to my experiment playground for Android. I like **Computer Vision** and **Machine Learning** and am trying to run them on the smartphone.

> `app` and `lib-ml` is under refactoring.

Demo Apps
---

###demo-widget
The demo app using `lib-core` and `lib-widget`.

###demo-dlib
The demo app using `lib-core`, `lib-widget`, `lib-protobuf` and `lib-dlib`.

Library
---

###lib-core
The most common Java/JNI code used generally in the demo apps.

###lib-widget
The custom *View* fishpound. e.g. [ElasticDragLayout](lib-widget/src/main/java/com/my/widget/ElasticDragLayout.java)

###lib-component
The custom *Activity* or *Fragment* that could be repeatedly used.

###lib-dlib
The `dlib` port and Java/JNI functions I'm experimenting.

###lib-protobuf
A serialization/deserialization library from Google.

Temporary Folder
---

The `lib-distribution` folder is generated after first build.