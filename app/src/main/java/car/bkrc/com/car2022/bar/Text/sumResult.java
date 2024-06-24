package car.bkrc.com.car2022.bar.Text;

import java.util.List;

public class sumResult {
//    static boolean isNumber(String str) {//判断表达式是不是只有一个数字
//        for(int i=0;i<str.length();i++) {
//            if(!Character.isDigit(str.charAt(i)) && str.charAt(i)!='.') return false;
//        }
//        return true;
//    }
//    static Double getResult(String str) {
//        if(str.isEmpty() || isNumber(str)) {//递归头
//            return str.isEmpty() ? 0  : Double.parseDouble(str);
//        }
//
//        //递归体
//        if(str.contains(")")) {
//            int lIndex = str.lastIndexOf("(");//最后一个左括号
//            int rIndex = str.indexOf(")", lIndex);//对于的右括号
//            return getResult(str.substring(0,lIndex) + getResult(str.substring(lIndex+1, rIndex)) + str.substring(rIndex+1));
//        }
//        if(str.contains("+")) {
//            int index = str.lastIndexOf("+");
//            return getResult(str.substring(0,index)) + getResult(str.substring(index+1));
//        }
//        if(str.contains("-")) {
//            int index = str.lastIndexOf("-");
//            return getResult(str.substring(0,index)) - getResult(str.substring(index+1));
//        }
//        if(str.contains("*")) {
//            int index = str.lastIndexOf("*");
//            return getResult(str.substring(0,index)) * getResult(str.substring(index+1));
//        }
//        if(str.contains("/")) {
//            int index = str.lastIndexOf("/");
//            return getResult(str.substring(0,index)) / getResult(str.substring(index+1));
//        }
//        if(str.contains("%")) {
//            int index = str.lastIndexOf("%");
//            return getResult(str.substring(0,index)) % getResult(str.substring(index+1));
//        }
//        return null;//出错
//    }
//
//    public static Double Result(String Qr_Recongenize) {
//        double str = getResult(Qr_Recongenize);
//        System.out.println(getResult(Qr_Recongenize));
//
//        return str;

//    }


    /*
     * @brief 函数将两个字符组合成一个十六进制数
     *
     * @param Str1 字符1
     * @param Str2 字符2
     * @return uint8_t 合成的十六进制数
     * 例如Str_To_Hex('C','8') == 0xC8;
     */
    public static byte strToHex(char str1, char str2) {
        char[] str = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
        byte[] hex = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
        byte str1Flag = 0;
        byte str2Flag = 0;
        byte hexNum = 0;

        for (byte i = 0; i < 16; i++) {
            if (str1 == str[i]) {
                str1Flag = 1;
                hexNum |= (hex[i] << 4);
            }
            if (str2 == str[i]) {
                str2Flag = 1;
                hexNum |= hex[i];
            }
            if ((str1Flag & str2Flag) == 1) {
                break;
            }
        }

        return hexNum;
    }
    /*
     * @brief 找到数组中的英文并将其加num
     * 例如输入的数组data_one = "A123B4"  ---->  "D123E4"
     * @param data_one 输入的参数
     * @param num 加的数值大小
     * @return uint8_t
     */
    public static void findEnglishAdd(byte[] dataOne, int num) {
        int size = dataOne.length;
        for (int i = 0; i < size; i++) {
            if ((dataOne[i] >= 'A') && (dataOne[i] <= 'Z')) {
                dataOne[i] = (byte) (dataOne[i] + num);
            }
        }
    }
    /*
     * @brief 传入两个数组 返回两个数组中不同的值
     *
     * @param data_1 数组1
     * @param data_2 数组2
     * @param data3 存入不同值的数组
     * @return uint8_t
     */
    public static void DataCompare(byte[] data1, byte[] data2, byte[] data3) {
        int data1Len = data1.length;
        int data2Len = data2.length;
        if (data1Len == data2Len) {
            for (int i = 0, j = 0; i < data1Len; i++) {
                if (data1[i] != data2[i]) {
                    data3[j] = data1[i];
                    data3[j + 3] = data2[i];
                    j++;
                    if (j >= 3) {
                        break;
                    }
                }
            }
        }
    }
/*
 * @brief 提取数组中的数字
 *
 * @param data_1 需要处理数据的数组
 * @param data_2 用来接收处理完的数据的数组
 * @return uint8_t
 */
    public static byte DataExtractionNumber(byte[] data1, byte[] data2) {
        byte j = 0;

        for (int i = 0; i < data1.length; i++) {
            if (data1[i] >= '0' && data1[i] <= '9') {
                data2[j++] = data1[i];
            }
        }

        return 0;
    }

///////////////////////////////////////////////////////////////////////////////////
    /*

    寻找最长公共子串
    例如String[] strs = {"A23445324", "B52445362", "D21445390", "F51445390"};
        System.out.println(findLongestCommonSubstring(strs));
        4453
     */
    public static String findLongestCommonSubstring(List<String> strs) {
        if (strs == null || strs.isEmpty()) return "";

        // 初始化最长公共子串为第一个字符串
        String longestCommonStr = strs.get(0);

        for (int i = 1; i < strs.size(); i++) {
            longestCommonStr = findCommonSubstring(longestCommonStr, strs.get(i));
            // 如果最长公共子串变空，说明没有公共子串，直接返回
            if (longestCommonStr.isEmpty()) break;
        }

        return longestCommonStr;
    }

    private static String findCommonSubstring(String str1, String str2) {
        int[][] dp = new int[str1.length() + 1][str2.length() + 1];
        int maxLength = 0;
        int endIndex = -1;

        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                    if (dp[i][j] > maxLength) {
                        maxLength = dp[i][j];
                        endIndex = i - 1; // 记录最长子串在str1中的结束位置
                    }
                } else {
                    dp[i][j] = 0;
                }
            }
        }

        // 根据结束位置和最大长度获取最长公共子串
        return maxLength > 0 ? str1.substring(endIndex - maxLength + 1, endIndex + 1) : "";
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////
        public  int Result(String data) {
            int expression =evaluateExpression(data);
           return expression;
        }

    public static int evaluateExpression(String expression) {
        int length = expression.length();
        if (length == 0) {
            return 0;
        }
        int index = 0;
        int num = 0;
        int result = 0;
        char operator = '+';
        int lastNum = 0;
        while (index < length) {
            char c = expression.charAt(index);
            if (Character.isDigit(c)) {
                num = num * 10 + (c - '0');
            } else if (c == '(') {
                int j = index + 1;
                int count = 1;
                while (count > 0) {
                    if (expression.charAt(j) == '(') {
                        count++;
                    } else if (expression.charAt(j) == ')') {
                        count--;
                    }
                    j++;
                }
                num = evaluateExpression(expression.substring(index + 1, j - 1));
                index = j - 1;
            }
            if ((!Character.isDigit(c) && c != ' ' && c != '(' && c != ')') || index == length - 1) {
                if (operator == '+') {
                    result += lastNum;
                    lastNum = num;
                } else if (operator == '-') {
                    result += lastNum;
                    lastNum = -num;
                } else if (operator == '*') {
                    lastNum *= num;
                } else if (operator == '/') {
                    lastNum /= num;
                } else if (operator == '%') {
                    lastNum %= num;
                }
                operator = c;
                num = 0;
            }
            index++;
        }
        result += lastNum;
        return result;
    }




}



