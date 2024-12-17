package functions;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Random;

// 历史最小：0.030220112
public class Processing {

    /**
     * 默认变量
     */
    // 输出模型文件夹
    static final String EXPORT_PART = System.getProperty("user.home") + "\\Desktop\\" + "Model_Data.txt";
    // 读取模型文件
    static final String IMPORT_PATH = "resources/model/Model_Data.txt";
    /**
     * 三层神经网络，结构组成如下：
     * 输入层：X，用于接收外部输入数据。
     * 隐藏层：A 层与 B 层，负责对输入数据进行处理和特征提取。
     * 输出层：Y，输出最终的网络计算结果。
     * 网络包含四层结构，配置有三权重、三偏置用于调整计算逻辑。
     */
    static double[][] weightXA = new double[784][20];
    static double[][] weightAB = new double[20][20];
    static double[][] weightBY = new double[20][10];
    static double[] biasA = new double[20];
    static double[] biasB = new double[20];
    static double[] biasY = new double[10];
    /**
     * 这里是Adam优化器的数据
     */
    static double BETA1 = 0.9;
    static double BETA2 = 0.999;
    static double ALPHA = 0.001;
    static double EPSILON = 1.0E-08;
    static double LAMBDA_L1 = 0.001;
    static double LAMBDA_L2 = 0.001;

    static double[][] weightXA_Mt = new double[784][20];
    static double[][] weightXA_Vt = new double[784][20];
    static int weightXA_Iteration = 0;
    static double[][] weightAB_Mt = new double[20][20];
    static double[][] weightAB_Vt = new double[20][20];
    static int weightAB_Iteration = 0;
    static double[][] weightBY_Mt = new double[20][10];
    static double[][] weightBY_Vt = new double[20][10];
    static int weightBY_Iteration = 0;
    static double[] biasA_Mt = new double[20];
    static double[] biasA_Vt = new double[20];
    static int biasA_Iteration = 0;
    static double[] biasB_Mt = new double[20];
    static double[] biasB_Vt = new double[20];
    static int biasB_Iteration = 0;
    static double[] biasY_Mt = new double[20];
    static double[] biasY_Vt = new double[20];
    static int biasY_Iteration = 0;
    // 总训练轮数
    static int EPOCH_TOTAL = 5000;
    // 每个Batch大小
    static int DEFINE_BATCH_SIZE = 64;

    /**
     * 其余储存变量
     */
    // 储存最近10次的损失函数值
    static ArrayList<Double> lossValueHistory = new ArrayList<>();

    static {
        initialize(); // 初始化
    }


    /**
     * run()用于开始训练模型”
     */
    public static void run() throws IOException {
        System.out.println("开始训练...");
        GUI.stopButtonPressed = false;
        Long start = System.currentTimeMillis();
        Random rand = new Random(0);

        ArrayList<double[][]> allBatch = DevelopTool.getTrainBatch();
        allBatch = normalization(allBatch); // 预处理

        int EPOCH_CURRENT = 0;
        if (allBatch.size() > 10000 && EPOCH_TOTAL != EPOCH_CURRENT) {
            System.err.println(String.format("[警告] 数据集过大[%d]", allBatch.size()) + ",每轮Epoch可能时间较长,请耐心等待");
        }
        double lossValue = getLossValueForBatchSet(allBatch); // 当前损失函数。在while执行完更新

        // 停止按钮和Epoch到达训练两个判断条件
        while (!GUI.stopButtonPressed && EPOCH_TOTAL > EPOCH_CURRENT) {

            System.out.println(String.format("损失函数[%.16f]", lossValue) + String.format("\t最近10次损失方差[%.4f] ×10^-4", lossFunctionVar() * 10000.00) + "\tEpoch = " + (++EPOCH_CURRENT) + String.format("进度[%.2f%%]", (double) EPOCH_CURRENT / (double) EPOCH_TOTAL * 100.00));
            ArrayList<double[][]> batch = new ArrayList<>();
            // 这个for运行完之后，就代表一个EPOCH结束
            for (int i = 0; i < GUI.BATCH_SET_SIZE / DEFINE_BATCH_SIZE; i++) {
                for (int j = 0; j < DEFINE_BATCH_SIZE; j++) {
                    // 投入图像
                    int randomIndex = rand.nextInt(GUI.BATCH_SET_SIZE);
                    batch.add(allBatch.get(randomIndex));
                }
                trainBatch(batch);
                batch.clear();
                //new Scanner(System.in).nextLine();
            }
            // 更新lossValueHistory
            lossValue = getLossValueForBatchSet(allBatch);
            if (lossValueHistory.size() > 10) lossValueHistory.removeFirst();
            lossValueHistory.add(lossValue);
        }
        record();
        Long end = System.currentTimeMillis();
        System.out.println("用时：" + String.format("%.3f", (double) (end - start) / 1000.00) + "s");
        System.out.println("[训练完成！]");
        GUI.stopButtonPressed = false;
    }

