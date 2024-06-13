package car.bkrc.com.car2022.FragmentView;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.List;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import car.bkrc.com.car2022.R;
import car.bkrc.com.car2022.ViewAdapter.OtherAdapter;
import car.bkrc.com.car2022.ViewAdapter.Other_Landmark;

public class RightOtherFragment extends Fragment {

    private List<Other_Landmark> otherList = new ArrayList<Other_Landmark>();
    Context minstance =null;

  //  private RightOtherFragment(){}

    public static RightOtherFragment getInstance()
    {
        return RightZigbeeHolder.mInstance;
    }

    private static class RightZigbeeHolder
    {
        private static final RightOtherFragment mInstance =new RightOtherFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        minstance =getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.right_other_fragment, container, false);
        initFruits();
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        OtherAdapter adapter = new OtherAdapter(otherList,getActivity());
        recyclerView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initFruits() {
        otherList.clear();
        Other_Landmark mainCamera = new Other_Landmark("摄像头角度预设位调节", R.mipmap.default_position);
        otherList.add(mainCamera);
        Other_Landmark robotCamera = new Other_Landmark("摄像头俯仰角调节", R.mipmap.robot_camera);
        otherList.add(robotCamera);
        Other_Landmark banana = new Other_Landmark("二维码识别", R.mipmap.qr_code);
        otherList.add(banana);
        Other_Landmark orange = new Other_Landmark("蜂鸣器控制", R.mipmap.buzzer);
        otherList.add(orange);
        Other_Landmark watermelon = new Other_Landmark("转向灯控制", R.mipmap.light);
        otherList.add(watermelon);
    }
}

