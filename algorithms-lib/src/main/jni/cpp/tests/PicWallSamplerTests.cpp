#include <iostream>
#include <algorithm>

#include <Catch.hpp>
#include <Math.hpp>
#include <algorithm/pic_wall_6694305/PicWallSampler.hpp>

inline bool checkRectSize(PWTreeNode* node) {
    bool ok1 = true;
    bool ok2 = true;

    if (node->firstChild) {
        ok1 = checkRectSize(node->firstChild);
    }
    if (node->secondChild) {
        ok2 = checkRectSize(node->secondChild);
    }

    return node->actualRect.width > 0.f &&
           node->actualRect.height > 0.f &&
           ok1 && ok2;
}

inline void calculateRectBound(PWTreeNode* node,
                               float& left,
                               float& top,
                               float& right,
                               float& bottom) {
    left = std::min(left, node->actualRect.left());
    top = std::min(top, node->actualRect.top());
    right = std::max(right, node->actualRect.right());
    bottom = std::max(bottom, node->actualRect.bottom());

    if (node->firstChild) {
        calculateRectBound(node->firstChild,
                           left,
                           top,
                           right,
                           bottom);
    }
    if (node->secondChild) {
        calculateRectBound(node->secondChild,
                           left,
                           top,
                           right,
                           bottom);
    }
}

