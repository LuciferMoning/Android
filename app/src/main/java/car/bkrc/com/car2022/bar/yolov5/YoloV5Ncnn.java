// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2020 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package car.bkrc.com.car2022.bar.yolov5;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class YoloV5Ncnn
{
    // 车型
    public native boolean Init(AssetManager mgr);
    // 交通标志物
    public native boolean Init1(AssetManager mgr);
    // 行人
    public native boolean Init2(AssetManager mgr);
    //二维码
    public native boolean Init3(AssetManager mgr);
    public class Obj
    {
        public float x;
        public float y;
        public float w;
        public float h;
        public String label;
        public float prob;
    }


    public native Obj[] Detect(Bitmap bitmap, boolean use_gpu);
    public native Obj[] Detect1(Bitmap bitmap, boolean use_gpu);
    public native Obj[] Detect2(Bitmap bitmap, boolean use_gpu);
    public native Obj[] Detect3(Bitmap bitmap, boolean use_gpu);
    static {
        System.loadLibrary("yolov5ncnn");
    }
}
