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

    public static final double backgroundsize = 2.5;

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Scalar colorRGB(double red, double green, double blue) {
        return new Scalar(blue, green, red);
    }

    public static void getHeight(Mat img5, ArrayList<MatOfPoint> contours, double humanImgHeight){
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
        System.out.println("Рост человека в пикселях: " +  humanImgHeight);
        System.out.println("Высота предмета в пикселях: " +  (upper - lower));
        System.out.println("Рост человека: " + ((backgroundsize * humanImgHeight)/ (upper - lower)));
    }

    public static void main(String[] args) {
        ArrayList<MatOfPoint> contours = null;
        Mat img = Imgcodecs.imread("photo.jpg");
        if (img.empty()) {
            System.out.println("Не удалось загрузить изображение");
            return;
        }
        String path = "classifier/haarcascade_fullbody.xml";
        CascadeClassifier fullbody_detector = new CascadeClassifier();
        if (!fullbody_detector.load(path)) {
            System.out.println("Не удалось загрузить классификатор " + path);
            return;
        }

        MatOfRect fullbodyMat = new MatOfRect();
        fullbody_detector.detectMultiScale(img, fullbodyMat, 1.6, 3, 0, new Size(), new Size());

        Rect bodyRect = null;
        for (Rect r : fullbodyMat.toList()) {
            bodyRect = new Rect(r.x, r.y, r.width, r.height);
        }

        Mat bodyImg = new Mat(img, bodyRect);

        boolean r = Imgcodecs.imwrite("fullbody.jpg", bodyImg);
        if (!r) {
            System.out.println("Не удалось сохранить изображение");
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

        getHeight(height, contours, bodyRect.height);

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
        Imgproc.line(img, new Point(0, 1173), new Point(img.width(), 1173), COLOR_RED);
        Imgproc.line(img, new Point(0, 119), new Point(img.height(), 119), COLOR_GREEN);
        Imgproc.rectangle(img, new Point(bodyRect.x, bodyRect.y),
                new Point(bodyRect.x + bodyRect.width, bodyRect.y + bodyRect.height), COLOR_BLACK);

        boolean crop = Imgcodecs.imwrite("photo_new.jpg",hMat);
        if (!crop) {
            System.out.println("Не удалось сохранить изображение");
        }

        boolean w = Imgcodecs.imwrite("colour.jpg",result);
        if (!w) {
            System.out.println("Не удалось сохранить изображение");
        }

        boolean m = Imgcodecs.imwrite("contr.jpg",finish);
        if (!m) {
            System.out.println("Не удалось сохранить изображение");
        }

        boolean q = Imgcodecs.imwrite("img.jpg",img);
        if (!q) {
            System.out.println("Не удалось сохранить изображение");
        }
    }
}

