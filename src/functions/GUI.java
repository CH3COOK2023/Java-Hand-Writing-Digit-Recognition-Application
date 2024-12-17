package functions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Random;


public class GUI extends JFrame {
    int CONSTANT_K = 300;
    static boolean stopButtonPressed = false;
    protected static int BATCH_SET_SIZE = 1000;
    public GUI() {
        // 先弹出语言界面
        SwingUtilities.invokeLater(() -> {
            // 创建 JDialog 弹窗，并设置为模态对话框
            JDialog dialog = new JDialog((Frame) null, "Language:", true);
            dialog.setSize(300, 150);
            dialog.setResizable(false);
            dialog.setLocationRelativeTo(null);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

            // 使用默认字体设置标签字体
            JLabel label = new JLabel("语言设置/ Language Setting");
            label.setFont(new Font(Font.DIALOG, Font.PLAIN, 16));
            panel.add(label);

            JButton chineseButton = new JButton("中文");
            // 使用默认字体设置按钮字体
            chineseButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
            chineseButton.setPreferredSize(new Dimension(80, 30));
            chineseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 这里可以添加选择中文后的具体逻辑，比如设置程序语言为中文等
                    LanguageSetting.runChineseText();
                    // 关闭对话框，解除阻塞，后续代码才能继续执行
                    dialog.dispose();
                }
            });
            panel.add(chineseButton);

