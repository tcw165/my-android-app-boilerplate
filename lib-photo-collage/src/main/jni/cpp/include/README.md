Include
=======

This is the directory where the main exposed APIs are.

Protocol
--------

In a `*.proto` file, you could declare multiple `message`s. And the name of generated code will be like `YOUR_PROTO.pb.h` or `YOUR_PROTO.pb..cc`. We prefix the proto name with `Proto` and the message name with `Msg` so that the generated names won't conflict and it's intuitively to let us find the class by simple uniform prefix , `Proto` or `Msg`, when programming.

e.g. In Java, `ProtoPhoto.MsgPhoto` to reference the message, `Photo`, defined in the proto.

* `MsgPhoto` in `<protocol/ProtoPhoto.pb.h>`.
* `MsgRectF` in `<protocol/ProtoRectF.pb.h>`.
* `MsgRectSlot` in `<protocol/ProtoRectSlot.pb.h>`.
* `MsgGrid` in `<protocol/ProtoGrid.pb.h>`.

API
---

* `GridsGenerator::generate()`
* `GridsOptimizer::sort()`



