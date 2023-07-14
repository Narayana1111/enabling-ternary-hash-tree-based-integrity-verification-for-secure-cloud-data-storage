import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
@WebServlet("/UploadServlet")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/ternaryhashtree";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "Narayana#3837";
    private static final int MAX_BUFFER_SIZE = 4096;

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = "";
        InputStream fileContent = null;

        try {
            Part filePart = request.getPart("file");
            fileName = filePart.getSubmittedFileName();
            fileContent = filePart.getInputStream();

            // Build the THT using the file content
            TernaryHashTree tht = buildTHT(fileContent);

            // Connect to the database
            try {
				Class.forName("com.mysql.cj.jdbc.Driver");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
            try (Connection conn = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
                // Prepare the SQL statement to insert file details into the database
                String insertQuery = "INSERT INTO files (name, hash) VALUES (?, ?)";
                PreparedStatement statement = conn.prepareStatement(insertQuery);

                // Traverse the THT and insert block details into the database
                traverseTHT(tht.getRoot(), statement, fileName);

                // Execute the batch insert
                statement.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } finally {
            if (fileContent != null) {
                fileContent.close();
            }
        }

        // Redirect the user to a success page or display a success message
        response.sendRedirect("uploadSuccess.html");
    }

    private TernaryHashTree buildTHT(InputStream fileContent) throws IOException {
        List<byte[]> blocks = splitFileIntoBlocks(fileContent);
        return buildTHTRecursive(blocks);
    }

    private List<byte[]> splitFileIntoBlocks(InputStream fileContent) throws IOException {
        List<byte[]> blocks = new ArrayList<>();
        byte[] buffer = new byte[MAX_BUFFER_SIZE];
        int bytesRead;

        while ((bytesRead = fileContent.read(buffer)) != -1) {
            byte[] block = new byte[bytesRead];
            System.arraycopy(buffer, 0, block, 0, bytesRead);
            blocks.add(block);
        }

        return blocks;
    }

    private TernaryHashTree buildTHTRecursive(List<byte[]> blocks) {
        if (blocks.isEmpty()) {
            return null;
        }

        byte[] rootHash = blocks.remove(0);
        TernaryHashTree.TernaryHashTreeNode rootNode = new TernaryHashTree.TernaryHashTreeNode(rootHash);
        buildTHTRecursive(rootNode, blocks);

        return new TernaryHashTree(rootNode);
    }

    private void buildTHTRecursive(TernaryHashTree.TernaryHashTreeNode node, List<byte[]> blocks) {
        if (blocks.isEmpty()) {
            return;
        }

        byte[] leftHash = blocks.remove(0);
        TernaryHashTree.TernaryHashTreeNode leftNode = new TernaryHashTree.TernaryHashTreeNode(leftHash);
        node.setLeft(leftNode);
        buildTHTRecursive(leftNode, blocks);

        if (blocks.isEmpty()) {
            return;
        }

        byte[] middleHash = blocks.remove(0);
        TernaryHashTree.TernaryHashTreeNode middleNode = new TernaryHashTree.TernaryHashTreeNode(middleHash);
        node.setMiddle(middleNode);
        buildTHTRecursive(middleNode, blocks);

        if (blocks.isEmpty()) {
            return;
        }

        byte[] rightHash = blocks.remove(0);
        TernaryHashTree.TernaryHashTreeNode rightNode = new TernaryHashTree.TernaryHashTreeNode(rightHash);
        node.setRight(rightNode);
        buildTHTRecursive(rightNode, blocks);
    }

    private void traverseTHT(TernaryHashTree.TernaryHashTreeNode node, PreparedStatement statement, String fileName) throws SQLException {
        if (node == null) {
            return;
        }

        byte[] blockHash = node.getData();
        String blockHashBase64 = java.util.Base64.getEncoder().encodeToString(blockHash);

        statement.setString(1, fileName);
        statement.setString(2, blockHashBase64);
        statement.addBatch();

        traverseTHT(node.getLeft(), statement, fileName);
        traverseTHT(node.getMiddle(), statement, fileName);
        traverseTHT(node.getRight(), statement, fileName);
    }

    private static class TernaryHashTree {
        private TernaryHashTreeNode root;

        public TernaryHashTree(TernaryHashTreeNode root) {
            this.root = root;
        }

        public TernaryHashTreeNode getRoot() {
            return root;
        }

        private static class TernaryHashTreeNode {
            private byte[] data;
            private TernaryHashTreeNode left;
            private TernaryHashTreeNode middle;
            private TernaryHashTreeNode right;

            public TernaryHashTreeNode(byte[] data) {
                this.data = data;
            }

            public byte[] getData() {
                return data;
            }

            public TernaryHashTreeNode getLeft() {
                return left;
            }

            public void setLeft(TernaryHashTreeNode left) {
                this.left = left;
            }

            public TernaryHashTreeNode getMiddle() {
                return middle;
            }

            public void setMiddle(TernaryHashTreeNode middle) {
                this.middle = middle;
            }

            public TernaryHashTreeNode getRight() {
                return right;
            }

            public void setRight(TernaryHashTreeNode right) {
                this.right = right;
            }
        }
    }
}
