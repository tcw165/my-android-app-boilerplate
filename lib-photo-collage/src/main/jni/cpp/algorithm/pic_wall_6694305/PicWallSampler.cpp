#include <cassert>
#include <cmath>
#include <algorithm>
#include <sstream>
#include <Util.hpp>
#include <algorithm/pic_wall_6694305/PicWallSampler.hpp>

const std::string gGridName = "picwall: ";

///////////////////////////////////////////////////////////////////////////////
// Static Methods /////////////////////////////////////////////////////////////

bool compareAspectRatio(const Photo& a,
                        const Photo& b) {
    return a.constrainedAspectRatio < b.constrainedAspectRatio;
}

///////////////////////////////////////////////////////////////////////////////
// Public /////////////////////////////////////////////////////////////////////

PicWallSampler::PicWallSampler(int layoutPolicy,
                               int genPolicy)
        : IGridsGenerator(layoutPolicy, genPolicy),
          mTreeRoot(NULL),
          mCost(0.f) {}

PicWallSampler::~PicWallSampler() {
    // Release the photos.
    mPhotos.clear();
    mLocalPhotos.clear();

    // Release the tree.
    releaseTree(mTreeRoot);
    mTreeRoot = NULL;
}

std::vector<Grid> PicWallSampler::sample(RectF& canvas,
                                         std::vector<Photo>& photos) {
    return sample(canvas,
                  photos,
                  DEFAULT_ERR_TOLERANCE_RATE,
                  DEFAULT_MAX_OPT_ITERATIONS,
                  DEFAULT_MAX_SAMPLE_REPEAT);
}

std::vector<Grid> PicWallSampler::sample(RectF& canvas,
                                         std::vector<Photo>& photos,
                                         float errToleranceRate,
                                         u_long maxOptIterations,
                                         u_long maxSampleRepeat) {
    if (photos.size() == 0 ||
        !Util::checkPhotos(photos)) {
        return std::vector<Grid>(0);
    }

    std::vector<Grid> grids;
    // We found that the visual outcome is better as given more photos.
    u_int genTimes = 1;
    if (photos.size() > 4) {
        // The log is base 2.
        genTimes = 3 + (u_int) std::log(photos.size());
    }

    for (u_int i = 0; i < genTimes; ++i) {
        PWTreeNode* tree = sampleTree(canvas,
                                      photos,
                                      errToleranceRate,
                                      maxOptIterations,
                                      maxSampleRepeat);

        if (tree) {
            // Calc the real layout in terms of the given canvas size.
            calculateLayoutRect(tree,
                                static_cast<u_long>(canvas.width),
                                static_cast<u_long>(canvas.height),
                                layoutPolicy);

            // Append the iteration number to the grid's name.
            std::ostringstream ss;
            ss << " (" << i << ")";

            Grid grid = toGrid(tree);
            grid.name.append(ss.str());

            grids.push_back(grid);
        }
    }

    return grids;
}

PWTreeNode* PicWallSampler::sampleTree(RectF& canvas,
                                       std::vector<Photo>& photos,
                                       float errToleranceRate,
                                       u_long maxOptIterations,
                                       u_long maxSampleRepeat) {
    if (canvas.aspectRatio <= 0.f || photos.size() == 0) {
        return NULL;
    }

    // Step 1: Copy the photo list and sort it.
    mPhotos = photos;
    std::sort(mPhotos.begin(), mPhotos.end(), compareAspectRatio);

    // Step 2: Generate a guided binary tree by using divide-and-conquer.
    releaseTree(mTreeRoot);
    mTreeRoot = createTree(canvas.aspectRatio);

    // Step 3: Calculate the actual aspect ratio and iteratively adjust the
    // tree to find out an optimal solution.
    if (mTreeRoot) {
        mTreeRoot = optimize(mTreeRoot,
                             errToleranceRate,
                             maxOptIterations,
                             maxSampleRepeat);
    }

    return mTreeRoot;
}

PWTreeNode* PicWallSampler::optimize(PWTreeNode* treeRoot) {
    return optimize(treeRoot,
                    DEFAULT_ERR_TOLERANCE_RATE,
                    DEFAULT_MAX_OPT_ITERATIONS,
                    DEFAULT_MAX_SAMPLE_REPEAT);
}

