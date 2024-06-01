import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                createAndShowGUI();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createAndShowGUI() throws IOException, InterruptedException {
        JFrame frame = new JFrame("Directed Graph Viewer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);

        JPanel panel = new JPanel();
        JButton button = new JButton("Load and Show Graph");
        button.addActionListener(e -> {
            try {
                loadAndShowGraph();
            } catch (IOException | InterruptedException ex) {
                ex.printStackTrace();
            }
        });
        panel.add(button);
        frame.add(panel);

        frame.setVisible(true);
    }

    private static void loadAndShowGraph() throws IOException, InterruptedException {
        // 接收用户输入文件路径
        String fileName = JOptionPane.showInputDialog("Enter the file path (e.g., input.txt):");

        if (fileName == null || fileName.isEmpty()) {
            JOptionPane.showMessageDialog(null, "File path cannot be empty.");
            return;
        }
        String filePath = Paths.get(System.getProperty("user.dir"), fileName).toString();
        // 读取并清洗文件内容
        String content = readFile(filePath);

        // 解析单词列表
        List<String> words = parseWords(content);

        // 构建有向图
        Map<String, Map<String, Integer>> adjacencyList = new HashMap<>();
        for (int i = 0; i < words.size() - 1; i++) {
            String from = words.get(i);
            String to = words.get(i + 1);
            adjacencyList.putIfAbsent(from, new HashMap<>());
            Map<String, Integer> neighbors = adjacencyList.get(from);
            neighbors.put(to, neighbors.getOrDefault(to, 0) + 1);
        }

        // 打印有向图到控制台
        printGraph(adjacencyList);

        // 生成DOT文件
        String dotFilePath = "graph.dot";
        createDotFile(adjacencyList, dotFilePath);

        // 生成并保存图像
        String outputImagePath = "graph.png";
        generateGraphImage(dotFilePath, outputImagePath);
        JOptionPane.showMessageDialog(null, "Graph image saved to " + outputImagePath);

        // 展示图像
        showDirectedGraph(outputImagePath);
    }

    private static String readFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        // 清洗数据，替换非字母字符为空格
        return content.replaceAll("[^a-zA-Z]", " ");
    }

    private static List<String> parseWords(String text) {
        List<String> words = new ArrayList<>();
        StringTokenizer tokenizer = new StringTokenizer(text);
        while (tokenizer.hasMoreTokens()) {
            words.add(tokenizer.nextToken());
        }
        return words;
    }

    private static void printGraph(Map<String, Map<String, Integer>> adjacencyList) {
        for (String node : adjacencyList.keySet()) {
            System.out.print(node + " -> ");
            for (Map.Entry<String, Integer> edge : adjacencyList.get(node).entrySet()) {
                System.out.print(edge.getKey() + "(" + edge.getValue() + ") ");
            }
            System.out.println();
        }
    }

    private static void createDotFile(Map<String, Map<String, Integer>> adjacencyList, String filePath) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("digraph G {");

            for (String node : adjacencyList.keySet()) {
                for (Map.Entry<String, Integer> edge : adjacencyList.get(node).entrySet()) {
                    writer.printf("  \"%s\" -> \"%s\" [label=\"%d\"];\n", node, edge.getKey(), edge.getValue());
                }
            }

            writer.println("}");
        }
    }

    private static void generateGraphImage(String dotFilePath, String outputImagePath) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("dot", "-Tpng", dotFilePath, "-o", outputImagePath);
        processBuilder.start().waitFor();
    }

    private static void showDirectedGraph(String imagePath) {
        JFrame frame = new JFrame("Directed Graph");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        ImageIcon icon = new ImageIcon(imagePath);
        JLabel label = new JLabel(icon);
        frame.add(new JScrollPane(label));

        frame.setVisible(true);
    }
}