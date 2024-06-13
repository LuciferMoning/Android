package car.bkrc.com.car2022.bar.Decode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class xml {
    public static void  xml(){

    }
    private static String Result(String xml) {
        try {
//            String xmlString = "<root><person><name>张三</name><age>28</age></person></root>"; //XML格式的字符串
//            String xml = "<message><other>test</other> <key>ABCD</key> </message>";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(xml)); //将字符串转化为输入源
            Document doc = dBuilder.parse(inputSource); // 解析XML格式字符串，并创建Document对象
            doc.getDocumentElement().normalize(); //获取文档的根元素，并将节点进行正规化处理

            System.out.println("Root element :" + doc.getDocumentElement().getNodeName()); // 读取根节点的名称
            NodeList nodeList = doc.getElementsByTagName("message"); // 获取所有名为 "person" 的节点
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) { // 如果该节点是元素节点
                    Element element = (Element) node;
                    System.out.println("key : " + getValue("key", element)); // 读取 name 元素的值
                    System.out.println("Age : " + getValue("age", element));  // 读取 age 元素的值
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getValue(String tag, Element element) { // 根据标签名称和元素节点获取标签值
        if (element.getElementsByTagName(tag).item(0) != null) {
            return element.getElementsByTagName(tag).item(0).getTextContent(); // 返回标签中的文本节点值
        } else {
            return "";
        }
    }
}