TEST_CASE("PicWall6694305") {
    // 3:2 photo.
    Photo photo0(1000, 1.5f);

    // 19:10 photo.
    Photo photo1(1001, 1.9f);

    // 3:4 photo.
    Photo photo2(1002, .75f);

    // 9:16 Photo.
    Photo photo3(1003, .56f);

    // Square Photo.
    Photo photo4(1004, 1.f);

    // 16:9 Photo.
    Photo photo5(1005, 1.7f);

    // Some real photo.
    Photo photo6(1006, 1.33333334f);

    // Some real photo.
    Photo photo7(1007, 0.9788972f);

    // Some real photo.
    Photo photo8(1008, 0.9917355f);

    // Some real photo.
    Photo photo9(1009, 0.89669424f);

    // Some real photo.
    Photo photo10(1010, 1.4444444f);

    SECTION("The sampler takes Photo, {1.7f}, and put it into a canvas with 1.f.") {
        RectF canvas(1.f, 1.f);

        std::vector<Photo> photos;
        photos.push_back(photo5);

        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        PWTreeNode* outcome = sampler.sampleTree(canvas,
                                                 photos,
                                                 DEFAULT_ERR_TOLERANCE_RATE,
                                                 DEFAULT_MAX_OPT_ITERATIONS,
                                                 DEFAULT_MAX_SAMPLE_REPEAT);

        // The outcome should be a NULL
        REQUIRE_FALSE(outcome);
    }

    SECTION("The sampler takes Photo, {1.f}, and put it into a canvas with 1.f.") {
        RectF canvas(1.f, 1.f);

        std::vector<Photo> photos;
        photos.push_back(photo4);

        PicWallSampler sampler(LAYOUT_CENTER_INSIDE,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        PWTreeNode* outcome = sampler.sampleTree(canvas,
                                                 photos,
                                                 DEFAULT_ERR_TOLERANCE_RATE,
                                                 DEFAULT_MAX_OPT_ITERATIONS,
                                                 DEFAULT_MAX_SAMPLE_REPEAT);

        sampler.calculateLayoutRect(outcome, 500, 500, LAYOUT_CENTER_INSIDE);
        //            PicWallSampler::printTree(outcome);

        // The outcome should be within the tolerance.
        REQUIRE(outcome->aspectRatio.actualVal ==
                Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
        REQUIRE(sampler.cost() == Approx(0.f));
    }

    SECTION("The sampler takes photos, {1.5f, 1.9f}, and put them into a canvas with 1.f.") {
        RectF canvas(1.f, 1.f);

        std::vector<Photo> photos;
        photos.push_back(photo0);
        photos.push_back(photo1);

        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        PWTreeNode* outcome = sampler.sampleTree(canvas,
                                                 photos,
                                                 DEFAULT_ERR_TOLERANCE_RATE,
                                                 DEFAULT_MAX_OPT_ITERATIONS,
                                                 DEFAULT_MAX_SAMPLE_REPEAT);
        //            PicWallSampler::printTree(outcome);

        // The outcome should be within the tolerance.
        REQUIRE(outcome->aspectRatio.actualVal ==
                Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
    }

    SECTION("The sampler takes photos, {1.5f, 1.9f, .67f, .63f, 1.f}, and put them into a canvas with 1.f.") {
        int count = 10;
        RectF canvas(500.f, 500.f);

        std::vector<Photo> photos;
        photos.push_back(photo0);
        photos.push_back(photo1);
        photos.push_back(photo2);
        photos.push_back(photo3);
        photos.push_back(photo4);

        std::vector<Grid> outGrids;
        PWTreeNode* outTree = NULL;

        // Try to get a valid tree before the maximum limit.
        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        while (outTree == NULL && count-- > 0) {
            outTree = sampler.sampleTree(canvas,
                                         photos,
                                         DEFAULT_ERR_TOLERANCE_RATE,
                                         DEFAULT_MAX_OPT_ITERATIONS,
                                         DEFAULT_MAX_SAMPLE_REPEAT);
        }
        if (outTree) {
            // Give it a real boundary.
            sampler.calculateLayoutRect(outTree,
                                        static_cast<u_long>(canvas.width),
                                        static_cast<u_long>(canvas.height),
                                        LAYOUT_SCALE_TO_FILL);
//                PicWallSampler::printTree(outcome);

            // Check the boundary.
            float left = 0, top = 0, right = 0, bottom = 0;
            calculateRectBound(outTree, left, top, right, bottom);

            // The outcome should be within the tolerance.
            REQUIRE(outTree->aspectRatio.actualVal ==
                    Approx(outTree->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
            REQUIRE(checkRectSize(outTree));
            REQUIRE(static_cast<u_long>(right - left) <= static_cast<u_long>(canvas.width));
            REQUIRE(static_cast<u_long>(bottom - top) <= static_cast<u_long>(canvas.height));
        }

        // Try to get valid list before the maximum limit.
        while (outGrids.size() == 0 && count-- > 0) {
            outGrids = sampler.sample(canvas,
                                      photos);
        }
        if (outGrids.size() > 0 && outGrids[0].slots.size() > 0) {
            // The list size should be 5.
            REQUIRE(outGrids[0].slots.size() == photos.size());
        }
    }

    SECTION("The sampler takes photos and manually optimize the tree structure again.") {
        int count = 5;
        RectF canvas(500.f, 500.f);

        std::vector<Photo> photos;
        photos.push_back(photo0);
        photos.push_back(photo1);
        photos.push_back(photo2);
        photos.push_back(photo3);
        photos.push_back(photo4);

        PWTreeNode* outcome = NULL;

        // Try to get a valid outcome before the maximum limit.
        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        while (outcome == NULL && count-- > 0) {
            outcome = sampler.sampleTree(canvas,
                                         photos,
                                         DEFAULT_ERR_TOLERANCE_RATE,
                                         DEFAULT_MAX_OPT_ITERATIONS,
                                         DEFAULT_MAX_SAMPLE_REPEAT);
        }

        if (outcome) {
            // Swap sibling of level 1.
            PWTreeNode* temp = outcome->firstChild;
            outcome->firstChild = outcome->secondChild;
            outcome->secondChild = temp;

            // Manually optimize again.
            int optTimes = 1 + Math::rand() % 15;
            std::cout << "Manually optimize " << optTimes << " times" << std::endl;
            for (int i = 0; i < optTimes; ++i) {
                outcome = sampler.optimize(outcome);
            }

            // Give it a real boundary.
            canvas.setRect(0.f, 0.f, 500.f, 700.f);
            sampler.calculateLayoutRect(outcome,
                                        static_cast<u_long>(canvas.width),
                                        static_cast<u_long>(canvas.height),
                                        LAYOUT_SCALE_TO_FILL);

            // Check the boundary.
            float left = 0, top = 0, right = 0, bottom = 0;
            calculateRectBound(outcome, left, top, right, bottom);

            // The outcome should still be within the tolerance.
            REQUIRE(outcome->aspectRatio.actualVal ==
                    Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
            REQUIRE(checkRectSize(outcome));
            REQUIRE(static_cast<u_long>(right - left) <= static_cast<u_long>(canvas.width));
            REQUIRE(static_cast<u_long>(bottom - top) <= static_cast<u_long>(canvas.height));
        }
    }

    SECTION("The sampler takes 5 photos and put them into a canvas with 0.6557377f with LAYOUT_CENTER_INSIDE policy.") {
        int count = 5;
        RectF canvas(1440.f, 2196.f);

        std::vector<Photo> photos;
        photos.push_back(photo6);
        photos.push_back(photo7);
        photos.push_back(photo8);
        photos.push_back(photo9);
        photos.push_back(photo10);

        PWTreeNode* outcome = NULL;

        // Try to get a valid outcome before the maximum limit.
        PicWallSampler sampler(LAYOUT_CENTER_INSIDE,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        while (outcome == NULL && count-- > 0) {
            outcome = sampler.sampleTree(canvas,
                                         photos,
                                         DEFAULT_ERR_TOLERANCE_RATE,
                                         DEFAULT_MAX_OPT_ITERATIONS,
                                         DEFAULT_MAX_SAMPLE_REPEAT);
        }

        if (outcome) {
            // Give it a real boundary.
            sampler.calculateLayoutRect(outcome,
                                        static_cast<u_long>(canvas.width),
                                        static_cast<u_long>(canvas.height),
                                        LAYOUT_CENTER_INSIDE);
//                PicWallSampler::printTree(outcome);

            // Check the boundary.
            float left = 0, top = 0, right = 0, bottom = 0;
            calculateRectBound(outcome, left, top, right, bottom);

            // The outcome should be within the tolerance.
            REQUIRE(outcome->aspectRatio.actualVal ==
                    Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
            REQUIRE(checkRectSize(outcome));
            REQUIRE(static_cast<u_long>(right - left) <= static_cast<u_long>(canvas.width));
            REQUIRE(static_cast<u_long>(bottom - top) <= static_cast<u_long>(canvas.height));
        }
    }

    SECTION("The sampler takes 5 photos and put them into a canvas with 0.6557377f with LAYOUT_SCALE_TO_FILL policy.") {
        int count = 5;
        RectF canvas(1440.f, 2196.f);

        std::vector<Photo> photos;
        photos.push_back(photo6);
        photos.push_back(photo7);
        photos.push_back(photo8);
        photos.push_back(photo9);
        photos.push_back(photo10);

        PWTreeNode* outcome = NULL;

        // Try to get a valid outcome before the maximum limit.
        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        while (outcome == NULL && count-- > 0) {
            outcome = sampler.sampleTree(canvas,
                                         photos,
                                         DEFAULT_ERR_TOLERANCE_RATE,
                                         DEFAULT_MAX_OPT_ITERATIONS,
                                         DEFAULT_MAX_SAMPLE_REPEAT);
        }

        if (outcome) {
            // Give it a real boundary.
            sampler.calculateLayoutRect(outcome,
                                        static_cast<u_long>(canvas.width),
                                        static_cast<u_long>(canvas.height),
                                        LAYOUT_SCALE_TO_FILL);
//                PicWallSampler::printTree(outcome);

            // Check the boundary.
            float left = 0, top = 0, right = 0, bottom = 0;
            calculateRectBound(outcome, left, top, right, bottom);

            // The outcome should be within the tolerance.
            REQUIRE(outcome->aspectRatio.actualVal ==
                    Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
            REQUIRE(checkRectSize(outcome));
            REQUIRE(static_cast<u_long>(right - left) <= static_cast<u_long>(canvas.width));
            REQUIRE(static_cast<u_long>(bottom - top) <= static_cast<u_long>(canvas.height));
        }
    }

    SECTION("The sampler take one photo (320 x 480) and put it into a canvas of 500 by 500 with LAYOUT_SCALE_TO_FILL policy.") {
        PWTreeNode node;

        node.aspectRatio.actualVal = node.aspectRatio.targetVal = 320.f / 480;

        u_long canvasWidth = 500, canvasHeight = 500;
        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        sampler.calculateLayoutRect(&node, canvasWidth, canvasHeight, LAYOUT_SCALE_TO_FILL);

        // The outcome rectangle should be {0, 0, 500, 500}
        REQUIRE(static_cast<int>(node.actualRect.x) == 0);
        REQUIRE(static_cast<int>(node.actualRect.y) == 0);
        REQUIRE(static_cast<int>(node.actualRect.width) == canvasWidth);
        REQUIRE(static_cast<int>(node.actualRect.height) == canvasHeight);
    }

    SECTION("The sampler take one photo and put it into a canvas of 500 by 500 with LAYOUT_CENTER_INSIDE policy.") {
        PWTreeNode node;

        node.aspectRatio.actualVal = node.aspectRatio.targetVal = 320.f / 480;

        u_long canvasWidth = 500, canvasHeight = 500;
        PicWallSampler sampler(LAYOUT_CENTER_INSIDE,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        sampler.calculateLayoutRect(&node, canvasWidth, canvasHeight, LAYOUT_CENTER_INSIDE);

        // Given the photo is size of 320 by 480.") {
        REQUIRE(node.actualRect.x == Approx(83.33333f));
        REQUIRE(static_cast<int>(node.actualRect.y) == 0);
        REQUIRE(node.actualRect.width == Approx(333.333337f));
        REQUIRE(static_cast<int>(node.actualRect.height) == canvasHeight);

        node.aspectRatio.actualVal = node.aspectRatio.targetVal = 480.f / 320;

        sampler.calculateLayoutRect(&node, canvasWidth, 500, LAYOUT_CENTER_INSIDE);

        // Given the photo is size of 480 by 320.
        REQUIRE(static_cast<int>(node.actualRect.x) == 0);
        REQUIRE(node.actualRect.y == Approx(83.3333f));
        REQUIRE(static_cast<int>(node.actualRect.width) == canvasWidth);
        REQUIRE(node.actualRect.height == Approx(333.33337f));
    }

    SECTION("The sampler takes photos, {set provided by CIC}, and put them into a canvas with 1.46453f.") {
        int count = 10;
        RectF canvas(1024.f, 699.f);

        Photo p0(1011, 1000.f / 562);
        Photo p1(1012, 796.f / 1024);
        Photo p2(1013, 1132.f / 1024);
        Photo p3(1014, 1280.f / 720);
        Photo p4(1015, 1280.f / 853);
        Photo p5(1016, 1280.f / 854);
        Photo p6(1017, 1280.f / 768);
        Photo p7(1018, 622.f / 1024);

        std::vector<Photo> photos;
        photos.push_back(p0);
        photos.push_back(p1);
        photos.push_back(p2);
        photos.push_back(p3);
        photos.push_back(p3);
        photos.push_back(p4);
        photos.push_back(p7);
        photos.push_back(p3);
        photos.push_back(p3);
        photos.push_back(p6);
        photos.push_back(p5);

        PWTreeNode* outcome = NULL;

        // Try to get a valid outcome before the maximum limit.
        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        while (outcome == NULL && count-- > 0) {
            outcome = sampler.sampleTree(canvas,
                                         photos,
                                         DEFAULT_ERR_TOLERANCE_RATE,
                                         DEFAULT_MAX_OPT_ITERATIONS,
                                         DEFAULT_MAX_SAMPLE_REPEAT);
        }

        if (outcome) {
            // Give it a real boundary.
            sampler.calculateLayoutRect(outcome,
                                        static_cast<u_long>(canvas.width),
                                        static_cast<u_long>(canvas.height),
                                        LAYOUT_SCALE_TO_FILL);
//                PicWallSampler::printTree(outcome);

            // Check the boundary.
            float left = 0, top = 0, right = 0, bottom = 0;
            calculateRectBound(outcome, left, top, right, bottom);

            // The outcome should be within the tolerance.
            REQUIRE(outcome->aspectRatio.actualVal ==
                    Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
            REQUIRE(checkRectSize(outcome));
            REQUIRE(static_cast<u_long>(right - left) <= static_cast<u_long>(canvas.width));
            REQUIRE(static_cast<u_long>(bottom - top) <= static_cast<u_long>(canvas.height));
        }
    }

    SECTION("The sampler takes two photos.") {
        int count = 10;

        Photo p0(0, 180.f, 180.f);
        Photo p1(1, 180.f, 180.f);
        RectF canvas(320.f, 480.f);

        std::vector<Photo> photos;
        photos.push_back(p0);
        photos.push_back(p1);

        PWTreeNode* outcome = NULL;

        // Try to get a valid outcome before the maximum limit.
        PicWallSampler sampler(LAYOUT_SCALE_TO_FILL,
                               GEN_POLICY_SLOTS_COULD_BE_MORE_THAN_PHOTOS);
        while (outcome == NULL && count-- > 0) {
            outcome = sampler.sampleTree(canvas,
                                         photos,
                                         DEFAULT_ERR_TOLERANCE_RATE,
                                         DEFAULT_MAX_OPT_ITERATIONS,
                                         DEFAULT_MAX_SAMPLE_REPEAT);
        }

        if (outcome) {
            // Give it a real boundary.
            sampler.calculateLayoutRect(outcome,
                                        static_cast<u_long>(canvas.width),
                                        static_cast<u_long>(canvas.height),
                                        LAYOUT_SCALE_TO_FILL);
//                PicWallSampler::printTree(outcome);

            // Check the boundary.
            float left = 0, top = 0, right = 0, bottom = 0;
            calculateRectBound(outcome, left, top, right, bottom);

            // The outcome should be within the tolerance.
            REQUIRE(outcome->aspectRatio.actualVal ==
                    Approx(outcome->aspectRatio.targetVal).epsilon(DEFAULT_ERR_TOLERANCE_RATE));
            REQUIRE(checkRectSize(outcome));
            REQUIRE(static_cast<u_long>(right - left) <= static_cast<u_long>(canvas.width));
            REQUIRE(static_cast<u_long>(bottom - top) <= static_cast<u_long>(canvas.height));
        } else {
            WARN("Somehow it cannot generate grid options.");
            REQUIRE(true);
        }
    }
}