PWTreeNode* PicWallSampler::optimize(PWTreeNode* treeRoot,
                                     float errToleranceRate,
                                     u_long maxOptIterations,
                                     u_long maxSampleRepeat) {
    if (treeRoot == NULL || treeRoot->aspectRatio.targetVal <= 0.f) {
        return NULL;
    }

    PWTreeNode* localRoot = treeRoot;
    float canvasAspectRatio = localRoot->aspectRatio.targetVal;
    float lowerBound = canvasAspectRatio / (1.f + errToleranceRate);
    float upperBound = canvasAspectRatio * (1.f + errToleranceRate);
    float& realCanvasAspectRatio = localRoot->aspectRatio.actualVal;

    realCanvasAspectRatio = calculateAspectRatioAt(localRoot);

    u_long iterCount = 0;
    u_long reSampleCount = 0;
    while ((realCanvasAspectRatio < lowerBound || realCanvasAspectRatio > upperBound) &&
           iterCount < maxOptIterations) {
        // Adjust the tree.
        bool changed = adjustAspectRatioAt(localRoot, errToleranceRate);

        // Calculate actual aspect ratio again.
        if (changed) {
            realCanvasAspectRatio = calculateAspectRatioAt(localRoot);
        }
        ++iterCount;

        // Should generate binary tree again.
        if (maxOptIterations > 0 &&
            (iterCount >= maxOptIterations || !changed)) {
            iterCount = 0;

            // Regenerate the tree and keep track of it so that the tree could
            // be freed automatically when destructing.
            releaseTree(localRoot);
            localRoot = createTree(canvasAspectRatio);

            realCanvasAspectRatio = calculateAspectRatioAt(localRoot);
            ++reSampleCount;

            if (reSampleCount > maxSampleRepeat) {
                return NULL;
            }
        }
    }

    return localRoot;
}

void PicWallSampler::calculateLayoutRect(PWTreeNode* treeRoot,
                                         u_long canvasWidth,
                                         u_long canvasHeight,
                                         int layoutPolicy) {
    if (treeRoot == NULL || treeRoot->aspectRatio.actualVal <= 0.f) {
        return;
    } else if (canvasWidth == 0 && canvasHeight == 0) {
        return;
    } else if (ULONG_MAX / canvasWidth < canvasHeight) {
        // In case that canvasWidth * canvasHeight is too big.
        return;
    }

    float x = 0.f;
    float y = 0.f;
    float width;
    float height;
    float rootWidth = static_cast<float>(canvasWidth);
    float rootHeight = static_cast<float>(canvasHeight);

    // If it's an unacceptable policy, set it to default value.
    if (layoutPolicy < LAYOUT_CENTER_INSIDE || layoutPolicy > LAYOUT_SCALE_TO_FILL) {
        layoutPolicy = LAYOUT_SCALE_TO_FILL;
    }

    switch (layoutPolicy) {
        case LAYOUT_CENTER_INSIDE: {
            float canvasAr = static_cast<float>(canvasWidth) / canvasHeight;

            if (treeRoot->aspectRatio.actualVal < canvasAr) {
                width = treeRoot->aspectRatio.actualVal * canvasHeight;
                height = canvasHeight;

                x = (static_cast<float>(canvasWidth) - width) / 2.f;
            } else {
                width = canvasWidth;
                height = static_cast<float>(canvasWidth) / treeRoot->aspectRatio.actualVal;

                y = (static_cast<float>(canvasHeight) - height) / 2.f;
            }
            break;
        }
        case LAYOUT_SCALE_TO_FILL:
        default: {
            // Pin the width and change height accordingly.
            width = static_cast<float>(canvasWidth);
            height = static_cast<float>(canvasWidth) / treeRoot->aspectRatio.actualVal;
            rootHeight = height;
            break;
        }
    }

    // Calculate the rectangles.
    treeRoot->setRect(x, y, width, height, rootWidth, rootHeight);
    size_t nodesNum = calculateChildLayoutRectAt(treeRoot, rootWidth, rootHeight);

    // Post process of scaling the rectangle for LAYOUT_SCALE_TO_FILL because
    // we pin the width and the height might not be equal to the expected
    // height.
    if (layoutPolicy == LAYOUT_SCALE_TO_FILL) {
        float factorX = static_cast<float>(canvasWidth) / width;
        float factorY = static_cast<float>(canvasHeight) / height;

        scaleLayoutRectAt(treeRoot, factorX, factorY);
    }

    // Calculate the cost.
    u_long canvasArea = canvasWidth * canvasHeight;
    float blankArea = 1.f;
    float avgArea = static_cast<float>(canvasArea) / nodesNum;
    float avgAreaError = 0;
    float aspectRatioError = 0;

    calculateCostAt(treeRoot,
                    canvasArea,
                    blankArea,
                    avgArea,
                    avgAreaError,
                    aspectRatioError);

    mCost = blankArea + avgAreaError + aspectRatioError;
}

