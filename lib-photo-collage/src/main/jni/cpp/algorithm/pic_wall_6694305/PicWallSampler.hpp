#ifndef CB_ALGORITHMS_LIB_PICWALLSAMPLER_H
#define CB_ALGORITHMS_LIB_PICWALLSAMPLER_H

#include <vector>
#include <cmath>
#include <cstdlib>

#include <RectF.hpp>
#include <Photo.hpp>
#include <IGridsGenerator.hpp>

/**
 * The error tolerance rate for the target canvas aspect ratio.
 */
#define DEFAULT_ERR_TOLERANCE_RATE      0.45f
/**
 * The maximum optimization iterations.
 */
#define DEFAULT_MAX_OPT_ITERATIONS      50
/**
 * The maximum sample (create tree) repeat times.
 */
#define DEFAULT_MAX_SAMPLE_REPEAT       2

/**
 * The dividing type, horizontal or vertical.
 */
enum PWTreeNodeType {
    FIRST_LEAF_NODE = 0,
    SECOND_LEAF_NODE,
    H_DIV_NODE,
    V_DIV_NODE
};

/**
 * The aspect ratio set containing the expected value and real value.
 */
class PWTreeAR {
public:
    float actualVal;
    float targetVal;

    /**
     * Explicit constructor (non-aggregate class).
     */
    PWTreeAR() : actualVal(0),
                 targetVal(0) {}
};

/**
 * The node structure.
 */
class PWTreeNode {
public:

    PWTreeNodeType type;

    PWTreeAR aspectRatio;
    /**
     * The ID representing the selected photo for the leaf node.
     */
    u_long photoId;
    /**
     * The aspect ratio of the selected photo.
     */
    float photoAspectRatio;
    /**
     * The boundary rect of the tree in terms of the canvas width and height.
     */
    RectF actualRect;
    /**
     * The boundary rect of the tree in terms of the canvas width and height.
     * The rectangle is normalized (0..1).
     */
    RectF normalizedRect;

    PWTreeNode* parent;
    PWTreeNode* firstChild;
    PWTreeNode* secondChild;

    /**
     * Explicit constructor (non-aggregate class).
     */
    PWTreeNode() : type(FIRST_LEAF_NODE),
                   photoId(INVALID_PHOTO_ID),
                   parent(NULL),
                   firstChild(NULL),
                   secondChild(NULL) {}

    /**
     * Set the non-normalized and normalized rectangles in terms of the given
     * canvas width and height.
     */
    void setRect(float x,
                 float y,
                 float width,
                 float height,
                 float canvasWidth,
                 float canvasHeight) {
        actualRect.setRect(x, y, width, height);
        normalizedRect.setNormalizedRect(x, y, width, height, canvasWidth, canvasHeight);
    }
};

/**
 * It's a knapsack algorithm which generate binary tree in a guided way and
 * optimize the tree in divide-and-conquer way.
 *
 * For example: Given 3 photos with different aspect ratios.
 *
 *                        .---.
 *                        | H |           <-- Intermediate nodes.
 *                        '---'
 *                        /   \
 *                    .---.   .---.
 *    Leaf nodes -->  |   |   | V |
 *                    '---'   '---'
 *                            /    \
 *                        .---.    .---.
 *                        |   |    |   |  <-- Leaf nodes.
 *                        '---'    '---'
 *
 * There'll be total 7 nodes (2n - 1) including 2 intermediate nodes and 3 leaf
 * nodes. There're two possible dividing type for the intermediate nodes,
 * saying horizontal dividing and vertical dividing.
 *
 *   Horizontal Dividing:       Vertical Dividing:
 *
 *                                .----------.
 *   .--------+--------.          |          |
 *   |        |        |          |  First   |
 *   | First  | Second |          |          |
 *   |        |        |          +----------+
 *   '--------+--------'          |          |
 *                                |  Second  |
 *                                |          |
 *                                '----------'
 *
 * Given the tree structure, the algorithm could optimize the aspect ratio by
 * changing the dividing type of the intermediate nodes.
 *
 * Original paper was from: http://ieeexplore.ieee.org/xpl/login.jsp?tp=&arnumber=6694305&url=http%3A%2F%2Fieeexplore.ieee.org%2Fxpls%2Fabs_all.jsp%3Farnumber%3D6694305
 */
