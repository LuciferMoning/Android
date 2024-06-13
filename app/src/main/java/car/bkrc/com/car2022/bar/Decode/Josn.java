package car.bkrc.com.car2022.bar.Decode;

import com.google.gson.Gson;

public class Josn {
    class Result {
        private String purpose;

        // 省略 getter 和 setter 方法

        public String getPurpose() {
            return purpose;
        }

        public void setPurpose(String purpose) {
            this.purpose = purpose;
        }
    }

    class Data {
        private String code;
        private String algorithm;
        private Result result;
        private int number;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public Result getResult() {
            return result;
        }

        public void setResult(Result result) {
            this.result = result;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
// 省略 getter 和 setter 方法
    }

    String jsonStr = "{\"code\":\"2023\",\"algorithm\":\"A1B4C6D23\",\"result\":{\"purpose\":\"赛课融通，综合育人\"},\"number\":1}";
    Gson gson = new Gson();
    Data data = gson.fromJson(jsonStr, Data.class);
}