void PicWallSampler::releaseTree(PWTreeNode* node) {
    if (node) {
        releaseNode(node);
    }
}

void PicWallSampler::printTree(PWTreeNode* node) {
    u_long level = 0;

    if (node) {
        PicWallSampler::printTree(node, level);
    } else {
        std::cout << "Empty!" << std::endl;
    }
}

void PicWallSampler::printTree(PWTreeNode* node,
                               u_long& level) {
    std::string type;
    std::string type0 = "FIRST_LEAF_NODE";
    std::string type1 = "SECOND_LEAF_NODE";
    std::string type2 = "H_DIV_NODE";
    std::string type3 = "V_DIV_NODE";

    switch (node->type) {
        case V_DIV_NODE:
            type = type3;
            break;
        case H_DIV_NODE:
            type = type2;
            break;
        case SECOND_LEAF_NODE:
            type = type1;
            break;
        case FIRST_LEAF_NODE:
        default:
            type = type0;
            break;
    }

    // Indent string.
    std::string prefix = "";
    for (u_long i = 0; i < level; ++i) {
        prefix += "----";
    }

    if (node->type == SECOND_LEAF_NODE || node->type == FIRST_LEAF_NODE) {
        std::cout << prefix << "node with Photo id = " << node->photoId;
    } else {
        std::cout << prefix << "node (intermediate)";
    }
    std::cout << ", " << type << std::endl;
    std::cout << prefix << ", aspect ratio {target: " << node->aspectRatio.targetVal <<
              ", " << node->aspectRatio.actualVal << "}" << std::endl;
    std::cout << prefix << ", rect {actual: " << node->actualRect.x <<
              ", " << node->actualRect.y <<
              ", " << node->actualRect.width <<
              ", " << node->actualRect.height << "}";
    std::cout << ", {normalized: " << node->normalizedRect.x <<
              ", " << node->normalizedRect.y <<
              ", " << node->normalizedRect.width <<
              ", " << node->normalizedRect.height << "}" << std::endl;
    std::cout << std::endl;

    if (node->firstChild || node->secondChild) {
        ++level;

        if (node->firstChild) {
            PicWallSampler::printTree(node->firstChild, level);
        }
        if (node->secondChild) {
            PicWallSampler::printTree(node->secondChild, level);
        }

        if (level - 1l > 0l) {
            --level;
        }
    }
}

///////////////////////////////////////////////////////////////////////////////
// Private/Protected Functions ////////////////////////////////////////////////

PWTreeNode* PicWallSampler::createTree(float canvasAspectRatio) {
    if (!mPhotos.empty()) {
        // Copy to local pool.
        mLocalPhotos.clear();
        mLocalPhotos = mPhotos;

        return createNode(NULL, canvasAspectRatio, canvasAspectRatio, mLocalPhotos, mLocalPhotos.size());
    } else {
        return NULL;
    }
}

