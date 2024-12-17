package functions;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

public class DevelopTool {
    static final String PATH_TRAIN_IMG_FOLDER = "resources/TrainImg";
    static final String PATH_VERIFY_IMG_FOLDER = "resources/VerifyImg";
    static final String PATH_TRAIN_TXT = "resources/database/MNIST_TRAIN_DATA.txt";
    static final String PATH_VERIFY_TXT = "resources/database/MNIST_VERIFY_DATA.txt";
    private DevelopTool(){}

    public static void clearCache() {
        // 清空图像文件
        System.out.println("[Deleting img folder...]");
        deleteAllFilesInFolder(PATH_TRAIN_IMG_FOLDER);
        deleteAllFilesInFolder(PATH_VERIFY_IMG_FOLDER);

        // 删除txt文件
        System.out.println("[Deleting txt file...]");
        deleteTxtFile(PATH_TRAIN_TXT);
        deleteTxtFile(PATH_VERIFY_TXT);

    }

    // 对外初始化
    public static void regenerateResourcesFile() throws IOException {
        System.out.println("[Initializing...]");

        // 清空图像文件
        System.out.println("[Deleting img folder...]");
        deleteAllFilesInFolder(PATH_TRAIN_IMG_FOLDER);
        deleteAllFilesInFolder(PATH_VERIFY_IMG_FOLDER);


        // 删除txt文件
        System.out.println("[Deleting txt file...]");
        deleteTxtFile(PATH_TRAIN_TXT);
        deleteTxtFile(PATH_VERIFY_TXT);


        // 写入txt
        writeTxtFile(PATH_TRAIN_TXT);
        writeTxtFile(PATH_VERIFY_TXT);

        // 生成train img文件
        genImg(PATH_TRAIN_IMG_FOLDER);
        genImg(PATH_VERIFY_IMG_FOLDER);

        System.out.println("[Finish]");


    }








