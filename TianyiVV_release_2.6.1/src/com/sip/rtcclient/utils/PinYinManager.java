package com.sip.rtcclient.utils;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;
/**
 * 汉字转拼音
 *
 */
public class PinYinManager {
	/**
	 * 汉子转换成拼音
	 * @param hanzhis 要转换的汉字符串
	 * @return 符串数组第一个为全拼，第二个为首字母。(小写)
	 */
	public static  String[] toPinYin(String hanzhis){
		CharSequence s= hanzhis;
		String[] returnDate = new String[2];
		char [] hanzhi=new char[s.length()];
		for(int i=0;i<s.length();i++){
			hanzhi[i]=s.charAt(i);
		}
		/** *//**
		 * 设置输出格式
		 *  net.sourceforge.pinyin4j.format.
		 */
		
		HanyuPinyinOutputFormat t3 = new
		 HanyuPinyinOutputFormat();
		 //大小写
		 t3.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		 //音调
		 t3.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		 //
		 t3.setVCharType(HanyuPinyinVCharType.WITH_V);
		 
		 char [] t1 =hanzhi; 
			String[] t2 = new String[s.length()];
		 int t0=t1.length;
		 String pyAll = "";
		 String py = "";
		 try {
			 for (int i=0;i<t0;i++)
			 {
				 t2 = PinyinHelper.toHanyuPinyinStringArray(t1[i], t3);
				 if (t2 != null) {
					 pyAll=pyAll+t2[0].substring(0, 1) ;
					 py=py+t2[0];
				} else {
					pyAll += t1[i];
					py += t1[i];
				}
				 
			 }
		 }
		 catch (BadHanyuPinyinOutputFormatCombination e1) {
			 e1.printStackTrace();
		 }
		 //去前后空格
		 returnDate[1] = pyAll.trim().toLowerCase();
		 returnDate[0] = py.trim().toLowerCase();
		 
		 return returnDate;
	}
}