            JButton englishButton = new JButton("English");
            // 使用默认字体设置按钮字体
            englishButton.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
            englishButton.setPreferredSize(new Dimension(80, 30));
            englishButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 这里可以添加选择英文后的具体逻辑，比如设置程序语言为英文等
                    LanguageSetting.runEnglishText();
                    // 关闭对话框，解除阻塞，后续代码才能继续执行
                    dialog.dispose();
                }
            });
            panel.add(englishButton);

            dialog.add(panel);
            dialog.setVisible(true);

            // 这里的代码在对话框关闭前不会执行，因为对话框是模态的，阻塞了此处代码的运行
            GUI.this.setSize(1024, 768);

            initApplication();

            GUI.this.setVisible(true);
        });


    }

    private void initApplication() {

        // 这里设置基本属性
        {

            this.setSize(703, 820);

            // 设置界面标题
            this.setTitle("手写数字识别");

            // 设置软件置顶
            this.setAlwaysOnTop(true);

            // 设置界面居中
            this.setLocationRelativeTo(null);

            // 游戏关闭那么就结束运行
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            // 取消默认的界面内部的居中方式，这样下面图片才可以设置坐标
            this.setLayout(null);

            // 不允许resize
            this.setResizable(false);
        }

        // 这里设置按钮
        {
            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu(LanguageSetting.b1);
            JMenuItem reset = new JMenuItem(LanguageSetting.b2);
            JMenuItem clearCache = new JMenuItem(LanguageSetting.b3);


            clearCache.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GUI.this.setVisible(false);
                    DevelopTool.clearCache();
                    GUI.this.setVisible(true);
                    System.out.println("[已成功清除缓存]");
                }
            });
            reset.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showJDialog1();
                }
            });

            fileMenu.add(reset);
            fileMenu.add(clearCache);
            menuBar.add(fileMenu);

            this.getContentPane().add(menuBar);
            this.setJMenuBar(menuBar);
        }



        // 这里设置界面内的内容
        {
            final int panelX = 20;
            final int panelY = 20;

            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // 创建用于放置28*28个小方块的面板
            JPanel newJPanel = new JPanel();
            newJPanel.setLayout(null);  // 设置为null布局，便于手动定位小方块

            int squareSize = 10;  // 每个小方块的边长（包含边框）
            int spacing = 0;  // 小方块之间的间隔，用于显示边框效果

            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                    // 创建每个小方块（以JPanel模拟）
                    JPanel squarePanel = new JPanel() {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            g.setColor(new Color(0,0,0));
                            // 绘制小方块的边框，这里绘制四条边
                            g.drawRect(0, 0, getWidth(), getHeight());
                        }
                    };
                    squarePanel.setBackground(new Color(0, 0, 0));
                    squarePanel.setBounds(i * (squareSize + spacing), j * (squareSize + spacing), squareSize, squareSize);
                    newJPanel.add(squarePanel);
                }
            }
            newJPanel.setBounds(panelX, panelY, 280, 280);


            // 键盘监听
            Random rand = new Random();
            newJPanel.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    for (int dx = -5; dx <= 5; dx++) {
                        for (int dy = -5; dy <= 5; dy++) {
                            if (e.getX() + dx >= 1 && e.getX() + dx <= 279 || e.getY() + dy >= 1 && e.getY() + dy <= 279)
                                updatePanel(e.getX(), e.getY(), dx, dy, newJPanel);
                        }
                    }
                }
            });
            newJPanel.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    for (int dx = -5; dx <= 5; dx++) {
                        for (int dy = -5; dy <= 5; dy++) {
                            if (e.getX() + dx >= 1 && e.getX() + dx <= 279 || e.getY() + dy >= 1 && e.getY() + dy <= 279)
                                updatePanel(e.getX(), e.getY(), dx, dy, newJPanel);
                        }
                    }
                }
            });


            // 将包含小方块的面板添加到窗口的内容面板
            this.getContentPane().add(newJPanel);




            JButton reset = new JButton(LanguageSetting.a1);
            reset.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int x=0;x<28;x++)
                    {
                        for(int y=0;y<28;y++)
                        {
                            newJPanel.getComponentAt(x*10,y*10).setBackground(new Color(0,0,0));
                        }
                    }

                    System.out.println("已重置画布！");
                }
            });
            reset.setBounds(20 ,310,130,30);
            this.getContentPane().add(reset);

            JButton stopButton = new JButton(LanguageSetting.a9);
            stopButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    stopButtonPressed = true;
                }
            });
            stopButton.setBounds(330,370,150,20);
            this.getContentPane().add(stopButton);

            JButton recordButton = new JButton(LanguageSetting.a10);
            recordButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Processing.record();
                        System.out.println("[成功导出模型到桌面！]");
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });
            recordButton.setBounds(330,410,350,20);
            this.getContentPane().add(recordButton);

            JButton loadButton = new JButton(LanguageSetting.a11);
            loadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    GUI.this.setVisible(false);
                    try {
                        Processing.readModel();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                    System.out.println("[已成功导入模型！]");
                    GUI.this.setVisible(true);
                }
            });
            loadButton.setBounds(330,440,350,20);
            this.getContentPane().add(loadButton);

            JButton setAsTrainImg = new JButton(LanguageSetting.a5);
            setAsTrainImg.setBounds(330,120,150,20);
            setAsTrainImg.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JTextField jt = (JTextField) GUI.this.getContentPane().getComponentAt(330,200);
                    GUI.BATCH_SET_SIZE = Integer.parseInt(jt.getText());
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Processing.run();
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    thread.start();
                }
            });
            this.getContentPane().add(setAsTrainImg);

            JLabel text1 = new JLabel(LanguageSetting.a6);
            text1.setBounds(330,140,170,20);
            this.getContentPane().add(text1);

            JTextField input1 = new JTextField();
            input1.setText("10000");
            input1.setBounds(330, 170, 75, 40);

            input1.setFont(new Font("custom", Font.BOLD,25));
            this.getContentPane().add(input1);

            JButton getImgFromFolder = new JButton(LanguageSetting.a7);
            getImgFromFolder.setBounds(330, 220, 350, 20);
            getImgFromFolder.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        getRandomImgFromFile();
                        Processing.getDigit(GUI.this);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            });


            this.getContentPane().add(getImgFromFolder);

            // 训练该样本的准确性
            JButton verifyThisModel = new JButton(LanguageSetting.a8);
            verifyThisModel.setBounds(330, 260, 350, 20);
            verifyThisModel.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    // 创建一个新线程来执行长时间运行的任务
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Processing.VerifyAccuracy(GUI.this);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    });
                    thread.start();
                }
            });
            this.getContentPane().add(verifyThisModel);


            JLabel correctNumber = new JLabel("0");
            correctNumber.setFont(new Font("custom", Font.BOLD,18));
            correctNumber.setBounds(430,280,150,30);
            this.getContentPane().add(correctNumber);

            JLabel per10000 = new JLabel("/ 10000");
            per10000.setFont(new Font("custom", Font.BOLD,18));
            per10000.setBounds(480,280,150,30);
            this.getContentPane().add(per10000);

            JLabel countText = new JLabel("Count:");
            countText.setFont(new Font("custom", Font.BOLD,18));
            countText.setBounds(330,280,150,30);
            this.getContentPane().add(countText);


            JLabel accuracyNumber = new JLabel("00.00%");
            accuracyNumber.setFont(new Font("custom", Font.BOLD,18));
            accuracyNumber.setBounds(430,320,150,30);
            this.getContentPane().add(accuracyNumber);

            JLabel accracyText = new JLabel("Accuracy:");
            accracyText.setFont(new Font("custom", Font.BOLD,18));
            accracyText.setBounds(330,300,150,30);
            this.getContentPane().add(accracyText);







            JButton getPredict = new JButton(LanguageSetting.a3);
            getPredict.setBounds(20 ,370,280,20);
            getPredict.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("已经更新获取的数据");
                    Processing.getDigit(GUI.this);
                }
            });
            this.getContentPane().add(getPredict);


            // 设置随机图片按钮
            JButton randomButton = new JButton(LanguageSetting.a2);
            randomButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for(int x=0;x<28;x++)
                    {
                        for(int y=0;y<28;y++)
                        {
                            int randColor = (int)(Math.random()*255.00);

                            newJPanel.getComponentAt(x*10,y*10).setBackground(new Color(randColor,randColor,randColor));
                        }
                    }
                }
            });
            randomButton.setBounds(20 +280 - 130 ,310,130,30);
            this.getContentPane().add(randomButton);

            // 加入0~9预测
            {
                JPanel predictPanel = new JPanel();
                predictPanel.setLayout(null);
                Font font = new Font("custom", Font.BOLD,20);
                JLabel digit0 = new JLabel("[0]");
                JLabel digit1 = new JLabel("[1]");
                JLabel digit2 = new JLabel("[2]");
                JLabel digit3 = new JLabel("[3]");
                JLabel digit4 = new JLabel("[4]");
                JLabel digit5 = new JLabel("[5]");
                JLabel digit6 = new JLabel("[6]");
                JLabel digit7 = new JLabel("[7]");
                JLabel digit8 = new JLabel("[8]");
                JLabel digit9 = new JLabel("[9]");
                JLabel preDigit0 = new JLabel("0.00%");
                JLabel preDigit1 = new JLabel("0.00%");
                JLabel preDigit2 = new JLabel("0.00%");
                JLabel preDigit3 = new JLabel("0.00%");
                JLabel preDigit4 = new JLabel("0.00%");
                JLabel preDigit5 = new JLabel("0.00%");
                JLabel preDigit6 = new JLabel("0.00%");
                JLabel preDigit7 = new JLabel("0.00%");
                JLabel preDigit8 = new JLabel("0.00%");
                JLabel preDigit9 = new JLabel("0.00%");
                digit0.setFont(font);
                digit1.setFont(font);
                digit2.setFont(font);
                digit3.setFont(font);
                digit4.setFont(font);
                digit5.setFont(font);
                digit6.setFont(font);
                digit7.setFont(font);
                digit8.setFont(font);
                digit9.setFont(font);

                preDigit0.setFont(font);
                preDigit1.setFont(font);
                preDigit2.setFont(font);
                preDigit3.setFont(font);
                preDigit4.setFont(font);
                preDigit5.setFont(font);
                preDigit6.setFont(font);
                preDigit7.setFont(font);
                preDigit8.setFont(font);
                preDigit9.setFont(font);


                int YBIAS  = 30;
                digit0.setBounds(0,0*YBIAS,35,24);
                digit1.setBounds(0,1*YBIAS,35,24);
                digit2.setBounds(0,2*YBIAS,35,24);
                digit3.setBounds(0,3*YBIAS,35,24);
                digit4.setBounds(0,4*YBIAS,35,24);
                digit5.setBounds(0,5*YBIAS,35,24);
                digit6.setBounds(0,6*YBIAS,35,24);
                digit7.setBounds(0,7*YBIAS,35,24);
                digit8.setBounds(0,8*YBIAS,35,24);
                digit9.setBounds(0,9*YBIAS,35,24);
                preDigit0.setBounds(35,0*YBIAS,140,24);
                preDigit1.setBounds(35,1*YBIAS,140,24);
                preDigit2.setBounds(35,2*YBIAS,140,24);
                preDigit3.setBounds(35,3*YBIAS,140,24);
                preDigit4.setBounds(35,4*YBIAS,140,24);
                preDigit5.setBounds(35,5*YBIAS,140,24);
                preDigit6.setBounds(35,6*YBIAS,140,24);
                preDigit7.setBounds(35,7*YBIAS,140,24);
                preDigit8.setBounds(35,8*YBIAS,140,24);
                preDigit9.setBounds(35,9*YBIAS,140,24);

                predictPanel.add(digit0);
                predictPanel.add(digit1);
                predictPanel.add(digit2);
                predictPanel.add(digit3);
                predictPanel.add(digit4);
                predictPanel.add(digit5);
                predictPanel.add(digit6);
                predictPanel.add(digit7);
                predictPanel.add(digit8);
                predictPanel.add(digit9);

                predictPanel.add(preDigit0);
                predictPanel.add(preDigit1);
                predictPanel.add(preDigit2);
                predictPanel.add(preDigit3);
                predictPanel.add(preDigit4);
                predictPanel.add(preDigit5);
                predictPanel.add(preDigit6);
                predictPanel.add(preDigit7);
                predictPanel.add(preDigit8);
                predictPanel.add(preDigit9);


                predictPanel.setBounds(20,400,300,480);
                this.getContentPane().add(predictPanel);
            }

            // 加入参数调整
            {
                JLabel lambdaText = new JLabel      (LanguageSetting.a12);
                JLabel alphaText = new JLabel       (LanguageSetting.a14);
                JLabel stopVarText = new JLabel     (LanguageSetting.a15);
                JLabel miniBatchSize = new JLabel   (LanguageSetting.a16);

                lambdaText.setFont(new Font("customize",Font.BOLD,17));
                alphaText.setFont(new Font("customize",Font.BOLD,17));
                stopVarText.setFont(new Font("customize",Font.BOLD,17));
                miniBatchSize.setFont(new Font("customize",Font.BOLD,17));

                lambdaText.setBounds(330,470,350,20);
                alphaText.setBounds(330,530,350,20);
                stopVarText.setBounds(330,590,350,20);
                miniBatchSize.setBounds(330,650,350,20);

                this.getContentPane().add(lambdaText);
                this.getContentPane().add(alphaText);
                this.getContentPane().add(stopVarText);
                this.getContentPane().add(miniBatchSize);

                JTextField lambda = new JTextField();
                lambda.setText(String.valueOf(Processing.LAMBDA_L1));
                int constant1 = 30;
                lambda.setFont(new Font("customize",Font.BOLD,17));
                lambda.setBounds(330,470+constant1,100,25);
                this.getContentPane().add(lambda);

                JTextField alpha = new JTextField();
                alpha.setText(String.valueOf(Processing.ALPHA));
                alpha.setFont(new Font("customize",Font.BOLD,17));
                alpha.setBounds(330,530+constant1,100,25);
                this.getContentPane().add(alpha);

                JTextField stopEpoch = new JTextField();
                stopEpoch.setText(String.valueOf(Processing.EPOCH_TOTAL));
                stopEpoch.setFont(new Font("customize",Font.BOLD,17));
                stopEpoch.setBounds(330,590+constant1,100,25);
                this.getContentPane().add(stopEpoch);

                JTextField miniBatch = new JTextField();
                miniBatch.setText(String.valueOf(Processing.DEFINE_BATCH_SIZE));
                miniBatch.setFont(new Font("customize",Font.BOLD,17));
                miniBatch.setBounds(330,650+constant1,100,25);
                this.getContentPane().add(miniBatch);


                JButton update1 = new JButton(LanguageSetting.a13);
                update1.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("已更改正则化L1、L2 Lambda为"+lambda.getText());
                        lambda.setForeground(Color.black);
                        Processing.LAMBDA_L1 = Double.parseDouble(lambda.getText());
                        Processing.LAMBDA_L2 = Double.parseDouble(lambda.getText());
                    }
                });
                update1.setFont(new Font("customize",Font.BOLD,17));
                update1.setBounds(330+115,470+constant1,100,25);
                this.getContentPane().add(update1);

                JButton update2 = new JButton(LanguageSetting.a13);
                update2.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        alpha.setForeground(Color.black);
                        Processing.ALPHA =  Double.parseDouble(alpha.getText());
                        System.out.println("已更改学习率Alpha为"+Processing.ALPHA);
                    }
                });
                update2.setFont(new Font("customize",Font.BOLD,17));
                update2.setBounds(330+115,530+constant1,100,25);
                this.getContentPane().add(update2);

                JButton update3 = new JButton(LanguageSetting.a13);
                update3.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("已更改Epoch轮数为"+stopEpoch.getText());
                        stopEpoch.setForeground(Color.black);
                        Processing.EPOCH_TOTAL = (int) Double.parseDouble(stopEpoch.getText());
                    }
                });
                update3.setFont(new Font("customize",Font.BOLD,17));
                update3.setBounds(330+115,590+constant1,100,25);
                this.getContentPane().add(update3);

                JButton update4 = new JButton(LanguageSetting.a13);
                update4.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.out.println("已更改Batch值为"+miniBatch.getText());
                        miniBatch.setForeground(Color.black);
                        Processing.EPOCH_TOTAL = (int) Double.parseDouble(miniBatch.getText());
                    }
                });
                update4.setFont(new Font("customize",Font.BOLD,17));
                update4.setBounds(330+115,650+constant1,100,25);
                this.getContentPane().add(update4);



            }


            this.setVisible(true);
        }

        // 设置浓度滑条
        {
            // 创建滑条（范围：0-100，初始值：0）
            JSlider slider = new JSlider(0, 100, 0);
            slider.setMajorTickSpacing(10);  // 每10个单位显示一个大刻度
            slider.setMinorTickSpacing(1);   // 每1个单位显示一个小刻度
            slider.setPaintTicks(true);      // 显示刻度线
            slider.setPaintLabels(true);    // 显示刻度标签
            slider.setValue(91);

            slider.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    CONSTANT_K = (101-slider.getValue())*35;

                }
            });

            // 创建按钮
            JButton defButton = new JButton(LanguageSetting.a4);

            // 添加按钮点击事件

            defButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    System.out.println("已还原为默认值！");
                    CONSTANT_K = 300;
                }
            });
            // 创建容器JPanel并设置布局
            JPanel panel = new JPanel();
            panel.setLayout(new FlowLayout());

            panel.setBounds(330,10,200,200);
            // 将滑条和按钮添加到JPanel
            panel.add(slider);
            panel.add(defButton);

            // 将panel添加到JFrame（假设你已经创建了JFrame）
            add(panel);

        }


    }

    private void getRandomImgFromFile() throws IOException {
        JPanel newJPanel = (JPanel) this.getContentPane().getComponentAt(20,20);
        Random rand = new Random();
        double[][] array = DevelopTool.imgToArray("resources/TrainImg/"+rand.nextInt(GUI.BATCH_SET_SIZE)+".png");
        for(int x=0;x<28;x++)
        {
            for (int y=0;y<28;y++)
            {
                int RGB = (int)(array[x][y]*255.00);
                newJPanel.getComponentAt(y*10,x*10).setBackground(new Color(RGB,RGB,RGB));
            }
        }
    }


    private void updatePanel(int x, int y, int dx, int dy, JPanel newJPanel) {
        // 找不到零件就返回
        if (newJPanel.getComponentAt(x + dx, y + dy) == null) return;
        int RGB = (int) ((dx * dx + dy * dy / 50.0) / (double) CONSTANT_K * 255.0); // 非常小的一个数字，例如0.21
        int originalRGB = newJPanel.getComponentAt(x + dx, y + dy).getBackground().getRed(); //0， 或者150这种
        if (originalRGB + RGB > 255) {
            newJPanel.getComponentAt(x + dx, y + dy).setBackground(new Color(255,255,255));
            return;
        }

        newJPanel.getComponentAt(x + dx, y + dy).setBackground(new Color(originalRGB + RGB, originalRGB + RGB, originalRGB + RGB));
    }

    private void showJDialog1() {
        // 创建对话框
        JDialog dialog = new JDialog(this, "警告", true);

        // 创建面板用于放置组件，这里设置为null布局，以便进行绝对定位
        JPanel panel = new JPanel(null);

        JLabel text = new JLabel("你确定要这么做吗？可能会等待较长时间");

        JButton yesButton = new JButton("Yes");
        yesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.setVisible(false);
                dialog.dispose();
                GUI.this.setVisible(false);
                try {
                    DevelopTool.regenerateResourcesFile();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                GUI.this.setVisible(true);
            }
        });

        JButton noButton = new JButton("No");
        noButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });

        // 设置按钮的大小和位置
        yesButton.setBounds(10, 30, 80, 30);
        noButton.setBounds(150, 30, 80, 30);

        // 设置文本标签的位置和大小，这里简单设置了下，可根据实际调整
        text.setBounds(10, 10, 300, 20);

        // 将组件添加到面板中
        panel.add(text);
        panel.add(yesButton);
        panel.add(noButton);

        // 将面板添加到对话框中
        dialog.add(panel);

        // 设置对话框大小
        dialog.setSize(275, 110);

        // 设置对话框相对于当前窗口的位置
        dialog.setLocationRelativeTo(this);

        // 设置对话框始终在最上层显示
        dialog.setAlwaysOnTop(true);
        // 不可resize
        dialog.setResizable(false);

        // 让对话框可见
        dialog.setVisible(true);
    }
}
