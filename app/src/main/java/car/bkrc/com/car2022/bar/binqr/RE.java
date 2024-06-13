package car.bkrc.com.car2022.bar.binqr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RE {
    public RE(){

    };
    public  String result(String jsonStr, String fengli ){
//        String jsonStr = "{\"code\":\"2023\",\"algorithm\":\"A1B4C6D23\",\"result\":{\"purpose\":\"赛课融通，综合育人\"},\"number\":1}";
//        String fengli = "purpose"+"\":\"(.*?)\"";
        Pattern pafengli= Pattern.compile(fengli);
        Matcher matfengli = pafengli.matcher(jsonStr);
        if(matfengli.find())
        {
            String cutstr = matfengli.group(1);  //group为捕获组
            System.out.println("cutstr="+cutstr);
            return cutstr;
        }
        else{
            System.out.println("没找到");
            return null;
        }
    };
}