class PicWallSampler : public IGridsGenerator {
public:

    PicWallSampler(int layoutPolicy,
                   int genPolicy);

    virtual ~PicWallSampler();

    /**
     * Calculate iteratively and return the best layout before it hits the
     * maximum iteration limit. Return non-null pointer if it could find an
     * optimal solution. And the length of the returned array is nodesNum();
     * NULL if not.
     *
     * Note: The outcome allocation would be freed when destructor is called!
     * So you'd need to copy them to your memory before it is freed.
     *
     * @param   canvas  The canvas rect.
     * @param   photos  The input Photo array.
     * @return          The return array which can be inflated to a tree.
     */
    virtual std::vector<Grid> sample(RectF& canvas,
                                     std::vector<Photo>& photos);

    /**
     * Calculate iteratively and return the best layout before it hits the
     * maximum iteration limit. Return non-null pointer if it could find an
     * optimal solution. And the length of the returned array is nodesNum();
     * NULL if not.
     *
     * Note: The outcome allocation would be freed when destructor is called!
     * So you'd need to copy them to your memory before it is freed.
     *
     * @param   canvas              The canvas rect.
     * @param   photos              The input Photo array.
     * @param   errToleranceRate    The error tolerance rate.
     * @param   maxOptIterations    The maximum optimization iterations.
     * @param   maxSampleRepeat     The maximum times of repeatedly sampling.
     * @return          The return array which can be inflated to a tree.
     */
    std::vector<Grid> sample(RectF& canvas,
                             std::vector<Photo>& photos,
                             float errToleranceRate,
                             u_long maxOptIterations,
                             u_long maxSampleRepeat);

    /**
     * Calculate iteratively and return the best layout before it hits the
     * maximum iteration limit. Return non-null pointer if it could find an
     * optimal solution. And the length of the returned array is nodesNum();
     * NULL if not.
     *
     * Note: The outcome allocation would be freed when destructor is called!
     * So you'd need to copy them to your memory before it is freed.
     *
     * @param   canvas              The canvas rect.
     * @param   photos              The input Photo array.
     * @param   errToleranceRate    The error tolerance rate.
     * @param   maxOptIterations    The maximum optimization iterations.
     * @param   maxSampleRepeat     The maximum times of repeatedly sampling.
     * @return                      The return array which can be inflated to
     *                              a tree.
     */
    PWTreeNode* sampleTree(RectF& canvas,
                           std::vector<Photo>& photos,
                           float errToleranceRate,
                           u_long maxOptIterations,
                           u_long maxSampleRepeat);

    /**
     * Optimize the given layout by iterating over and over again until the
     * outcome aspect ratio of root node is close to expectation.
     *
     * Note: The sample() must be called before calling it.
     *
     * @param   treeRoot            The layout tree being optimized.
     */
    PWTreeNode* optimize(PWTreeNode* treeRoot);

    /**
     * Optimize the given layout by iterating over and over again until the
     * outcome aspect ratio of root node is close to expectation.
     *
     * Note: The sample() must be called before calling it.
     *
     * @param   treeRoot            The layout tree being optimized.
     * @param   errToleranceRate    The error tolerance rate.
     * @param   maxOptIterations    The maximum optimization iterations.
     * @param   maxSampleRepeat     The maximum times of repeatedly sampling.
     */
    PWTreeNode* optimize(PWTreeNode* treeRoot,
                  float errToleranceRate,
                  u_long maxOptIterations,
                  u_long maxSampleRepeat);

    /**
     * Calculate the boundary rectangles for every nodes of the given layout
     * in terms of the given canvas width and height so that the boundary fits
     * center in the canvas.
     */
    void calculateLayoutRect(PWTreeNode* treeRoot,
                             u_long canvasWidth,
                             u_long canvasHeight,
                             int layoutPolicy);

    /**
     * The error of the target outcome versus the actual outcome in terms of
     * the blank area, the area error of nodes' area versus the average node's
     * area and the aspect ratio error of nodes' actual value versus the actual
     * value.
     *
     * Note: It would be valid after calling calculateLayoutRect method. Or it
     * just returns zero.
     */
    float cost() {
        return mCost;
    }