PWTreeNode* PicWallSampler::createNode(PWTreeNode* parent,
                                       float rootAr,
                                       float targetAr,
                                       std::vector<Photo>& photos,
                                       u_long photosNum) {
    // Create node.
    PWTreeNode* node = new PWTreeNode();
    node->parent = parent;
    node->aspectRatio.targetVal = targetAr;

    if (photosNum == 1) {
        // Set the new node.
        node->type = FIRST_LEAF_NODE;

        // Find the best fit aspect ratio.
        findOneImageFor(*node, photos);
    } else if (photosNum == 2) {
        // Set the new node.
        PWTreeNode* leftChild = new PWTreeNode();
        leftChild->type = FIRST_LEAF_NODE;
        leftChild->parent = parent;

        PWTreeNode* rightChild = new PWTreeNode();
        rightChild->type = SECOND_LEAF_NODE;
        rightChild->parent = parent;

        node->firstChild = leftChild;
        node->secondChild = rightChild;

        // Find the best fit aspect ratio with two nodes.
        // As well as the split type for node.
        findTwoImagesFor(*node,
                         *leftChild,
                         *rightChild,
                         photos);
    } else {
        // Determine divided type.
        // Note: DON'T use rand() here because Android Ver.<4.4.4 doesn't
        // support it.
        u_int v_h = static_cast<u_int>(Math::rand()) % 2;
        if (targetAr > rootAr * 2) v_h = 1;
        if (targetAr < rootAr / 2) v_h = 0;
        // Determine the new target aspect ratio.
        float newTargetAr = 0;
        if (v_h == 1) {
            node->type = V_DIV_NODE;
            newTargetAr = targetAr / 2;
        } else {
            node->type = H_DIV_NODE;
            newTargetAr = targetAr * 2;
        }
        // Calculate the Photo numbers for first and second children nodes
        // respectively.
        u_long firstPhotosNum = photosNum / 2;
        u_long secondPhotosNum = photosNum - firstPhotosNum;

        if (firstPhotosNum > 0) {
            node->firstChild = createNode(node, rootAr, newTargetAr, photos, firstPhotosNum);
        }
        if (secondPhotosNum > 0) {
            node->secondChild = createNode(node, rootAr, newTargetAr, photos, secondPhotosNum);
        }
    }

    return node;
}

void PicWallSampler::releaseNode(PWTreeNode* node) {
    if (node == NULL) return;
    if (node->firstChild) releaseNode(node->firstChild);
    if (node->secondChild) releaseNode(node->secondChild);

    node->parent = NULL;
    node->firstChild = NULL;
    node->secondChild = NULL;

    delete node;
}

bool PicWallSampler::findOneImageFor(PWTreeNode& node,
                                     std::vector<Photo>& photos) {
    if (photos.empty()) return false;

    // Use binary search to find the best-match result.
    bool isFound = false;
    u_long finder = 0;
    u_long min_ind = 0;
    u_long mid_ind = 0;
    u_long max_ind = photos.size() - 1;

    if (1 == photos.size()) {
        finder = 0;
    } else {
        while (min_ind + 1 < max_ind) {
            mid_ind = (min_ind + max_ind) / 2;
            if (photos[mid_ind].constrainedAspectRatio == node.aspectRatio.targetVal) {
                finder = mid_ind;
                isFound = true;
                break;
            } else if (photos[mid_ind].constrainedAspectRatio > node.aspectRatio.targetVal) {
                max_ind = mid_ind - 1;
            } else {
                min_ind = mid_ind + 1;
            }
        }

        if (!isFound) {
            if (std::fabs(photos[max_ind].constrainedAspectRatio - node.aspectRatio.targetVal) >
                std::fabs(photos[min_ind].constrainedAspectRatio - node.aspectRatio.targetVal)) {
                finder = min_ind;
            } else {
                finder = max_ind;
            }
        }
    }

    // Dispatch image to leaf node.
    Photo selectedPhoto = photos[finder];
    node.photoId = selectedPhoto.id;
    node.photoAspectRatio = selectedPhoto.constrainedAspectRatio;
    node.aspectRatio.actualVal = selectedPhoto.constrainedAspectRatio;

    // Remove the find result from alpha_array.
    photos.erase(photos.begin() + finder);

    return true;
}