    /**
     * 用于对一个batch进行梯度下降
     */
    private static void trainBatch(ArrayList<double[][]> batch) {
        // 这里先计算所有batch的网络
        // 在同一个batch中，所用到的权重、偏置、神经元数据都必须是上一batch的。
        // 因此在这里我们复制了全局中的权重、偏置、神经元数据，然后对于更新完的权重，我们存在这里
        // 但是在权重更新过程中我们不适用这里的weight，而是weight
        // 在整个batch更新完之后，我们才给weight 赋值为 我们更新的 weight
        double[][] XList = new double[batch.size()][784];
        double[][] AList = new double[batch.size()][20];
        double[][] BList = new double[batch.size()][20];
        double[][] YList = new double[batch.size()][10];

        double[][] gradient_weightXA = new double[784][20];
        double[][] gradient_weightAB = new double[20][20];
        double[][] gradient_weightBY = new double[20][10];
        double[] gradient_biasA = new double[20];
        double[] gradient_biasB = new double[20];
        double[] gradient_biasY = new double[10];

        ++weightXA_Iteration;
        ++weightAB_Iteration;
        ++weightBY_Iteration;
        ++biasA_Iteration;
        ++biasB_Iteration;
        ++biasY_Iteration;

        for (int numberOfBatch = 0; numberOfBatch < batch.size(); numberOfBatch++) {
            double[] X = new double[784];
            double[] A = new double[20];
            double[] B = new double[20];
            double[] Y = new double[10];
            double[][] currentImg = batch.get(numberOfBatch);
            // 存到X
            int counter = 0;
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    X[counter++] = currentImg[x][y];
                }
            }
            // 计算隐藏层A
            for (int i = 0; i < 20; i++) {
                for (int left = 0; left < 784; left++) {
                    A[i] += X[left] * weightXA[left][i];
                }
                // 添加偏置后，压入sigmoid
                A[i] += biasA[i];
                A[i] = sigmoid(A[i]);
            }
            // 计算隐藏层B
            for (int i = 0; i < 20; i++) {
                for (int left = 0; left < 20; left++) {
                    B[i] += A[left] * weightAB[left][i];
                }
                B[i] += biasB[i];
                B[i] = sigmoid(B[i]);
            }
            // 计算Y
            for (int i = 0; i < 10; i++) {
                for (int left = 0; left < 20; left++) {
                    Y[i] += B[left] * weightBY[left][i];
                }
                Y[i] += biasY[i];
                Y[i] = sigmoid(Y[i]);
            }
            XList[numberOfBatch] = arrayCopy(X);
            AList[numberOfBatch] = arrayCopy(A);
            BList[numberOfBatch] = arrayCopy(B);
            YList[numberOfBatch] = arrayCopy(Y);
        }

        // 设置梯度
        for (int n = 0; n < batch.size(); n++) {
            ArrayList<Double> storage1 = new ArrayList<>(); // 0~10
            ArrayList<Double> storage2 = new ArrayList<>(); // 0~20 : i
            ArrayList<Double> storage3 = new ArrayList<>(); // 0~20 : r

            // biasY
            double label = batch.get(n)[28][0];
            for (int j = 0; j < 10; j++) {
                double ygt;
                if (label == j) ygt = 1.0;
                else ygt = 0.0;
                double value = (YList[n][j] - ygt) * YList[n][j] * (1.0 - YList[n][j]);
                gradient_biasY[j] += value;
                storage1.add(value);
            }
            // weightBY
            for (int i = 0; i < 20; i++) {
                double temp = 0; // 添加temp
                for (int j = 0; j < 10; j++) {
                    double value = storage1.get(j) * BList[n][i];
                    gradient_weightBY[i][j] += value;

                    temp += storage1.get(j) * weightBY[i][j]* BList[n][i]*(1.0 -  BList[n][i]); // 累加
                }
                storage2.add(temp);
            }
            // biasB
            for (int i = 0; i < 20; i++) {
                gradient_biasB[i] += storage2.get(i);
            }
            // weightAB
            for (int r = 0; r < 20; r++) {
                double temp = 0;
                for (int i = 0; i < 20; i++) {
                    gradient_weightAB[r][i] += storage2.get(i) * AList[n][r];
                    temp += storage2.get(i) * AList[n][r] * weightAB[r][i] * (1.0 -AList[n][r]);
                }
                storage3.add(temp);
            }
            // biasA
            for (int r = 0; r < 20; r++) {
                gradient_biasA[r] += storage3.get(r);
            }

            // weightXA
            for (int l = 0; l < 784; l++) {
                for (int r = 0; r < 20; r++) {
                    gradient_weightXA[l][r] += storage3.get(r) * XList[n][l];
                }
            }
        }


        // 给梯度添加L1和L2正则化 // 这有必要吗
        {
            // gradient_weightBY
            for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 10; j++) {

                    gradient_weightBY[i][j] += 2 * LAMBDA_L2 * weightBY[i][j] + LAMBDA_L1 * sign(weightBY[i][j]);
                }
            }
            // gradient_weightAB
            for (int right = 0; right < 20; right++) {
                for (int i = 0; i < 20; i++) {
                    gradient_weightAB[right][i] += 2 * LAMBDA_L2 * weightAB[right][i] + LAMBDA_L1 * sign(weightAB[right][i]);
                }
            }
            // gradient_weightXA
            for (int left = 0; left < 784; left++) {
                for (int right = 0; right < 20; right++) {
                    gradient_weightXA[left][right] += 2 * LAMBDA_L2 * weightXA[left][right] + LAMBDA_L1 * sign(weightXA[left][right]);
                }
            }
        }


        // Adam 优化器对权重和偏置进行下降

        // biasY
        for (int j = 0; j < 10; j++) {
            biasY_Mt[j] = BETA1 * biasY_Mt[j] + (1.0 - BETA1) * gradient_biasY[j];
            biasY_Vt[j] = BETA2 * biasY_Vt[j] + (1.0 - BETA2) * gradient_biasY[j] * gradient_biasY[j];
            double MtHat = biasY_Mt[j] / (1.0 - Math.pow(BETA1, biasY_Iteration));
            double VtHat = biasY_Vt[j] / (1.0 - Math.pow(BETA2, biasY_Iteration));
            biasY[j] = biasY[j] - (ALPHA * MtHat) / (Math.sqrt(VtHat) + EPSILON);
        }
        // weight_BY
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 10; j++) {
                weightBY_Mt[i][j] = BETA1 * weightBY_Mt[i][j] + (1.0 - BETA1) * gradient_weightBY[i][j];
                weightBY_Vt[i][j] = BETA2 * weightBY_Vt[i][j] + (1.0 - BETA2) * gradient_weightBY[i][j] * gradient_weightBY[i][j];
                double MtHat = weightBY_Mt[i][j] / (1.0 - Math.pow(BETA1, weightBY_Iteration));
                double VtHat = weightBY_Vt[i][j] / (1.0 - Math.pow(BETA2, weightBY_Iteration));
                weightBY[i][j] = weightBY[i][j] - (ALPHA * MtHat) / (Math.sqrt(VtHat) + EPSILON);

            }
        }
        // biasB
        for (int i = 0; i < 20; i++) {
            biasB_Mt[i] = BETA1 * biasB_Mt[i] + (1.0 - BETA1) * gradient_biasB[i];
            biasB_Vt[i] = BETA2 * biasB_Vt[i] + (1.0 - BETA2) * gradient_biasB[i] * gradient_biasB[i];

            double MtHat = biasB_Mt[i] / (1.0 - Math.pow(BETA1, biasB_Iteration));
            double VtHat = biasB_Vt[i] / (1.0 - Math.pow(BETA2, biasB_Iteration));

            biasB[i] = biasB[i] - (ALPHA * MtHat) / (Math.sqrt(VtHat) + EPSILON);
        }
        // weightAB
        for (int right = 0; right < 20; right++) {
            for (int i = 0; i < 20; i++) {
                weightAB_Mt[right][i] = BETA1 * weightAB_Mt[right][i] + (1.0 - BETA1) * gradient_weightAB[right][i];
                weightAB_Vt[right][i] = BETA2 * weightAB_Vt[right][i] + (1.0 - BETA2) * gradient_weightAB[right][i] * gradient_weightAB[right][i];

                double MtHat = weightAB_Mt[right][i] / (1.0 - Math.pow(BETA1, weightAB_Iteration));
                double VtHat = weightAB_Vt[right][i] / (1.0 - Math.pow(BETA2, weightAB_Iteration));

                weightAB[right][i] = weightAB[right][i] - (ALPHA * MtHat) / (Math.sqrt(VtHat) + EPSILON);
            }
        }
        // biasA
        for (int right = 0; right < 20; right++) {
            biasA_Mt[right] = BETA1 * biasA_Mt[right] + (1.0 - BETA1) * gradient_biasA[right];
            biasA_Vt[right] = BETA2 * biasA_Vt[right] + (1.0 - BETA2) * gradient_biasA[right] * gradient_biasA[right];

            double MtHat = biasA_Mt[right] / (1.0 - Math.pow(BETA1, biasA_Iteration));
            double VtHat = biasA_Vt[right] / (1.0 - Math.pow(BETA2, biasA_Iteration));

            biasA[right] = biasA[right] - (ALPHA * MtHat) / (Math.sqrt(VtHat) + EPSILON);
        }
        // weightXA
        for (int left = 0; left < 784; left++) {
            for (int right = 0; right < 20; right++) {
                weightXA_Mt[left][right] = BETA1 * weightXA_Mt[left][right] + (1.0 - BETA1) * gradient_weightXA[left][right];
                weightXA_Vt[left][right] = BETA2 * weightXA_Vt[left][right] + (1.0 - BETA2) * gradient_weightXA[left][right] * gradient_weightXA[left][right];

                double MtHat = weightXA_Mt[left][right] / (1.0 - Math.pow(BETA1, weightXA_Iteration));
                double VtHat = weightXA_Vt[left][right] / (1.0 - Math.pow(BETA2, weightXA_Iteration));

                weightXA[left][right] = weightXA[left][right] - (ALPHA * MtHat) / (Math.sqrt(VtHat) + EPSILON);
            }
        }

    }

    /**
     * 获取GUI的数字，并且输出判断
     */
    public static void getDigit(JFrame frame) {
        JPanel newPanel = (JPanel) frame.getContentPane().getComponentAt(20, 20);
        // 黑色是0，白色是1
        double[][] array = new double[28][28];
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 28; y++) {
                array[x][y] = (double) (newPanel.getComponentAt(y * 10, x * 10).getBackground().getRed()) / 255.0;
            }
        }
        array = normalization(array);
        // 重新绘制
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 28; y++) {
                int RBG = (int) (255.00 * array[x][y]);
                newPanel.getComponentAt(y * 10, x * 10).setBackground(new Color(RBG, RBG, RBG));
            }
        }


        double[] X = new double[784];
        double[] A = new double[20];
        double[] B = new double[20];
        double[] Y = new double[10];
        double[][] currentImg = array;
        // 存到X
        int counter = 0;
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 28; y++) {
                X[counter++] = currentImg[x][y];
            }
        }
        // 计算隐藏层A
        for (int i = 0; i < 20; i++) {
            for (int left = 0; left < 784; left++) {
                A[i] += X[left] * weightXA[left][i];
            }
            // 添加偏置后，压入sigmoid
            A[i] += biasA[i];
            A[i] = sigmoid(A[i]);
        }
        // 计算隐藏层B
        for (int i = 0; i < 20; i++) {
            for (int left = 0; left < 20; left++) {
                B[i] += A[left] * weightAB[left][i];
            }
            B[i] += biasB[i];
            B[i] = sigmoid(B[i]);
        }
        // 计算Y
        for (int i = 0; i < 10; i++) {
            for (int left = 0; left < 20; left++) {
                Y[i] += B[left] * weightBY[left][i];
            }
            Y[i] += biasY[i];
            Y[i] = sigmoid(Y[i]);
        }

        JPanel predictPanel = (JPanel) frame.getContentPane().getComponentAt(20, 400);
        for (int i = 0; i < 10; i++) {
            String temp = String.format("%.2f", Y[i] * 100) + "%";
            JLabel tempLabel = (JLabel) predictPanel.getComponentAt(35, i * 30);
            JLabel tempLabel2 = (JLabel) predictPanel.getComponentAt(0, i * 30);
            tempLabel.setText(temp);
            int constant = 100;
            int RGB = 255 - (constant + (int) (Y[i] * (255.0 - (double) constant)));
            tempLabel.setForeground(new Color(RGB, RGB, RGB));
            tempLabel2.setForeground(new Color(RGB, RGB, RGB));
        }
        int maxIndex = 0;
        for (int i = 0; i < 10; i++) {
            if (Y[i] > Y[maxIndex]) {
                maxIndex = i;
            }
        }
        JLabel tempLabel = (JLabel) predictPanel.getComponentAt(35, maxIndex * 30);
        JLabel tempLabel2 = (JLabel) predictPanel.getComponentAt(0, maxIndex * 30);
        tempLabel.setForeground(Color.RED);
        tempLabel2.setForeground(Color.RED);


    }

    /**
     * 验证模型准确度
     */
    public static void VerifyAccuracy(JFrame jframe) throws IOException {
        int totalCorrectNumber = 0;
        ArrayList<double[][]> allBatch = DevelopTool.getVerifyBatch();
        allBatch = normalization(allBatch);
        int loopCounter = 0;
        for (int numberOfBatch = 0; numberOfBatch < allBatch.size(); numberOfBatch++) {
            ++loopCounter;

            double[] X = new double[784];
            double[] A = new double[20];
            double[] B = new double[20];
            double[] Y = new double[10];
            double[][] currentImg = allBatch.get(numberOfBatch);
            // 存到X
            int counter = 0;
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    X[counter++] = currentImg[x][y];
                }
            }
            // 计算隐藏层A
            for (int i = 0; i < 20; i++) {
                for (int left = 0; left < 784; left++) {
                    A[i] += X[left] * weightXA[left][i];
                }
                // 添加偏置后，压入sigmoid
                A[i] += biasA[i];
                A[i] = sigmoid(A[i]);
            }
            // 计算隐藏层B
            for (int i = 0; i < 20; i++) {
                for (int left = 0; left < 20; left++) {
                    B[i] += A[left] * weightAB[left][i];
                }
                B[i] += biasB[i];
                B[i] = sigmoid(B[i]);
            }
            // 计算Y
            for (int i = 0; i < 10; i++) {
                for (int left = 0; left < 20; left++) {
                    Y[i] += B[left] * weightBY[left][i];
                }
                Y[i] += biasY[i];
                Y[i] = sigmoid(Y[i]);
            }


            JPanel paintingPanel = (JPanel) jframe.getContentPane().getComponentAt(20, 20);
            // 重新绘制画板
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    int RGB = (int) (currentImg[x][y] * 255.00);
                    paintingPanel.getComponentAt(y * 10, x * 10).setBackground(new Color(RGB, RGB, RGB));
                }
            }
            // 重新绘制digit
            JPanel predictPanel = (JPanel) jframe.getContentPane().getComponentAt(20, 400);
            for (int i = 0; i < 10; i++) {
                String temp = String.format("%.2f", Y[i] * 100) + "%";
                JLabel tempLabel = (JLabel) predictPanel.getComponentAt(35, i * 30);
                JLabel tempLabel2 = (JLabel) predictPanel.getComponentAt(0, i * 30);
                tempLabel.setText(temp);
                int constant = 100;
                int RGB = 255 - (constant + (int) (Y[i] * (255.0 - (double) constant)));
                tempLabel.setForeground(new Color(RGB, RGB, RGB));
                tempLabel2.setForeground(new Color(RGB, RGB, RGB));
            }
            // 获取最大值Index
            int maxIndex = 0;
            for (int i = 0; i < 10; i++) {
                if (Y[i] > Y[maxIndex]) {
                    maxIndex = i;
                }
            }
            int label = (int) allBatch.get(numberOfBatch)[28][0];

            if (label == maxIndex) ++totalCorrectNumber;

            JLabel countNum = (JLabel) jframe.getContentPane().getComponentAt(430, 280);
            countNum.setText("" + numberOfBatch);


            JLabel accuracyValue = (JLabel) jframe.getContentPane().getComponentAt(430, 320);
            accuracyValue.setText(String.format("%.2f", (double) totalCorrectNumber / (double) loopCounter * 100.00) + "%");


            JLabel temp = (JLabel) predictPanel.getComponentAt(35, maxIndex * 30);
            JLabel temp2 = (JLabel) predictPanel.getComponentAt(0, maxIndex * 30);
            temp.setForeground(Color.RED);
            temp2.setForeground(Color.RED);

            JLabel tempLabel = (JLabel) predictPanel.getComponentAt(35, label * 30);
            JLabel tempLabel2 = (JLabel) predictPanel.getComponentAt(0, label * 30);
            tempLabel.setForeground(Color.GREEN);
            tempLabel2.setForeground(Color.GREEN);
        }
    }


    // 复制数组函数
    private static double[][] arrayCopy(double[][] sourceArray) {
        double[][] returnArray = new double[sourceArray.length][sourceArray[0].length];
        for (int i = 0; i < sourceArray.length; i++) {
            System.arraycopy(sourceArray[i], 0, returnArray[i], 0, sourceArray[0].length);
        }
        return returnArray;
    }

    private static double[] arrayCopy(double[] sourceArray) {
        double[] returnArray = new double[sourceArray.length];
        System.arraycopy(sourceArray, 0, returnArray, 0, sourceArray.length);
        return returnArray;
    }


    // sign函数
    private static double sign(double weight) {
        if (weight < 0) return -1.0;
        else if (weight > 0) return 1.0;
        else return 0.0;
    }


    // 损失函数方差
    private static double lossFunctionVar() {
        double finalResult = 0.0;
        for (int i = 0; i < lossValueHistory.size(); i++) {
            finalResult += lossValueHistory.get(i);
        }
        return finalResult / (double) lossValueHistory.size();
    }


    // 计算BatchSet总损失
    private static double getLossValueForBatchSet(ArrayList<double[][]> allBatch) {
        double loss = 0.0;
        // 遍历整个BatchSet
        for (int numberOfBatch = 0; numberOfBatch < allBatch.size(); numberOfBatch++) {
            double[][] currentBatch = allBatch.get(numberOfBatch);
            double[] X = new double[784];
            double[] A = new double[20];
            double[] B = new double[20];
            double[] Y = new double[10];
            // 存到X
            int counter = 0;
            for (int x = 0; x < 28; x++) {
                for (int y = 0; y < 28; y++) {
                    X[counter++] = currentBatch[x][y];
                }
            }
            // 计算隐藏层A
            for (int i = 0; i < 20; i++) {
                for (int left = 0; left < 784; left++) {
                    A[i] += X[left] * weightXA[left][i];
                }
                // 添加偏置后，压入sigmoid
                A[i] += biasA[i];
                A[i] = sigmoid(A[i]);
            }
            // 计算隐藏层B
            for (int i = 0; i < 20; i++) {
                for (int left = 0; left < 20; left++) {
                    B[i] += A[left] * weightAB[left][i];
                }
                B[i] += biasB[i];
                B[i] = sigmoid(B[i]);
            }
            // 计算Y
            for (int i = 0; i < 10; i++) {
                for (int left = 0; left < 20; left++) {
                    Y[i] += B[left] * weightBY[left][i];
                }
                Y[i] += biasY[i];
                Y[i] = sigmoid(Y[i]);
            }

            // 计算损失
            int label = (int) currentBatch[28][0];
            double miniBatchLoss = 0;
            for (int i = 0; i < 10; i++) {
                double possibility;
                if (i == label) possibility = 1.0;
                else possibility = 0.0;
                miniBatchLoss += Math.pow(possibility - Y[i], 2);
            }
            loss += miniBatchLoss;
        }
        return loss / (double) allBatch.size();
    }


    // sigmoid 函数
    private static double sigmoid(double x) {
        return 1.00 / (Math.exp(-x) + 1.00);
    }


    // 预处理
    public static ArrayList<double[][]> normalization(ArrayList<double[][]> allBatch) {
        // 先处理图像
        for (int numberOfBatch = 0; numberOfBatch < allBatch.size(); numberOfBatch++) {
            allBatch.set(numberOfBatch, resizeImg(allBatch.get(numberOfBatch)));
        }

        // 然后归一化
        System.out.println("[Normalization Start...]");
        ArrayList<double[][]> normalizedBatch = new ArrayList<>();
        for (int i = 0; i < allBatch.size(); i++) {
            double[][] currentBatch = allBatch.get(i);
            double max = currentBatch[0][0];
            double min = currentBatch[0][0];
            for (int x = 0; x < currentBatch.length - 1; x++) {
                for (int y = 0; y < currentBatch[0].length; y++) {
                    if (currentBatch[x][y] > max) {
                        max = currentBatch[x][y];
                    }
                    if (currentBatch[x][y] < min) {
                        min = currentBatch[x][y];
                    }
                }
            }
            for (int x = 0; x < currentBatch.length - 1; x++) {
                for (int y = 0; y < currentBatch[0].length; y++) {
                    currentBatch[x][y] = (currentBatch[x][y] - min) / (max - min);
                }
            }
            normalizedBatch.add(currentBatch);
        }
        System.out.println("[Normalization Finished]");
        return normalizedBatch;

    }
    public static double[][] normalization(double[][] img) {
        // 先处理图像
        img = resizeImg(img);

        // 然后归一化
        double max = img[0][0];
        double min = img[0][0];
        for (int x = 0; x < img.length - 1; x++) {
            for (int y = 0; y < img[0].length; y++) {
                if (img[x][y] > max) {
                    max = img[x][y];
                }
                if (img[x][y]!=0 && img[x][y] < min) {
                    min = img[x][y];
                }
            }
        }
        for (int x = 0; x < img.length - 1; x++) {
            for (int y = 0; y < img[0].length; y++) {
                img[x][y] = (img[x][y] - min) / (max - min);
            }
        }
        return img;

    }


    private static double[][] resizeImg(double[][] array) {
        int left = -1;
        int right = -1;
        int top = -1;
        int bottom = -1;

        for (int i = 0; i < 28; i++) {
            if (checkColumn(array, i)) {
                left = i;
                break;
            }
        }
        for (int i = 27; i >= 0; i--) {
            if (checkColumn(array, i)) {
                right = i;
                break;
            }
        }

        for (int i = 0; i < 28; i++) {
            if (checkRow(array, i)) {
                top = i;
                break;
            }
        }

        for (int i = 27; i >= 0; i--) {
            if (checkRow(array, i)) {
                bottom = i;
                break;
            }
        }


        if (left == -1 || right == -1 || top == -1 || bottom == -1) {
            return new double[29][28];
        }

        double[][] B;
        int height = Math.abs(bottom - top) + 1;
        int weight = Math.abs(left - right) + 1;
        double[][] A = new double[height][weight];
        for (int x = top; x <= bottom; x++) {
            if (right + 1 - left >= 0) System.arraycopy(array[x], left, A[x - top], 0, right + 1 - left);
        }


        if (height >= weight) {
            B = new double[28][(int) (Math.round((weight) * (28.00 / (height))))];
        } else {
            B = new double[(int) (Math.round((height) * (28.00 / (weight))))][28];
        }

        for (int x = 0; x < B.length; x++) {
            for (int y = 0; y < B[0].length; y++) {
                int w1 = weight;
                int h1 = height;
                int b1 = B.length;
                int b2 = B[0].length;
                int castX = (int) ((double) x * ((double) height / (double) B.length));
                int castY = (int) ((double) y * ((double) weight / (double) B[0].length));
                B[x][y] = A[castX][castY];
            }
        }
        for (int x = 0; x < 28; x++) {
            for (int y = 0; y < 28; y++) {
                array[x][y] = 0.0;
            }
        }
        if (B.length == 28) {
            int bias = (int) (Math.round((28.0 - (double) B[0].length) / 2.0));
            for (int x = 0; x < 28; x++) {
                System.arraycopy(B[x], 0, array[x], bias, B[0].length);
            }
        } else if (B[0].length == 28) {
            int bias = (int) (Math.round((28.0 - (double) B.length) / 2.0));
            for (int x = 0; x < B.length; x++) {
                System.arraycopy(B[x], 0, array[x + bias], 0, 28);
            }
        }
        return array;
    }

    private static boolean checkColumn(double[][] array, int column) {
        for (int x = 0; x < 28; x++) {
            if (array[x][column] != 0) return true;
        }
        return false;
    }

    private static boolean checkRow(double[][] array, int row) {
        for (int x = 0; x < 28; x++) {
            if (array[row][x] != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * record()用于导出模型到桌面
     */
    public static void record() throws IOException {
        // 如果有了就删除，以便覆盖
        File file = new File(EXPORT_PART);
        if (file.exists()) {
            file.delete();
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(EXPORT_PART, true));
        // 权重
        for (int i = 0; i < weightXA.length; i++) {
            for (int j = 0; j < weightXA[i].length; j++) {
                writer.write(weightXA[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightAB.length; i++) {
            for (int j = 0; j < weightAB[i].length; j++) {
                writer.write(weightAB[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightBY.length; i++) {
            for (int j = 0; j < weightBY[i].length; j++) {
                writer.write(weightBY[i][j] + "\n");
            }
        }
        // 偏置
        for (int i = 0; i < biasA.length; i++) {
            writer.write(biasA[i] + "\n");
        }
        for (int i = 0; i < biasB.length; i++) {
            writer.write(biasB[i] + "\n");
        }
        for (int i = 0; i < biasY.length; i++) {
            writer.write(biasY[i] + "\n");
        }
        // 权重的Adam数据
        for (int i = 0; i < weightXA_Mt.length; i++) {
            for (int j = 0; j < weightXA_Mt[i].length; j++) {
                writer.write(weightXA_Mt[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightXA_Vt.length; i++) {
            for (int j = 0; j < weightXA_Vt[i].length; j++) {
                writer.write(weightXA_Vt[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightAB_Mt.length; i++) {
            for (int j = 0; j < weightAB_Mt[i].length; j++) {
                writer.write(weightAB_Mt[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightAB_Vt.length; i++) {
            for (int j = 0; j < weightAB_Vt[i].length; j++) {
                writer.write(weightAB_Vt[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightBY_Mt.length; i++) {
            for (int j = 0; j < weightBY_Mt[i].length; j++) {
                writer.write(weightBY_Mt[i][j] + "\n");
            }
        }
        for (int i = 0; i < weightBY_Vt.length; i++) {
            for (int j = 0; j < weightBY_Vt[i].length; j++) {
                writer.write(weightBY_Vt[i][j] + "\n");
            }
        }
        // 偏置的Adam数据
        for (int i = 0; i < biasA_Mt.length; i++) {
            writer.write(biasA_Mt[i] + "\n");
        }
        for (int i = 0; i < biasA_Vt.length; i++) {
            writer.write(biasA_Vt[i] + "\n");
        }
        for (int i = 0; i < biasB_Mt.length; i++) {
            writer.write(biasB_Mt[i] + "\n");
        }
        for (int i = 0; i < biasB_Vt.length; i++) {
            writer.write(biasB_Vt[i] + "\n");
        }
        for (int i = 0; i < biasY_Mt.length; i++) {
            writer.write(biasY_Mt[i] + "\n");
        }
        for (int i = 0; i < biasY_Vt.length; i++) {
            writer.write(biasY_Vt[i] + "\n");
        }
        // 迭代数
        writer.write(weightXA_Iteration + "\n");
        writer.write(weightAB_Iteration + "\n");
        writer.write(weightBY_Iteration + "\n");
        writer.write(biasA_Iteration + "\n");
        writer.write(biasB_Iteration + "\n");
        writer.write(biasY_Iteration + "\n");

        writer.close();
    }

    /**
     * readModel()用于读取model文件夹的模型
     */
    public static void readModel() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(IMPORT_PATH));
        // 权重
        for (int i = 0; i < weightXA.length; i++) {
            for (int j = 0; j < weightXA[i].length; j++) {
                weightXA[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightAB.length; i++) {
            for (int j = 0; j < weightAB[i].length; j++) {
                weightAB[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightBY.length; i++) {
            for (int j = 0; j < weightBY[i].length; j++) {
                weightBY[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        // 偏置
        for (int i = 0; i < biasA.length; i++) {
            biasA[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasB.length; i++) {
            biasB[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasY.length; i++) {
            biasY[i] = Double.parseDouble(reader.readLine());
        }
        // 权重的Adam数据
        for (int i = 0; i < weightXA_Mt.length; i++) {
            for (int j = 0; j < weightXA_Mt[i].length; j++) {
                weightXA_Mt[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightXA_Vt.length; i++) {
            for (int j = 0; j < weightXA_Vt[i].length; j++) {
                weightXA_Vt[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightAB_Mt.length; i++) {
            for (int j = 0; j < weightAB_Mt[i].length; j++) {
                weightAB_Mt[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightAB_Vt.length; i++) {
            for (int j = 0; j < weightAB_Vt[i].length; j++) {
                weightAB_Vt[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightBY_Mt.length; i++) {
            for (int j = 0; j < weightBY_Mt[i].length; j++) {
                weightBY_Mt[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        for (int i = 0; i < weightBY_Vt.length; i++) {
            for (int j = 0; j < weightBY_Vt[i].length; j++) {
                weightBY_Vt[i][j] = Double.parseDouble(reader.readLine());
            }
        }
        // 偏置的Adam数据
        for (int i = 0; i < biasA_Mt.length; i++) {
            biasA_Mt[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasA_Vt.length; i++) {
            biasA_Vt[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasB_Mt.length; i++) {
            biasB_Mt[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasB_Vt.length; i++) {
            biasB_Vt[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasY_Mt.length; i++) {
            biasY_Mt[i] = Double.parseDouble(reader.readLine());
        }
        for (int i = 0; i < biasY_Vt.length; i++) {
            biasY_Vt[i] = Double.parseDouble(reader.readLine());
        }
        // 迭代数
        weightXA_Iteration = Integer.parseInt(reader.readLine());
        weightAB_Iteration = Integer.parseInt(reader.readLine());
        weightBY_Iteration = Integer.parseInt(reader.readLine());
        biasA_Iteration = Integer.parseInt(reader.readLine());
        biasB_Iteration = Integer.parseInt(reader.readLine());
        biasY_Iteration = Integer.parseInt(reader.readLine());

        reader.close();
    }

    /**
     * 初始化
     */
    private static void initialize() {
        weightXA = initializeWeights(784, 20);
        weightAB = initializeWeights(20, 20);
        weightBY = initializeWeights(20, 10);

        biasA = initializeBias(20);
        biasB = initializeBias(20);
        biasY = initializeBias(10);
    }

    public static double[][] initializeWeights(int fromSize, int toSize) {
        double[][] weights = new double[fromSize][toSize];
        double limit = Math.sqrt(6.0 / fromSize);

        for (int i = 0; i < fromSize; i++) {
            for (int j = 0; j < toSize; j++) {
                Random random1 = new Random();
                Random random2 = new Random(random1.nextInt());
                // 使用均匀分布的He初始化
                weights[i][j] = (random2.nextDouble() * 2.0 * limit - limit);
            }
        }
        return weights;
    }

    public static double[] initializeBias(int size) {
        double[] bias = new double[size];


        for (int m = 0; m < size; m++) {
            Random random1 = new Random();
            Random random2 = new Random(random1.nextInt());
            // 使用正态分布初始化偏置，均值为0，标准差为0.01，可根据实际调整
            bias[m] = random2.nextGaussian() * 0.01;
        }
        return bias;
    }
}