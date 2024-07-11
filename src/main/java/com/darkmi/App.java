package com.darkmi;

import com.huaban.analysis.jieba.JiebaSegmenter;

import java.util.Arrays;
import java.util.List;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println("武汉海明智业电子商务有限公司");
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List result = segmenter.sentenceProcess("武汉海明智业电子商务有限公司");
        System.out.println(Arrays.toString(result.toArray()));
    }
}
