#include <string>

#include <Catch.hpp>
#include <Grid.hpp>
#include <Photo.hpp>

TEST_CASE("Data") {
    SECTION("Memory copy Photo instance.") {
        Photo data1(1, 400, 300);
        Photo data2 = data1;

        REQUIRE(data1.id == data2.id);
        REQUIRE(data1.width == data2.width);
        REQUIRE(data1.height == data2.height);
        REQUIRE(data1.aspectRatio == data2.aspectRatio);
        REQUIRE(data1.aspectRatioRecip == data2.aspectRatioRecip);

        REQUIRE(data1.constrainedWidth == data2.constrainedWidth);
        REQUIRE(data1.constrainedHeight == data2.constrainedHeight);
        REQUIRE(data1.constrainedAspectRatio == data2.constrainedAspectRatio);
        REQUIRE(data1.constrainedAspectRatioRecip == data2.constrainedAspectRatioRecip);

        // Alter the data1.
        data1.setSize(200, 200);

        REQUIRE(data1.width != data2.width);
        REQUIRE(data1.height != data2.height);
        REQUIRE(data1.aspectRatio != data2.aspectRatio);
        REQUIRE(data1.aspectRatioRecip != data2.aspectRatioRecip);
    }

    SECTION("Memory copy RectF instance.") {
        RectF data1(0, 0, 600, 500);
        RectF data2 = data1;

        REQUIRE(data1.x == data2.x);
        REQUIRE(data1.y == data2.y);
        REQUIRE(data1.width == data2.width);
        REQUIRE(data1.height == data2.height);
        REQUIRE(data1.aspectRatio == data2.aspectRatio);
        REQUIRE(data1.aspectRatioRecip == data2.aspectRatioRecip);

        // Alter the data1.
        data1.setRect(50, 50, 200, 200);

        REQUIRE(data1.x != data2.x);
        REQUIRE(data1.y != data2.y);
        REQUIRE(data1.width != data2.width);
        REQUIRE(data1.height != data2.height);
        REQUIRE(data1.aspectRatio != data2.aspectRatio);
        REQUIRE(data1.aspectRatioRecip != data2.aspectRatioRecip);
    }

    SECTION("Memory copy RectSlot instance.") {
        RectSlot data1(0, 0, 600, 500, 1);
        RectSlot data2 = data1;

        REQUIRE(data1.x == data2.x);
        REQUIRE(data1.y == data2.y);
        REQUIRE(data1.width == data2.width);
        REQUIRE(data1.height == data2.height);
        REQUIRE(data1.aspectRatio == data2.aspectRatio);
        REQUIRE(data1.aspectRatioRecip == data2.aspectRatioRecip);
        REQUIRE(data1.relatedPhotoId == data2.relatedPhotoId);

        // Alter the data1.
        data1.setRect(10, 10, 200, 200);

        REQUIRE(data1.x != data2.x);
        REQUIRE(data1.y != data2.y);
        REQUIRE(data1.width != data2.width);
        REQUIRE(data1.height != data2.height);
        REQUIRE(data1.aspectRatio != data2.aspectRatio);
        REQUIRE(data1.aspectRatioRecip != data2.aspectRatioRecip);
    }
}