bool PicWallSampler::findTwoImagesFor(PWTreeNode& node,
                                      PWTreeNode& leftChild,
                                      PWTreeNode& rightChild,
                                      std::vector<Photo>& photos) {
    if (photos.size() < 2) return false;

    // There are two situations:
    // [1]: parent node is vertical cut.
    float targetAspectRatio = node.aspectRatio.targetVal;
    u_long i = 0;
    u_long j = photos.size() - 1;
    u_long best_v_i = i;
    u_long best_v_j = j;
    float min_v_diff = std::fabs(photos[best_v_i].constrainedAspectRatio +
                                 photos[best_v_j].constrainedAspectRatio -
                                 targetAspectRatio);
    while (i < j) {
        if (photos[i].constrainedAspectRatio + photos[j].constrainedAspectRatio > targetAspectRatio) {
            float diff = std::fabs(photos[i].constrainedAspectRatio +
                                   photos[j].constrainedAspectRatio -
                                   targetAspectRatio);
            if (diff < min_v_diff) {
                min_v_diff = diff;
                best_v_i = i;
                best_v_j = j;
            }
            --j;
        } else if (photos[i].constrainedAspectRatio + photos[j].constrainedAspectRatio < targetAspectRatio) {
            float diff = std::fabs(photos[i].constrainedAspectRatio +
                                   photos[j].constrainedAspectRatio -
                                   targetAspectRatio);
            if (diff < min_v_diff) {
                min_v_diff = diff;
                best_v_i = i;
                best_v_j = j;
            }
            ++i;
        } else {
            best_v_i = i;
            best_v_j = j;
            min_v_diff = 0;
            break;
        }
    }
    // [2]: parent node is horizontal cut;
    float targetArRecip = 1 / targetAspectRatio;
    i = 0;
    j = photos.size() - 1;
    u_long best_h_i = i;
    u_long best_h_j = j;
    float min_h_diff = std::fabs(photos[best_h_i].constrainedAspectRatioRecip +
                                 photos[best_h_j].constrainedAspectRatioRecip -
                                 targetArRecip);
    while (i < j) {
        if (photos[i].constrainedAspectRatioRecip + photos[j].constrainedAspectRatioRecip >
            targetArRecip) {
            float diff = std::fabs(photos[i].constrainedAspectRatioRecip +
                                   photos[j].constrainedAspectRatioRecip -
                                   targetArRecip);
            if (diff < min_h_diff) {
                min_h_diff = diff;
                best_h_i = i;
                best_h_j = j;
            }
            ++i;
        } else if (photos[i].constrainedAspectRatioRecip + photos[j].constrainedAspectRatioRecip <
                   targetArRecip) {
            float diff = std::fabs(photos[i].constrainedAspectRatioRecip +
                                   photos[j].constrainedAspectRatioRecip -
                                   targetArRecip);
            if (diff < min_h_diff) {
                min_h_diff = diff;
                best_h_i = i;
                best_h_j = j;
            }
            --j;
        } else {
            best_h_i = i;
            best_h_j = j;
            min_h_diff = 0;
            break;
        }
    }

    // Find the best-match from the above two situations.
    float realArV = photos[best_v_i].constrainedAspectRatio + photos[best_v_j].constrainedAspectRatio;
    float realArH = (photos[best_h_i].constrainedAspectRatio * photos[best_h_j].constrainedAspectRatio) /
                    (photos[best_h_i].constrainedAspectRatio + photos[best_h_j].constrainedAspectRatio);

    float ratio_diff_v;
    float ratio_diff_h;
    if (realArV > targetAspectRatio) {
        ratio_diff_v = realArV / targetAspectRatio;
    } else {
        ratio_diff_v = targetAspectRatio / realArV;
    }
    if (realArH > targetAspectRatio) {
        ratio_diff_h = realArH / targetAspectRatio;
    } else {
        ratio_diff_h = targetAspectRatio / realArH;
    }

    assert(best_h_i < best_h_j);
    assert(best_v_i < best_v_j);

    if (ratio_diff_v <= ratio_diff_h) {
        node.type = V_DIV_NODE;

        leftChild.photoId = photos[best_v_i].id;
        leftChild.photoAspectRatio = photos[best_v_i].constrainedAspectRatio;
        leftChild.aspectRatio.targetVal = targetAspectRatio / 2;
        leftChild.aspectRatio.actualVal = photos[best_v_i].constrainedAspectRatio;

        rightChild.photoId = photos[best_v_j].id;
        rightChild.photoAspectRatio = photos[best_v_j].constrainedAspectRatio;
        rightChild.aspectRatio.targetVal = targetAspectRatio / 2;
        rightChild.aspectRatio.actualVal = photos[best_v_j].constrainedAspectRatio;

        //    std::std::cout << alpha_array[best_v_i].image_ind_
        //    << ":" << alpha_array[best_v_j].image_ind_ << std::std::endl;

        photos.erase(photos.begin() + best_v_i);
        photos.erase(photos.begin() + best_v_j - 1);
    } else {
        node.type = H_DIV_NODE;

        leftChild.photoId = photos[best_h_i].id;
        leftChild.photoAspectRatio = photos[best_h_i].constrainedAspectRatio;
        leftChild.aspectRatio.targetVal = targetAspectRatio * 2;
        leftChild.aspectRatio.actualVal = photos[best_h_i].constrainedAspectRatio;

        rightChild.photoId = photos[best_h_j].id;
        rightChild.photoAspectRatio = photos[best_h_j].constrainedAspectRatio;
        rightChild.aspectRatio.targetVal = targetAspectRatio * 2;
        rightChild.aspectRatio.actualVal = photos[best_h_j].constrainedAspectRatio;

        //    std::std::cout << alpha_array[best_h_i].image_ind_
        //    << ":" << alpha_array[best_h_j].image_ind_ << std::std::endl;

        photos.erase(photos.begin() + best_h_i);
        photos.erase(photos.begin() + best_h_j - 1);
    }
    return true;
}

