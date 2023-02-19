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

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static Scalar colorRGB(double red, double green, double blue) {
        return new Scalar(blue, green, red);
    }

    public static Mat getHeight(Mat img5, ArrayList<MatOfPoint> contours){
        Rect item = new Rect(1, 1, 1,1);
        Rect body = new Rect(1, 1, 1,1);
        int k = 0;
        for (int i = 0; i < contours.size(); i++){
            Rect r = Imgproc.boundingRect(contours.get(i));
            if (r.height > body.height){
                body = item;
                item = r;
                k = i;
            }
        }
        for (int i = 0; i < contours.size(); i++){
            double cont_area = Imgproc.contourArea(contours.get(i));
            if (cont_area > 1000 && i != k) {
                Imgproc.drawContours(img5, contours, i, new Scalar(130, 0, 15), 6);
            }
        }
        System.out.println("Рост человека: " + ((97* body.height)/ item.height + 97));
        return img5;
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

        boolean crop = Imgcodecs.imwrite("photo_new.jpg",getHeight(height, contours));
        if (!crop) {
            System.out.println("Не удалось сохранить изображение");
        }
//          MatOfRect fullbody = new MatOfRect();
//        fullbody_detector.detectMultiScale(img5, fullbody);
//        for (Rect r : fullbody.toList()) {
//            Imgproc.rectangle(img5, new Point(r.x, r.y),
//                    new Point(r.x + r.width, r.y + r.height),
//                    CvUtils.COLOR_BLACK, 2);
//        }
    }
}

