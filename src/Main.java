import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.util.ArrayList;

public class Main {
    public static final Scalar COLOR_BLACK = colorRGB(0, 0, 0);
    public static final Scalar COLOR_WHITE = colorRGB(255, 255, 255);
    public static final Scalar COLOR_RED = colorRGB(255, 0, 0);
    public static final Scalar COLOR_ORANGE = colorRGB(255, 100, 0);
    public static final Scalar COLOR_YELLOW = colorRGB(255, 255, 0);
    public static final Scalar COLOR_GREEN = colorRGB(0, 255, 0);
    public static final Scalar COLOR_LIGHTBLUE = colorRGB(60, 170, 255);
    public static final Scalar COLOR_BLUE = colorRGB(0, 0, 255);
    public static final Scalar COLOR_VIOLET = colorRGB(194, 0, 255);
    public static final Scalar COLOR_GINGER = colorRGB(215, 125, 49);
    public static final Scalar COLOR_PINK = colorRGB(255, 192, 203);
    public static final Scalar COLOR_LIGHTGREEN = colorRGB(153, 255, 153);
    public static final Scalar COLOR_BROWN = colorRGB(150, 75, 0);

    public static final double backgroundsize = 3.5;
    public static Mat img;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Scalar colorRGB(double red, double green, double blue) {
        return new Scalar(blue, green, red);
    }

    public static void getHeight(Mat img5, ArrayList<MatOfPoint> contours, Rect faceRect){
        double pixelHeight = 0;
        int upper = 0;
        int lower = img5.height();
        Rect r = null;
        int k = 0;
        for (MatOfPoint contour : contours) {
            r = Imgproc.boundingRect(contour);
            if (r.y > upper) {
                upper = r.y;
            }
            if (r.y < lower) {
                lower = r.y;
            }
        }
        Imgproc.line(img, new Point(0, upper), new Point(img.width(), upper), COLOR_RED);
        Imgproc.line(img, new Point(0, lower), new Point(img.width(), lower), COLOR_GREEN);
        pixelHeight = backgroundsize / (upper - lower);
        System.out.println("???????? ???????????????? ?? ????????????????: " +  (faceRect.y - lower));
        System.out.println("???????????? ???????????????? ?? ????????????????: " +  (upper - lower));
        System.out.println("???????? ????????????????: " + pixelHeight * (faceRect.y - lower));
        System.out.println("?????????? ????????????: " + pixelHeight * faceRect.width);
    }

    public static void main(String[] args) {
        ArrayList<MatOfPoint> contours = null;
        img = Imgcodecs.imread("test6.jpg");
        if (img.empty()) {
            System.out.println("???? ?????????????? ?????????????????? ??????????????????????");
            return;
        }
        String path = "classifier/haarcascade_fullbody.xml";
        String pathFace = "classifier/haarcascade_frontalface_alt.xml";
        CascadeClassifier fullbody_detector = new CascadeClassifier();
        if (!fullbody_detector.load(path)) {
            System.out.println("???? ?????????????? ?????????????????? ?????????????????????????? " + path);
            return;
        }

        CascadeClassifier face_detector = new CascadeClassifier();
        if (!face_detector.load(pathFace)) {
            System.out.println("???? ?????????????? ?????????????????? ?????????????????????????? " + pathFace);
            return;
        }

        MatOfRect fullbodyMat = new MatOfRect();
        fullbody_detector.detectMultiScale(img, fullbodyMat, 1.6, 3, 0, new Size(), new Size());

        MatOfRect faceMat = new MatOfRect();
        face_detector.detectMultiScale(img, faceMat);

        Rect bodyRect = null;
        for (Rect r : fullbodyMat.toList()) {
            bodyRect = new Rect(r.x, r.y, r.width, r.height);
        }

        Rect faceRect = null;
        for (Rect r : faceMat.toList()) {
            faceRect = new Rect(r.x, r.y, r.width, r.height);
        }

        Mat bodyImg = new Mat(img, bodyRect);

        boolean r = Imgcodecs.imwrite("fullbody.jpg", bodyImg);
        if (!r) {
            System.out.println("???? ?????????????? ?????????????????? ??????????????????????");
        }

        MainColour colour = new MainColour(img);
        Mat result = new Mat();
        Imgproc.cvtColor(colour.getDst2(), result, Imgproc.COLOR_BGR2HSV);

        Mat finish = new Mat();
        double[] dstScalar = result.get(1, 1);
        Scalar s = new Scalar(dstScalar[0], dstScalar[1], dstScalar[2]);

        Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2HSV);
        Core.inRange(img, new Scalar(s.val[0] - 10, 100, 100), new Scalar(s.val[0] + 10, 255, 255), finish);

        Contours d = new Contours(finish, contours);
        contours = d.getContour();
        Mat height = d.getResult();

        getHeight(height, contours, faceRect);

        Imgproc.cvtColor(bodyImg, bodyImg, Imgproc.COLOR_BGR2GRAY);

        Contours l = new Contours(bodyImg, contours);
        contours = l.getContour();
        Mat hMat = l.getResult();

        for (int i = 0; i < contours.size(); i++){
            double cont_area = Imgproc.contourArea(contours.get(i));
            if (cont_area > 1000) {
                Imgproc.drawContours(hMat, contours, i, new Scalar(130, 0, 15), 6);
            }
        }

        Imgproc.cvtColor(img, img, Imgproc.COLOR_HSV2BGR);
        Imgproc.rectangle(img, new Point(bodyRect.x, bodyRect.y),
                new Point(bodyRect.x + bodyRect.width, bodyRect.y + bodyRect.height), COLOR_BLACK);
        Imgproc.rectangle(img, new Point(faceRect.x, faceRect.y),
                new Point(faceRect.x + faceRect.width, faceRect.y + faceRect.height), COLOR_BLUE);

        Imgproc.line(img, new Point(0, faceRect.y), new Point(img.width(), faceRect.y), COLOR_PINK);

        boolean crop = Imgcodecs.imwrite("photo_new.jpg",hMat);
        if (!crop) {
            System.out.println("???? ?????????????? ?????????????????? ??????????????????????");
        }

        boolean w = Imgcodecs.imwrite("colour.jpg",result);
        if (!w) {
            System.out.println("???? ?????????????? ?????????????????? ??????????????????????");
        }

        boolean m = Imgcodecs.imwrite("contr.jpg",finish);
        if (!m) {
            System.out.println("???? ?????????????? ?????????????????? ??????????????????????");
        }

        boolean q = Imgcodecs.imwrite("img.jpg",img);
        if (!q) {
            System.out.println("???? ?????????????? ?????????????????? ??????????????????????");
        }
    }
}

