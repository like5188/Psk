package com.twsz.twsystempre;
import  com.twsz.twsystempre.UnityValueModel;
interface IGame {
    void setUnityValueModel(in UnityValueModel unityValueModel);
}
/*
1，不能用private，public，protect修饰方法
2，支持传递Java的基本数据类型，（byte、short、int、long、float、double、char、boolean），String，CharSequence，List（接受端必须是ArrayList），Map（接收端必须是HashMap），其他自定义的类型需要实现Parcelable序列化。
3，Aidl定义的接口和实现Parcelable序列化的类必须import，即使在相同的包结构下，其余的类型不需要import
4，对于基本数据类型，也不是String和CharSequence类型的，需要有方向指示，包括in（表示由客户端设置），out（表示由服务端设置），inout，（表示两者均可设置）
*/