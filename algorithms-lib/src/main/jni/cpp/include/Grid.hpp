#ifndef CB_ALGORITHMS_LIB_GRID_HPP
#define CB_ALGORITHMS_LIB_GRID_HPP

#ifdef _WINRT_DLL
#include <ExtDefs.h>
#endif // _WINRT_DLL

#include <iostream>
#include <string>
#include <vector>

#include <RectSlot.hpp>
#include <IProtoParcelable.hpp>
#if USE_PROTOBUF
#include <protocol/ProtoGrid.pb.h>
#endif

#define IGNORED_GRID_ID ULONG_MAX

class Grid : public IProtoParcelable<Grid> {
public:
    /**
     * Create a grid with the number of slots the same with the exact number
     * of photos and the slots are layouted in the ColxRow way.
     *
     * @param [in] cols         The number of columns.
     * @param [in] rows         The nubmer of rows.
     * @param [in] photoNum     The exact nubmer of photos.
     */
    static Grid createByColsRows(std::string name,
                                 size_t cols,
                                 size_t rows) {
        std::vector<RectSlot> slots;
        float eachWidth = 1.f / cols;
        float eachHeight = 1.f / rows;
        u_long id = 0;

        for (size_t row = 0; row < rows; ++row) {
            for (size_t col = 0; col < cols; ++col) {
                slots.push_back(RectSlot(eachWidth * col,
                                         eachHeight * row,
                                         eachWidth,
                                         eachHeight,
                                         id++));
            }
        }

        return Grid(name, slots);
    }

    u_long id;
    std::string name;
    std::vector<RectSlot> slots;

    Grid() : id(IGNORED_GRID_ID) {}

    Grid(u_long id)
            : id(id) {}

    Grid(u_long id,
         std::vector<RectSlot>& slots)
            : id(id),
              slots(slots) {}

    Grid(std::string name)
            : name(name) {}

    Grid(std::string name,
         std::vector<RectSlot>& slots)
            : name(name),
              slots(slots) {}

#if USE_PROTOBUF
    /**
     * Instantiate from the given protobuf {@code MsgGrid} instance.
     */
    virtual Grid& fromProto(const ::google::protobuf::MessageLite& p) {
        const MsgGrid& proto = dynamic_cast<const MsgGrid&>(p);
        
        id = proto.id();
        name = proto.name();
        
        for (int i = 0; i < proto.slots_size(); ++i) {
            const MsgRectSlot& inSlot = proto.slots(i);
            RectSlot outSlot = dynamic_cast<RectSlot&>(RectSlot().fromProto(proto.slots(i)));
            
            slots.push_back(outSlot);
        }
        
        return *this;
    }
    
    /**
     * Convert to a protobuf {@code MsgGrid} instance.
     */
    virtual void toProto(::google::protobuf::MessageLite* const p) {
        MsgGrid* proto = dynamic_cast<MsgGrid*>(p);
        
        // Copy primitive fields.
        proto->set_id(id);
        proto->set_name(name);
        
        // Copy slots.
        for (std::vector<RectSlot>::iterator s = slots.begin();
             s != slots.end(); ++s) {
            RectSlot& inSlot = *s;
            MsgRectSlot* outSlot = proto->add_slots();
            
            inSlot.toProto(outSlot);
        }
    }
#endif

    ///////////////////////////////////////////////////////////////////////////

    Grid& operator=(const Grid& other) {
        id = other.id;
        name = other.name;
        slots = other.slots;

        return *this;
    }

    friend std::ostream& operator<<(std::ostream& output,
                                    const Grid& data) {
        output << "Grid(\"" << data.name << ")" << std::endl;
        for (int i = 0; i < data.slots.size(); ++i) {
            output << "  #" << i << ": "
                   << data.slots[i]
                   << std::endl;
        }

        return output;
    }
};

#endif //CB_ALGORITHMS_LIB_GRID_HPP
