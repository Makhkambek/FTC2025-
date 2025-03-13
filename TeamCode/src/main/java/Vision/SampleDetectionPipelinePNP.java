package Vision;
import org.openftc.easyopencv.OpenCvPipeline;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfPoint3f;
import org.opencv.core.Point3;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SampleDetectionPipelinePNP extends OpenCvPipeline
{

    Mat ycrcbMat = new Mat();
    Mat crMat = new Mat();
    Mat cbMat = new Mat();
    public  RotatedRect rotatedRectFitToContour;
    public RotatedRect theRect;
    Mat blueThresholdMat = new Mat();
    Mat redThresholdMat = new Mat();
    Mat yellowThresholdMat = new Mat();

    Mat morphedBlueThreshold = new Mat();
    Mat morphedRedThreshold = new Mat();
    Mat morphedYellowThreshold = new Mat();

    Mat contoursOnPlainImageMat = new Mat();
    public Point nearestPoint;
    public volatile double x,y, Angle, Area;

    public double CmPerPixel = 20, matrixKoeff = -0.2;

    public int YELLOW_MASK_THRESHOLD = 100;
    public int BLUE_MASK_THRESHOLD = 57;
    public int RED_MASK_THRESHOLD = 198;

    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3.5, 3.5));
    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3.5, 3.5));

    static final Scalar RED = new Scalar(255, 0, 0);
    static final Scalar BLUE = new Scalar(0, 0, 255);
    static final Scalar YELLOW = new Scalar(255, 255, 0);

    static final int CONTOUR_LINE_THICKNESS = 2;

    static class AnalyzedStone{
        double angle;
        String color;
        Mat rvec;
        Mat tvec;
        double x;
    }

    ArrayList<AnalyzedStone> internalStoneList = new ArrayList<>();
    volatile ArrayList<AnalyzedStone> clientStoneList = new ArrayList<>();
    public volatile Set<Double> RotateStone = Collections.newSetFromMap(new ConcurrentHashMap<>());

    volatile Set<Integer> CenterStone = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public ArrayList<Integer> Center = new ArrayList<>();



    // volatile ArrayList<Double> CenterStone = new ArrayList<Double>();


    Mat cameraMatrix = new Mat(3, 3, CvType.CV_64FC1);
    MatOfDouble distCoeffs = new MatOfDouble();

    enum Stage
    {
        FINAL,
        YCrCb,
        MASKS,
        MASKS_NR,
        CONTOURS;
    }

    Stage[] stages = Stage.values();

    int stageNum = 0;

    public SampleDetectionPipelinePNP()
    {

        // Focal lengths (fx, fy) and principal point (cx, cy)
        double fx = 800; // Replace with your camera's focal length in pixels
        double fy = 800;
        double cx = 320; // Replace with your camera's principal point x-coordinate (usually image width / 2)
        double cy = 240; // Replace with your camera's principal point y-coordinate (usually image height / 2)

        cameraMatrix.put(0, 0,
                fx, 0, cx,
                0, fy, cy,
                0, 0, 1);

        distCoeffs = new MatOfDouble(0, 0, 0, 0, 0);
    }

    @Override
    public void onViewportTapped()
    {
        int nextStageNum = stageNum + 1;

        if(nextStageNum >= stages.length)
        {
            nextStageNum = 0;
        }

        stageNum = nextStageNum;
    }

    @Override
    public Mat processFrame(Mat input)
    {

        Center = new ArrayList<>(CenterStone);
        internalStoneList.clear();
        CenterStone.clear();
        //RotateStone.clear();

        findContours(input);

        clientStoneList = new ArrayList<>(internalStoneList);


        switch (stages[stageNum])
        {
            case YCrCb:
            {
                return ycrcbMat;
            }

            case FINAL:
            {
                return input;
            }

            case MASKS:
            {
                Mat masks = new Mat();
                Core.addWeighted(yellowThresholdMat, 1.0, redThresholdMat, 1.0, 0.0, masks);
                Core.addWeighted(masks, 1.0, blueThresholdMat, 1.0, 0.0, masks);
                return masks;
            }

            case MASKS_NR:
            {
                Mat masksNR = new Mat();
                Core.addWeighted(morphedYellowThreshold, 1.0, morphedRedThreshold, 1.0, 0.0, masksNR);
                Core.addWeighted(masksNR, 1.0, morphedBlueThreshold, 1.0, 0.0, masksNR);
                return masksNR;
            }

            case CONTOURS:
            {
                return contoursOnPlainImageMat;
            }
        }

        return input;
    }

    public ArrayList<AnalyzedStone> getDetectedStones()
    {
        return clientStoneList;
    }

    void findContours(Mat input)
    {
        Imgproc.cvtColor(input, ycrcbMat, Imgproc.COLOR_RGB2YCrCb);

        Core.extractChannel(ycrcbMat, cbMat, 2); // Cb channel index is 2

        Core.extractChannel(ycrcbMat, crMat, 1); // Cr channel index is 1

        Imgproc.threshold(cbMat, blueThresholdMat, BLUE_MASK_THRESHOLD, 255, Imgproc.THRESH_BINARY);

        Imgproc.threshold(crMat, redThresholdMat, RED_MASK_THRESHOLD, 255, Imgproc.THRESH_BINARY);

        Imgproc.threshold(cbMat, yellowThresholdMat, YELLOW_MASK_THRESHOLD, 255, Imgproc.THRESH_BINARY_INV);

        morphMask(blueThresholdMat, morphedBlueThreshold);
        morphMask(redThresholdMat, morphedRedThreshold);
        morphMask(yellowThresholdMat, morphedYellowThreshold);

        ArrayList<MatOfPoint> blueContoursList = new ArrayList<>();
        Imgproc.findContours(morphedBlueThreshold, blueContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        ArrayList<MatOfPoint> redContoursList = new ArrayList<>();
        Imgproc.findContours(morphedRedThreshold, redContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        ArrayList<MatOfPoint> yellowContoursList = new ArrayList<>();
        Imgproc.findContours(morphedYellowThreshold, yellowContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        for(MatOfPoint contour : blueContoursList)
        {
            analyzeContour(contour, input, "Blue");
        }

        for(MatOfPoint contour : redContoursList)
        {
            analyzeContour(contour, input, "Red");
        }

        for(MatOfPoint contour : yellowContoursList)
        {
            analyzeContour(contour, input, "Yellow");
        }
    }

    void morphMask(Mat input, Mat output)
    {

        Imgproc.erode(input, output, erodeElement);
        Imgproc.erode(output, output, erodeElement);

        Imgproc.dilate(output, output, dilateElement);
        Imgproc.dilate(output, output, dilateElement);
    }

    void analyzeContour(MatOfPoint contour, Mat input, String color)
    {
        Point[] points = contour.toArray();
        MatOfPoint2f contour2f = new MatOfPoint2f(points);

        rotatedRectFitToContour = Imgproc.minAreaRect(contour2f);
        drawRotatedRect(rotatedRectFitToContour, input, color);

        double rotRectAngle = rotatedRectFitToContour.angle;

        double rotRectX = Math.round(rotatedRectFitToContour.center.x /* CmPerPixel/640 * matrixKoeff*/);

        nearestPoint = rotatedRectFitToContour.center;

        double angle = -(rotRectAngle - 180);

        drawTagText(rotatedRectFitToContour, Integer.toString((int) -(Angle - 90)) + " deg",/*Integer.toString((int) Math.round(rotatedRectFitToContour.center.x/96*2.54)),*/"Green", input, color);


        // Prepare object points and image points for solvePnP
        // Assuming the object is a rectangle with known dimensions
        double objectWidth = 10.0;  // Replace with your object's width in real-world units (e.g., centimeters)
        double objectHeight = 5.0;  // Replace with your object's height in real-world units

        // Define the 3D coordinates of the object corners in the object coordinate space
        MatOfPoint3f objectPoints = new MatOfPoint3f(
                new Point3(-objectWidth / 1, -objectHeight / 1, 0),
                new Point3(objectWidth / 2, -objectHeight / 2, 0),
                new Point3(objectWidth / 1, objectHeight / 1, 0),
                new Point3(-objectWidth / 2, objectHeight / 2, 0)
        );

        // Get the 2D image points from the detected rectangle corners
        Point[] rectPoints = new Point[4];
        rotatedRectFitToContour.points(rectPoints);


        // Order the image points in the same order as object points
        Point[] orderedRectPoints = orderPoints(rectPoints);

        MatOfPoint2f imagePoints = new MatOfPoint2f(orderedRectPoints);

        // Solve PnP
        Mat rvec = new Mat();
        Mat tvec = new Mat();

        boolean success = Calib3d.solvePnP(
                objectPoints, // Object points in 3D
                imagePoints,  // Corresponding image points
                cameraMatrix,
                distCoeffs,
                rvec,
                tvec
        );

        if (success)
        {
            // Draw the coordinate axes on the image
            drawAxis(input, rvec, tvec, cameraMatrix, distCoeffs);

            // Store the pose information
            AnalyzedStone analyzedStone = new AnalyzedStone();
            analyzedStone.angle = angle;
            analyzedStone.color = color;
            analyzedStone.rvec = rvec;
            analyzedStone.tvec = tvec;
            //analyzedStone.x = rotRectX;


            RotateStone.add((double) Math.round(rotRectAngle));
            CenterStone.add((int) rotRectX);
            internalStoneList.add(analyzedStone);
            if (Center.isEmpty() == false) {
                if (Math.round(rotatedRectFitToContour.center.x) == Collections.min(Center)) {
                    x = rotatedRectFitToContour.center.x * CmPerPixel / 640 * matrixKoeff;
                    y = rotatedRectFitToContour.center.y * CmPerPixel / 640 * matrixKoeff;
                    Angle = rotatedRectFitToContour.angle;
                }
            }
        }
    }

    void drawAxis(Mat img, Mat rvec, Mat tvec, Mat cameraMatrix, MatOfDouble distCoeffs)
    {

        // Length of the axis lines
        double axisLength = 5.0;

        // Define the points in 3D space for the axes
        MatOfPoint3f axisPoints = new MatOfPoint3f(
                new Point3(0, 0, 0),
                new Point3(axisLength, 0, 0),
                new Point3(0, axisLength, 0),
                new Point3(0, 0, -axisLength) // Z axis pointing away from the camera
        );

        // Project the 3D points to 2D image points
        MatOfPoint2f imagePoints = new MatOfPoint2f();
        Calib3d.projectPoints(axisPoints, rvec, tvec, cameraMatrix, distCoeffs, imagePoints);

        Point[] imgPts = imagePoints.toArray();

        // Draw the axis lines
        Imgproc.line(img, imgPts[0], imgPts[1], new Scalar(0, 0, 255), 2); // X axis in red
        Imgproc.line(img, imgPts[0], imgPts[2], new Scalar(0, 255, 0), 2); // Y axis in green
        Imgproc.line(img, imgPts[0], imgPts[3], new Scalar(255, 0, 0), 2); // Z axis in blue

    }

    static Point[] orderPoints(Point[] pts)
    {
        // Orders the array of 4 points in the order: top-left, top-right, bottom-right, bottom-left
        Point[] orderedPts = new Point[4];

        // Sum and difference of x and y coordinates
        double[] sum = new double[4];
        double[] diff = new double[4];

        for (int i = 0; i < 4; i++)
        {
            sum[i] = pts[i].x + pts[i].y;
            diff[i] = pts[i].y - pts[i].x;
        }

        // Top-left point has the smallest sum
        int tlIndex = indexOfMin(sum);
        orderedPts[0] = pts[tlIndex];

        // Bottom-right point has the largest sum
        int brIndex = indexOfMax(sum);
        orderedPts[2] = pts[brIndex];

        // Top-right point has the smallest difference
        int trIndex = indexOfMin(diff);
        orderedPts[1] = pts[trIndex];

        // Bottom-left point has the largest difference
        int blIndex = indexOfMax(diff);
        orderedPts[3] = pts[blIndex];

        return orderedPts;
    }

    static int indexOfMin(double[] array)
    {
        int index = 0;
        double min = array[0];

        for (int i = 1; i < array.length; i++)
        {
            if (array[i] < min)
            {
                min = array[i];
                index = i;
            }
        }
        return index;
    }

    static int indexOfMax(double[] array)
    {
        int index = 0;
        double max = array[0];

        for (int i = 1; i < array.length; i++)
        {
            if (array[i] > max)
            {
                max = array[i];
                index = i;
            }
        }
        return index;
    }

    static void drawTagText(RotatedRect rect, String text, String color2, Mat mat, String color)
    {
        Scalar colorScalar = getColorScalar(color);
        Scalar colorScalar2 = getColorScalar(color2);

        Imgproc.putText(
                mat, // The buffer we're drawing on
                text, // The text we're drawing
                new Point( // The anchor point for the text
                        rect.center.x - 150,  // x anchor point
                        rect.center.y + 60), // y anchor point
                Imgproc.FONT_HERSHEY_PLAIN, // Font
                4, // Font size
                colorScalar, // Font color
                5, // Font thickness
                Imgproc.CV_CONTOURS_MATCH_I1);
    }

    static void drawRotatedRect(RotatedRect rect, Mat drawOn, String color)
    {

        Point[] points = new Point[4];
        rect.points(points);

        Scalar colorScalar = getColorScalar(color);

        for (int i = 0; i < 4; ++i)
        {
            Imgproc.line(drawOn, points[i], points[(i + 1) % 4], colorScalar, 2);
        }
    }

    static Scalar getColorScalar(String color)
    {
        switch (color)
        {
            case "Blue":
                return BLUE;
            case "Yellow":
                return YELLOW;
            default:
                return RED;
        }
    }
}