    /**
     * Release the resource used by the tree.
     */
    static void releaseTree(PWTreeNode* node);

    static void printTree(PWTreeNode* node);

    static void printTree(PWTreeNode* node,
                          u_long& level);

protected:
    std::vector<Photo> mPhotos;
    std::vector<Photo> mLocalPhotos;
    PWTreeNode* mTreeRoot;

    /**
     * The cost formula is the summary of:
     * -----------------------------------
     * 1) blank_area / canvas_area.
     *
     * 2) sum(abs(R(i) - R(avg)) / canvas_area), given R(i) is the actual
     * rectangle and R(avg) is the average area in terms of the canvas size
     * and amount of photos.
     * It's for minimizing the case that some rectangles are greatly bigger
     * than the others.
     *
     * 3) sum(abs(AR(photo) - AR(actual))), given AR(photo) is the aspect ratio
     * of the selected photo and AR(actual) is the actual aspect ratio after the
     * calculation.
     */
    float mCost;

    /**
     * Initialize the tree.
     */
    PWTreeNode* createTree(float canvasAspectRatio);

    /**
     * Create node with guide strategy which the divided type of the node is
     * determined heuristically.
     */
    static PWTreeNode* createNode(PWTreeNode* parent,
                                  float rootAr,
                                  float targetAr,
                                  std::vector<Photo>& photos,
                                  u_long photosNum);

    static void releaseNode(PWTreeNode* node);

    static bool findOneImageFor(PWTreeNode& node,
                                std::vector<Photo>& photos);

    static bool findTwoImagesFor(PWTreeNode& node,
                                 PWTreeNode& leftChild,
                                 PWTreeNode& rightChild,
                                 std::vector<Photo>& photos);

    /**
     * It traverses tree nodes in a way of bottom-up strategy to calculate every
     * node's aspect ratio.
     */
    static float calculateAspectRatioAt(PWTreeNode* node);

    /**
     * It traverses tree nodes in a way of top-down strategy to calculate every
     * node's boundary rectangle.
     *
     * @return                      The amount of leaf nodes.
     */
    static size_t calculateChildLayoutRectAt(PWTreeNode* parent,
                                             float canvasWidth,
                                             float canvasHeight);

    /**
     * It traverses tree nodes to collect the blank area, the area error of nodes
     * versus average area and the aspect ratio error of nodes' target value
     * versus actual value.
     *
     * @param   canvasArea          The canvas area.
     * @param   blankArea           The percentage of blank area. The default
     *                              value is one.
     * @param   avgArea             The average area of nodes, saying
     *                              canvas_area / nodesNum.
     * @param   avgAreaError        The summary of percentages of the area error
     *                              which is the absolute of the node's area
     *                              subtracts by the average node area.
     * @param   aspectRatioError    The summary of the aspect ratio errors which
     *                              is the aspect ratio of the selected photo
     *                              subtracts the actual aspect ratio.
     */
    static void calculateCostAt(PWTreeNode* node,
                                u_long canvasArea,
                                float& blankArea,
                                float avgArea,
                                float& avgAreaError,
                                float& aspectRatioError);

    /**
     * Scale the rectangle in terms of the given factor x and y for
     * LAYOUT_SCALE_TO_FILL policy.
     */
    static void scaleLayoutRectAt(PWTreeNode* node,
                                  float factorX,
                                  float factorY);

    /**
     * It traverses children nodes (if so) of the given node in a way of top-down
     * to adjust the node's aspect ratio. Intuitively, "V" cut makes the node’s
     * aspect ratio larger as it is the sum of its child nodes' aspect ratios.
     * On the contrary, "H" cut makes the node’s aspect ratio smaller.
     */
    static bool adjustAspectRatioAt(PWTreeNode* node,
                                    float errToleranceRate);

    void toSlotList(PWTreeNode* treeNode,
                    std::vector<RectSlot>& outSlots);

    Grid toGrid(PWTreeNode* treeRoot);
};

#endif //CB_ALGORITHMS_LIB_PICWALLSAMPLER_H
