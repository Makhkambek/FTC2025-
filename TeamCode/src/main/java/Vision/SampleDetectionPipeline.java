package Vision;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.openftc.easyopencv.OpenCvPipeline;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Config
public class SampleDetectionPipeline extends OpenCvPipeline {
    /*
     * Working image buffers
     */
    public RotatedRect rotatedRectFitToContour;
    Mat ycrcbMat = new Mat();
    Mat crMat = new Mat();
    Mat cbMat = new Mat();

    Mat blueThresholdMat = new Mat();
    Mat redThresholdMat = new Mat();
    Mat yellowThresholdMat = new Mat();

    Mat morphedBlueThreshold = new Mat();
    Mat morphedRedThreshold = new Mat();
    Mat morphedYellowThreshold = new Mat();

    Mat contoursOnPlainImageMat = new Mat();
    public double xSample, ySample, Angle, Area, lastAngle;

    public double InchPerPixel = 10.4, matrixKoeff = -0.2;

    /*
     * Threshold values
     */
    public static int YELLOW_MASK_THRESHOLD = 110;
    public static int BLUE_MASK_THRESHOLD = 57;
    public static int RED_MASK_THRESHOLD = 198;

    /*
     * Elements for noise reduction
     */
    Mat erodeElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3.5, 3.5));
    Mat dilateElement = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3.5, 3.5));

    /*
     * Colors
     */
    static final Scalar RED = new Scalar(255, 0, 0);
    static final Scalar BLUE = new Scalar(0, 0, 255);
    static final Scalar YELLOW = new Scalar(255, 255, 0);

    static final int CONTOUR_LINE_THICKNESS = 2;

    static class AnalyzedStone {
        double angle;
        String color;
        double xS;
        double yS;
    }

    ArrayList<AnalyzedStone> internalStoneList = new ArrayList<>();
    volatile ArrayList<AnalyzedStone> clientStoneList = new ArrayList<>();
    public volatile Set<Double> RotateStone = Collections.newSetFromMap(new ConcurrentHashMap<>());

    volatile Set<Integer> CenterStone = Collections.newSetFromMap(new ConcurrentHashMap<>());
    public ArrayList<Integer> Center = new ArrayList<>();

    /*
     * Viewport stages
     */
    enum Stage {
        FINAL,
        YCrCb,
        MASKS,
        MASKS_NR,
        CONTOURS;
    }

    Stage[] stages = Stage.values();
    public static int stageNum = 0;


    @Override
    public void onViewportTapped() {
        int nextStageNum = stageNum + 1;

        if (nextStageNum >= stages.length) {
            nextStageNum = 0;
        }

        stageNum = nextStageNum;
    }

    @Override
    public Mat processFrame(Mat input) {

        // We'll be updating this with new data below
        internalStoneList.clear();
        CenterStone.clear();
        //RotateStone.clear();

        /*
         * Run the image processing
         */
        findContours(input);

        clientStoneList = new ArrayList<>(internalStoneList);

        /*
         * Decide which buffer to send to the viewport
         */
        switch (stages[stageNum]) {
            case YCrCb: {
                return ycrcbMat;
            }

            case FINAL: {
                return input;
            }

            case MASKS: {
                Mat masks = new Mat();
                Core.addWeighted(yellowThresholdMat, 1.0, redThresholdMat, 1.0, 0.0, masks);
                Core.addWeighted(masks, 1.0, blueThresholdMat, 1.0, 0.0, masks);
                return masks;
            }

            case MASKS_NR: {
                Mat masksNR = new Mat();
                Core.addWeighted(morphedYellowThreshold, 1.0, morphedRedThreshold, 1.0, 0.0, masksNR);
                Core.addWeighted(masksNR, 1.0, morphedBlueThreshold, 1.0, 0.0, masksNR);
                return masksNR;
            }

            case CONTOURS: {
                return contoursOnPlainImageMat;
            }

            default: {
                return input;
            }
        }
    }

    public ArrayList<AnalyzedStone> getDetectedStones() {
        return clientStoneList;
    }

    void findContours(Mat input) {
        // Convert the input image to YCrCb color space
        Imgproc.cvtColor(input, ycrcbMat, Imgproc.COLOR_RGB2YCrCb);

        // Extract the Cb and Cr channels
        Core.extractChannel(ycrcbMat, cbMat, 2); // Cb channel index is 2
        Core.extractChannel(ycrcbMat, crMat, 1); // Cr channel index is 1

        // Threshold the channels to form masks
        Imgproc.threshold(cbMat, blueThresholdMat, BLUE_MASK_THRESHOLD, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(crMat, redThresholdMat, RED_MASK_THRESHOLD, 255, Imgproc.THRESH_BINARY);
        Imgproc.threshold(cbMat, yellowThresholdMat, YELLOW_MASK_THRESHOLD, 255, Imgproc.THRESH_BINARY_INV);

        // Apply morphology to the masks
        morphMask(blueThresholdMat, morphedBlueThreshold);
        morphMask(redThresholdMat, morphedRedThreshold);
        morphMask(yellowThresholdMat, morphedYellowThreshold);

        // Find contours in the masks
        ArrayList<MatOfPoint> blueContoursList = new ArrayList<>();
        Imgproc.findContours(morphedBlueThreshold, blueContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        ArrayList<MatOfPoint> redContoursList = new ArrayList<>();
        Imgproc.findContours(morphedRedThreshold, redContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        ArrayList<MatOfPoint> yellowContoursList = new ArrayList<>();
        Imgproc.findContours(morphedYellowThreshold, yellowContoursList, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);

        // Create a plain image for drawing contours
        contoursOnPlainImageMat = Mat.zeros(input.size(), input.type());

        // Analyze and draw contours
        for (MatOfPoint contour : blueContoursList) {
            analyzeContour(contour, input, "Blue");
        }

        for (MatOfPoint contour : redContoursList) {
            analyzeContour(contour, input, "Red");
        }

        for (MatOfPoint contour : yellowContoursList) {
            analyzeContour(contour, input, "Yellow");
        }
    }

    void morphMask(Mat input, Mat output) {
        /*
         * Apply erosion and dilation for noise reduction
         */
        Imgproc.erode(input, output, erodeElement);
        Imgproc.erode(output, output, erodeElement);

        Imgproc.dilate(output, output, dilateElement);
        Imgproc.dilate(output, output, dilateElement);
    }

    void analyzeContour(MatOfPoint contour, Mat input, String color) {
        // Transform the contour to a different format
        Point[] points = contour.toArray();
        MatOfPoint2f contour2f = new MatOfPoint2f(points);

        // Fit a rotated rectangle to the contour and draw it
        if (Imgproc.contourArea(contour2f) >= 20000) {
            rotatedRectFitToContour = Imgproc.minAreaRect(contour2f);
        }
        drawRotatedRect(rotatedRectFitToContour, input, color);
        drawRotatedRect(rotatedRectFitToContour, contoursOnPlainImageMat, color);

        // Adjust the angle based on rectangle dimensions
        double rotRectAngle = rotatedRectFitToContour.angle;

        if (rotatedRectFitToContour.size.width < rotatedRectFitToContour.size.height) {
            rotRectAngle += 90;
        }
        double rotRectX = Math.round(rotatedRectFitToContour.center.x /* CmPerPixel/640 * matrixKoeff*/);
        double rotRectY = Math.round(rotatedRectFitToContour.center.y /* CmPerPixel/640 * matrixKoeff*/);
        // Compute the angle and store it
        double angle;
        double x;
        double y;
        if (clientStoneList.size() == 1) {
            angle = 195;
            x = 0;
            y = 0;
        } else {
            angle = -( rotRectAngle - 180 );
            x = rotRectX;
            y = rotRectY;
         /*  if(lastAngle == 45){
               angle = 45;
           }
           else if(lastAngle == -45){
               angle = -45;
           }*/
        }

        drawTagText(rotatedRectFitToContour, Integer.toString((int) -Math.round(angle - 90)) + " deg", input, color);
        // drawTagText(rotatedRectFitToContour, Integer.toString((int) Math.round(x)) + " from center", input, color);
        // Store the detected stone information
        AnalyzedStone analyzedStone = new AnalyzedStone();
        Center = new ArrayList<>(CenterStone);
        analyzedStone.angle = angle;
        analyzedStone.color = color;
        analyzedStone.xS = x;
        analyzedStone.yS = y;
        RotateStone.add((double) Math.round(rotRectAngle));
        CenterStone.add((int) rotRectX);
        internalStoneList.add(analyzedStone);
        if (!Center.isEmpty()) {
            //   if (Imgproc.contourArea(contour2f) >= 5000) {
            if (Math.round(rotatedRectFitToContour.center.x) == Collections.min(Center)) {

            }
            //Я вот это чуть чуть поменял
            //кросава
            xSample = analyzedStone.xS - 320/*(InchPerPixel / 640 )/* matrixKoeff*/;
            ySample = analyzedStone.yS/*(InchPerPixel / 640 )/* matrixKoeff*/;
            Angle = -( analyzedStone.angle - 90 );
                /*
                if (analyzedStone.angle <= 90) {
                    Angle = analyzedStone.angle;
                }
                else{
                    Angle = analyzedStone.angle - 180;
                }
                 */
            Area = Imgproc.contourArea(contour2f);
            // }
        }
    }

    static void drawTagText(RotatedRect rect, String text, Mat mat, String color) {
        Scalar colorScalar = getColorScalar(color);

        Imgproc.putText(
                mat, // The buffer we're drawing on
                text, // The text we're drawing
                new Point( // The anchor point for the text
                        rect.center.x - 50,  // x anchor point
                        rect.center.y + 25), // y anchor point
                Imgproc.FONT_HERSHEY_PLAIN, // Font
                1, // Font size
                colorScalar, // Font color
                1); // Font thickness
    }

    static void drawRotatedRect(RotatedRect rect, Mat drawOn, String color) {
        /*
         * Draws a rotated rectangle by drawing each of the 4 lines individually
         */
        Point[] points = new Point[4];
        rect.points(points);

        Scalar colorScalar = getColorScalar(color);

        for (int i = 0; i < 4; ++i) {
            Imgproc.line(drawOn, points[i], points[( i + 1 ) % 4], colorScalar, 2);
        }
    }


    static Scalar getColorScalar(String color) {
        switch (color) {
            case "Blue":
                return BLUE;
            case "Yellow":
                return YELLOW;
            default:
                return RED;
        }
    }
}