    private static void genImg(String PATH) throws IOException {
        BufferedReader reader;
        if(PATH.equals(PATH_TRAIN_IMG_FOLDER)) {
            reader = new BufferedReader(new FileReader(PATH_TRAIN_TXT));
        }
        else
        {
            reader = new BufferedReader(new FileReader(PATH_VERIFY_TXT));
        }
        String line ;

        int countProcess = 1;

        while((line = reader.readLine()) != null){
            if(line.charAt(0) == 'L') {
                ++countProcess;
                continue;
            }

            int length = stringToDoubleArray(line).length;

            double[][] array = new double[length][length];
            double[] temp = stringToDoubleArray(line);
            for(int i = 0; i < length; ++i){
                array[0][i] = temp[i];
            }
            for(int i = 1; i <= length-1; ++i){
                line = reader.readLine();
                temp = stringToDoubleArray(line);
                for(int j = 0; j < length; ++j){
                    array[i][j] = temp[j];
                }
            }


            if(countProcess%1000==0)
            {
                System.out.println("[Generating image. Process] \t"+String.format("%.2f",((double)countProcess/60000.00*100.00))+"%");
            }

            if(PATH.equals(PATH_TRAIN_IMG_FOLDER))
                arrayToImage(array, PATH_TRAIN_IMG_FOLDER, ""+countProcess);
            else
                arrayToImage(array, PATH_VERIFY_IMG_FOLDER, ""+countProcess);
        }
        reader.close();
    }
    private static void writeTxtFile(String path) throws IOException {
        ArrayList<double[][]> mnist;
        if(path.equals(PATH_TRAIN_TXT)) {
            mnist = MNISTReader.getMNISTTrainDataFromFile();
        }
        else
        {
            mnist = MNISTReader.getMNISTVerifyDataFromFile();
        }
        BufferedWriter writer = new BufferedWriter(new FileWriter(path,true));

        System.out.println("[Writing txt file...]");
        for(int i = 0; i < mnist.size(); i++){
            double[][] arr = mnist.get(i);
            for(int j = 0; j < arr.length-1; j++){
                for(int k = 0; k < arr[j].length; k++){
                    writer.write(arr[j][k] + " ");
                }
                writer.newLine();
            }
            writer.write("Label ↑:"+mnist.get(i)[mnist.get(i).length-1][0]);
            writer.newLine();
        }
        writer.close();
    }
    // 给一个double[][] 输出图片
    private static void arrayToImage(double[][] array, String path, String name) throws IOException {
        int height = array.length;
        int width = array[0].length;

        // Create a BufferedImage
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // Populate the image with the array values
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Clamp and scale the value from [0, 1] to [0, 255]
                int value = (int) Math.round(Math.min(1.0, Math.max(0.0, array[y][x])) * 255);
                int gray = (value << 16) | (value << 8) | value; // Grayscale value
                image.setRGB(x, y, gray);
            }
        }

        // Create the output file
        File outputFile = new File(path, name + ".png");

        // Ensure the directory exists
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        // Write the image to file
        ImageIO.write(image, "png", outputFile);
    }
    // 给一个图片 输出double[][]
    // 静态方法，传入图片路径，返回double[][]数组

    public static double[][] convertToGrayscale(String path) throws IOException {
        // 读取图片
        BufferedImage image = ImageIO.read(new File(path));

        // 获取图片的宽度和高度
        int width = image.getWidth();
        int height = image.getHeight();

        // 创建二维数组存储灰度值
        double[][] grayscale = new double[height][width];

        // 遍历每个像素，获取精确的灰度值
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 读取像素的ARGB值
                int argb = image.getRGB(x, y);

                // 获取灰度值（假设图片为灰度图，RGB通道值相同）
                int gray = argb & 0xFF; // 直接取低8位（灰度图中，R=G=B）

                // 将灰度值归一化到 0.0~1.0 范围
                grayscale[y][x] = gray / 255.0;
            }
        }

        return grayscale;
    }
    // 删除 img文件夹所有子文件
    private static void deleteAllFilesInFolder(String path) {

        File folder = new File(path);

        // Check if the path is a valid directory
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("[Warning, Class: DevelopTool] Invalid directory: " + path);
            return;
        }

        // Get all files in the directory
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.err.println("[Warning, Class: DevelopTool] No files to delete in directory: " + path);
            return;
        }

        System.err.println("[Deleting data. Please wait]");
        // Iterate through the files and delete each one
        for (File file : files) {
            if (file.isFile()) {
                if (!file.delete()) {
                    System.err.println("[Warning, Class:DevelopTool] Failed to delete file: " + file.getName());
                    return;
                }
            } else if (file.isDirectory()) {
                System.err.println("[Warning, Class:DevelopTool] Skipping subdirectory: " + file.getName());
            }
        }

    }
    // 删除 txt文件
    private static void deleteTxtFile(String path) {
        File file = new File(path);
        if(file.exists()) {
            file.delete();
        }

    }
    // 获取double
    private static double[] stringToDoubleArray(String input) {
        return Stream.of(input.split(" "))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }


    // return 28*29 *n Batch
    public static ArrayList<double[][]> getTrainBatch() throws IOException {
        ArrayList<double[][]> batch = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(PATH_TRAIN_TXT));
            String line = null;
            int batchSize = GUI.BATCH_SET_SIZE;

            for(int i = 0;i<batchSize;i++){
                double[][] returnArr = new double[29][28];
                for(int x= 0;x<28;x++)
                {
                    double[] temp = stringToDoubleArray(reader.readLine());
                    for(int y=0;y<28;y++)
                        returnArr[x][y] = temp[y];
                }
                int label = reader.readLine().charAt(8)-'0';
                returnArr[28][0] = label;

                batch.add(returnArr);
            }
            reader.close();
        return batch;
    }
    public static ArrayList<double[][]> getVerifyBatch() throws IOException {
        ArrayList<double[][]> batch = new ArrayList<>();

            BufferedReader reader = new BufferedReader(new FileReader(PATH_VERIFY_TXT));
            String line = null;
            int batchSize = 10000; // 验证集的大小是10000

        System.out.println(batchSize);
            for(int i = 0;i<batchSize;i++){
                double[][] returnArr = new double[29][28];
                for(int x= 0;x<28;x++)
                {
                    double[] temp = stringToDoubleArray(reader.readLine());
                    for(int y=0;y<28;y++)
                        returnArr[x][y] = temp[y];
                }
                int label = reader.readLine().charAt(8)-'0';
                returnArr[28][0] = label;

                batch.add(returnArr);
            }
            reader.close();
        return batch;
    }



    public static double[][] imgToArray(String path) throws IOException {
        File file = new File(path);
        // 使用ImageIO读取灰度图像，确保传入的图像确实是灰度图格式（比如常见的单通道灰度图）
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        double[][] result = new double[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 获取每个像素点的灰度值（对于灰度图，RGB三个分量值是一样的）
                int argb = image.getRGB(x, y);
                int alpha = (argb >> 24) & 0xff;
                int red = (argb >> 16) & 0xff;
                int green = (argb >> 8) & 0xff;
                int blue = argb & 0xff;
                // 灰度值的计算（这里简单平均RGB三个分量，对于灰度图本身RGB相等，也可以直接取其一）
                int gray = (red + green + blue) / 3;
                // 将灰度值归一化到0到1区间
                result[y][x] = (double) gray / 255.0;
            }
        }
        return result;
    }
}
