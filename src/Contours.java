import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class Contours {
    private final Mat img4;
    private Mat result;
    ArrayList<MatOfPoint> contours;


    public Mat getResult() {
        return result;
    }

    public ArrayList<MatOfPoint> getContour(){
        return contours;
    }

    public Contours(Mat img,  ArrayList<MatOfPoint> contours){
        this.contours = contours;
        this.img4 = img;
        getContours();
    }

    private void getContours(){
        Imgproc.adaptiveThreshold(img4, img4, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,
                Imgproc.THRESH_BINARY_INV, 7, 3);
        Imgproc.Canny(img4, img4, 150, 255);
        Imgproc.GaussianBlur(img4, img4, new Size(5, 5), 0);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,
                new Size(3, 3));
        Imgproc.morphologyEx(img4, img4, Imgproc.MORPH_CLOSE, kernel);

        boolean r = Imgcodecs.imwrite("fullbody.jpg", img4);
        if (!r) {
            System.out.println("Не удалось сохранить изображение");
        }

        Mat hierarchy = new Mat();
        contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(img4, contours, hierarchy,
                Imgproc.RETR_EXTERNAL,
                Imgproc.CHAIN_APPROX_NONE);

        result = new Mat(new Size(img4.width(), img4.height()), CvType.CV_8UC3, new Scalar(254, 188, 4));
    }
}
