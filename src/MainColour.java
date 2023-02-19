import javafx.util.Pair;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Comparator;
import java.util.Vector;

import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

public class MainColour {

    private final Mat img;

    private Mat dst2;

    public MainColour(Mat img){
        this.img = img;
        getColour();
    }

    public Mat getDst2() {
        return dst2;
    }

    static class ColorCluster {
        Scalar color;
        Scalar new_color;
        int count;
    }

    private static float rgb_euclidean(Scalar p1, Scalar p2)
    {
        return (float) sqrt( (p1.val[0]-p2.val[0])*(p1.val[0]-p2.val[0]) +
                (p1.val[1]-p2.val[1])*(p1.val[1]-p2.val[1]) +
                (p1.val[2]-p2.val[2])*(p1.val[2]-p2.val[2]) +
                (p1.val[3]-p2.val[3])*(p1.val[3]-p2.val[3]));
    }

    private void getColour(){
        Mat cluster_indexes = new Mat(img.size(), CvType.CV_8UC3);

        int cluster_count = 1;
        ColorCluster[] clusters = new ColorCluster[100];

        int i=0, j=0, k=0, x=0, y=0;

        float min_rgb_euclidean = 0, old_rgb_euclidean=0;

        double[] size;
        clusters[0] = new ColorCluster();
        size = img.get((int)img.height()/4, (int)img.width()/4);
        clusters[0].new_color = new Scalar(size[0], size[1], size[2]);
        clusters[1] = new ColorCluster();
        size = img.get((int)img.height()/4, (int)img.width()/4 * 3);
        clusters[1].new_color = new Scalar(size[0], size[1], size[2]);
        clusters[2] = new ColorCluster();
        size = img.get((int)img.height()/4 * 3, (int)img.width()/4);
        clusters[2].new_color = new Scalar(size[0], size[1], size[2]);
        clusters[3] = new ColorCluster();
        size = img.get((int)img.height()/4 * 3, (int)img.width()/4 * 3);
        clusters[3].new_color = new Scalar(size[0], size[1], size[2]);

        while(true) {
            for(k=0; k<cluster_count; k++) {
                clusters[k].count = 0;
                clusters[k].color = clusters[k].new_color;
                clusters[k].new_color = new Scalar(0, 0 ,0);
            }
            int h = img.height();
            int w = img.width();
            for (y=0; y<w; ++y) {
                for (x=0; x<h; ++x) {
                    double B = img.get(x, y)[0];
                    double G = img.get(x, y)[1];
                    double R = img.get(x, y)[2];
                    min_rgb_euclidean = 255*255*255;
                    int cluster_index = -1;
                    for(k=0; k<cluster_count; k++) {
                        float euclid = rgb_euclidean(new Scalar(B, G, R, 0), clusters[k].color);
                        if(  euclid < min_rgb_euclidean ) {
                            min_rgb_euclidean = euclid;
                            cluster_index = k;
                        }
                    }
                    cluster_indexes.get(x, y)[0] = cluster_index;
                    clusters[cluster_index].count++;
                    clusters[cluster_index].new_color.val[0] += B;
                    clusters[cluster_index].new_color.val[1] += G;
                    clusters[cluster_index].new_color.val[2] += R;
                }
            }
            min_rgb_euclidean = 0;
            for(k=0; k<cluster_count; k++) {
                clusters[k].new_color.val[0] /= clusters[k].count;
                clusters[k].new_color.val[1] /= clusters[k].count;
                clusters[k].new_color.val[2] /= clusters[k].count;
                float ecli = rgb_euclidean(clusters[k].new_color, clusters[k].color);
                if(ecli > min_rgb_euclidean)
                    min_rgb_euclidean = ecli;
            }
            if( abs(min_rgb_euclidean - old_rgb_euclidean)<1 )
                break;
            old_rgb_euclidean = min_rgb_euclidean;
        }

        Vector<Pair<Integer, Integer>> colors = new Vector<>(cluster_count);
        int colors_count = 0;
        for(i=0; i<cluster_count; i++){
            Pair<Integer, Integer> color = new Pair<>(i, clusters[i].count);
            colors.addElement( color );
            if(clusters[i].count>0)
                colors_count++;
        }
        colors.sort(new Comparator<Pair<Integer, Integer>>() {
            @Override
            public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2) {
                if (o1.getValue() > o2.getValue())
                    return 1;
                else
                    return 0;
            }
        });
        dst2 = new Mat(img.size(), CvType.CV_8UC3);
        int h = dst2.height() / cluster_count;
        int w = dst2.width();
        for(i=0; i<cluster_count; i++ ){
            Imgproc.rectangle(dst2, new Point(0, i*h), new Point(w, i*h+h), clusters[colors.get(i).getKey()].color, -1);
        }
    }
}