float PicWallSampler::calculateAspectRatioAt(PWTreeNode* node) {
    float& aspectRatio = node->aspectRatio.actualVal;

    if (node->type == FIRST_LEAF_NODE ||
        node->type == SECOND_LEAF_NODE) {
        // This is a leaf node.
        return aspectRatio;
    } else {
        float leftAr = calculateAspectRatioAt(node->firstChild);
        float rightAr = calculateAspectRatioAt(node->secondChild);

        if (node->type == V_DIV_NODE) {
            aspectRatio = leftAr + rightAr;
            return aspectRatio;
        } else if (node->type == H_DIV_NODE) {
            aspectRatio = (leftAr * rightAr) / (leftAr + rightAr);
            return aspectRatio;
        }
        assert(false);
        return 0;
    }
}

size_t PicWallSampler::calculateChildLayoutRectAt(PWTreeNode* parent,
                                                  float canvasWidth,
                                                  float canvasHeight) {
    size_t count = 0;
    float x = parent->actualRect.x;
    float y = parent->actualRect.y;
    float width = parent->actualRect.width;
    float height = parent->actualRect.height;

    if (parent->type == H_DIV_NODE) {
        float newY = y;
        float newHeight = 0.f;

        if (parent->firstChild) {
            newHeight = width / parent->firstChild->aspectRatio.actualVal;

            parent->firstChild->setRect(x, newY, width, newHeight, canvasWidth, canvasHeight);

            count += calculateChildLayoutRectAt(parent->firstChild, canvasWidth, canvasHeight);
        }
        if (parent->secondChild) {
            newY = y + newHeight;
            newHeight = height - newHeight;

            parent->secondChild->setRect(x, newY, width, newHeight, canvasWidth, canvasHeight);

            count += calculateChildLayoutRectAt(parent->secondChild, canvasWidth, canvasHeight);
        }
    } else if (parent->type == V_DIV_NODE) {
        float newX = x;
        float newWidth = 0.f;

        if (parent->firstChild) {
            newWidth = height * parent->firstChild->aspectRatio.actualVal;

            parent->firstChild->setRect(newX, y, newWidth, height, canvasWidth, canvasHeight);

            count += calculateChildLayoutRectAt(parent->firstChild, canvasWidth, canvasHeight);
        }
        if (parent->secondChild) {
            newX = x + newWidth;
            newWidth = width - newWidth;

            parent->secondChild->setRect(newX, y, newWidth, height, canvasWidth, canvasHeight);

            count += calculateChildLayoutRectAt(parent->secondChild, canvasWidth, canvasHeight);
        }
    } else {
        // Count because it's a leaf node.
        count = 1;
    }

    return count;
}

void PicWallSampler::calculateCostAt(PWTreeNode* node,
                                     u_long canvasArea,
                                     float& blankArea,
                                     float avgArea,
                                     float& avgAreaError,
                                     float& aspectRatioError) {
    if (node->type == H_DIV_NODE || node->type == V_DIV_NODE) {
        if (node->firstChild) {
            calculateCostAt(node->firstChild,
                            canvasArea,
                            blankArea,
                            avgArea,
                            avgAreaError,
                            aspectRatioError);
        }
        if (node->secondChild) {
            calculateCostAt(node->secondChild,
                            canvasArea,
                            blankArea,
                            avgArea,
                            avgAreaError,
                            aspectRatioError);
        }
    } else {
        double nodeArea = static_cast<double>(node->actualRect.width) *
                          node->actualRect.height;

        blankArea -= static_cast<float>(nodeArea / canvasArea);
        avgAreaError += static_cast<float>(std::abs(nodeArea - avgArea) / nodeArea);
        aspectRatioError += std::abs(node->photoAspectRatio -
                                     node->aspectRatio.actualVal);
    }
}

