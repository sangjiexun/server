package com.context;

/**
 * 胡牌规则
 * @author luck
 *
 */
public class Rule {
	//胡型
	//通用胡型
    //public static String Hu_tian = "tian"; //
    //public static String Hu_di = "di"; //
    //public static String Hu_dasixi = "dasixi"; //
    //public static String Hu_queyise = "queyise"; //
    //public static String Hu_duidui = "duidui"; //
    //public static String Hu_haidilao = "haidilao"; //
    //public static String Hu_haidipao = "haidipao"; //
    //public static String Hu_quanqiuren = "quanqiuren"; //
    //public static String Hu_qianggang = "qianggang"; //
	public static String Hu_zi_common = "zi_common";//自摸普通胡 //3
	public static String Hu_d_self = "d_self";//别人点自己胡 //3
	public static String Hu_d_other = "d_other";//自己点别人胡 //2
    public static String Hu_d_qingyise = "qingyise";//清一色 //3
    public static String Hu_self_qixiaodui = "qixiaodui"; //7小对 //3
    //public static String Hu_longqidui = "longqidui"; //
    //public static String Hu_gangshangpao = "gangshangpao"; //
    public static String Hu_gangshanghua = "gangshanghua"; //3
	
	//长沙麻将
    //public static String Hu_qs_jiangjiang = "jiangjiang";//
	
	//转转麻将胡牌，普通胡牌
    
    
    
    //杠牌
    public static String Gang_ming = "ming";
    public static String Gang_an = "an";
    public static String Gang_dian = "diangang";
    //划水麻将
    public static String Gang_ming_guolu = "gang_guolu";//过路杠
    public static String Gang_fang = "fanggang";//放杠
    
}

