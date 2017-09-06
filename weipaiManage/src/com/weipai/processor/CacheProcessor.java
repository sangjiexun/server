package com.weipai.processor;

/**
 * 缓存信息
 * @author luck
 *
 */
public  class CacheProcessor {
	
	
	
	
	private static CacheProcessor cacheProcessor;
	
	private CacheProcessor(){
		
	}
	
	public static CacheProcessor getInstance(){
		synchronized (cacheProcessor) {
			if(cacheProcessor == null){
				cacheProcessor = new CacheProcessor();
			}
			return cacheProcessor;
		}
	}
}
