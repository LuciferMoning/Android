package car.bkrc.com.car2022.bar.binqr;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Sum {
    String result = new String(); //有效字符串
    List<String> zi = new ArrayList<>();//有效内容
    int[] ints = new int[10];//返回的数组
    /*
    提取有效信息
     */
    public int[] tiqu(List<String> list){
        System.out.println("pppp");
        for(int i = 0; i < list.size(); i++){
            int m = 0,n = 0;
            int index = 0;
            Boolean x = true;
            zi.clear();
            while (m<=n){
                m  = list.get(i).indexOf("->",index);
                System.out.println(m);
                index = m+2;
                n  = list.get(i).indexOf("<-",index);
                if( m!=-1){
                    x = false;
                    String t1 = new String();
                    for(int ii = m+2; ii < n;ii++){
                        t1 += list.get(i).charAt(ii);
                    }
                    zi.add(t1);

                    Log.e("TAG", "子字符串: "+zi );
                }else break;

            }
            if(x){
                result = list.get(i);
                Log.e("TAG", "目标: "+result );
            }
        }
        ty();
        return ints;
    }

    /*
    通过这个调用其他方法
     */
    private void ty() {
        int cns = 0;
        //全部清零
        for(int j = 0;j<ints.length-1;j++){
            ints[j]=0;
        }

        for(int j = 0; j < zi.size(); j ++){
            cns = KMP(result,zi.get(j),cns);
            ints[j] = cns;
        }

    }


    public void sum(){

    }

    private int KMP(String str , String str1,int i) {
        int index =str.indexOf(str1,i);
        return index;
    }

}