void PicWallSampler::scaleLayoutRectAt(PWTreeNode* node,
                                       float factorX,
                                       float factorY) {
    node->actualRect.x *= factorX;
    node->actualRect.width *= factorX;
    node->actualRect.y *= factorY;
    node->actualRect.height *= factorY;

    if (node->firstChild) {
        scaleLayoutRectAt(node->firstChild, factorX, factorY);
    }
    if (node->secondChild) {
        scaleLayoutRectAt(node->secondChild, factorX, factorY);
    }
}

bool PicWallSampler::adjustAspectRatioAt(PWTreeNode* node,
                                         float errToleranceRate) {
    assert(node != NULL);

    if (node->type == FIRST_LEAF_NODE || node->type == SECOND_LEAF_NODE) return false;

    bool isChanged = false;
    float errToleranceRate2 = 1.f + errToleranceRate / 2;

    if (node->aspectRatio.actualVal > node->aspectRatio.targetVal * errToleranceRate2) {
        // Too big actual aspect ratio.
        if (node->type == V_DIV_NODE) isChanged = true;
        node->type = H_DIV_NODE;
        node->firstChild->aspectRatio.targetVal = node->aspectRatio.targetVal * 2;
        node->secondChild->aspectRatio.targetVal = node->aspectRatio.targetVal * 2;
    } else if (node->aspectRatio.actualVal < node->aspectRatio.targetVal / errToleranceRate2) {
        // Too small actual aspect ratio.
        if (node->type == H_DIV_NODE) isChanged = true;
        node->type = V_DIV_NODE;
        node->firstChild->aspectRatio.targetVal = node->aspectRatio.targetVal / 2;
        node->secondChild->aspectRatio.targetVal = node->aspectRatio.targetVal / 2;
    } else {
        // Aspect ratio is okay. Update the children's aspect ratio because current
        // aspect ratio might be changed by its parent.
        if (node->type == H_DIV_NODE) {
            node->firstChild->aspectRatio.targetVal = node->aspectRatio.targetVal * 2;
            node->secondChild->aspectRatio.targetVal = node->aspectRatio.targetVal * 2;
        } else if (node->type == V_DIV_NODE) {
            node->firstChild->aspectRatio.targetVal = node->aspectRatio.targetVal / 2;
            node->secondChild->aspectRatio.targetVal = node->aspectRatio.targetVal / 2;
        }
    }
    bool isChanged1 = adjustAspectRatioAt(node->firstChild, errToleranceRate);
    bool isChanged2 = adjustAspectRatioAt(node->secondChild, errToleranceRate);
    return isChanged || isChanged1 || isChanged2;
}

void PicWallSampler::toSlotList(PWTreeNode* treeNode,
                                std::vector<RectSlot>& outSlots) {
    if (treeNode->firstChild) {
        toSlotList(treeNode->firstChild, outSlots);
    }
    if (treeNode->secondChild) {
        toSlotList(treeNode->secondChild, outSlots);
    }

    // Stop condition: if the node is a leaf node.
    if (treeNode->type == FIRST_LEAF_NODE || treeNode->type == SECOND_LEAF_NODE) {
        RectF& rect = treeNode->normalizedRect;
        RectSlot slot(rect.x,
                      rect.y,
                      rect.width,
                      rect.height,
                      treeNode->photoId);
        outSlots.push_back(slot);
    }
}

Grid PicWallSampler::toGrid(PWTreeNode* treeRoot) {
    // Flatten the tree to a list.
    std::vector<RectSlot> slots;
    toSlotList(treeRoot, slots);

    std::string name(gGridName);
    switch (layoutPolicy) {
        case LAYOUT_IGNORED:
            name += "ignored";
            break;
        case LAYOUT_CENTER_INSIDE:
            name += "center_inside";
            break;
        case LAYOUT_SCALE_TO_FILL:
        default:
            name += "scale_to_fit";
    }

    return Grid(name, slots);
}
